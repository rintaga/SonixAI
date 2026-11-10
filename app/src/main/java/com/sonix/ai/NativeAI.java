package com.sonix.ai;

public final class NativeAI {
    private static boolean loaded = false;

    static {
        try {
            System.loadLibrary("sonix_native");
            loaded = true;
        } catch (UnsatisfiedLinkError | SecurityException error) {
            loaded = false;
        }
    }

    private NativeAI() {}

    private static native String nativeReply(String prompt);
    private static native String nativeVersion();
    private static native int nativeTokenCount(String input);

    public static boolean isAvailable() {
        return loaded;
    }

    public static String version() {
        return loaded ? nativeVersion() : "Native engine tidak tersedia.";
    }

    public static String reply(String prompt) {
        if (!loaded) return "Native engine tidak tersedia.";
        try {
            return nativeReply(prompt == null ? "" : prompt);
        } catch (Exception e) {
            return "Native engine error: " + e.getMessage();
        }
    }

    public static int tokenCount(String input) {
        return loaded && input != null ? nativeTokenCount(input) : 0;
    }
}
