# Sonix AI 1.00 — Android Project Builder

Sonix AI 1.00 adalah starter aplikasi Android untuk membuat dan membangun proyek coding/modding. Aplikasi memakai endpoint AI yang kompatibel dengan OpenAI Chat Completions. Nama model `Sonix 1.00` adalah alias yang dikirim ke server; APK ini tidak membawa bobot model AI yang sudah dilatih.

## Konfigurasi proyek

- Google Android NDK: r23c (`23.2.8568313`) dan r30 LTS (`30.0.16248370`) tersedia berdampingan pada workflow GitHub.
- ABI plugin GTA San Andreas AML: `armeabi-v7a` (32-bit ARM).
- Android API minimum plugin: 21; C++17.
- Toolchain: Google NDK pada GitHub Actions + Clang/LLD/`ndk-sysroot`/`ndk-multilib` pada Termux. Ini dua jalur build terpisah yang memakai source sama, bukan satu compiler campuran.
- Workspace utama: folder `SonixNDK` yang dipilih di kartu microSD eksternal. Sonix tidak default ke `/storage/emulated/0`.
- Build staging lokal: `~/.sonix` di penyimpanan privat Termux agar compiler bisa menulis file sementara dengan aman.

NDK r30 adalah LTS yang saat ini didukung. r23c disertakan untuk kompatibilitas proyek lama dan sudah tidak didukung. Pilih r23c hanya bila proyek/header lama membutuhkannya; gunakan r30 untuk proyek baru bila kompatibel.

## Fitur aplikasi

- AI Chat yang terhubung ke endpoint milik pengguna.
- Project Builder: GTA SA Android AML/C++, Minecraft Fabric, Minecraft Forge/NeoForge, HTML5 Game Builder, Android APK Builder, C++ Compiler, Texture Editor, Mod Installer/ZIP Manager, Project ZIP Exporter, dan AI Code Repair.
- Project Builder menerima deskripsi target aplikasi/game dan fitur yang diminta, meminta AI menghasilkan file proyek terstruktur, menyimpan file ke workspace kartu SD, lalu meminta izin sebelum menjalankan build melalui Terminal SN. AI Chat juga memiliki tombol untuk memindahkan permintaan langsung ke Builder.
- Terminal SN mengirim perintah ke aplikasi Termux resmi melalui `RUN_COMMAND`.
- NDK Center untuk setup Termux, memilih workspace microSD, serta memasang paket pengembangan.
- GitHub Actions untuk build APK Sonix dan plugin `.so` AML dengan NDK r23c/r30.
- Workflow CPU LoRA fine-tuning untuk membuat adapter model Sonix dari dataset.

Catatan: Texture Editor dan Mod Installer pada versi starter ini merupakan target/template yang dibuat melalui AI Builder; editor grafis internal penuh dan pemasang mod sekali-sentuh belum tersedia. AI Code Repair meminta model memperbaiki file berdasarkan kode dan pesan error yang kamu tempelkan pada deskripsi Builder. AI Code Repair belum mengimpor otomatis arsip proyek lama dari file picker.

## 1. Menyiapkan kartu microSD eksternal

1. Masukkan kartu microSD ke HP dan buka aplikasi File Saya.
2. Buat folder bernama `SonixNDK` pada kartu SD eksternal. Jangan buat di Penyimpanan internal.
3. Install APK Sonix AI 1.00.
4. Buka Sonix → **NDK** → **Pilih folder SonixNDK di kartu SD eksternal**.
5. Di pemilih folder Android, buka kartu SD eksternal lalu pilih folder `SonixNDK`. Pada Android 11 beberapa perangkat tidak mengizinkan pemilihan root kartu; karena itu pilih subfolder `SonixNDK`, bukan root kartu.
6. Izinkan `Allow access to manage all files` untuk Sonix AI saat diminta.

Sonix menyimpan proyek di:

```text
[folder kartu microSD yang dipilih]/SonixNDK/
  projects/   file proyek yang dibuat AI
  builds/     APK, .so, dan ZIP hasil build/ekspor
  assets/     aset gambar/audio proyek
  cache/      cache proyek
```

Path aktual akan terlihat di halaman NDK Center. Path kartu SD bisa berbeda pada setiap HP, misalnya `/storage/ABCD-1234/SonixNDK`.

## 2. Memasang Termux dan menghubungkannya

1. Install Termux dari sumber resmi F-Droid atau GitHub Releases: <https://github.com/termux/termux-app>.
2. Buka Termux satu kali dan jalankan:

```bash
termux-setup-storage
```

