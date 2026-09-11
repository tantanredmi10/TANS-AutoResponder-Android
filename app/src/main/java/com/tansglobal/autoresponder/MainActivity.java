package com.tansglobal.autoresponder;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
    private EditText phone, greeting, contractor, supplier, survey, quotation, fallback;
    private Switch enabled;
    private CheckBox businessOnly;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences("tans", MODE_PRIVATE);
        if (android.os.Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 10);
        }
        setContentView(buildUi());
        load();
    }

    private View buildUi() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(18), dp(18), dp(28));
        root.setBackgroundColor(Color.rgb(244,246,248));
        scroll.addView(root);

        TextView head = new TextView(this);
        head.setText("PT. TANS GLOBAL PERSADA\nAUTO RESPON WHATSAPP");
        head.setTextColor(Color.WHITE); head.setTextSize(22); head.setGravity(Gravity.CENTER);
        head.setPadding(dp(14), dp(18), dp(14), dp(18)); head.setBackgroundColor(Color.rgb(11,31,58));
        root.addView(head, new LinearLayout.LayoutParams(-1,-2));

        TextView sub = text("Kontraktor • Supplier • General Trading", 15, Color.rgb(242,140,40));
        sub.setGravity(Gravity.CENTER); sub.setPadding(0,dp(10),0,dp(10)); root.addView(sub);

        enabled = new Switch(this); enabled.setText("Aktifkan Auto Respon"); enabled.setTextSize(17); root.addView(enabled);
        businessOnly = new CheckBox(this); businessOnly.setText("Prioritaskan WhatsApp Business"); businessOnly.setChecked(true); root.addView(businessOnly);

        Button access = button("IZINKAN NOTIFICATION ACCESS");
        access.setOnClickListener(v -> startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")));
        root.addView(access);

        phone = field(root, "Nomor bisnis", "08587183014");
        greeting = field(root, "Sapaan umum", Defaults.GREETING);
        contractor = field(root, "Jawaban keyword KONTRAKTOR / PROYEK", Defaults.CONTRACTOR);
        supplier = field(root, "Jawaban keyword SUPPLIER / MATERIAL", Defaults.SUPPLIER);
        survey = field(root, "Jawaban keyword SURVEY / LOKASI", Defaults.SURVEY);
        quotation = field(root, "Jawaban keyword PENAWARAN / RAB / HARGA", Defaults.QUOTATION);
        fallback = field(root, "Jawaban umum lainnya", Defaults.FALLBACK);

        Button save = button("SIMPAN PENGATURAN");
        save.setOnClickListener(v -> save()); root.addView(save);
        Button test = button("LIHAT CONTOH RESPON");
        test.setOnClickListener(v -> new AlertDialog.Builder(this).setTitle("Contoh Auto Respon").setMessage(quotation.getText().toString()).setPositiveButton("OK",null).show()); root.addView(test);

        TextView note = text("Cara pakai: aktifkan aplikasi, tekan Notification Access, izinkan TANS Auto Respon, lalu biarkan WhatsApp/WhatsApp Business menampilkan notifikasi. Aplikasi membalas melalui tombol Reply pada notifikasi Android.", 13, Color.DKGRAY);
        note.setPadding(0,dp(14),0,0); root.addView(note);
        return scroll;
    }

    private EditText field(LinearLayout root, String label, String def) {
        TextView tv = text(label,14,Color.rgb(23,33,43)); tv.setPadding(0,dp(12),0,dp(4)); root.addView(tv);
        EditText e = new EditText(this); e.setText(def); e.setMinLines(2); e.setGravity(Gravity.TOP); e.setBackgroundColor(Color.WHITE); e.setPadding(dp(10),dp(10),dp(10),dp(10)); root.addView(e,new LinearLayout.LayoutParams(-1,-2)); return e;
    }
    private TextView text(String s,float z,int c){ TextView t=new TextView(this); t.setText(s); t.setTextSize(z); t.setTextColor(c); return t; }
    private Button button(String s){ Button b=new Button(this); b.setText(s); b.setTextColor(Color.WHITE); b.setBackgroundColor(Color.rgb(11,31,58)); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.setMargins(0,dp(10),0,0); b.setLayoutParams(lp); return b; }
    private int dp(int n){ return (int)(n*getResources().getDisplayMetrics().density); }

    private void load(){
        enabled.setChecked(prefs.getBoolean("enabled",false)); businessOnly.setChecked(prefs.getBoolean("businessOnly",true));
        phone.setText(prefs.getString("phone","08587183014")); greeting.setText(prefs.getString("greeting",Defaults.GREETING)); contractor.setText(prefs.getString("contractor",Defaults.CONTRACTOR)); supplier.setText(prefs.getString("supplier",Defaults.SUPPLIER)); survey.setText(prefs.getString("survey",Defaults.SURVEY)); quotation.setText(prefs.getString("quotation",Defaults.QUOTATION)); fallback.setText(prefs.getString("fallback",Defaults.FALLBACK));
    }
    private void save(){
        prefs.edit().putBoolean("enabled",enabled.isChecked()).putBoolean("businessOnly",businessOnly.isChecked()).putString("phone",phone.getText().toString().trim()).putString("greeting",greeting.getText().toString()).putString("contractor",contractor.getText().toString()).putString("supplier",supplier.getText().toString()).putString("survey",survey.getText().toString()).putString("quotation",quotation.getText().toString()).putString("fallback",fallback.getText().toString()).apply();
        Toast.makeText(this,"Pengaturan tersimpan",Toast.LENGTH_SHORT).show();
    }
}
