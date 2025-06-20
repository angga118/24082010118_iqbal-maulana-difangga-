# 🚗 Sistem Reservasi Tempat Parkir di Gedung Bertingkat

Sistem ini dirancang untuk membantu pengguna melakukan **reservasi parkir secara online**, serta membantu pengelola gedung dalam **mengelola data kendaraan, pengguna, dan slot parkir** secara efisien dan akurat.

Dibangun menggunakan **Java Console** dan **MySQL**, sistem ini dilengkapi dengan fitur **Stored Procedure**, **Trigger**, dan **laporan-laporan otomatis** seperti Crosstab, Subquery, dan CTE.

---

## 📊 Tampilan SQL

### Gunakan database

```sql
USE parkir_gedung;
```

### Tabel Utama

```sql
CREATE TABLE user (
    id_user INT AUTO_INCREMENT PRIMARY KEY,
    nama VARCHAR(100),
    username VARCHAR(50) UNIQUE,
    email VARCHAR(100),
    no_hp VARCHAR(15),
    password VARCHAR(100),
    role VARCHAR(10) DEFAULT 'pengguna'
);

CREATE TABLE kendaraan (
    id_kendaraan INT AUTO_INCREMENT PRIMARY KEY,
    id_user INT,
    merk VARCHAR(50),
    tipe VARCHAR(50),
    plat_nomor VARCHAR(20) UNIQUE,
    FOREIGN KEY (id_user) REFERENCES user(id_user)
);

CREATE TABLE slot_parkir (
    id_slot VARCHAR(10) PRIMARY KEY,
    lantai INT,
    lokasi VARCHAR(10),
    status VARCHAR(20)
);

CREATE TABLE reservasi_parkir (
    id_reservasi INT AUTO_INCREMENT PRIMARY KEY,
    id_user INT,
    id_kendaraan INT,
    id_slot VARCHAR(10),
    waktu_masuk DATETIME,
    waktu_keluar DATETIME,
    total_biaya DECIMAL(10,2),
    FOREIGN KEY (id_user) REFERENCES user(id_user),
    FOREIGN KEY (id_kendaraan) REFERENCES kendaraan(id_kendaraan),
    FOREIGN KEY (id_slot) REFERENCES slot_parkir(id_slot)
);

CREATE TABLE log_parkir (
    id_log INT AUTO_INCREMENT PRIMARY KEY,
    id_reservasi INT,
    waktu_parkir DATETIME,
    keterangan VARCHAR(255),
    FOREIGN KEY (id_reservasi) REFERENCES reservasi_parkir(id_reservasi)
);
```

### Data Awal

```sql
INSERT INTO user (username, password, role)
VALUES ('adminparkir', 'surabayatimur', 'admin');

INSERT INTO slot_parkir (id_slot, lantai, lokasi, status) VALUES
('L11A', 1, '1A', 'tersedia'), ('L11B', 1, '1B', 'tersedia'),
('L12A', 1, '2A', 'tersedia'), ('L12B', 1, '2B', 'tersedia'),
('L13A', 1, '3A', 'tersedia'), ('L13B', 1, '3B', 'tersedia'),
('L14A', 1, '4A', 'tersedia'), ('L14B', 1, '4B', 'tersedia'),
('L15A', 1, '5A', 'tersedia'), ('L15B', 1, '5B', 'tersedia'),
('L21A', 2, '1A', 'tersedia'), ('L21B', 2, '1B', 'tersedia'),
('L22A', 2, '2A', 'tersedia'), ('L22B', 2, '2B', 'tersedia'),
('L23A', 2, '3A', 'tersedia'), ('L23B', 2, '3B', 'tersedia'),
('L24A', 2, '4A', 'tersedia'), ('L24B', 2, '4B', 'tersedia'),
('L25A', 2, '5A', 'tersedia'), ('L25B', 2, '5B', 'tersedia');


INSERT INTO user (nama, username, email, no_hp, password, role) VALUES
('Muhammad Rafi', 'rafi123', 'rafi123@gmail.com', '081234567891', 'rafipass21', 'pengguna'),
('Aulia Rahmawati', 'aulia456', 'aulia456@gmail.com', '082345678912', 'aulia88', 'pengguna'),
('Dimas Prasetyo', 'dimas789', 'dimas789@gmail.com', '083456789123', 'dimas2023', 'pengguna'),
('Nadya Kusuma', 'nadya321', 'nadya321@gmail.com', '084567891234', 'nadya77', 'pengguna'),
('Fahri Hidayat', 'fahri654', 'fahri654@gmail.com', '085678912345', 'fahriogin', 'pengguna');
```

