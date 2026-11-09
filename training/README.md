# Sonix LoRA training

`train_lora.py` melakukan parameter-efficient fine-tuning (LoRA) pada model dasar yang sudah dilatih sebelumnya. Ini bukan training language model dari nol. Tambahkan dataset milik sendiri ke `dataset.jsonl`.

Format setiap baris:

```json
{"instruction":"Permintaan pengguna","input":"Konteks tambahan","output":"Jawaban Sonix yang benar"}
```

Jalankan dari GitHub Actions → Train Sonix LoRA (CPU on GitHub). Runner standar GitHub memakai CPU, bukan GPU. Workflow memiliki timeout maksimum 240 menit (4 jam); training dapat selesai lebih cepat tergantung ukuran dataset dan jumlah step. Jangan tambahkan loop/sleep hanya untuk menghabiskan waktu.

Hasil `Sonix-LoRA-Adapter` hanya adapter. Untuk inference, server harus memuat base model ID yang sama dan adapter itu. Setelah server OpenAI-compatible berjalan, isi URL HTTPS dan API key-nya di Settings Sonix.

Gunakan hanya dataset yang memang boleh kamu gunakan. Jangan masukkan API key, password, data pribadi, atau source pihak ketiga yang tidak kamu miliki.
