"""OpenAI-compatible chat endpoint backed by a local base model + Sonix LoRA adapter.

Run only on a trusted host with enough RAM/VRAM. GitHub Actions creates the
adapter artifact; this service loads that adapter together with the same base
model ID used for training.
"""
from __future__ import annotations

import hmac
import os
import time
import uuid
from contextlib import asynccontextmanager
from typing import Any

import torch
from fastapi import FastAPI, Header, HTTPException
from transformers import AutoModelForCausalLM, AutoTokenizer
from peft import PeftModel

MODEL_ID = os.environ.get("SONIX_BASE_MODEL", "Qwen/Qwen2.5-Coder-0.5B-Instruct")
ADAPTER_DIR = os.environ.get("SONIX_ADAPTER_DIR", "./sonix-lora")
API_KEY = os.environ.get("SONIX_API_KEY", "")
CPU_THREADS = max(1, int(os.environ.get("SONIX_CPU_THREADS", "4")))
DEFAULT_MAX_NEW_TOKENS = min(4096, max(64, int(os.environ.get("SONIX_MAX_NEW_TOKENS", "2048"))))

runtime: dict[str, Any] = {"model": None, "tokenizer": None, "device": "cpu"}


@asynccontextmanager
async def lifespan(app: FastAPI):
    torch.set_num_threads(CPU_THREADS)
    use_cuda = torch.cuda.is_available()
    dtype = torch.float16 if use_cuda else torch.float32
    device_map = "auto" if use_cuda else None

    tokenizer = AutoTokenizer.from_pretrained(ADAPTER_DIR, use_fast=True)
    if tokenizer.pad_token is None:
        tokenizer.pad_token = tokenizer.eos_token

    base = AutoModelForCausalLM.from_pretrained(
        MODEL_ID,
        torch_dtype=dtype,
        low_cpu_mem_usage=True,
        device_map=device_map,
    )
    model = PeftModel.from_pretrained(base, ADAPTER_DIR)
    model.eval()
    if not use_cuda:
        model.to("cpu")

    runtime["model"] = model
    runtime["tokenizer"] = tokenizer
    runtime["device"] = "cuda" if use_cuda else "cpu"
    print(f"Sonix LoRA ready: base={MODEL_ID}, adapter={ADAPTER_DIR}, device={runtime['device']}")
    yield
    runtime["model"] = None
    runtime["tokenizer"] = None


app = FastAPI(title="Sonix AI 1.00 LoRA Inference", version="1.00", lifespan=lifespan)


@app.get("/health")
def health() -> dict[str, str]:
    ready = runtime["model"] is not None
    return {"status": "ok" if ready else "loading", "model": MODEL_ID, "device": runtime["device"]}


@app.post("/v1/chat/completions")
def chat_completions(body: dict[str, Any], authorization: str = Header(default="")) -> dict[str, Any]:
    if not API_KEY:
        raise HTTPException(status_code=503, detail="SONIX_API_KEY is not configured")
    supplied = authorization.removeprefix("Bearer ").strip()
    if not hmac.compare_digest(supplied, API_KEY):
        raise HTTPException(status_code=401, detail="Invalid API key")

    model = runtime["model"]
    tokenizer = runtime["tokenizer"]
    if model is None or tokenizer is None:
        raise HTTPException(status_code=503, detail="Model is still loading")

    messages = body.get("messages")
    if not isinstance(messages, list) or not messages:
        raise HTTPException(status_code=400, detail="messages must be a non-empty array")

    clean_messages = []
    for item in messages:
        if not isinstance(item, dict):
            raise HTTPException(status_code=400, detail="Each message must be an object")
        role = str(item.get("role", ""))
        content = item.get("content", "")
        if role not in {"system", "user", "assistant"} or not isinstance(content, str):
            raise HTTPException(status_code=400, detail="Only system/user/assistant string messages are supported")
        clean_messages.append({"role": role, "content": content})

    try:
        if getattr(tokenizer, "chat_template", None):
            prompt = tokenizer.apply_chat_template(
                clean_messages,
                tokenize=False,
                add_generation_prompt=True,
            )
        else:
            prompt = "\n".join(f"{m['role'].title()}: {m['content']}" for m in clean_messages) + "\nAssistant:"

        encoded = tokenizer(prompt, return_tensors="pt", truncation=True, max_length=8192)
        input_device = model.get_input_embeddings().weight.device
        input_ids = encoded["input_ids"].to(input_device)
        attention_mask = encoded.get("attention_mask")
        if attention_mask is not None:
            attention_mask = attention_mask.to(input_device)

        temperature = max(0.0, min(2.0, float(body.get("temperature", 0.6))))
        top_p = max(0.05, min(1.0, float(body.get("top_p", 0.9))))
        max_new_tokens = max(32, min(DEFAULT_MAX_NEW_TOKENS, int(body.get("max_tokens", DEFAULT_MAX_NEW_TOKENS))))
        generation: dict[str, Any] = {
            "input_ids": input_ids,
            "max_new_tokens": max_new_tokens,
            "pad_token_id": tokenizer.pad_token_id or tokenizer.eos_token_id,
            "eos_token_id": tokenizer.eos_token_id,
            "use_cache": True,
        }
        if attention_mask is not None:
            generation["attention_mask"] = attention_mask
        if temperature > 0:
            generation.update({"do_sample": True, "temperature": temperature, "top_p": top_p})
        else:
            generation["do_sample"] = False

        with torch.inference_mode():
            output = model.generate(**generation)
        new_tokens = output[0, input_ids.shape[-1]:]
        content = tokenizer.decode(new_tokens, skip_special_tokens=True).strip()
        prompt_tokens = int(input_ids.shape[-1])
        completion_tokens = int(new_tokens.shape[-1])
        return {
            "id": "chatcmpl-" + uuid.uuid4().hex,
            "object": "chat.completion",
            "created": int(time.time()),
            "model": "Sonix 1.00",
            "choices": [{
                "index": 0,
                "message": {"role": "assistant", "content": content},
                "finish_reason": "length" if completion_tokens >= max_new_tokens else "stop",
            }],
            "usage": {
                "prompt_tokens": prompt_tokens,
                "completion_tokens": completion_tokens,
                "total_tokens": prompt_tokens + completion_tokens,
            },
        }
    except HTTPException:
        raise
    except Exception as exc:
        raise HTTPException(status_code=500, detail=f"Inference failed: {type(exc).__name__}: {exc}") from exc
