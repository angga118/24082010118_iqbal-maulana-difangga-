# 🚗 Sistem Reservasi Tempat Parkir di Gedung Bertingkat

Sistem ini dirancang untuk membantu pengguna melakukan **reservasi parkir secara online**, serta membantu pengelola gedung dalam **mengelola data kendaraan, pengguna, dan slot parkir** secara efisien dan akurat.

Dibangun menggunakan **Java Console** dan **MySQL**, sistem ini dilengkapi dengan fitur **Stored Procedure**, **Trigger**, dan **laporan-laporan otomatis** seperti Crosstab, Subquery, dan CTE.

---

## 🖼️ Tampilan SQL

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
('L11A', 1, '1A', 'tersedia'),
... -- (lanjutan hingga L25B)

INSERT INTO user (nama, username, email, no_hp, password, role) VALUES
('Muhammad Rafi', 'rafi123', 'rafi123@gmail.com', '081234567891', 'rafipass21', 'pengguna'),
... -- (data user lainnya)
```

### Stored Procedure & Trigger

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

### View

```sql
CREATE OR REPLACE VIEW ViewSlotTersedia AS
SELECT id_slot, lokasi, lantai
FROM slot_parkir
WHERE status = 'tersedia';
```

### Laporan

#### Crosstab Report

```sql
SELECT
  id_slot,
  SUM(CASE WHEN waktu_keluar IS NOT NULL THEN 1 ELSE 0 END) AS selesai,
  SUM(CASE WHEN waktu_keluar IS NULL THEN 1 ELSE 0 END) AS pending
FROM reservasi_parkir
GROUP BY id_slot;
```

#### CTE Report

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

#### Subquery Report

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

* Registrasi & Login
* Reservasi parkir
* Lihat slot parkir
* Bayar parkir otomatis
* Ubah username/password
* Logout

### Untuk Admin

* Lihat & filter reservasi
* Lihat data kendaraan & user
* Laporan pemakaian slot
* Laporan pendapatan
* View slot tersedia
* Crosstab, CTE, dan Subquery report
* Hapus akun user
* Logout admin

---

## 🪩 Struktur Sistem

### Tabel Utama

| Tabel             | Deskripsi           |
| ----------------- | ------------------- |
| user              | Data pengguna       |
| kendaraan         | Data kendaraan user |
| slot\_parkir      | Slot parkir         |
| reservasi\_parkir | Data reservasi      |
| viewslottersedia  | View slot kosong    |

### Relasi Tabel

* 1 user → banyak kendaraan & reservasi
* 1 kendaraan → banyak reservasi
* 1 slot → banyak reservasi (tidak bersamaan)

---

## ▶ Cara Menjalankan Program

### Compile:

```cmd
javac -cp ".;lib/mysql-connector-j-8.0.33.jar" *.java
```

### Jalankan:

```cmd
java -cp ".;lib/mysql-connector-j-8.0.33.jar" Main
```

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
> Universitas Pembangunan Nasional "Veteran" Jawa Timur