## ⚙ Fitur Khusus Database

### 📌 Stored Procedure: hitung_biaya

* Menghitung otomatis biaya parkir berdasarkan selisih waktu masuk dan keluar
* Biaya minimum: *Rp3.000 / jam*
* Tetap dihitung 1 jam meskipun parkir < 60 menit

```sql
CREATE PROCEDURE hitung_biaya (
    IN waktu_masuk DATETIME,
    IN waktu_keluar DATETIME,
    OUT biaya DECIMAL(10,2)
)
BEGIN
    DECLARE jam INT;
    SET jam = TIMESTAMPDIFF(HOUR, waktu_masuk, waktu_keluar);
    IF jam = 0 THEN
        SET jam = 1;
    END IF;
    SET biaya = jam * 3000;
END;
```

### 🚨 Trigger: blokir_slot

* Mencegah *double booking* pada slot yang sama
* Menolak insert jika slot sudah dipakai kendaraan lain yang belum keluar

```sql
CREATE TRIGGER blokir_slot
BEFORE INSERT ON reservasi_parkir
FOR EACH ROW
BEGIN
    DECLARE count_reservasi INT;

    SELECT COUNT(*) INTO count_reservasi
    FROM reservasi_parkir
    WHERE id_slot = NEW.id_slot
      AND waktu_keluar IS NULL;

    IF count_reservasi > 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Slot sudah terisi!';
    END IF;
END;
```

### 👁 View: viewslottersedia

* Menampilkan daftar slot parkir yang *statusnya masih kosong*
* Digunakan oleh user saat ingin melihat slot yang tersedia secara real-time

```sql
CREATE OR REPLACE VIEW ViewSlotTersedia AS
SELECT id_slot, lokasi, lantai
FROM slot_parkir
WHERE status = 'tersedia';
```

### Laporan

#### 📊 Crosstab Report

* Menampilkan *jumlah reservasi per slot parkir*
* Digunakan admin untuk melihat slot mana yang paling sering digunakan

```sql
SELECT
  id_slot,
  SUM(CASE WHEN waktu_keluar IS NOT NULL THEN 1 ELSE 0 END) AS selesai,
  SUM(CASE WHEN waktu_keluar IS NULL THEN 1 ELSE 0 END) AS pending
FROM reservasi_parkir
GROUP BY id_slot;
```

#### 🧠 CTE Report (Common Table Expression)

* Menampilkan *reservasi terakhir dari setiap pengguna*
* Membantu admin melacak aktivitas terakhir user

```sql
WITH TerakhirReservasi AS (
  SELECT id_user, MAX(waktu_masuk) AS waktu_terakhir
  FROM reservasi_parkir
  GROUP BY id_user
)
SELECT u.nama, t.waktu_terakhir
FROM user u
JOIN TerakhirReservasi t ON u.id_user = t.id_user;
```

#### 🔍 Subquery Report

* Menampilkan *user dengan jumlah reservasi terbanyak*
* Cocok untuk analisis pengguna aktif

