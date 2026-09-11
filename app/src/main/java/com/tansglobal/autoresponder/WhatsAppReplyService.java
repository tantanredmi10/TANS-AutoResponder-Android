package com.tansglobal.autoresponder;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
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
        if (title.equals(lastConversation) && now - lastReplyAt < 60_000L) return;

        String reply = choose(message, p);
        if (sendReply(n, reply)) {
            lastConversation = title;
            lastReplyAt = now;
        }
    }

    private String choose(String m, SharedPreferences p) {
        String s = m.toLowerCase(Locale.ROOT);
        if (has(s,"kontraktor","proyek","pekerjaan","spk","tender")) return p.getString("contractor", Defaults.CONTRACTOR);
        if (has(s,"supplier","material","barang","semen","besi","baja","pasir","beton")) return p.getString("supplier", Defaults.SUPPLIER);
        if (has(s,"survey","survei","lokasi","site visit")) return p.getString("survey", Defaults.SURVEY);
        if (has(s,"penawaran","rab","harga","quotation","boq","estimasi")) return p.getString("quotation", Defaults.QUOTATION);
        if (has(s,"halo","hallo","hai","assalamualaikum","selamat pagi","selamat siang","selamat sore","selamat malam")) return p.getString("greeting", Defaults.GREETING);
        return p.getString("fallback", Defaults.FALLBACK);
    }
    private boolean has(String s, String... ks){ for(String k:ks) if(s.contains(k)) return true; return false; }

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
            try { a.actionIntent.send(this, 0, intent); return true; }
            catch (PendingIntent.CanceledException ignored) { }
        }
        return false;
    }
}