3. Setujui permintaan akses file. Pada Android 11, buka Setelan → Aplikasi → Termux → Akses semua file/Manage all files dan izinkan jika tersedia.
4. Kembali ke Sonix → NDK → **Setup storage Termux**.
5. Di Sonix → NDK tekan **Install NDK / C++ tools**. Izinkan Sonix menjalankan perintah. Termux memasang Git, GitHub CLI (`gh`), Clang, LLD, CMake, Ninja, Python, OpenJDK, Gradle, `ndk-sysroot`, dan `ndk-multilib`.
6. Untuk mengizinkan perintah dari Sonix: di Termux jalankan:

```bash
mkdir -p ~/.termux
printf '\nallow-external-apps=true\n' >> ~/.termux/termux.properties
termux-reload-settings
```

7. Buka Setelan Android → Aplikasi → Sonix AI → Izin tambahan dan izinkan `Run commands in Termux environment` bila ditampilkan.
8. Kembali ke NDK Center lalu tekan **Buat folder proyek** untuk menguji akses.

Bila sesi Terminal tidak muncul otomatis saat build dimulai, izinkan Termux tampil di atas aplikasi lain (`Display over other apps`) pada Setelan Android, atau buka notifikasi Termux untuk melihat sesi build.

Termux tidak mengeksekusi program dari media publik. Karena itu Sonix menyimpan source proyek di microSD, lalu script build menyalin source ke `~/.sonix/workspaces` sebelum menjalankan compiler. Ini juga mengurangi masalah file sementara, permission, dan symlink di kartu SD.

## 3. Membuat proyek/mod dengan AI

1. Buka Sonix → **Settings**.
2. Isi URL HTTPS endpoint AI yang kompatibel dengan `POST /v1/chat/completions`, isi model yang server kenali, dan API key bila diperlukan.
3. Simpan.
4. Buka **AI Chat**, tulis permintaan mod/aplikasi/game. Tekan **Buat proyek + compile dari pesan ini** agar prompt dipindahkan ke Builder. Kamu juga bisa langsung membuka **Builder**.
5. Pilih atau periksa jenis proyek (misalnya GTA San Andreas Android - AML C++).
6. Isi nama proyek, nama aplikasi/game target dan versi game/loader yang diketahui.
7. Tulis deskripsi fitur dengan spesifik. Misalnya sebutkan apa yang harus berubah, kapan fitur aktif, opsi konfigurasi, dan batasan yang tidak boleh berubah.
8. Tekan **AI BUAT PROYEK + COMPILE**. Sonix meminta AI mengembalikan struktur file JSON, menyimpan file ke `SonixNDK/projects/NamaProyek`, lalu mengajukan dialog persetujuan sebelum perintah dikirim ke Terminal SN.
9. Periksa perintah yang muncul di dialog, lalu pilih Jalankan. Termux akan menjalankan build/export.

Contoh deskripsi GTA AML:

```text
Target: GTA San Andreas Android 32-bit, AML.
Buat plugin C++17 dengan ABI armeabi-v7a.
Tolong jelaskan di ringkasan fitur apa yang dibuat.
Jangan mengubah waktu, cuaca, kendaraan, atau gameplay lain yang tidak saya minta.
```

Contoh fitur tertentu bisa diminta pada proyek yang sesuai, tetapi contoh tidak otomatis menjadi pengaturan untuk semua game atau proyek lain.

## 4. Build plugin GTA San Andreas AML `.so`

### Jalur lokal Termux

Sonix mencoba menjalankan `scripts/build-termux.sh`. Script akan menyalin proyek dari kartu SD ke direktori privat Termux, mengunduh template/header AML resmi bila belum tersedia, lalu mencari `ndk-build`.

**Penting:** paket Termux Clang + `ndk-sysroot` + `ndk-multilib` tidak selalu menyediakan semua runtime ARM32 yang diperlukan. Jika `ndk-build` tidak tersedia atau linker ARM32 gagal, itu bukan berarti source rusak. Gunakan jalur Google NDK pada GitHub.

### Jalur Google NDK melalui GitHub

Jalur ini direkomendasikan untuk menghasilkan `.so` `armeabi-v7a` secara konsisten.

1. Di Sonix Builder buat plugin AML.
2. Jalankan ekspor ZIP dari script `scripts/export-project.sh` melalui Terminal SN bila dibutuhkan. Hasil ZIP ada di `builds` atau `~/.sonix/builds` jika Termux tidak boleh menulis ke kartu SD.
3. Upload ZIP proyek tersebut ke root repository GitHub (contoh `MyAMLPlugin.zip`).
4. Buka repository → Actions → **Build GTA SA AML Plugin (.so)** → Run workflow.
5. Pada `project_dir` masukkan nama file ZIP, contohnya `MyAMLPlugin.zip`.
6. Pilih `both`. GitHub menjalankan dua job terpisah: satu NDK r23c dan satu NDK r30.
7. Unduh artifact `Sonix-AML-r23c-armeabi-v7a` dan `Sonix-AML-r30-armeabi-v7a`.
8. Gunakan `.so` hasil build yang cocok dengan versi AML/game. Jangan menganggap build sukses berarti pasti kompatibel dengan semua versi GTA SA.

