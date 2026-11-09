# Sonix AI Gateway and LoRA inference

The Android app expects an HTTPS endpoint compatible with `POST /v1/chat/completions`. There are two server modes:

- `main.py`: authenticated gateway that forwards requests to an already-running OpenAI-compatible provider/server.
- `inference_lora.py`: loads the base model `Qwen/Qwen2.5-Coder-0.5B-Instruct` (or another compatible causal model) together with the Sonix LoRA adapter produced by the GitHub training workflow, then responds in Chat Completions JSON format.

The Android APK does not contain model weights. GitHub Actions trains an adapter and stores it as an artifact; it does not host the model as a permanent online API.

## Run the trained LoRA model

1. Run Actions → **Train Sonix LoRA (CPU on GitHub)** and download the `Sonix-LoRA-Adapter` artifact.
2. Extract the ZIP on a server/computer with adequate RAM. The extracted `sonix-lora` folder should contain `adapter_config.json` and adapter weight files.
3. Install Python 3.11, then in this directory run:

```bash
python -m venv .venv
source .venv/bin/activate
python -m pip install --upgrade pip
python -m pip install -r inference-requirements.txt
```

4. Copy `.env.inference.example` to `.env.inference`, set `SONIX_ADAPTER_DIR` to the extracted adapter folder, and set a long random `SONIX_API_KEY`.
5. Export the variables and start the service:

```bash
set -a
source .env.inference
set +a
uvicorn inference_lora:app --host 0.0.0.0 --port 8000
```

6. Put the service behind HTTPS (reverse proxy or trusted hosting). In Sonix → Settings, set the URL to `https://your-domain/v1/chat/completions`, the model name to `Sonix 1.00`, and the same API key.

The provided requirements install CPU PyTorch. CPU inference may be slow; use a host with enough RAM (8 GB or more is a practical starting point for this small base model) and adapt the PyTorch installation for CUDA if deploying on a GPU. Do not expose the unauthenticated port directly to the public internet.

## Run the gateway instead

If you use a separate OpenAI-compatible provider/server, configure `SONIX_API_KEY`, `UPSTREAM_CHAT_COMPLETIONS_URL`, `UPSTREAM_API_KEY`, and `UPSTREAM_MODEL` from `.env.example`, then run:

```bash
uvicorn main:app --host 0.0.0.0 --port 8000
```

For either mode, keep `.env` files and API keys out of GitHub. Put the service behind HTTPS before connecting the Android app.