```sql
SELECT u.nama, COUNT(*) AS jumlah
FROM user u
JOIN reservasi_parkir r ON u.id_user = r.id_user
GROUP BY u.id_user
HAVING jumlah = (
  SELECT MAX(jml) FROM (
    SELECT COUNT(*) AS jml
    FROM reservasi_parkir
    GROUP BY id_user
  ) AS data
);
```

---

## 🔑 Fitur Utama

### Untuk Pengguna

* Registrasi & Login: Akses sistem dengan akun masing-masing
* Reservasi Parkir: Pilih slot parkir yang kosong untuk tanggal dan jam tertentu
* Lihat Slot Parkir: Tampilkan semua slot dan statusnya (tersedia/terisi)
* Bayar Parkir: Hitung dan bayar otomatis sesuai durasi parkir
* Ubah Username / Password: Ganti data akun dengan cepat & aman
* Logout: Keluar dari sistem kembali ke menu utama

### Untuk Admin

* Lihat Slot Parkir: Pantau semua slot dan statusnya secara real-time
* Filter Reservasi: Telusuri reservasi berdasarkan nama, tipe kendaraan, atau plat nomor
* Lihat Data Kendaraan: Daftar kendaraan yang terdaftar
* Lihat Data User: Informasi pengguna lengkap: nama, username, email, dan no\_hp
* Laporan Pemakaian Slot: Lihat frekuensi pemakaian setiap slot
* Laporan Pendapatan: Lihat total pendapatan dari reservasi
* Lihat Slot Tersedia (VIEW): Tampilkan slot kosong via database view
* Laporan Crosstab: Jumlah pemakaian slot dalam format pivot
* Laporan CTE: Reservasi terakhir tiap user
* Laporan Subquery: User dengan jumlah reservasi terbanyak
* Hapus Akun User: Hapus user berdasarkan ID atau username
* Logout Admin: Keluar dari menu admin


---

## 🪩 Struktur Sistem

### Tabel Utama

| Tabel              | Deskripsi                                                                 |
| ------------------ | ------------------------------------------------------------------------- |
| user             | Data pengguna (admin dan user biasa): nama, username, email, no\_hp, role |
| kendaraan        | Data kendaraan milik user: merk, tipe, plat nomor                         |
| slot_parkir      | Slot parkir: lokasi, lantai, status                                       |
| reservasi_parkir | Reservasi parkir: user, kendaraan, slot, waktu masuk/keluar, total biaya  |
| viewslottersedia | View untuk menampilkan slot parkir yang tersedia                          |


### Relasi Tabel

* *User ↔ Kendaraan*: 1 user bisa punya banyak kendaraan
* *User ↔ Reservasi*: 1 user bisa membuat banyak reservasi
* *Kendaraan ↔ Reservasi*: 1 kendaraan bisa digunakan berkali-kali
* *Slot ↔ Reservasi*: 1 slot bisa dipakai berkali-kali *(tidak bersamaan)
* *Reservasi ↔ ViewSlotTersedia*: View khusus untuk menampilkan slot yang belum dipakai

---

## ▶ Cara Menjalankan Program

### Compile:

```cmd
javac -cp ".;lib/mysql-connector-j-8.0.33.jar" *.java
```

### Jalankan Program:

```cmd
java -cp ".;lib/mysql-connector-j-8.0.33.jar" Main
```

## Catatan 📝:Pastikan nama class utama adalah Main.java, dan koneksi database sudah sesuai (lihat DBConnection.java).

---

## 👨‍💼 Biodata Kelompok 5

| Nama                          | NPM         |
| ----------------------------- | ----------- |
| Muhammad Rasya Dzikri Subagio | 24082010091 |
| Syifa Mauliddia               | 24082010104 |
| Dian Indri Eklesia L. Tobing  | 24082010110 |
| Iqbal Maulana Difangga        | 24082010118 |

> 🎓 Proyek Akhir - Basis Data
> Program Studi Sistem Informasi
> Fakultas Ilmu Komputer
> Universitas Pembangunan Nasional "Veteran" Jawa Timur
