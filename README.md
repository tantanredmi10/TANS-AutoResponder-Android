# TANS Auto Respon Android

Aplikasi Android untuk membantu auto-respon notifikasi WhatsApp/WhatsApp Business PT. TANS GLOBAL PERSADA.

## Fitur
- Tema biru tua & orange.
- Nomor bisnis default: 08587183014.
- Mode ON/OFF.
- Notification Access.
- Dukungan WhatsApp (`com.whatsapp`) dan WhatsApp Business (`com.whatsapp.w4b`).
- Opsi prioritas hanya WhatsApp Business.
- Respon keyword: kontraktor/proyek, supplier/material, survey/lokasi, penawaran/RAB/harga, sapaan, fallback.
- Anti-spam sederhana: maksimal satu balasan otomatis per percakapan per 60 detik.
- Semua template dapat diedit langsung dari aplikasi.

## Cara kerja
Android NotificationListenerService membaca notifikasi WhatsApp. Bila notifikasi menyediakan aksi **Reply**, aplikasi mengirim balasan melalui RemoteInput Android. Karena ini bergantung pada format notifikasi WhatsApp, fitur perlu diuji pada perangkat pengguna setelah update WhatsApp/Android.

## Build di Android Studio
1. Buka folder proyek.
2. Tunggu Gradle sync.
3. Build > Build APK(s).
4. APK debug: `app/build/outputs/apk/debug/app-debug.apk`.

## Build via GitHub Actions
Workflow `.github/workflows/build-apk.yml` sudah tersedia. Upload repository ke GitHub lalu jalankan workflow **Build Android APK**. Artifact bernama `TANS-AutoResponder-APK`.

## Aktivasi di HP
1. Install APK.
2. Buka aplikasi.
3. Aktifkan **Auto Respon** dan simpan.
4. Tekan **Izinkan Notification Access**.
5. Aktifkan akses untuk **TANS Auto Respon**.
6. Pastikan notifikasi WhatsApp Business aktif.