Workflow mengunduh template/header AML resmi dari `RusJJ/AndroidModLoader`, menyiapkan `mod/`, `include/`, dan `external/`, lalu menjalankan `ndk-build`. Source dan API AML harus sesuai dengan versi loader target.

## 5. Build APK, Minecraft, HTML5 dan proyek lain

- **Minecraft Fabric/Forge:** AI menyiapkan project Gradle; Termux menjalankan `gradle build` atau `./gradlew build`. Pastikan versi Java sesuai versi Minecraft/loader.
- **HTML5 Game Builder:** file HTML/CSS/JS disimpan lalu dikemas menjadi ZIP proyek untuk dibuka di browser atau server lokal.
- **Android APK Builder:** AI menyiapkan project Gradle; build lokal di Termux membutuhkan Android SDK/platform yang cocok. Untuk APK Android yang lebih andal, gunakan workflow GitHub dengan Android SDK terpasang.
- **C++ Compiler:** proyek CMake/Make dapat dibangun lewat Termux untuk target yang didukung. Native plugin GTA SA Android 32-bit disarankan memakai workflow AML khusus di GitHub.
- **Texture Editor / Mod Installer / Project ZIP Exporter:** starter menyediakan alur pembuatan proyek/ZIP; editor visual penuh dan pemasang mod sekali-sentuh belum tersedia.
- **AI Code Repair:** tempel potongan kode dan pesan error pada deskripsi Builder. AI diminta memperbaiki file sambil mempertahankan fitur yang sudah benar. Impor proyek ZIP lama untuk repair otomatis belum tersedia.

## 6. Build proyek buatan AI lewat GitHub

Untuk proyek APK Android, Minecraft Fabric/Forge, atau project Gradle lain yang tidak dapat dibangun lengkap di Termux:

1. Ekspor proyek dari Terminal SN memakai `scripts/export-project.sh` (AML), atau gunakan ZIP proyek yang dibuat Sonix.
2. Upload ZIP tersebut ke root repository GitHub.
3. Buka Actions → **Build Sonix Generated Project** → Run workflow.
4. Isi `project_zip` dengan nama file ZIP di root repository.
5. Pilih `android_apk` untuk proyek Android Gradle, `gradle` untuk Minecraft/Java Gradle, `auto` untuk mendeteksi Gradle atau mengekspor source, atau `zip_only` untuk HTML5/source statis.
6. Pilih Java 17 atau Java 21 sesuai versi Minecraft/Gradle yang dipakai (Minecraft modern tertentu memerlukan Java 21).
7. Unduh artifact **Sonix-Generated-Project**. APK/JAR/SO yang ditemukan ada dalam folder `compiled`; source ZIP juga disertakan.

Workflow ini tidak dapat menebak dependensi atau versi game yang hilang. Jika project butuh Java 21, SDK/NDK tertentu, mod loader, atau repository eksternal, sesuaikan `build.gradle` dan workflow sebelum menjalankan build.

## 7. Build APK Sonix dari GitHub

1. Buat repository GitHub baru.
2. Ekstrak ZIP proyek Sonix AI 1.00 yang kamu unduh dari percakapan ini di HP atau komputer.
3. Cara termudah dari HP adalah memakai GitHub CLI lewat Termux agar folder tersembunyi `.github` ikut ter-upload otomatis:

   - Buat repository **kosong** di `github.com` (tanpa README/license/gitignore).
   - Pastikan Termux sudah terpasang `git`, `gh`, dan `unzip`, lalu jalankan `gh auth login --web --git-protocol https`. Pilih GitHub.com dan ikuti kode login yang dibuka di browser. Lanjutkan dengan `gh auth setup-git`.
   - Di Termux jalankan `termux-setup-storage`, lalu lihat nama ZIP yang diunduh dengan `ls ~/storage/downloads`.
   - Ekstrak ZIP (ganti nama file sesuai unduhanmu) ke direktori privat Termux:

```bash
mkdir -p ~/sonix-upload
unzip ~/storage/downloads/NAMA-ZIP-SONIX.zip -d ~/sonix-upload
cd ~/sonix-upload/SonixAI-1.00
```

   - Jalankan:

```bash
git init
git add -A
git config user.name "Nama GitHub kamu"
git config user.email "Email GitHub kamu"
git commit -m "Upload Sonix AI 1.00"
git branch -M main
git remote add origin https://github.com/USERNAME/NAMA-REPO.git
git push -u origin main
```

   Ganti `USERNAME` dan `NAMA-REPO` dengan alamat repository milikmu. Jangan mengirim kode login, token, atau password ke chat. GitHub CLI tersedia sebagai paket Termux melalui `pkg install gh`; metode `gh auth login --web` menggunakan login browser/device flow.
