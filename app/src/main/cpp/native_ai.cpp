#include <jni.h>
#include <string>
#include <algorithm>
#include <sstream>
#include <cctype>

static std::string lowerText(std::string s) {
    std::transform(s.begin(), s.end(), s.begin(),
        [](unsigned char c) { return static_cast<char>(std::tolower(c)); });
    return s;
}

static int countWords(const std::string& s) {
    std::istringstream in(s);
    std::string word;
    int count = 0;
    while (in >> word) ++count;
    return count;
}

static std::string answer(const std::string& prompt) {
    if (prompt.empty()) return "Silakan tulis pertanyaan terlebih dahulu.";

    std::string p = lowerText(prompt);

    if (p.find("halo") != std::string::npos ||
        p.find("hai") != std::string::npos ||
        p.find("hello") != std::string::npos)
        return "Halo! Sonix Native Brain berhasil berjalan di mesin C++ lokal.";

    if (p.find("ndk") != std::string::npos ||
        p.find("jni") != std::string::npos ||
        p.find("c++") != std::string::npos)
        return "Sonix Native Brain menggunakan C++17, Android NDK, dan JNI.";

    if (p.find("sonix") != std::string::npos)
        return "Saya adalah Sonix Native Brain, prototipe pemrosesan teks lokal.";

    return "Pesan berhasil diproses oleh Sonix Native Brain. "
           "Ini masih prototipe berbasis aturan, belum menggunakan model AI terlatih.";
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_sonix_ai_NativeAI_nativeReply(JNIEnv* env, jclass, jstring input) {
    if (!input) return env->NewStringUTF("Input kosong.");

    const char* chars = env->GetStringUTFChars(input, nullptr);
    if (!chars) return env->NewStringUTF("Gagal membaca input.");

    std::string prompt(chars);
    env->ReleaseStringUTFChars(input, chars);

    std::string output = answer(prompt);
    return env->NewStringUTF(output.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_sonix_ai_NativeAI_nativeVersion(JNIEnv* env, jclass) {
    return env->NewStringUTF("Sonix Native Brain 1.0 | C++17 | JNI");
}

extern "C" JNIEXPORT jint JNICALL
Java_com_sonix_ai_NativeAI_nativeTokenCount(JNIEnv* env, jclass, jstring input) {
    if (!input) return 0;
    const char* chars = env->GetStringUTFChars(input, nullptr);
    if (!chars) return 0;
    int count = countWords(chars);
    env->ReleaseStringUTFChars(input, chars);
    return count;
}
