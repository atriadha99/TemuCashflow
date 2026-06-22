# 💰 TemuCashflow

TemuCashflow adalah aplikasi manajemen keuangan pribadi berbasis Android yang membantu pengguna mencatat pemasukan, pengeluaran, target tabungan, serta memantau kondisi keuangan melalui statistik dan insight keuangan yang informatif.

Aplikasi ini dikembangkan sebagai Project Akhir Mata Kuliah Mobile Programming menggunakan Android Studio, Java, dan SQLite.

---

## 📱 Fitur Utama

### 🔐 Authentication
* Login
* Register
* Remember Me
* Session Management

### 💵 Manajemen Transaksi
* Tambah Transaksi
* Edit Transaksi
* Hapus Transaksi
* Detail Transaksi
* Pencarian Transaksi
* Filter Kategori
* Filter Tanggal

### 🎯 Target Tabungan
* Membuat Target Tabungan
* Monitoring Progress
* Persentase Pencapaian
* Deadline Target

### 📊 Statistik Keuangan
* Total Pemasukan
* Total Pengeluaran
* Saldo Saat Ini
* Grafik Pengeluaran
* Grafik Pemasukan
* Financial Health Score

### 💡 Financial Insight
* Analisis Pengeluaran
* Prediksi Target Tabungan
* Ringkasan Keuangan Bulanan
* Kategori Pengeluaran Terbesar

### 🔔 Reminder & Notification
* Reminder Tagihan
* Reminder Catat Transaksi
* Target Tabungan
* Pengingat Keuangan Harian

### 📚 Financial Education
* Artikel Edukasi Keuangan
* Tips Menabung
* Pengelolaan Cashflow
* WebView Learning Module

### 📷 Bukti Transaksi
* Ambil Foto dari Kamera
* Pilih dari Galeri
* Lampiran Bukti Transaksi

### 🔥 Gamification
* Financial Streak
* Achievement Badge
* Aktivitas Harian

### 📤 Export Data
* Export CSV
* Export PDF
* Laporan Keuangan

### ♿ Accessibility
* Text To Speech
* Voice Input
* Dynamic Font Size
* High Contrast Mode
* Vibration Feedback
* Accessibility Friendly Design

### 🌙 User Experience
* Dark Mode
* Edge-to-Edge UI
* Material Design 3
* Responsive Layout

---

## 🛠️ Teknologi yang Digunakan
* Android Studio
* Java
* SQLite
* RecyclerView
* Fragment
* Bottom Navigation
* SharedPreferences
* AlarmManager
* BroadcastReceiver
* NotificationManager
* MPAndroidChart
* Camera Intent
* Gallery Intent
* WebView
* Speech Recognition
* Text To Speech
* Material Design 3

---

## 🗄️ Database
Database: `temucashflow.db`

Tabel:
* `users`
* `transactions`
* `savings_goal`
* `reminders`

---

## 📂 Struktur Project
```text
com.andika.temucashflow
│
├── adapter     - Adapter untuk RecyclerView (Transaksi & Goals)
├── data        - DatabaseHelper dan SharedPrefManager
├── model       - POJO classes (Transaction, SavingsGoal, etc.)
├── ui          - UI Components (Activities & Fragments)
│   ├── dashboard
│   ├── login
│   ├── transaction
│   ├── analytics
│   └── education
└── utils       - Helper classes (DateUtils, CurrencyFormatter, etc.)
```

---

## 📸 Screenshot

### Dashboard
Menampilkan ringkasan keuangan pengguna.

### Transaction Management
CRUD transaksi pemasukan dan pengeluaran.

### Statistics
Visualisasi data menggunakan MPAndroidChart.

### Savings Goal
Monitoring target tabungan.

### Accessibility
Fitur aksesibilitas untuk semua pengguna.

---

## 🚀 Cara Menjalankan
1. Clone repository
```bash
git clone https://github.com/username/temucashflow.git
```
2. Buka menggunakan Android Studio
3. Sync Gradle
4. Jalankan aplikasi pada emulator atau perangkat Android

---

## 🎓 Project Information
**Nama Aplikasi:** TemuCashflow  
**Jenis:** Android Mobile Application  
**Kategori:** Personal Finance Management  
**Platform:** Android  
**Bahasa Pemrograman:** Java  
**Database:** SQLite  

---

## Financial Health Score

TemuCashflow menyediakan Financial Health Score yang membantu pengguna memahami kondisi keuangan mereka berdasarkan rasio pemasukan, pengeluaran, tabungan, dan pola transaksi. Sistem akan memberikan skor, status kesehatan keuangan, serta rekomendasi yang dapat membantu pengguna mengambil keputusan finansial yang lebih baik.

---

## Financial Insight

Financial Insight merupakan fitur analisis otomatis yang menampilkan ringkasan kondisi keuangan pengguna, kategori pengeluaran terbesar, tingkat pengeluaran terhadap pemasukan, serta rekomendasi penghematan yang dihasilkan berdasarkan data transaksi yang tersimpan.

---

## Accessibility Support

TemuCashflow mendukung berbagai fitur aksesibilitas seperti Dynamic Font Size, Text To Speech, Speech Recognition, High Contrast Mode, dan Vibration Feedback untuk meningkatkan kenyamanan penggunaan bagi seluruh pengguna. Fitur Voice Input memungkinkan pengguna mencatat transaksi hanya dengan suara, sementara Read Aloud membacakan insight dan kesehatan keuangan secara otomatis.

---

## Security Features

Aplikasi dilengkapi dengan Biometric Authentication menggunakan Fingerprint dan Face Unlock yang didukung perangkat untuk meningkatkan keamanan akses terhadap data keuangan pengguna.

---

## 👨‍💻 Developer
**Andika Agung Triadha**  
**NIM:** 231011400165  
**Program Studi:** Teknik Informatika  
**Universitas:** Universitas Pamulang  

---

## 📄 Lisensi
Project ini dibuat untuk kebutuhan pembelajaran dan Project Akhir Mata Kuliah Mobile Programming.
