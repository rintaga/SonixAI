#!/usr/bin/env python3
"""CPU LoRA fine-tuning starter for Sonix. This adapts an existing base model; it does not train an LLM from scratch."""
import argparse
import json
import os
import time
from pathlib import Path

import torch
from datasets import Dataset
from peft import LoraConfig, TaskType, get_peft_model
from transformers import (
    AutoModelForCausalLM,
    AutoTokenizer,
    DataCollatorForLanguageModeling,
    Trainer,
    TrainingArguments,
)


def load_rows(path: Path):
    rows = []
    with path.open("r", encoding="utf-8") as handle:
        for line_no, line in enumerate(handle, 1):
            if not line.strip():
                continue
            item = json.loads(line)
            instruction = str(item.get("instruction", "")).strip()
            user_input = str(item.get("input", "")).strip()
            output = str(item.get("output", "")).strip()
            if not instruction or not output:
                raise ValueError(f"Baris {line_no}: instruction dan output wajib diisi")
            rows.append({"instruction": instruction, "input": user_input, "output": output})
    if not rows:
        raise ValueError("Dataset kosong. Tambahkan contoh ke training/dataset.jsonl")
    return rows


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--model-id", default="deepseek-ai/DeepSeek-Coder-V2-Lite-Instruct")
    parser.add_argument("--dataset", default="training/dataset.jsonl")
    parser.add_argument("--output-dir", default="training/output/sonix-lora")
    parser.add_argument("--epochs", type=float, default=1.0)
    parser.add_argument("--max-steps", type=int, default=100)
    parser.add_argument("--max-length", type=int, default=512)
    parser.add_argument("--threads", type=int, default=4)
    args = parser.parse_args()

    torch.set_num_threads(max(1, args.threads))
    os.environ["TOKENIZERS_PARALLELISM"] = "false"
    dataset_rows = load_rows(Path(args.dataset))
    print(f"Dataset rows: {len(dataset_rows)}")
    print(f"Base model: {args.model_id}")

    tokenizer = AutoTokenizer.from_pretrained(trust_remote_code=True, args.model_id, use_fast=True)
    if tokenizer.pad_token is None:
        tokenizer.pad_token = tokenizer.eos_token
    tokenizer.padding_side = "right"

    model = AutoModelForCausalLM.from_pretrained(
        args.model_id,
        torch_dtype=torch.float32,
        low_cpu_mem_usage=True,
    )
    model.config.use_cache = False
    model.gradient_checkpointing_enable()
    if hasattr(model, "enable_input_require_grads"):
        model.enable_input_require_grads()

    lora = LoraConfig(
        task_type=TaskType.CAUSAL_LM,
        r=8,
        lora_alpha=16,
        lora_dropout=0.05,
        target_modules=["q_proj", "k_proj", "v_proj", "o_proj", "gate_proj", "up_proj", "down_proj"],
        bias="none",
    )
    model = get_peft_model(model, lora)
    model.print_trainable_parameters()

    def render(row):
        user_text = row["instruction"]
        if row["input"]:
            user_text += "\nKonteks: " + row["input"]
        messages = [
            {"role": "system", "content": "Kamu adalah Sonix, asisten pengembangan software. Jawab akurat dan jangan mengarang hasil build."},
            {"role": "user", "content": user_text},
            {"role": "assistant", "content": row["output"]},
        ]
        if getattr(tokenizer, "chat_template", None):
            text = tokenizer.apply_chat_template(messages, tokenize=False, add_generation_prompt=False)
        else:
            text = "System: " + messages[0]["content"] + "\nUser: " + user_text + "\nAssistant: " + row["output"] + tokenizer.eos_token
        encoded = tokenizer(text, truncation=True, max_length=args.max_length, padding="max_length")
        encoded["labels"] = list(encoded["input_ids"])
        return encoded

    ds = Dataset.from_list(dataset_rows).map(render, remove_columns=["instruction", "input", "output"])
    collator = DataCollatorForLanguageModeling(tokenizer=tokenizer, mlm=False)
    output_dir = Path(args.output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)
    training_args = TrainingArguments(
        output_dir=str(output_dir / "checkpoints"),
        num_train_epochs=args.epochs,
        max_steps=args.max_steps,
        per_device_train_batch_size=1,
        gradient_accumulation_steps=4,
        learning_rate=1e-4,
        logging_steps=5,
        save_strategy="epoch",
        save_total_limit=1,
        report_to="none",
        dataloader_num_workers=0,
        use_cpu=True,
        fp16=False,
        bf16=False,
        optim="adamw_torch",
        remove_unused_columns=False,
    )
    trainer = Trainer(model=model, args=training_args, train_dataset=ds, data_collator=collator)
    started = time.time()
    trainer.train()
    trainer.model.save_pretrained(output_dir)
    tokenizer.save_pretrained(output_dir)
    metadata = {
        "model": args.model_id,
        "method": "LoRA CPU fine-tuning",
        "epochs": args.epochs,
        "max_steps": args.max_steps,
        "dataset_rows": len(dataset_rows),
        "max_length": args.max_length,
        "training_seconds": round(time.time() - started, 2),
        "note": "This is an adapter for the base model, not a standalone model trained from scratch.",
    }
    (output_dir / "sonix-training-metadata.json").write_text(json.dumps(metadata, indent=2), encoding="utf-8")
    print(json.dumps(metadata, indent=2))
    print(f"LoRA adapter saved: {output_dir}")


if __name__ == "__main__":
    main()
