package com.tansglobal.autoresponder;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class WhatsAppReplyService extends NotificationListenerService {
    private long lastReplyAt = 0L;
    private String lastConversation = "";

    @Override public void onNotificationPosted(StatusBarNotification sbn) {
        String pkg = sbn.getPackageName();
        if (!"com.whatsapp".equals(pkg) && !"com.whatsapp.w4b".equals(pkg)) return;

        SharedPreferences p = getSharedPreferences("tans", MODE_PRIVATE);
        if (!p.getBoolean("enabled", false)) return;
        if (p.getBoolean("businessOnly", true) && !"com.whatsapp.w4b".equals(pkg)) return;

        Notification n = sbn.getNotification();
        Bundle ex = n.extras;
        String title = String.valueOf(ex.getCharSequence(Notification.EXTRA_TITLE, ""));
        String message = String.valueOf(ex.getCharSequence(Notification.EXTRA_TEXT, ""));
        if (message.trim().isEmpty()) return;

        long now = System.currentTimeMillis();
        int cooldownMinutes = Math.max(1, p.getInt("cooldownMinutes", 3));
        long cooldownMs = cooldownMinutes * 60_000L;
        if (title.equals(lastConversation) && now - lastReplyAt < cooldownMs) return;

        Choice choice = choose(message, p);
        if (p.getBoolean("businessHoursOnly", false) && !isBusinessHour(p)) {
            choice = new Choice("DI LUAR JAM", p.getString("outHours", Defaults.OUT_OF_HOURS));
        }

        if (sendReply(n, choice.text)) {
            lastConversation = title;
            lastReplyAt = now;
            appendLog(p, title, choice.category, message);
        }
    }

    private Choice choose(String m, SharedPreferences p) {
        String s = m.toLowerCase(Locale.ROOT);
        if (has(s,"tender","spk","pengadaan","lelang","pagu")) return new Choice("TENDER/SPK", p.getString("tender", Defaults.TENDER));
        if (has(s,"kontraktor","proyek","pekerjaan","borongan","konstruksi")) return new Choice("KONTRAKTOR", p.getString("contractor", Defaults.CONTRACTOR));
        if (has(s,"supplier","material","barang","semen","besi","baja","pasir","beton","pengiriman")) return new Choice("SUPPLIER", p.getString("supplier", Defaults.SUPPLIER));
        if (has(s,"survey","survei","lokasi","site visit","kunjungan")) return new Choice("SURVEY", p.getString("survey", Defaults.SURVEY));
        if (has(s,"penawaran","rab","harga","quotation","boq","estimasi","proposal")) return new Choice("PENAWARAN", p.getString("quotation", Defaults.QUOTATION));
        if (has(s,"profil","profile","tentang perusahaan","perusahaan apa","bidang usaha","layanan")) return new Choice("PROFIL", p.getString("profile", Defaults.PROFILE));
        if (has(s,"halo","hallo","hai","assalamualaikum","selamat pagi","selamat siang","selamat sore","selamat malam")) return new Choice("SAPaan", p.getString("greeting", Defaults.GREETING));
        return new Choice("UMUM", p.getString("fallback", Defaults.FALLBACK));
    }

    private boolean isBusinessHour(SharedPreferences p) {
        int start = p.getInt("startHour", 8);
        int end = p.getInt("endHour", 17);
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (start == end) return true;
        if (start < end) return hour >= start && hour < end;
        return hour >= start || hour < end;
    }

    private boolean has(String s, String... ks){
        for(String k:ks) if(s.contains(k)) return true;
        return false;
    }

    private boolean sendReply(Notification n, String text) {
        Notification.Action[] actions = n.actions;
        if (actions == null) return false;
        for (Notification.Action a : actions) {
            RemoteInput[] inputs = a.getRemoteInputs();
            if (inputs == null || inputs.length == 0) continue;
            Intent intent = new Intent();
            Bundle results = new Bundle();
            for (RemoteInput ri : inputs) results.putCharSequence(ri.getResultKey(), text);
            RemoteInput.addResultsToIntent(inputs, intent, results);
            try {
                a.actionIntent.send(this, 0, intent);
                return true;
            } catch (PendingIntent.CanceledException ignored) { }
        }
        return false;
    }

    private void appendLog(SharedPreferences p, String title, String category, String message) {
        String clean = message.replace('\n',' ').replace('\r',' ').trim();
        if (clean.length() > 60) clean = clean.substring(0, 60) + "…";
        String time = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(new Date());
        String entry = time + " | " + category + " | " + (title.trim().isEmpty() ? "WhatsApp" : title) + "\n" + clean;
        String old = p.getString("logs", "");
        String merged = entry + (old.trim().isEmpty() ? "" : "\n\n" + old);
        String[] blocks = merged.split("\\n\\n");
        StringBuilder out = new StringBuilder();
        for (int i=0; i<blocks.length && i<15; i++) {
            if (i>0) out.append("\n\n");
            out.append(blocks[i]);
        }
        p.edit().putString("logs", out.toString()).apply();
    }

    private static class Choice {
        final String category;
        final String text;
        Choice(String category, String text){ this.category=category; this.text=text; }
    }
}
