package com.tansglobal.autoresponder;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private SharedPreferences prefs;
    private EditText phone, greeting, contractor, supplier, survey, quotation, profile, tender, outHours, fallback;
    private EditText startHour, endHour, cooldown;
    private Switch enabled;
    private CheckBox businessOnly, businessHoursOnly;
    private TextView statusText, logText;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences("tans", MODE_PRIVATE);
        if (android.os.Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 10);
        }
        setContentView(buildUi());
        load();
    }

    @Override protected void onResume() {
        super.onResume();
        if (statusText != null) refreshStatus();
        if (logText != null) refreshLogs();
    }

    private View buildUi() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(16), dp(16), dp(28));
        root.setBackgroundColor(Color.rgb(244,246,248));
        scroll.addView(root);

        TextView head = new TextView(this);
        head.setText("PT. TANS GLOBAL PERSADA\nAUTO RESPON BUSINESS");
        head.setTextColor(Color.WHITE);
        head.setTextSize(22);
        head.setGravity(Gravity.CENTER);
        head.setPadding(dp(14), dp(18), dp(14), dp(18));
        head.setBackgroundColor(Color.rgb(11,31,58));
        root.addView(head, new LinearLayout.LayoutParams(-1,-2));

        TextView sub = text("Kontraktor • Supplier • General Trading", 15, Color.rgb(242,140,40));
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0,dp(10),0,dp(10));
        root.addView(sub);

        statusText = text("", 14, Color.rgb(11,31,58));
        statusText.setPadding(dp(12),dp(10),dp(12),dp(10));
        statusText.setBackgroundColor(Color.WHITE);
        root.addView(statusText);

        enabled = new Switch(this);
        enabled.setText("Aktifkan Auto Respon");
        enabled.setTextSize(17);
        enabled.setOnCheckedChangeListener((b, checked) -> refreshStatus());
        root.addView(enabled);

        businessOnly = new CheckBox(this);
        businessOnly.setText("Prioritaskan WhatsApp Business saja");
        businessOnly.setChecked(true);
        root.addView(businessOnly);

        businessHoursOnly = new CheckBox(this);
        businessHoursOnly.setText("Gunakan jam operasional");
        root.addView(businessHoursOnly);

        Button access = button("IZINKAN NOTIFICATION ACCESS");
        access.setOnClickListener(v -> startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")));
        root.addView(access);

        phone = field(root, "Nomor bisnis", "08587183014");
        startHour = oneLineField(root, "Jam mulai operasional (0-23)", "8");
        endHour = oneLineField(root, "Jam selesai operasional (0-23)", "17");
        cooldown = oneLineField(root, "Anti-spam / jeda balasan per chat (menit)", "3");

        section(root, "TEMPLATE AUTO RESPON");
        greeting = field(root, "Sapaan umum", Defaults.GREETING);
        contractor = field(root, "KONTRAKTOR / PROYEK", Defaults.CONTRACTOR);
        supplier = field(root, "SUPPLIER / MATERIAL", Defaults.SUPPLIER);
        survey = field(root, "SURVEY / LOKASI", Defaults.SURVEY);
        quotation = field(root, "PENAWARAN / RAB / HARGA", Defaults.QUOTATION);
        profile = field(root, "PROFIL / TENTANG PERUSAHAAN", Defaults.PROFILE);
        tender = field(root, "TENDER / SPK / PENGADAAN", Defaults.TENDER);
        outHours = field(root, "Di luar jam operasional", Defaults.OUT_OF_HOURS);
        fallback = field(root, "Jawaban umum lainnya", Defaults.FALLBACK);

        Button save = orangeButton("SIMPAN SEMUA PENGATURAN");
        save.setOnClickListener(v -> save());
        root.addView(save);

        Button test = button("LIHAT CONTOH RESPON PENAWARAN");
        test.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Contoh Auto Respon")
                .setMessage(quotation.getText().toString())
                .setPositiveButton("OK",null).show());
        root.addView(test);

        Button wa = button("BUKA WHATSAPP BISNIS");
        wa.setOnClickListener(v -> openWhatsApp());
        root.addView(wa);

        Button clear = button("HAPUS RIWAYAT RESPON");
        clear.setOnClickListener(v -> {
            prefs.edit().remove("logs").apply();
            refreshLogs();
            Toast.makeText(this,"Riwayat dihapus",Toast.LENGTH_SHORT).show();
        });
        root.addView(clear);

        section(root, "RIWAYAT AUTO RESPON TERAKHIR");
        logText = text("Belum ada riwayat.", 13, Color.DKGRAY);
        logText.setPadding(dp(10),dp(10),dp(10),dp(10));
        logText.setBackgroundColor(Color.WHITE);
        root.addView(logText);

        TextView note = text("Cara pakai: aktifkan Auto Respon, tekan Notification Access, izinkan TANS Auto Respon, lalu pastikan notifikasi WhatsApp/WhatsApp Business aktif. Aplikasi membalas melalui tombol Reply pada notifikasi Android. Untuk penggunaan bisnis yang stabil, jangan menonaktifkan notifikasi WhatsApp.", 13, Color.DKGRAY);
        note.setPadding(0,dp(14),0,0);
        root.addView(note);
        return scroll;
    }

    private void section(LinearLayout root, String label) {
        TextView tv = text(label, 15, Color.WHITE);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(dp(10),dp(9),dp(10),dp(9));
        tv.setBackgroundColor(Color.rgb(11,31,58));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1,-2);
        lp.setMargins(0,dp(16),0,dp(4));
        root.addView(tv, lp);
    }

    private EditText field(LinearLayout root, String label, String def) {
        TextView tv = text(label,14,Color.rgb(23,33,43));
        tv.setPadding(0,dp(10),0,dp(4));
        root.addView(tv);
        EditText e = new EditText(this);
        e.setText(def);
        e.setMinLines(2);
        e.setGravity(Gravity.TOP);
        e.setBackgroundColor(Color.WHITE);
        e.setPadding(dp(10),dp(10),dp(10),dp(10));
        root.addView(e,new LinearLayout.LayoutParams(-1,-2));
        return e;
    }

    private EditText oneLineField(LinearLayout root, String label, String def) {
        TextView tv = text(label,14,Color.rgb(23,33,43));
        tv.setPadding(0,dp(10),0,dp(4));
        root.addView(tv);
        EditText e = new EditText(this);
        e.setText(def);
        e.setSingleLine(true);
        e.setBackgroundColor(Color.WHITE);
        e.setPadding(dp(10),dp(8),dp(10),dp(8));
        root.addView(e,new LinearLayout.LayoutParams(-1,-2));
        return e;
    }

    private TextView text(String s,float z,int c){
        TextView t=new TextView(this); t.setText(s); t.setTextSize(z); t.setTextColor(c); return t;
    }

    private Button button(String s){
        Button b=new Button(this);
        b.setText(s);
        b.setTextColor(Color.WHITE);
        b.setBackgroundColor(Color.rgb(11,31,58));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
        lp.setMargins(0,dp(10),0,0);
        b.setLayoutParams(lp);
        return b;
    }

    private Button orangeButton(String s){
        Button b=button(s);
        b.setBackgroundColor(Color.rgb(242,140,40));
        return b;
    }

    private int dp(int n){ return (int)(n*getResources().getDisplayMetrics().density); }

    private void load(){
        enabled.setChecked(prefs.getBoolean("enabled",false));
        businessOnly.setChecked(prefs.getBoolean("businessOnly",true));
        businessHoursOnly.setChecked(prefs.getBoolean("businessHoursOnly",false));
        phone.setText(prefs.getString("phone","08587183014"));
        startHour.setText(String.valueOf(prefs.getInt("startHour",8)));
        endHour.setText(String.valueOf(prefs.getInt("endHour",17)));
        cooldown.setText(String.valueOf(prefs.getInt("cooldownMinutes",3)));
        greeting.setText(prefs.getString("greeting",Defaults.GREETING));
        contractor.setText(prefs.getString("contractor",Defaults.CONTRACTOR));
        supplier.setText(prefs.getString("supplier",Defaults.SUPPLIER));
        survey.setText(prefs.getString("survey",Defaults.SURVEY));
        quotation.setText(prefs.getString("quotation",Defaults.QUOTATION));
        profile.setText(prefs.getString("profile",Defaults.PROFILE));
        tender.setText(prefs.getString("tender",Defaults.TENDER));
        outHours.setText(prefs.getString("outHours",Defaults.OUT_OF_HOURS));
        fallback.setText(prefs.getString("fallback",Defaults.FALLBACK));
        refreshStatus();
        refreshLogs();
    }

    private void save(){
        int start = parseHour(startHour.getText().toString(),8);
        int end = parseHour(endHour.getText().toString(),17);
        int cool = parseRange(cooldown.getText().toString(),3,1,120);
        prefs.edit()
                .putBoolean("enabled",enabled.isChecked())
                .putBoolean("businessOnly",businessOnly.isChecked())
                .putBoolean("businessHoursOnly",businessHoursOnly.isChecked())
                .putString("phone",phone.getText().toString().trim())
                .putInt("startHour",start)
                .putInt("endHour",end)
                .putInt("cooldownMinutes",cool)
                .putString("greeting",greeting.getText().toString())
                .putString("contractor",contractor.getText().toString())
                .putString("supplier",supplier.getText().toString())
                .putString("survey",survey.getText().toString())
                .putString("quotation",quotation.getText().toString())
                .putString("profile",profile.getText().toString())
                .putString("tender",tender.getText().toString())
                .putString("outHours",outHours.getText().toString())
                .putString("fallback",fallback.getText().toString())
                .apply();
        startHour.setText(String.valueOf(start));
        endHour.setText(String.valueOf(end));
        cooldown.setText(String.valueOf(cool));
        refreshStatus();
        Toast.makeText(this,"Pengaturan tersimpan",Toast.LENGTH_SHORT).show();
    }

    private int parseHour(String s, int def){ return parseRange(s,def,0,23); }
    private int parseRange(String s,int def,int min,int max){
        try { int v=Integer.parseInt(s.trim()); return Math.max(min,Math.min(max,v)); }
        catch(Exception e){ return def; }
    }

    private void refreshStatus(){
        boolean access = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners") != null &&
                Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners").contains(getPackageName());
        String a = enabled != null && enabled.isChecked() ? "AKTIF" : "NONAKTIF";
        String n = access ? "DIIZINKAN" : "BELUM DIIZINKAN";
        statusText.setText("Status Auto Respon: " + a + "\nNotification Access: " + n);
    }

    private void refreshLogs(){
        if (logText == null) return;
        String logs = prefs.getString("logs","");
        logText.setText(logs.trim().isEmpty() ? "Belum ada riwayat." : logs);
    }

    private void openWhatsApp(){
        String p = phone.getText().toString().replaceAll("[^0-9]","");
        if (p.startsWith("0")) p = "62" + p.substring(1);
        try {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/" + p));
            startActivity(i);
        } catch (Exception e) {
            Toast.makeText(this,"WhatsApp tidak dapat dibuka",Toast.LENGTH_SHORT).show();
        }
    }
}