4. Buka tab Actions. Jika Actions belum aktif, izinkan workflow.
5. Pilih **Build Sonix AI APK** → Run workflow.
6. Unduh artifact `SonixAI-debug-apk`.
7. Ekstrak ZIP artifact, kemudian pasang `app-debug.apk` pada Android. APK Sonix adalah aplikasi Java biasa; NDK r23c dan r30 dipakai pada workflow khusus plugin AML, bukan untuk mengubah APK Java menjadi APK 32-bit.

## 8. Melatih model AI Sonix di komputer GitHub

Repository ini menambahkan workflow **Train Sonix LoRA (CPU on GitHub)**. Cara menjalankan:

1. Tambahkan contoh percakapan coding yang akurat ke `training/dataset.jsonl`. Satu baris = satu objek JSON berisi `instruction`, `input`, dan `output`. Jangan masukkan password, API key, data pribadi, atau file game berhak cipta yang tidak kamu miliki.
2. Commit perubahan dataset ke GitHub.
3. Buka Actions → **Train Sonix LoRA (CPU on GitHub)** → Run workflow.
4. Model dasar default adalah `Qwen/Qwen2.5-Coder-0.5B-Instruct`. `max_steps` default 100; ubah perlahan setelah paham penggunaan RAM/waktu.
5. Workflow memakai runner standar GitHub berbasis CPU dan batas waktu maksimum 4 jam. Ia bisa selesai lebih cepat bila langkah training selesai; batas waktu bukan perintah untuk membuang waktu komputasi.
6. Unduh artifact `Sonix-LoRA-Adapter`.
7. Adapter LoRA **bukan model lengkap**. Ekstrak adapter di komputer/server yang memiliki RAM memadai, install `server/inference-requirements.txt`, isi `server/.env.inference.example`, lalu jalankan `uvicorn inference_lora:app --host 0.0.0.0 --port 8000` dari folder `server`. Letakkan di belakang HTTPS dan masukkan URL `/v1/chat/completions` serta API key ke Settings Sonix. Petunjuk lengkap ada di `server/README.md`.

GitHub Actions standar bukan komputer GPU. Fine-tuning CPU model 0.5B hanya cocok sebagai eksperimen kecil. Ini bukan melatih model besar dari nol dan hasilnya bergantung pada jumlah serta kualitas contoh. Untuk model yang lebih pintar, siapkan dataset lebih besar dan gunakan layanan/server GPU yang sesuai, atau runner GPU berbayar yang memang tersedia di akun organisasi.

## 9. Troubleshooting

- **Library file not found saat unduh:** gunakan tautan unduhan ZIP langsung pada balasan ChatGPT yang menyediakan file. Bila unduhan di aplikasi gagal, buka percakapan yang sama lewat Chrome lalu unduh tautan itu.
- **Folder tidak bisa dipilih:** buat subfolder `SonixNDK` pada microSD, bukan memilih root kartu SD. Android 11 membatasi sebagian folder root.
- **Sonix tidak dapat menulis ke kartu SD:** periksa izin `Allow access to manage all files` Sonix AI. Pastikan microSD tidak read-only dan masih memiliki ruang.
- **Termux Permission denied:** aktifkan izin pengelolaan file Termux dan jalankan ulang `termux-setup-storage`. Periksa juga `allow-external-apps=true` dan izin `RUN_COMMAND` untuk Sonix.
- **`ndk-build` tidak ditemukan di Termux:** gunakan workflow GitHub `Build GTA SA AML Plugin (.so)`.
- **Undefined reference / linker error ARM32:** cek ABI seluruh dependency, API minimum, C++ runtime, serta versi header AML. Jangan menghapus error linker tanpa mengetahui symbol/library yang hilang.
- **AI belum menjawab:** pastikan endpoint HTTPS dan API key benar. Sonix belum mengandung model AI yang berjalan offline.

## Referensi teknis

- Android NDK downloads/revisions: <https://developer.android.com/ndk/downloads>
- AML project and mod development documentation: <https://github.com/RusJJ/AndroidModLoader>
- Termux RUN_COMMAND: <https://github.com/termux/termux-app/wiki/RUN_COMMAND-Intent>
- Termux storage access: <https://github.com/termux/termux-tools/blob/master/doc/termux.1.md.in>
- GitHub-hosted runners/limits: <https://docs.github.com/en/actions/reference/runners/github-hosted-runners> and <https://docs.github.com/en/actions/reference/limits>
- Hugging Face PEFT/LoRA: <https://huggingface.co/docs/transformers/peft>
