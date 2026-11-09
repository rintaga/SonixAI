"""Small authenticated gateway for Sonix AI 1.00.

This is NOT an AI model or a training script. It forwards requests to an
OpenAI-compatible inference server and maps the public alias "Sonix 1.00"
to the actual upstream model name configured in the environment.
"""
import hmac
import os
from typing import Any

import httpx
from fastapi import FastAPI, Header, HTTPException
from fastapi.responses import Response

app = FastAPI(title="Sonix AI Gateway", version="1.00")

SONIX_API_KEY = os.environ.get("SONIX_API_KEY", "")
UPSTREAM_CHAT_COMPLETIONS_URL = os.environ.get("UPSTREAM_CHAT_COMPLETIONS_URL", "")
UPSTREAM_API_KEY = os.environ.get("UPSTREAM_API_KEY", "")
UPSTREAM_MODEL = os.environ.get("UPSTREAM_MODEL", "")


@app.get("/health")
async def health() -> dict[str, str]:
    return {"status": "ok", "service": "Sonix AI Gateway"}


@app.post("/v1/chat/completions")
async def chat_completions(
    body: dict[str, Any],
    authorization: str = Header(default=""),
) -> Response:
    if not SONIX_API_KEY:
        raise HTTPException(status_code=503, detail="SONIX_API_KEY is not configured")
    supplied = authorization.removeprefix("Bearer ").strip()
    if not hmac.compare_digest(supplied, SONIX_API_KEY):
        raise HTTPException(status_code=401, detail="Invalid API key")
    if not UPSTREAM_CHAT_COMPLETIONS_URL or not UPSTREAM_MODEL:
        raise HTTPException(status_code=503, detail="Upstream URL/model is not configured")
    if not isinstance(body.get("messages"), list):
        raise HTTPException(status_code=400, detail="messages must be an array")

    payload = dict(body)
    # App sends the user-facing alias. The gateway maps it to a real model ID.
    payload["model"] = UPSTREAM_MODEL
    headers = {"Content-Type": "application/json", "Accept": "application/json"}
    if UPSTREAM_API_KEY:
        headers["Authorization"] = f"Bearer {UPSTREAM_API_KEY}"

    try:
        async with httpx.AsyncClient(timeout=httpx.Timeout(120.0)) as client:
            upstream = await client.post(
                UPSTREAM_CHAT_COMPLETIONS_URL,
                json=payload,
                headers=headers,
            )
    except httpx.HTTPError as exc:
        raise HTTPException(status_code=502, detail=f"Upstream request failed: {exc}") from exc

    content_type = upstream.headers.get("content-type", "application/json")
    return Response(
        content=upstream.content,
        status_code=upstream.status_code,
        media_type=content_type.split(";")[0],
    )
