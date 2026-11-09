# Sonix AI — GTA San Andreas AML C++ starter

Template dasar untuk plugin Android Mod Loader (AML) 32-bit (`armeabi-v7a`). Contoh `main.cpp` hanya menulis log saat plugin dimuat. Tidak ada fitur gameplay, waktu, cuaca, kendaraan, atau efek lain yang otomatis ditambahkan. Sonix hanya membuat fitur yang secara khusus dijelaskan pengguna pada Builder.

## Target build

- ABI: `armeabi-v7a`
- API minimum: Android 21
- C++: C++17
- NDK utama: Google NDK r30 LTS
- NDK kompatibilitas: r23c (legacy/unsupported)
- Output: `libsonix_aml_sample.so`

## Build Termux di Android

1. Pasang Termux resmi dan jalankan `termux-setup-storage`.
2. Di Sonix AI → NDK pilih folder `SonixNDK` pada kartu microSD eksternal.
3. Beri Termux izin pengelolaan file jika Android memintanya.
4. Pasang toolchain melalui NDK Center.
5. Jalankan `bash scripts/build-termux.sh`.

Build lokal berjalan hanya jika Termux memiliki `ndk-build` yang benar-benar mendukung target 32-bit. Termux Clang + `ndk-sysroot` + `ndk-multilib` tidak selalu memiliki runtime ARM32 lengkap pada setiap versi/paket. Bila `ndk-build` tidak ditemukan atau link gagal, gunakan Google NDK lewat GitHub Actions.

## Build Google NDK di GitHub

1. Zip folder proyek ini dari Sonix Builder/Terminal SN.
2. Upload ZIP ke repository GitHub (contoh `MyAMLPlugin.zip`).
3. Buka Actions → **Build GTA SA AML Plugin (.so)** → Run workflow.
4. Isi `project_dir` dengan path ZIP, misalnya `MyAMLPlugin.zip`.
5. Pilih `both` untuk menjalankan NDK r23c dan r30 secara terpisah.
6. Unduh artifact `Sonix-AML-r23c-armeabi-v7a` dan/atau `Sonix-AML-r30-armeabi-v7a`.

Workflow mengunduh header resmi AML dan menyiapkan folder `mod/`, `include/`, dan `external/` sebelum memanggil `ndk-build`.

## Kompatibilitas

Build sukses belum menjamin mod kompatibel dengan semua versi GTA SA. Cocokkan versi game, versi AML/header, nama library game, ABI, dan API yang dipakai. Jangan menebak offset atau hook address. Uji pada salinan game dan backup file asli.
