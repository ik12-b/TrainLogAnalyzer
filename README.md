# TrainLog Analyzer

Aplikasi **Native Android** untuk mencatat dan menganalisis training run model AI (Language Model / Continued Pretraining).

## Fitur

- Daftar semua training run
- Form checklist lengkap sesuai praktik researcher AI profesional
- Detail analisis + diagnosis + keputusan
- **Export ke PDF** (share ke Drive, WhatsApp, email, dll)
- Edit & hapus run
- Penyimpanan lokal dengan Room Database
- Material 3 + Dark Mode support

## Cara Membuka di Android Studio

1. Buka **Android Studio** (Hedgehog / Iguana / lebih baru)
2. Pilih **Open** → arahkan ke folder `TrainLogAnalyzer`
3. Tunggu Gradle Sync selesai (wrapper akan di-generate otomatis jika belum ada)
4. Jalankan di emulator atau device (**min SDK 26**)

## Export PDF

Di halaman **Detail** run, tekan ikon **Share** di pojok kanan atas.  
PDF akan digenerate lalu muncul dialog share (Google Drive, WhatsApp, Email, Files, dll).

## Build dengan GitHub Actions

Workflow sudah tersedia di `.github/workflows/android.yml`.

### Cara pakai

1. Buat repository baru di GitHub
2. Push seluruh isi folder `TrainLogAnalyzer` ke repo tersebut
3. GitHub Actions akan otomatis:
   - Build debug APK setiap push ke `main` / `master`
   - Upload artifact **TrainLog-debug-apk**
4. Download APK dari tab **Actions** → pilih run → Artifacts

Atau trigger manual: **Actions** → **Android CI** → **Run workflow**.

### Requirements CI

- JDK 17
- Android SDK (disediakan oleh `android-actions/setup-android`)
- Gradle 8.x

## Struktur Utama

```
app/src/main/java/com/trainlog/analyzer/
├── data/
│   ├── model/TrainingRun.kt
│   ├── dao/TrainingRunDao.kt
│   └── db/AppDatabase.kt
├── ui/
│   ├── home/HomeScreen.kt
│   ├── form/FormScreen.kt
│   ├── detail/DetailScreen.kt
│   ├── navigation/Screen.kt
│   └── theme/Theme.kt
├── util/
│   └── PdfExporter.kt
├── viewmodel/TrainingViewModel.kt
└── MainActivity.kt
```

## Catatan

- Icon launcher memakai default sistem (bisa diganti nanti di `res/mipmap`).
- PDF memakai `android.graphics.pdf.PdfDocument` (tanpa library tambahan).
- FileProvider dipakai untuk share PDF secara aman.

## Lisensi

Bebas dipakai untuk keperluan pribadi / riset.
