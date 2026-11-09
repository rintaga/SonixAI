package com.sonix.ai;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.util.Base64;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.provider.DocumentsContract;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private static final int BG = Color.rgb(9, 13, 24);
    private static final int PANEL = Color.rgb(19, 26, 43);
    private static final int PANEL2 = Color.rgb(25, 34, 55);
    private static final int TEXT = Color.rgb(238, 242, 255);
    private static final int MUTED = Color.rgb(157, 169, 195);
    private static final int ACCENT = Color.rgb(124, 108, 255);
    private static final String PREFS = "sonix_settings";
    private static final String TERMUX_PACKAGE = "com.termux";
    private static final int REQUEST_PICK_EXTERNAL_SDK = 4401;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private LinearLayout root;
    private LinearLayout content;
    private TextView chatLog;
    private EditText chatInput;
    private EditText endpointInput;
    private EditText modelInput;
    private EditText keyInput;
    private EditText terminalInput;
    private EditText builderDescription;
    private EditText builderProjectName;
    private EditText builderAppTarget;
    private Spinner builderType;
    private String currentPage = "Home";
    private boolean chatIntroAdded = false;
    private final StringBuilder chatTranscript = new StringBuilder();
    private final JSONArray conversationHistory = new JSONArray();
    private boolean systemMessageAdded = false;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        buildShell();
        showPage("Home");
    }

    private void buildShell() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        root.setPadding(dp(16), dp(12), dp(16), dp(10));

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        TextView brand = text("SONIX", 21, TEXT, true);
        TextView version = text("AI  1.00", 12, ACCENT, true);
        LinearLayout brandBlock = new LinearLayout(this);
        brandBlock.setOrientation(LinearLayout.VERTICAL);
        brandBlock.addView(brand);
        brandBlock.addView(version);
        header.addView(brandBlock, new LinearLayout.LayoutParams(0, -2, 1));
        TextView status = text("● READY", 11, Color.rgb(98, 220, 159), true);
        header.addView(status);
        root.addView(header);

        HorizontalScrollView navScroll = new HorizontalScrollView(this);
        navScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        String[] pages = {"Home", "AI Chat", "Builder", "Terminal SN", "NDK", "Help", "Settings"};
        for (String page : pages) {
            Button b = button(page, false);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-2, dp(40));
            p.setMargins(0, dp(14), dp(7), dp(10));
            nav.addView(b, p);
            b.setOnClickListener(v -> showPage(page));
        }
        navScroll.addView(nav);
        root.addView(navScroll);

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        root.addView(content, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
    }

    private void showPage(String page) {
        currentPage = page;
        content.removeAllViews();
        switch (page) {
            case "AI Chat": buildChat(); break;
            case "Builder": buildBuilder(); break;
            case "Terminal SN": buildTerminal(); break;
            case "NDK": buildNdk(); break;
            case "Help": buildHelp(); break;
            case "Settings": buildSettings(); break;
            default: buildHome(); break;
        }
    }

    private void buildHome() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout box = pageColumn();
        box.addView(space(8));
        box.addView(text("Buat sesuatu. Bangun sesuatu.", 25, TEXT, true));
        box.addView(text("Asisten coding dan modding Android", 14, MUTED, false));
        box.addView(space(18));
        LinearLayout hero = panel();
        hero.addView(text("SONIX AI 1.00", 18, TEXT, true));
        hero.addView(text("AI Project Builder • Auto Compile • GTA SA AML • Minecraft • HTML5 • APK", 13, MUTED, false));
        hero.addView(space(12));
        Button chat = button("Mulai chat AI", true);
        chat.setOnClickListener(v -> showPage("AI Chat"));
        hero.addView(chat);
        box.addView(hero);
        box.addView(space(14));
        addFeature(box, "01  AI CHAT", "Konsultasi coding dan minta bantuan pengembangan aplikasi/mod.", "AI Chat");
        addFeature(box, "02  MOD BUILDER", "Jelaskan target aplikasi/game dan fitur mod yang ingin dibuat; Sonix menyiapkan proyek dan menjalankan build.", "Builder");
        addFeature(box, "03  TERMINAL SN", "Kirim perintah ke Termux asli agar tool berjalan di lingkungan Termux.", "Terminal SN");
        addFeature(box, "04  NDK CENTER", "Setup storage, Clang, CMake, Ninja, Git, dan paket native build.", "NDK");
        addFeature(box, "05  GITHUB BUILD", "Workflow untuk membuat APK debug secara otomatis di GitHub Actions.", "Help");
        box.addView(space(8));
        box.addView(text("Catatan: Sonix 1.00 adalah identitas model di aplikasi. Agar chat benar-benar menjawab, sambungkan endpoint AI dan API key pada Settings.", 12, MUTED, false));
        scroll.addView(box);
        content.addView(scroll);
    }

    private void addFeature(LinearLayout parent, String title, String desc, String page) {
        LinearLayout p = panel();
        p.addView(text(title, 14, TEXT, true));
        p.addView(text(desc, 12, MUTED, false));
        p.addView(space(7));
        Button open = button("Buka", false);
        open.setOnClickListener(v -> showPage(page));
        p.addView(open);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.bottomMargin = dp(10);
        parent.addView(p, lp);
    }

    private void buildChat() {
        LinearLayout page = pageColumn();
        page.addView(text("Sonix Assistant", 22, TEXT, true));
        page.addView(text("Model: " + getPrefs().getString("model", "Sonix 1.00"), 12, MUTED, false));
        ScrollView transcriptScroll = new ScrollView(this);
        transcriptScroll.setFillViewport(true);
        chatLog = text("", 13, TEXT, false);
        chatLog.setTextIsSelectable(true);
        chatLog.setPadding(dp(13), dp(12), dp(13), dp(12));
        chatLog.setBackground(bg(PANEL, 15));
        chatLog.setText(chatTranscript.toString());
        transcriptScroll.addView(chatLog);
        LinearLayout.LayoutParams transParams = new LinearLayout.LayoutParams(-1, 0, 1);
        transParams.topMargin = dp(12);
        page.addView(transcriptScroll, transParams);
        if (!chatIntroAdded) {
            appendChat("SONIX", "Hai! Jelaskan aplikasi/game dan fitur yang ingin dibuat. Untuk membuat proyek siap build, gunakan tombol 'Buat proyek + compile dari pesan ini' agar Sonix menyimpan file dan menjalankan Terminal SN. Sambungkan endpoint AI di Settings terlebih dahulu.");
            chatIntroAdded = true;
        }
        chatInput = new EditText(this);
        chatInput.setHint("Tulis permintaanmu di sini...");
        styleInput(chatInput);
        chatInput.setMinLines(2);
        chatInput.setMaxLines(5);
        LinearLayout.LayoutParams in = new LinearLayout.LayoutParams(-1, -2);
        in.topMargin = dp(10);
        page.addView(chatInput, in);
        Button send = button("Kirim ke Sonix", true);
        LinearLayout.LayoutParams sendLp = new LinearLayout.LayoutParams(-1, dp(46));
        sendLp.topMargin = dp(8);
        page.addView(send, sendLp);
        send.setOnClickListener(v -> sendChat(chatInput.getText().toString().trim()));
        Button buildFromChat = button("Buat proyek + compile dari pesan ini", false);
        LinearLayout.LayoutParams buildFromChatLp = new LinearLayout.LayoutParams(-1, dp(44));
        buildFromChatLp.topMargin = dp(7);
        page.addView(buildFromChat, buildFromChatLp);
        buildFromChat.setOnClickListener(v -> openBuilderWithPrompt(chatInput.getText().toString().trim()));
        content.addView(page);
    }

    private void openBuilderWithPrompt(String prompt) {
        if (prompt.isEmpty()) { toast("Tulis dulu deskripsi mod/aplikasi/game yang ingin dibuat."); return; }
        showPage("Builder");
        builderDescription.setText(prompt);
        String lower = prompt.toLowerCase(java.util.Locale.ROOT);
        int selected = 0;
        String target = "GTA San Andreas Android 32-bit (armeabi-v7a) + AML";
        if (lower.contains("fabric")) { selected = 1; target = "Minecraft + Fabric (isi versi game)"; }
        else if (lower.contains("forge") || lower.contains("neoforge")) { selected = 2; target = "Minecraft + Forge/NeoForge (isi versi game)"; }
        else if (lower.contains("html5") || lower.contains("website") || lower.contains("browser")) { selected = 5; target = "HTML5 / Browser"; }
        else if (lower.contains("texture") || lower.contains("tekstur")) { selected = 6; target = "Texture Editor / aset game"; }
        else if (lower.contains("apk") || lower.contains("aplikasi android")) { selected = 3; target = "Aplikasi Android"; }
        else if (lower.contains("repair") || lower.contains("perbaiki kode") || lower.contains("error")) { selected = 9; target = "Proyek yang ingin diperbaiki"; }
        else if (lower.contains("gta") || lower.contains("aml") || lower.contains("plugin")) { selected = 0; target = "GTA San Andreas Android 32-bit (armeabi-v7a) + AML"; }
        builderType.setSelection(selected);
        builderProjectName.setText("SonixProject");
        builderAppTarget.setText(target);
        toast("Prompt dipindahkan ke Builder. Periksa jenis proyek dan target game, lalu tekan AI BUAT PROYEK + COMPILE.");
    }

    private void sendChat(String prompt) {
        if (prompt.isEmpty()) return;
        SharedPreferences prefs = getPrefs();
        String endpoint = prefs.getString("endpoint", "").trim();
        String model = prefs.getString("model", "Sonix 1.00").trim();
        String apiKey = getStoredApiKey();
        appendChat("KAMU", prompt);
        chatInput.setText("");
        if (endpoint.isEmpty()) {
            appendChat("SONIX", "Endpoint AI belum diatur. Buka Settings, isi URL endpoint kompatibel OpenAI, nama model, dan API key jika diperlukan.");
            return;
        }
        try {
            if (!systemMessageAdded) {
                JSONObject system = new JSONObject();
                system.put("role", "system");
                system.put("content", "Kamu adalah Sonix AI 1.00, asisten pengembangan software dan modding. Untuk permintaan membuat aplikasi, game, plugin, atau mod, jangan menampilkan source code panjang di chat. Arahkan pengguna memakai tombol 'Buat proyek + compile dari pesan ini' atau menu Builder. Builder akan meminta AI menghasilkan file proyek, menyimpannya, lalu mengirim build ke Terminal SN/Termux setelah pengguna menyetujui perintah. Jangan pernah mengaku build sukses sebelum Termux/GitHub melaporkan hasil. Untuk GTA San Andreas AML, pastikan versi game/AML/ABI bila penting. Untuk Minecraft, pastikan versi game dan loader Fabric/Forge/NeoForge. Jelaskan ringkasan fitur dan error dengan singkat; jangan menampilkan isi semua source kecuali pengguna secara khusus meminta source.");
                conversationHistory.put(system);
                systemMessageAdded = true;
            }
            JSONObject user = new JSONObject();
            user.put("role", "user");
            user.put("content", prompt);
            conversationHistory.put(user);
        } catch (Exception e) { appendChat("SONIX", "Gagal menyiapkan pesan: " + e.getMessage()); return; }
        appendChat("SONIX", "Sedang menghubungi model...");
        final JSONArray snapshot;
        try { snapshot = new JSONArray(conversationHistory.toString()); }
        catch (Exception e) { appendChat("SONIX", "Gagal menyiapkan riwayat chat."); return; }
        executor.execute(() -> {
            String answer;
            try { answer = requestAi(endpoint, model, apiKey, snapshot); }
            catch (Exception e) { answer = "Gagal menghubungi endpoint: " + e.getMessage() + "\nPeriksa URL HTTPS, koneksi internet, API key, dan format API."; }
            final String result = answer;
            runOnUiThread(() -> {
                if (!result.startsWith("Gagal menghubungi endpoint:") && !result.startsWith("HTTP ")) {
                    try { JSONObject assistant = new JSONObject(); assistant.put("role", "assistant"); assistant.put("content", result); conversationHistory.put(assistant); } catch (Exception ignored) { }
                }
                appendChat("SONIX", result);
            });
        });
    }

    private String requestAi(String endpoint, String model, String apiKey, JSONArray messages) throws Exception {
        if (!endpoint.startsWith("https://")) throw new IllegalArgumentException("Gunakan URL HTTPS untuk melindungi API key.");
        HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(20000);
        conn.setReadTimeout(90000);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        conn.setRequestProperty("Accept", "application/json");
        if (!apiKey.isEmpty()) conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        JSONObject body = new JSONObject();
        body.put("model", model);
        body.put("messages", messages);
        body.put("temperature", 0.6);
        byte[] payload = body.toString().getBytes(StandardCharsets.UTF_8);
        conn.setFixedLengthStreamingMode(payload.length);
        try (OutputStream os = conn.getOutputStream()) { os.write(payload); }
        int status = conn.getResponseCode();
        InputStream stream = status >= 200 && status < 300 ? conn.getInputStream() : conn.getErrorStream();
        String response = readAll(stream);
        conn.disconnect();
        JSONObject parsed;
        try { parsed = new JSONObject(response); }
        catch (Exception e) { throw new IllegalStateException("Respons server bukan JSON: " + response); }
        if (status < 200 || status >= 300) {
            JSONObject err = parsed.optJSONObject("error");
            return "HTTP " + status + ": " + (err != null ? err.optString("message", response) : response);
        }
        JSONArray choices = parsed.optJSONArray("choices");
        if (choices == null || choices.length() == 0) return "Respons server tidak memiliki choices. Pastikan endpoint memakai format OpenAI Chat Completions.";
        JSONObject message = choices.getJSONObject(0).optJSONObject("message");
        return message == null ? response : message.optString("content", response);
    }

    private String readAll(InputStream input) throws Exception {
        if (input == null) return "";
        StringBuilder b = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line; while ((line = r.readLine()) != null) b.append(line);
        }
        return b.toString();
    }

    private void appendChat(String who, String message) {
        if (who.equals("SONIX") && !message.equals("Sedang menghubungi model...")) {
            String waiting = "\n\nSONIX\nSedang menghubungi model...";
            int idx = chatTranscript.lastIndexOf(waiting);
            if (idx >= 0) chatTranscript.delete(idx, chatTranscript.length());
        }
        if (chatTranscript.length() > 0) chatTranscript.append("\n\n");
        chatTranscript.append(who).append("\n").append(message);
        if (chatLog != null && currentPage.equals("AI Chat")) {
            chatLog.setText(chatTranscript.toString());
        }
    }

    private void buildBuilder() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout page = pageColumn();
        page.addView(text("AI Project Builder", 22, TEXT, true));
        page.addView(text("Jelaskan aplikasi/game yang dituju dan mod yang ingin dibuat. AI akan menyiapkan file proyek, menyimpannya, lalu meminta izin untuk menjalankan proses build lewat Terminal SN.", 13, MUTED, false));
        page.addView(space(12));
        page.addView(text("Jenis proyek", 13, TEXT, true));
        builderType = new Spinner(this);
        String[] types = {
            "GTA San Andreas Android - AML C++ (.so)",
            "Minecraft Mod - Fabric",
            "Minecraft Mod - Forge / NeoForge",
            "Android APK Builder - Java",
            "C++ Compiler - Android Native Library",
            "HTML5 Game Builder",
            "Texture Editor / Texture Tools",
            "Mod Installer / ZIP Manager",
            "Project ZIP Exporter",
            "AI Code Repair - Fix Project"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types);
        builderType.setAdapter(adapter);
        page.addView(builderType);
        page.addView(space(10));
        page.addView(text("Nama proyek", 13, TEXT, true));
        builderProjectName = new EditText(this);
        builderProjectName.setSingleLine(true);
        builderProjectName.setHint("Contoh: MyAMLPlugin");
        styleInput(builderProjectName);
        page.addView(builderProjectName);
        page.addView(space(10));
        page.addView(text("Aplikasi atau game target", 13, TEXT, true));
        builderAppTarget = new EditText(this);
        builderAppTarget.setSingleLine(true);
        builderAppTarget.setHint("Contoh: GTA San Andreas Android v2.00");
        styleInput(builderAppTarget);
        page.addView(builderAppTarget);
        page.addView(space(10));
        page.addView(text("Deskripsi fitur yang ingin dibuat", 13, TEXT, true));
        builderDescription = new EditText(this);
        builderDescription.setHint("Jelaskan mod atau aplikasi yang kamu inginkan. Contoh: buat plugin AML yang menambah fitur sesuai deskripsiku...");
        styleInput(builderDescription);
        builderDescription.setGravity(Gravity.TOP | Gravity.START);
        builderDescription.setMinLines(5);
        builderDescription.setMaxLines(9);
        page.addView(builderDescription);
        page.addView(space(10));
        Button generate = button("AI BUAT PROYEK + COMPILE", true);
        page.addView(generate);
        generate.setOnClickListener(v -> {
            String desc = builderDescription.getText().toString().trim();
            String name = builderProjectName.getText().toString().trim();
            String appTarget = builderAppTarget.getText().toString().trim();
            if (desc.isEmpty()) { toast("Tulis deskripsi mod/aplikasi/game dulu."); return; }
            if (name.isEmpty()) name = "SonixProject";
            if (appTarget.isEmpty()) appTarget = builderType.getSelectedItem().toString();
            generateProjectFromAI(builderType.getSelectedItem().toString(), name, appTarget, desc);
        });
        page.addView(space(8));
        page.addView(text("Alur otomatis: AI menyusun file → Sonix menyimpan ke SonixNDK/projects → Terminal SN menjalankan build → file hasil masuk ke SonixNDK/builds.", 12, MUTED, false));
        page.addView(space(14));
        LinearLayout examples = panel();
        examples.addView(text("Contoh permintaan", 14, TEXT, true));
        addPreset(examples, "AML plugin dasar", "Buat plugin AML sederhana untuk GTA San Andreas Android. Jangan menambahkan perubahan gameplay tertentu yang tidak saya minta. Target armeabi-v7a dan AML versi yang sesuai.");
        addPreset(examples, "Minecraft Fabric", "Buat mod Fabric untuk versi Minecraft yang nanti saya tentukan. Buat file Gradle, metadata mod, dan initializer.");
        addPreset(examples, "Minecraft Forge", "Buat mod Forge untuk Minecraft. Tanyakan versi Minecraft/Forge hanya jika belum bisa ditentukan dari deskripsi.");
        addPreset(examples, "HTML5 Game", "Buat game HTML5 2D/3D sederhana dengan kontrol sentuh Android dan optimisasi RAM rendah.");
        addPreset(examples, "Texture Editor", "Buat tool editor tekstur untuk membuka, mengubah ukuran, konversi, dan ekspor format gambar yang disebutkan.");
        addPreset(examples, "APK Builder", "Buat aplikasi Android native sederhana dan siapkan proyek Gradle agar dapat dibangun menjadi APK debug.");
        addPreset(examples, "AI Code Repair", "Periksa seluruh proyek ini berdasarkan pesan error yang saya sertakan. Temukan akar masalah, perbaiki file yang diperlukan, pertahankan fitur yang sudah ada, dan siapkan ulang build. Jangan menghapus kode hanya agar build lolos.");
        page.addView(examples);
        scroll.addView(page);
        content.addView(scroll);
    }

    private void generateProjectFromAI(String target, String requestedName, String appTarget, String description) {
        SharedPreferences prefs = getPrefs();
        String endpoint = prefs.getString("endpoint", "").trim();
        String model = prefs.getString("model", "Sonix 1.00").trim();
        String apiKey = getStoredApiKey();
        if (endpoint.isEmpty()) {
            new AlertDialog.Builder(this).setTitle("AI belum tersambung")
                .setMessage("Buka Settings, masukkan endpoint AI kompatibel OpenAI dan API key bila diperlukan. Sonix belum membawa model AI lokal.")
                .setPositiveButton("Buka Settings", (d,w) -> showPage("Settings"))
                .setNegativeButton("Tutup", null).show();
            return;
        }
        final String workspacePath = getWorkspacePath();
        if (workspacePath.isEmpty()) {
            new AlertDialog.Builder(this).setTitle("Pilih kartu SD eksternal dulu")
                .setMessage("Buka menu NDK, tekan 'Pilih folder SonixNDK di kartu SD eksternal', lalu pilih folder SonixNDK pada microSD. Sonix tidak akan diam-diam memakai penyimpanan internal.")
                .setPositiveButton("Buka NDK Center", (d,w) -> showPage("NDK"))
                .setNegativeButton("Tutup", null).show();
            return;
        }
        if (!hasSharedStorageAccess()) {
            new AlertDialog.Builder(this).setTitle("Izin akses kartu SD diperlukan")
                .setMessage("Aktifkan 'Allow access to manage all files' untuk Sonix AI. Setelah itu kembali ke aplikasi.")
                .setPositiveButton("Buka izin file", (d,w) -> openAllFilesSettings())
                .setNegativeButton("Batal", null).show();
            return;
        }
        final String projectName = safeProjectName(requestedName);
        final boolean isAml = target.startsWith("GTA San Andreas Android");
        appendChat("SONIX", "Sedang menyiapkan proyek " + projectName + " untuk " + appTarget + ". Source akan disimpan sebagai berkas proyek, bukan ditampilkan panjang di chat.");
        executor.execute(() -> {
            String outcome;
            File projectDir = null;
            try {
                JSONArray messages = new JSONArray();
                JSONObject system = new JSONObject();
                system.put("role", "system");
                system.put("content", "Kamu adalah Sonix AI 1.00, agen pembuat proyek software. Keluaran WAJIB satu objek JSON valid tanpa markdown atau teks di luar JSON dengan skema: {\"project_name\":string,\"summary\":string,\"files\":[{\"path\":string,\"content\":string}],\"build_notes\":string}. Buat file source lengkap yang diperlukan. Jangan mengeluarkan source di luar JSON. Setiap path relatif dari root proyek, tanpa ../ atau path absolut. Batasi 40 file dan total konten 900 KB. Untuk GTA SA AML Android, target ABI hanya armeabi-v7a, Android API 21+, C++17, dan WAJIB buat file source utama bernama persis main.cpp di root proyek. Buat header/source tambahan bila dibutuhkan; jangan mengarang alamat fungsi game atau API AML yang tidak ada. Gunakan header resmi AML dari SDK yang tersedia; build system dasar akan disediakan Sonix. Jangan menambahkan efek atau fitur yang tidak diminta. Contoh 'membuat game selalu malam' hanyalah contoh permintaan: jangan memasukkan fitur malam/ubah waktu kecuali pengguna secara eksplisit meminta fitur itu pada proyek GTA SA. Untuk Minecraft, buat proyek Gradle sesuai loader dan versi yang dijelaskan. Untuk HTML5, sediakan index.html, CSS dan JS. Untuk AI Code Repair, analisis error yang diberikan, pertahankan perilaku yang sudah benar, buat file revisi lengkap dan bila perlu tambahkan catatan perbaikan di build_notes. Jangan membuat perubahan fitur di luar permintaan. Kembalikan JSON valid saja.");
                messages.put(system);
                JSONObject user = new JSONObject();
                user.put("role", "user");
                user.put("content", "Jenis proyek: " + target + "\nNama: " + projectName + "\nAplikasi/game target: " + appTarget + "\nABI target untuk native: armeabi-v7a\nToolchain: Google Android NDK r23c dan r30 melalui GitHub; Termux Clang/ndk-sysroot sebagai jalur lokal jika tersedia.\nLokasi proyek: SonixNDK/projects/" + projectName + "\nFitur yang diminta: " + description + "\nBuat berkas proyek siap build. Jangan hanya memberikan instruksi atau source di chat.");
                messages.put(user);
                String raw = requestAi(endpoint, model, apiKey, messages);
                if (raw.startsWith("HTTP ") || raw.startsWith("Respons server")) throw new IOException(raw);
                JSONObject manifest = parseAiProjectJson(raw);
                JSONArray files = manifest.optJSONArray("files");
                if (files == null || files.length() == 0 || files.length() > 40) throw new IOException("AI tidak mengembalikan daftar file yang valid (maksimal 40 file).");
                File workspace = new File(workspacePath, "projects");
                if (!workspace.exists() && !workspace.mkdirs()) throw new IOException("Gagal membuat folder projects di kartu SD eksternal. Periksa izin kartu SD.");
                projectDir = new File(workspace, projectName);
                if (!projectDir.exists() && !projectDir.mkdirs()) throw new IOException("Gagal membuat folder proyek.");
                long totalBytes = 0;
                int written = 0;
                for (int i = 0; i < files.length(); i++) {
                    JSONObject item = files.optJSONObject(i);
                    if (item == null) continue;
                    String relative = item.optString("path", "").trim();
                    String fileContent = item.optString("content", "");
                    if (!isSafeRelativePath(relative)) continue;
                    if (relative.equalsIgnoreCase("Android.mk") || relative.equalsIgnoreCase("Application.mk") || relative.equalsIgnoreCase("scripts/build-termux.sh")) continue;
                    byte[] bytes = fileContent.getBytes(StandardCharsets.UTF_8);
                    totalBytes += bytes.length;
                    if (totalBytes > 900 * 1024) throw new IOException("Ukuran hasil AI melebihi batas 900 KB.");
                    File out = new File(projectDir, relative);
                    File parent = out.getParentFile();
                    if (parent != null && !parent.exists() && !parent.mkdirs()) throw new IOException("Gagal membuat folder: " + relative);
                    try (FileOutputStream stream = new FileOutputStream(out)) { stream.write(bytes); }
                    written++;
                }
                if (written == 0) throw new IOException("Tidak ada file proyek yang berhasil disimpan.");
                if (isAml) {
                    if (!new File(projectDir, "main.cpp").isFile()) throw new IOException("AI belum membuat main.cpp di root proyek AML. Coba ulangi Builder dan minta file main.cpp secara eksplisit.");
                    writeAmlBuildFiles(projectDir, projectName);
                } else writeGenericBuildFiles(projectDir, projectName, target);
                writeProjectMetadata(projectDir, projectName, target, appTarget, description, manifest.optString("summary", ""));
                if (isAml) outcome = "PROYEK_SIAP_BUILD_AML|" + projectDir.getAbsolutePath() + "|" + written + "|" + manifest.optString("summary", "");
                else outcome = "PROYEK_SIAP_BUILD_GENERIC|" + projectDir.getAbsolutePath() + "|" + written + "|" + manifest.optString("summary", "") + "|" + manifest.optString("build_notes", "");
            } catch (Exception e) {
                outcome = "GAGAL|" + e.getMessage();
            }
            final String result = outcome;
            runOnUiThread(() -> {
                if (result.startsWith("PROYEK_SIAP_BUILD_AML|")) {
                    String[] parts = result.split("\\|", 4);
                    appendChat("SONIX", "File proyek berhasil disimpan (" + parts[2] + " file). " + parts[3] + "\nMenyiapkan kompilasi plugin AML untuk armeabi-v7a.");
                    String command = "SONIX_SDK_ROOT=" + shellQuote(workspacePath) + " bash " + shellQuote(parts[1] + "/scripts/build-termux.sh");
                    runTermux(command, true);
                } else if (result.startsWith("PROYEK_SIAP_BUILD_GENERIC|")) {
                    String[] parts = result.split("\\|", 5);
                    appendChat("SONIX", "Proyek tersimpan di kartu SD eksternal (" + parts[2] + " file).\n" + parts[3] + "\n" + (parts.length > 4 ? parts[4] : "") + "\nSonix akan meminta Terminal SN menjalankan build yang cocok untuk jenis proyek ini.");
                    String command = "SONIX_SDK_ROOT=" + shellQuote(workspacePath) + " bash " + shellQuote(parts[1] + "/scripts/build-termux.sh");
                    runTermux(command, true);
                } else {
                    appendChat("SONIX", "Gagal membuat proyek: " + result.substring("GAGAL|".length()));
                    toast("Pembuatan proyek gagal. Periksa endpoint AI.");
                }
            });
        });
    }

    private String shellQuote(String value) {
        return "'" + value.replace("'", "'\\''") + "'";
    }

    private void writeGenericBuildFiles(File projectDir, String projectName, String target) throws IOException {
        String script = """
            #!/data/data/com.termux/files/usr/bin/bash
            set -euo pipefail
            SOURCE_DIR="$(cd "$(dirname "$0")/.." && pwd)"
            NAME="$(basename "$SOURCE_DIR")"
            SDK_ROOT="${SONIX_SDK_ROOT:-$HOME/.sonix}"
            WORK="$HOME/.sonix/workspaces/$NAME"
            OUT="$SDK_ROOT/builds/$NAME"
            PRIVATE_OUT="$HOME/.sonix/builds/$NAME"
            mkdir -p "$(dirname "$WORK")" "$PRIVATE_OUT"
            rm -rf "$WORK"
            mkdir -p "$WORK"
            cp -R "$SOURCE_DIR"/. "$WORK"/
            cd "$WORK"
            BUILT=0
            if [ -f gradlew ]; then
              chmod +x gradlew
              ./gradlew --no-daemon build
              BUILT=1
            elif [ -f build.gradle ] || [ -f build.gradle.kts ]; then
              command -v gradle >/dev/null 2>&1 || { echo 'Gradle belum terpasang. Jalankan pkg install gradle'; exit 5; }
              gradle --no-daemon build
              BUILT=1
            elif [ -f CMakeLists.txt ]; then
              command -v cmake >/dev/null 2>&1 || { echo 'CMake belum terpasang. Jalankan pkg install cmake'; exit 6; }
              cmake -S . -B build
              cmake --build build -j2
              BUILT=1
            fi
            mkdir -p "$PRIVATE_OUT"
            find . -type f \\( -name '*.apk' -o -name '*.jar' -o -name '*.so' \\) -not -path './.gradle/*' -exec cp -v {} "$PRIVATE_OUT"/ \\;
            if [ -f index.html ] || [ -f package.json ] || [ -f README.md ] || [ "$BUILT" -eq 0 ]; then
              (cd "$(dirname "$WORK")" && zip -qr "$PRIVATE_OUT/$NAME-project.zip" "$NAME" -x '*/build/*' '*/.gradle/*')
            fi
            if mkdir -p "$OUT" 2>/dev/null && cp -v "$PRIVATE_OUT"/* "$OUT"/ 2>/dev/null; then
              echo "[Sonix] Hasil disalin ke microSD: $OUT"
            else
              echo "[Sonix] Termux tidak dapat menulis ke folder microSD. Hasil aman tetap ada di: $PRIVATE_OUT"
            fi
            echo '[Sonix] Build/ekspor selesai.'
            """;
        writeTextFile(new File(projectDir, "scripts/build-termux.sh"), script);
        String exportScript = """
            #!/data/data/com.termux/files/usr/bin/bash
            set -euo pipefail
            P="$(cd "$(dirname "$0")/.." && pwd)"
            N="$(basename "$P")"
            SDK_ROOT="${SONIX_SDK_ROOT:-$HOME/.sonix}"
            PRIVATE_OUT="$HOME/.sonix/builds/$N"
            mkdir -p "$PRIVATE_OUT"
            (cd "$(dirname "$P")" && zip -qr "$PRIVATE_OUT/$N-project.zip" "$N" -x '*/build/*' '*/.gradle/*')
            if mkdir -p "$SDK_ROOT/builds/$N" 2>/dev/null && cp -v "$PRIVATE_OUT/$N-project.zip" "$SDK_ROOT/builds/$N/" 2>/dev/null; then
              echo "ZIP kartu microSD: $SDK_ROOT/builds/$N/$N-project.zip"
            else
              echo "ZIP tersimpan pada storage privat Termux: $PRIVATE_OUT/$N-project.zip"
            fi
            """;
        writeTextFile(new File(projectDir, "scripts/export-project.sh"), exportScript);
    }

    private JSONObject parseAiProjectJson(String raw) throws Exception {
        String cleaned = raw.trim();
        if (cleaned.startsWith("```")) {
            int first = cleaned.indexOf('\n');
            int last = cleaned.lastIndexOf("```");
            if (first >= 0 && last > first) cleaned = cleaned.substring(first + 1, last).trim();
        }
        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start < 0 || end <= start) throw new IOException("Jawaban AI bukan JSON proyek.");
        return new JSONObject(cleaned.substring(start, end + 1));
    }

    private String safeProjectName(String name) {
        String safe = name.replaceAll("[^A-Za-z0-9._-]", "_").replaceAll("^\\.+", "");
        if (safe.isEmpty()) safe = "SonixProject";
        return safe.length() > 48 ? safe.substring(0, 48) : safe;
    }

    private boolean isSafeRelativePath(String path) {
        if (path.isEmpty() || path.startsWith("/") || path.startsWith("\\") || path.contains("..") || path.contains(":") || path.contains("\\")) return false;
        return !path.startsWith("scripts/build-termux.sh");
    }

    private String getWorkspacePath() {
        return getPrefs().getString("workspace_path", "").trim();
    }

    private void chooseExternalSdFolder() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        try { startActivityForResult(intent, REQUEST_PICK_EXTERNAL_SDK); }
        catch (Exception e) { toast("File picker tidak tersedia di perangkat ini."); }
    }

    private void handleExternalSdSelection(Uri treeUri) {
        try {
            getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            String documentId = DocumentsContract.getTreeDocumentId(treeUri);
            int colon = documentId.indexOf(':');
            if (colon <= 0) throw new IOException("ID folder SD card tidak dikenali.");
            String volume = documentId.substring(0, colon);
            String relative = documentId.substring(colon + 1);
            if (volume.equalsIgnoreCase("primary")) throw new IOException("Folder yang dipilih adalah penyimpanan internal. Pilih microSD eksternal.");
            if (!volume.matches("[A-Za-z0-9_-]{4,64}")) throw new IOException("Nama volume kartu SD tidak valid.");
            if (relative.contains("..") || relative.startsWith("/") || relative.contains("\\")) throw new IOException("Path folder SD tidak aman.");
            String path = "/storage/" + volume + (relative.isEmpty() ? "" : "/" + relative);
            // ACTION_OPEN_DOCUMENT_TREE is authoritative for the selected folder. Save it first;
            // raw File access may not work until Android's all-files permission is granted.
            getPrefs().edit().putString("external_sd_tree_uri", treeUri.toString()).putString("workspace_path", path).apply();
            if (!hasSharedStorageAccess()) {
                new AlertDialog.Builder(this).setTitle("Aktifkan akses semua file")
                    .setMessage("Folder SD sudah dipilih: " + path + "\\nSekarang aktifkan 'Allow access to manage all files' untuk Sonix AI agar proyek dapat disimpan ke kartu SD.")
                    .setPositiveButton("Buka izin", (d,w) -> openAllFilesSettings()).setNegativeButton("Nanti", null).show();
            } else {
                toast("Lokasi SonixNDK diset ke kartu SD eksternal: " + path);
                showPage("NDK");
            }
        } catch (Exception e) {
            toast("Gagal memilih SD card: " + e.getMessage());
        }
    }

    private boolean hasSharedStorageAccess() {
        return android.os.Build.VERSION.SDK_INT < 30 || Environment.isExternalStorageManager();
    }

    private void openAllFilesSettings() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= 30) {
                Intent i = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivity(i);
            } else {
                Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
                startActivity(i);
            }
        } catch (Exception e) { toast("Tidak dapat membuka pengaturan izin file."); }
    }

    private void writeTextFile(File file, String content) throws IOException {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) throw new IOException("Gagal membuat folder " + parent.getAbsolutePath());
        try (FileOutputStream stream = new FileOutputStream(file)) { stream.write(content.getBytes(StandardCharsets.UTF_8)); }
    }

    private void writeAmlBuildFiles(File projectDir, String projectName) throws IOException {
        String module = safeProjectName(projectName).toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9_]", "_");
        if (module.isEmpty() || Character.isDigit(module.charAt(0))) module = "sonix_" + module;
        String androidMk = "LOCAL_PATH := $(call my-dir)\ninclude $(CLEAR_VARS)\nLOCAL_CPP_EXTENSION := .cpp .cc\nLOCAL_MODULE := " + module + "\nLOCAL_SRC_FILES := main.cpp $(wildcard src/*.cpp) $(wildcard cpp/*.cpp) mod/logger.cpp mod/config.cpp\nLOCAL_CFLAGS += -O2 -mfloat-abi=softfp -DNDEBUG\nLOCAL_CPPFLAGS += -std=c++17 -fexceptions\nLOCAL_C_INCLUDES += $(LOCAL_PATH)/include $(LOCAL_PATH)\nLOCAL_LDLIBS += -llog -ldl\ninclude $(BUILD_SHARED_LIBRARY)\n";
        String applicationMk = "APP_STL := c++_static\nAPP_ABI := armeabi-v7a\nAPP_PLATFORM := android-21\nAPP_OPTIM := release\nAPP_CPPFLAGS += -std=c++17\n";
        writeTextFile(new File(projectDir, "Android.mk"), androidMk);
        writeTextFile(new File(projectDir, "Application.mk"), applicationMk);
        writeTextFile(new File(projectDir, "scripts/build-termux.sh"), getAmlBuildScript(module));
        writeTextFile(new File(projectDir, "scripts/build-github.sh"), "#!/usr/bin/env bash\nset -euo pipefail\nndk-build NDK_PROJECT_PATH=. APP_BUILD_SCRIPT=Android.mk NDK_APPLICATION_MK=Application.mk NDK_LIBS_OUT=build/libs\nmkdir -p \"$SONIX_SDK_ROOT/builds/" + safeProjectName(projectName) + "\"\ncp -v build/libs/armeabi-v7a/*.so \"$SONIX_SDK_ROOT/builds/" + safeProjectName(projectName) + "/\"\n");
        writeTextFile(new File(projectDir, "scripts/export-project.sh"), "#!/data/data/com.termux/files/usr/bin/bash\nset -euo pipefail\nP=\"$(cd \"$(dirname \"$0\")/..\" && pwd)\"\nN=\"$(basename \"$P\")\"\nSDK_ROOT=\"${SONIX_SDK_ROOT:-$HOME/.sonix}\"\nPRIVATE_OUT=\"$HOME/.sonix/builds/$N\"\nmkdir -p \"$PRIVATE_OUT\"\n(cd \"$(dirname \"$P\")\" && zip -qr \"$PRIVATE_OUT/$N-source.zip\" \"$N\" -x '*/build/*')\nif mkdir -p \"$SDK_ROOT/builds/$N\" 2>/dev/null && cp -v \"$PRIVATE_OUT/$N-source.zip\" \"$SDK_ROOT/builds/$N/\" 2>/dev/null; then echo \"ZIP kartu microSD: $SDK_ROOT/builds/$N/$N-source.zip\"; else echo \"ZIP privat Termux: $PRIVATE_OUT/$N-source.zip\"; fi\n");
    }

    private String getAmlBuildScript(String module) {
        return """
            #!/data/data/com.termux/files/usr/bin/bash
            set -euo pipefail
            SOURCE_DIR="$(cd "$(dirname "$0")/.." && pwd)"
            NAME="$(basename "$SOURCE_DIR")"
            SDK_ROOT="${SONIX_SDK_ROOT:-$HOME/.sonix}"
            WORK="$HOME/.sonix/workspaces/$NAME"
            OUT_DIR="$SDK_ROOT/builds/$NAME"
            PRIVATE_OUT="$HOME/.sonix/builds/$NAME"
            mkdir -p "$(dirname "$WORK")" "$PRIVATE_OUT"
            rm -rf "$WORK"
            mkdir -p "$WORK"
            cp -R "$SOURCE_DIR"/. "$WORK"/
            cd "$WORK"
            echo '[Sonix] Source proyek disalin ke filesystem privat Termux sebelum compile.'
            test -f main.cpp || { echo '[Sonix] ERROR: main.cpp tidak ditemukan.'; exit 2; }
            if [ ! -f mod/amlmod.h ] || [ ! -f mod/logger.cpp ] || [ ! -f mod/config.cpp ]; then
              echo '[Sonix] Mengambil header/template AML resmi...'
              command -v git >/dev/null 2>&1 || { echo 'Git belum ada: pkg install git'; exit 3; }
              TMP="$HOME/.cache/sonix-aml-sdk"
              if [ ! -d "$TMP/.git" ]; then rm -rf "$TMP"; git clone --depth 1 https://github.com/RusJJ/AndroidModLoader "$TMP"; fi
              test -f "$TMP/template_of_mod/mod/amlmod.h" || { echo 'Header AML tidak ditemukan pada repo resmi.'; exit 4; }
              mkdir -p mod include external
              cp -R "$TMP/template_of_mod/mod/." mod/
              [ ! -d "$TMP/include" ] || cp -R "$TMP/include/." include/
              [ ! -d "$TMP/external" ] || cp -R "$TMP/external/." external/
            fi
            if [ -n "${ANDROID_NDK_HOME:-}" ] && [ -x "$ANDROID_NDK_HOME/ndk-build" ]; then
              NDK_BUILD="$ANDROID_NDK_HOME/ndk-build"
            elif command -v ndk-build >/dev/null 2>&1; then
              NDK_BUILD="$(command -v ndk-build)"
            else
              echo '[Sonix] ndk-build tidak ditemukan di Termux.'
              echo '[Sonix] Clang/ndk-sysroot/ndk-multilib adalah jalur lokal terpisah dan tidak selalu menyediakan runtime ARM32 lengkap.'
              echo '[Sonix] Jalur build yang direkomendasikan: GitHub Actions > Build GTA SA AML Plugin (.so), pilih both.'
              exit 10
            fi
            "$NDK_BUILD" NDK_PROJECT_PATH=. APP_BUILD_SCRIPT=Android.mk NDK_APPLICATION_MK=Application.mk NDK_LIBS_OUT=build/libs
            mkdir -p "$PRIVATE_OUT"
            cp -v build/libs/armeabi-v7a/*.so "$PRIVATE_OUT"/
            (cd "$(dirname "$WORK")" && zip -qr "$PRIVATE_OUT/$NAME-source.zip" "$NAME" -x '*/build/*')
            if mkdir -p "$OUT_DIR" 2>/dev/null && cp -v "$PRIVATE_OUT"/* "$OUT_DIR"/ 2>/dev/null; then
              echo "[Sonix] .so dan ZIP proyek disimpan ke microSD: $OUT_DIR"
            else
              echo "[Sonix] Termux tidak dapat menulis ke kartu microSD. Hasil tetap aman di $PRIVATE_OUT"
            fi
            echo '[Sonix] AML build selesai.'
            """;
    }

    private void writeProjectMetadata(File projectDir, String name, String target, String appTarget, String description, String summary) throws IOException {
        JSONObject meta = new JSONObject();
        try {
            meta.put("name", name);
            meta.put("target", target);
            meta.put("application", appTarget);
            meta.put("description", description);
            meta.put("summary", summary);
            meta.put("abi", "armeabi-v7a");
            meta.put("created_by", "Sonix AI 1.00");
        } catch (Exception ignored) { }
        writeTextFile(new File(projectDir, ".sonix-project.json"), meta.toString(2));
    }

    private void addPreset(LinearLayout parent, String label, String prompt) {
        Button b = button(label, false);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(42));
        lp.topMargin = dp(7);
        parent.addView(b, lp);
        b.setOnClickListener(v -> {
            showPage("AI Chat");
            chatInput.setText(prompt);
        });
    }

    private void buildTerminal() {
        LinearLayout page = pageColumn();
        page.addView(text("Terminal SN", 22, TEXT, true));
        page.addView(text("Terminal ini menjalankan perintah melalui Termux asli. Output, instalasi paket, dan akses file ditangani oleh Termux.", 13, MUTED, false));
        page.addView(space(12));
        LinearLayout panel = panel();
        panel.addView(text("COMMAND", 11, ACCENT, true));
        terminalInput = new EditText(this);
        terminalInput.setHint("pkg update && pkg upgrade");
        terminalInput.setSingleLine(false);
        terminalInput.setMinLines(3);
        terminalInput.setGravity(Gravity.TOP | Gravity.START);
        styleInput(terminalInput);
        panel.addView(terminalInput);
        Button run = button("Jalankan di Termux", true);
        LinearLayout.LayoutParams runLp = new LinearLayout.LayoutParams(-1, dp(46));
        runLp.topMargin = dp(9);
        panel.addView(run, runLp);
        run.setOnClickListener(v -> runTermux(terminalInput.getText().toString().trim(), true));
        page.addView(panel);
        page.addView(space(12));
        page.addView(text("Perintah cepat", 14, TEXT, true));
        quickCommand(page, "Buka Termux", null);
        quickCommand(page, "Setup storage", "termux-setup-storage");
        quickCommand(page, "Update paket", "pkg update -y && pkg upgrade -y");
        quickCommand(page, "Install Git + Python", "pkg install -y git python");
        String workspace = getWorkspacePath();
        if (workspace.isEmpty()) {
            quickCommand(page, "Pilih folder SonixNDK di microSD", null);
        } else {
            quickCommand(page, "Lihat folder SonixNDK di microSD", "mkdir -p " + shellQuote(workspace + "/projects") + " " + shellQuote(workspace + "/builds") + " " + shellQuote(workspace + "/assets") + " " + shellQuote(workspace + "/cache") + " && ls -la " + shellQuote(workspace));
        }
        LinearLayout help = panel();
        help.addView(text("HELP TERMINAL SN", 14, TEXT, true));
        help.addView(text("Contoh: pkg install git • python --version • clang --version • git clone URL • cmake --version • ls • cd • pwd. Terminal SN meneruskan perintah ke Termux asli.", 12, MUTED, false));
        Button helpButton = button("Buka panduan Terminal SN", false);
        LinearLayout.LayoutParams helpLp = new LinearLayout.LayoutParams(-1, dp(42)); helpLp.topMargin = dp(8);
        help.addView(helpButton, helpLp);
        helpButton.setOnClickListener(v -> showPage("Help"));
        LinearLayout.LayoutParams helpCardLp = new LinearLayout.LayoutParams(-1, -2); helpCardLp.topMargin = dp(12);
        page.addView(help, helpCardLp);
        content.addView(page);
    }

    private void quickCommand(LinearLayout parent, String label, String command) {
        Button b = button(label, false);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(42));
        lp.topMargin = dp(6);
        parent.addView(b, lp);
        b.setOnClickListener(v -> {
            if (command == null) openTermux();
            else runTermux(command, true);
        });
    }

    private void runTermux(String command, boolean foreground) {
        if (command == null || command.trim().isEmpty()) { toast("Perintah masih kosong."); return; }
        if (!isTermuxInstalled()) {
            new AlertDialog.Builder(this).setTitle("Termux belum terpasang")
                .setMessage("Pasang Termux dari sumber resmi GitHub/F-Droid, buka sekali, lalu coba lagi. Sonix tidak memasang APK lain secara diam-diam.")
                .setPositiveButton("Petunjuk Termux", (d,w) -> openUrl("https://github.com/termux/termux-app"))
                .setNegativeButton("Tutup", null).show();
            return;
        }
        if (checkSelfPermission("com.termux.permission.RUN_COMMAND") != PackageManager.PERMISSION_GRANTED) {
            new AlertDialog.Builder(this).setTitle("Izin Terminal diperlukan")
                .setMessage("Buka Setelan Android → Aplikasi → Sonix AI → Izin tambahan, lalu izinkan 'Run commands in Termux environment'. Di Termux aktifkan allow-external-apps=true. Baca menu Help untuk langkah lengkap.")
                .setPositiveButton("Buka info aplikasi", (d,w) -> {
                    Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
                    startActivity(i);
                }).setNegativeButton("Nanti", null).show();
            return;
        }
        TextView preview = text(command, 12, TEXT, false);
        preview.setTextIsSelectable(true);
        preview.setPadding(dp(12), dp(10), dp(12), dp(10));
        preview.setBackground(bg(PANEL2, 10));
        new AlertDialog.Builder(this)
            .setTitle("Jalankan perintah di Termux?")
            .setMessage("Periksa perintah sebelum dijalankan. Perintah akan berjalan dengan akses Termux milikmu.")
            .setView(preview)
            .setPositiveButton("Jalankan", (d,w) -> executeTermux(command, foreground))
            .setNegativeButton("Batal", null)
            .show();
    }

    private void executeTermux(String command, boolean foreground) {
        try {
            Intent intent = new Intent("com.termux.RUN_COMMAND");
            intent.setComponent(new ComponentName(TERMUX_PACKAGE, "com.termux.app.RunCommandService"));
            intent.putExtra("com.termux.RUN_COMMAND_PATH", "/data/data/com.termux/files/usr/bin/bash");
            intent.putExtra("com.termux.RUN_COMMAND_ARGUMENTS", new String[]{"-lc", command});
            intent.putExtra("com.termux.RUN_COMMAND_WORKDIR", "~/");
            intent.putExtra("com.termux.RUN_COMMAND_BACKGROUND", !foreground);
            intent.putExtra("com.termux.RUN_COMMAND_COMMAND_LABEL", "Sonix Terminal SN");
            intent.putExtra("com.termux.RUN_COMMAND_COMMAND_DESCRIPTION", "Perintah dikirim dari Sonix AI 1.00");
            intent.putExtra("com.termux.RUN_COMMAND_COMMAND_HELP", "Dokumentasi: https://github.com/termux/termux-app/wiki/RUN_COMMAND-Intent");
            startService(intent);
            toast("Perintah dikirim ke Termux. Lihat sesi/notifikasi Termux untuk hasilnya.");
        } catch (SecurityException e) {
            toast("Akses ditolak. Periksa izin RUN_COMMAND dan allow-external-apps=true.");
        } catch (Exception e) {
            toast("Tidak dapat menjalankan Termux: " + e.getMessage());
        }
    }

    private boolean isTermuxInstalled() {
        try { getPackageManager().getPackageInfo(TERMUX_PACKAGE, 0); return true; }
        catch (Exception e) { return false; }
    }

    private void openTermux() {
        try {
            Intent launch = getPackageManager().getLaunchIntentForPackage(TERMUX_PACKAGE);
            if (launch == null) throw new IllegalStateException("Termux tidak ditemukan");
            startActivity(launch);
        } catch (Exception e) { toast("Termux belum terpasang."); }
    }

    private void buildNdk() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout page = pageColumn();
        page.addView(text("NDK Center", 22, TEXT, true));
        page.addView(text("NDK Google r23c + r30 untuk GitHub, dan Clang/ndk-sysroot/ndk-multilib untuk Termux. Keduanya memakai source proyek yang sama tetapi merupakan jalur compiler berbeda.", 13, MUTED, false));
        page.addView(space(12));
        LinearLayout path = panel();
        path.addView(text("WORKSPACE MICROSD EKSTERNAL", 11, ACCENT, true));
        String selectedPath = getWorkspacePath();
        path.addView(text(selectedPath.isEmpty() ? "Belum dipilih — pilih folder SonixNDK pada kartu microSD" : selectedPath, 14, TEXT, true));
        path.addView(text("Proyek: projects/ • Hasil: builds/ • Aset: assets/ • Cache: cache/", 12, MUTED, false));
        page.addView(path);
        Button chooseSd = button("Pilih folder SonixNDK di kartu SD eksternal", true);
        LinearLayout.LayoutParams csp = new LinearLayout.LayoutParams(-1, dp(46)); csp.topMargin = dp(8); page.addView(chooseSd, csp);
        chooseSd.setOnClickListener(v -> chooseExternalSdFolder());
        Button setupStorage = button("1. Setup storage Termux", true);
        page.addView(space(10)); page.addView(setupStorage);
        setupStorage.setOnClickListener(v -> runTermux("termux-setup-storage", true));
        Button fileAccess = button("Izinkan akses folder SonixNDK", false);
        LinearLayout.LayoutParams sap = new LinearLayout.LayoutParams(-1, dp(44)); sap.topMargin = dp(8); page.addView(fileAccess, sap);
        fileAccess.setOnClickListener(v -> openAllFilesSettings());
        Button install = button("2. Install NDK / C++ tools", true);
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(-1, dp(46)); ip.topMargin = dp(8); page.addView(install, ip);
        install.setOnClickListener(v -> {
            String cmd = "pkg update -y && pkg upgrade -y && pkg install -y git gh clang lld make cmake ninja pkg-config python ndk-sysroot ndk-multilib openjdk-17 gradle wget curl unzip zip file && mkdir -p ~/.sonix/{cache,workspaces,builds} && echo 'Termux toolchain siap; build staging privat di ~/.sonix'";
            runTermux(cmd, true);
        });
        Button folder = button("3. Buat folder proyek", false);
        LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(-1, dp(44)); fp.topMargin = dp(8); page.addView(folder, fp);
        folder.setOnClickListener(v -> {
            String path = getWorkspacePath();
            if (path.isEmpty()) { toast("Pilih folder pada kartu SD eksternal terlebih dahulu."); return; }
            runTermux("mkdir -p " + shellQuote(path + "/projects") + " " + shellQuote(path + "/builds") + " " + shellQuote(path + "/assets") + " " + shellQuote(path + "/cache") + " && ls -la " + shellQuote(path), true);
        });
        page.addView(space(14));
        LinearLayout notes = panel();
        notes.addView(text("Tool yang disiapkan", 14, TEXT, true));
        notes.addView(text("Git • Clang • LLD • Make • CMake • Ninja • pkg-config • Python • OpenJDK 17 • Gradle • ndk-sysroot • ndk-multilib", 12, MUTED, false));
        notes.addView(space(7));
        notes.addView(text("Project dan ZIP disimpan di folder yang kamu pilih pada microSD. Build Termux disalin sementara ke ~/.sonix/workspaces agar compiler dapat membuat file sementara di filesystem privat. Google NDK r23c/r30 dipasang pada runner GitHub dan dipakai untuk menghasilkan APK atau plugin .so.", 12, MUTED, false));
        page.addView(notes);
        Button fullNdk = button("Download NDK Google penuh (PC / GitHub)", false);
        LinearLayout.LayoutParams ndkLp = new LinearLayout.LayoutParams(-1, dp(44)); ndkLp.topMargin = dp(10);
        page.addView(fullNdk, ndkLp);
        fullNdk.setOnClickListener(v -> openUrl("https://developer.android.com/ndk/downloads"));
        scroll.addView(page);
        content.addView(scroll);
    }

    private void buildHelp() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout page = pageColumn();
        page.addView(text("Help / Cara penggunaan", 22, TEXT, true));
        helpBlock(page, "A. Mengaktifkan AI", "1. Buka Settings.\n2. Masukkan URL endpoint chat completions yang kompatibel dengan OpenAI.\n3. Isi model Sonix 1.00 atau nama model dari server kamu.\n4. Isi API key bila server memerlukannya.\n5. Simpan, lalu coba chat.\n\nAplikasi ini belum membawa bobot model AI; jawaban berasal dari server endpoint yang kamu hubungkan.");
        helpBlock(page, "B. Menghubungkan Termux asli", "1. Pasang Termux dari sumber resmi GitHub/F-Droid dan buka sekali.\n2. Di Termux jalankan: termux-setup-storage\n3. Di Termux jalankan: mkdir -p ~/.termux && echo allow-external-apps=true >> ~/.termux/termux.properties\n4. Jalankan termux-reload-settings lalu restart Termux.\n5. Android Settings → Apps → Sonix AI → Permissions / Additional permissions → izinkan 'Run commands in Termux environment'.\n6. Buka Terminal SN dan jalankan perintah.\n\nPerintah berjalan dengan hak akses Termux milik pengguna. Periksa perintah sebelum dijalankan.");
        helpBlock(page, "C. Help Terminal SN", "Terminal SN menerima perintah shell Termux. Contoh: pkg update, pkg install git, python --version, clang --version, git clone URL, cmake --version, ls, cd. Tekan tombol Jalankan; baca dialog dan setujui hanya perintah yang kamu pahami. Untuk sesi interaktif, hasil ditampilkan di aplikasi Termux.");
        helpBlock(page, "D. Pasang toolchain dan folder microSD", "1. Buat folder SonixNDK di kartu microSD menggunakan aplikasi File Saya.\n2. Di Sonix → NDK, pilih folder SonixNDK tersebut (pastikan bukan memori internal).\n3. Izinkan akses semua file untuk Sonix AI.\n4. Buka Termux, jalankan termux-setup-storage, lalu izinkan akses file untuk Termux.\n5. Di Sonix → NDK → Install NDK/C++ tools.\n6. Build Termux menyalin proyek dari microSD ke ~/.sonix/workspaces untuk proses compile. Hasil akan dicoba disalin ke microSD/builds; jika Android memblokir tulis, hasil sementara tetap ada di ~/.sonix/builds. Jika sesi Terminal tidak muncul otomatis, izinkan Termux tampil di atas aplikasi lain (Display over other apps) dan buka notifikasi Termux.");
        helpBlock(page, "E. Build plugin AML .so dengan Google NDK", "1. Ekspor proyek dari scripts/export-project.sh atau ZIP-kan folder proyek AML dari microSD.\n2. Upload ZIP ke root repository GitHub.\n3. Buka Actions → Build GTA SA AML Plugin (.so) → Run workflow.\n4. Isi project_dir dengan nama ZIP, misalnya MyAMLPlugin.zip.\n5. Pilih both untuk membangun dengan r23c dan r30.\n6. Unduh artifact Sonix-AML-r23c-armeabi-v7a dan Sonix-AML-r30-armeabi-v7a.\n7. Salin .so ke folder mods game yang sesuai setelah memastikan versi GTA SA dan AML cocok.");
        helpBlock(page, "F. Build proyek AI lain di GitHub", "Untuk APK Android atau mod Minecraft, ekspor/upload ZIP proyek ke root repository, lalu Actions → Build Sonix Generated Project → Run workflow. Pilih android_apk untuk aplikasi Android, gradle untuk Fabric/Forge, auto untuk deteksi Gradle, atau zip_only untuk HTML5/static.");
        helpBlock(page, "G. Melatih model Sonix", "GitHub Actions menjalankan fine-tuning LoRA CPU pada model dasar kecil. Ini bukan melatih model besar dari nol. Tambahkan contoh instruksi/jawaban berkualitas ke training/dataset.jsonl, lalu Actions → Train Sonix LoRA → Run workflow. Batas workflow 4 jam; training bisa selesai lebih awal. Artifact hanya adapter LoRA, bukan model lengkap. Server AI harus memuat model dasar + adapter dan menyediakan endpoint OpenAI-compatible. Runner standar GitHub tidak memiliki GPU.");
        helpBlock(page, "H. Build APK Sonix", "1. Upload isi folder SonixAI-1.00 ke root repository GitHub. Pastikan folder .github ikut terunggah.\n2. Buka Actions → Build Sonix AI APK.\n3. Jalankan workflow dan unduh artifact SonixAI-debug-apk.\n4. Ekstrak ZIP artifact lalu pasang app-debug.apk pada Android. NDK r23c/r30 digunakan di workflow khusus plugin AML; APK Sonix sendiri adalah aplikasi Java biasa.");
        helpBlock(page, "Batasan penting", "• GitHub Actions membangun APK; GitHub tidak otomatis melatih model AI besar. Training memerlukan dataset, kode training, GPU/compute, dan biaya/kuota.\n• Sonix 1.00 saat ini nama/konfigurasi model yang dikirim ke server, bukan model lokal yang sudah dilatih.\n• Tidak semua mod dapat dibuat otomatis sempurna. Versi game, ABI, SDK, loader, dan header/API harus cocok.\n• Terminal SN mengirim perintah ke Termux dan hasil terminal ditampilkan oleh Termux. Project Builder dapat menyimpan file AI ke SonixNDK/projects dan meminta konfirmasi sebelum menjalankan build.");
        Button docs = button("Buka dokumentasi Termux RUN_COMMAND", false);
        docs.setOnClickListener(v -> openUrl("https://github.com/termux/termux-app/wiki/RUN_COMMAND-Intent"));
        page.addView(docs);
        scroll.addView(page);
        content.addView(scroll);
    }

    private void helpBlock(LinearLayout parent, String title, String body) {
        LinearLayout box = panel();
        box.addView(text(title, 14, TEXT, true));
        TextView t = text(body, 12, MUTED, false);
        t.setLineSpacing(dp(3), 1f);
        box.addView(t);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.bottomMargin = dp(10);
        parent.addView(box, lp);
    }

    private void buildSettings() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout page = pageColumn();
        page.addView(text("Settings", 22, TEXT, true));
        page.addView(text("Sambungkan model Sonix ke server AI milikmu.", 13, MUTED, false));
        SharedPreferences prefs = getPrefs();
        page.addView(space(12));
        page.addView(text("Chat Completions URL", 13, TEXT, true));
        endpointInput = new EditText(this);
        endpointInput.setSingleLine(true);
        endpointInput.setHint("https://server-kamu/v1/chat/completions");
        endpointInput.setText(prefs.getString("endpoint", ""));
        endpointInput.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
        styleInput(endpointInput);
        page.addView(endpointInput);
        page.addView(space(10));
        page.addView(text("Nama model", 13, TEXT, true));
        modelInput = new EditText(this);
        modelInput.setSingleLine(true);
        modelInput.setText(prefs.getString("model", "Sonix 1.00"));
        modelInput.setHint("Sonix 1.00");
        styleInput(modelInput);
        page.addView(modelInput);
        page.addView(space(10));
        page.addView(text("API key (disimpan di perangkat)", 13, TEXT, true));
        keyInput = new EditText(this);
        keyInput.setSingleLine(true);
        keyInput.setText(getStoredApiKey());
        keyInput.setHint("sk-... atau kosong bila tidak perlu");
        keyInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        styleInput(keyInput);
        page.addView(keyInput);
        page.addView(space(12));
        Button save = button("Simpan pengaturan", true);
        page.addView(save);
        save.setOnClickListener(v -> {
            try {
                String encrypted = encryptApiKey(keyInput.getText().toString().trim());
                prefs.edit().putString("endpoint", endpointInput.getText().toString().trim())
                    .putString("model", modelInput.getText().toString().trim().isEmpty() ? "Sonix 1.00" : modelInput.getText().toString().trim())
                    .putString("api_key_enc", encrypted).remove("api_key").apply();
                toast("Pengaturan disimpan. API key dienkripsi di perangkat.");
            } catch (Exception e) { toast("Tidak dapat mengamankan API key: " + e.getMessage()); }
        });
        page.addView(space(12));
        helpBlock(page, "Format server", "Endpoint harus menerima POST JSON dengan model + messages seperti API /v1/chat/completions. Server dapat berupa server milikmu sendiri atau penyedia yang kompatibel. Jangan bagikan API key kepada orang lain.");
        scroll.addView(page);
        content.addView(scroll);
    }

    private String encryptApiKey(String plain) throws Exception {
        if (plain == null || plain.isEmpty()) return "";
        final String alias = "SonixApiKeyAES";
        KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
        ks.load(null);
        if (!ks.containsAlias(alias)) {
            KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            generator.init(new KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true).build());
            generator.generateKey();
        }
        SecretKey key = (SecretKey) ks.getKey(alias, null);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encrypted = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
        byte[] iv = cipher.getIV();
        byte[] packed = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, packed, 0, iv.length);
        System.arraycopy(encrypted, 0, packed, iv.length, encrypted.length);
        return Base64.encodeToString(packed, Base64.NO_WRAP);
    }

    private String getStoredApiKey() {
        String packedText = getPrefs().getString("api_key_enc", "");
        if (packedText.isEmpty()) return "";
        try {
            byte[] packed = Base64.decode(packedText, Base64.NO_WRAP);
            if (packed.length <= 12) return "";
            byte[] iv = new byte[12];
            byte[] cipherText = new byte[packed.length - 12];
            System.arraycopy(packed, 0, iv, 0, 12);
            System.arraycopy(packed, 12, cipherText, 0, cipherText.length);
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            SecretKey key = (SecretKey) ks.getKey("SonixApiKeyAES", null);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));
            return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
        } catch (Exception e) { return ""; }
    }

    private SharedPreferences getPrefs() { return getSharedPreferences(PREFS, MODE_PRIVATE); }
    private LinearLayout pageColumn() { LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); l.setPadding(0, dp(2), 0, dp(8)); return l; }
    private LinearLayout panel() { LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); l.setPadding(dp(14), dp(14), dp(14), dp(14)); l.setBackground(bg(PANEL, 16)); return l; }
    private TextView text(String s, int size, int color, boolean bold) { TextView t = new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color); if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD); return t; }
    private View space(int h) { View v = new View(this); v.setLayoutParams(new LinearLayout.LayoutParams(1, dp(h))); return v; }
    private Button button(String label, boolean primary) { Button b = new Button(this); b.setText(label); b.setAllCaps(false); b.setTextSize(13); b.setTextColor(TEXT); b.setTypeface(Typeface.DEFAULT, Typeface.BOLD); b.setBackground(bg(primary ? ACCENT : PANEL2, 13)); b.setPadding(dp(12), 0, dp(12), 0); return b; }
    private void styleInput(EditText e) { e.setTextColor(TEXT); e.setHintTextColor(MUTED); e.setTextSize(14); e.setPadding(dp(12), dp(10), dp(12), dp(10)); e.setBackground(bg(PANEL2, 12)); e.setSelection(e.getText().length()); }
    private GradientDrawable bg(int color, int radius) { GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); return d; }
    private int dp(float n) { return (int)(n * getResources().getDisplayMetrics().density + 0.5f); }
    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_LONG).show(); }
    private void openUrl(String url) { try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); } catch (Exception e) { toast("Tidak bisa membuka tautan."); } }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_EXTERNAL_SDK && resultCode == RESULT_OK && data != null && data.getData() != null) {
            handleExternalSdSelection(data.getData());
        }
    }

    @Override protected void onDestroy() { super.onDestroy(); executor.shutdownNow(); }
}
