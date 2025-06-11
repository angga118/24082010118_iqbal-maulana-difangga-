import java.sql.*;
import java.util.Scanner;

public class AdminService {
    private static Scanner scanner = new Scanner(System.in);

    public static void menuAdmin() {
        while (true) {
            System.out.println("\n=== MENU ADMIN ===");
            System.out.println("1. Lihat Slot Parkir");
            System.out.println("2. Lihat Reservasi dengan Filter");
            System.out.println("3. Lihat Data Kendaraan");
            System.out.println("4. Lihat Data User");
            System.out.println("5. Laporan Pemakaian Slot");
            System.out.println("6. Laporan Pendapatan");
            System.out.println("0. Logout");
            System.out.print("Pilih menu: ");

            String pilihan = scanner.nextLine().trim();
            switch (pilihan) {
                case "1" -> lihatSlot();
                case "2" -> lihatReservasiDenganFilter();
                case "3" -> lihatKendaraan();
                case "4" -> lihatDataUser();
                case "5" -> laporanPemakaianSlot();
                case "6" -> laporanPendapatan();
                case "0" -> {
                    System.out.println("Logout berhasil.");
                    return; // keluar dari menu admin
                }
                default -> System.out.println("Pilihan tidak valid. Silakan coba lagi.");
            }
        }
    }

    public static void lihatSlot() {
    try (Connection conn = DBConnection.getConnection()) {
        PreparedStatement stmt = conn.prepareStatement(
            "SELECT s.id_slot, s.lantai, s.lokasi, s.status, k.plat_nomor " +
            "FROM slot_parkir s LEFT JOIN reservasi_parkir r ON s.id_slot = r.id_slot AND r.waktu_keluar IS NULL " +
            "LEFT JOIN kendaraan k ON r.id_kendaraan = k.id_kendaraan");
        ResultSet rs = stmt.executeQuery();

        System.out.println("\n--- STATUS SLOT PARKIR ---");
        boolean adaTerisi = false;
        while (rs.next()) {
            String idSlot = rs.getString("id_slot");
            int lantai = rs.getInt("lantai");
            String lokasi = rs.getString("lokasi");
            String status = rs.getString("status");
            String plat = rs.getString("plat_nomor");

            System.out.printf("Slot: %s | Lantai: %d | Lokasi: %s | Status: %s",
                idSlot, lantai, lokasi, status);

            if ("terisi".equalsIgnoreCase(status) && plat != null) {
                System.out.printf(" | Plat: %s", plat);
                adaTerisi = true;
            }
            System.out.println();
        }

        if (adaTerisi) {
            Scanner scanner = new Scanner(System.in);
            System.out.print("\nMasukkan Plat Nomor untuk lihat detail parkir (atau ENTER untuk kembali): ");
            String inputPlat = scanner.nextLine().trim();
            if (!inputPlat.isEmpty()) {
                // Query detail parkir berdasarkan plat nomor
                PreparedStatement detailStmt = conn.prepareStatement(
                    "SELECT u.nama, k.merk, k.tipe, r.waktu_masuk, s.id_slot " +
                    "FROM reservasi_parkir r " +
                    "JOIN kendaraan k ON r.id_kendaraan = k.id_kendaraan " +
                    "JOIN user u ON r.id_user = u.id_user " +
                    "JOIN slot_parkir s ON r.id_slot = s.id_slot " +
                    "WHERE k.plat_nomor = ? AND r.waktu_keluar IS NULL");
                detailStmt.setString(1, inputPlat);
                ResultSet detailRs = detailStmt.executeQuery();

                if (detailRs.next()) {
                    System.out.println("\n--- DETAIL PARKIR ---");
                    System.out.println("Nama Pengguna : " + detailRs.getString("nama"));
                    System.out.println("Merk Kendaraan : " + detailRs.getString("merk"));
                    System.out.println("Tipe Kendaraan : " + detailRs.getString("tipe"));
                    System.out.println("Jam Mulai Parkir : " + detailRs.getTimestamp("waktu_masuk"));
                    System.out.println("Slot Parkir : " + detailRs.getString("id_slot"));
                } else {
                    System.out.println("Data detail parkir tidak ditemukan untuk plat nomor tersebut atau slot kosong.");
                }
            }
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }
}


    private static void lihatReservasiDenganFilter() {
        while (true) {
            try (Connection conn = DBConnection.getConnection()) {
                System.out.print("Masukkan tanggal mulai (yyyy-MM-dd): ");
                String tglMulai = scanner.nextLine().trim();
                System.out.print("Masukkan tanggal akhir (yyyy-MM-dd): ");
                String tglAkhir = scanner.nextLine().trim();

                System.out.println("\nFilter berdasarkan (kosongkan untuk semua):");
                System.out.println("1. Nama Pengguna");
                System.out.println("2. Merk Kendaraan");
                System.out.println("3. Tipe Kendaraan");
                System.out.println("4. Plat Nomor");
                System.out.println("K. Kembali ke menu admin");
                System.out.print("Pilih filter (1-4) atau K untuk kembali: ");
                String pilihan = scanner.nextLine().trim();

                if (pilihan.equalsIgnoreCase("K")) {
                    break; // kembali ke menu admin
                }

                String sql = "SELECT r.id_reservasi, r.id_user, r.id_kendaraan, r.id_slot, r.waktu_masuk, r.waktu_keluar, r.total_biaya, " +
                             "k.plat_nomor, k.merk, k.tipe, u.nama " +
                             "FROM reservasi_parkir r " +
                             "LEFT JOIN kendaraan k ON r.id_kendaraan = k.id_kendaraan " +
                             "LEFT JOIN user u ON r.id_user = u.id_user " +
                             "WHERE r.waktu_masuk BETWEEN ? AND ? ";

                String filterValue = null;

                switch (pilihan) {
                    case "1" -> {
                        sql += "AND u.nama LIKE ? ";
                        System.out.print("Masukkan Nama Pengguna: ");
                        filterValue = scanner.nextLine().trim();
                    }
                    case "2" -> {
                        sql += "AND k.merk LIKE ? ";
                        System.out.print("Masukkan Merk Kendaraan: ");
                        filterValue = scanner.nextLine().trim();
                    }
                    case "3" -> {
                        sql += "AND k.tipe LIKE ? ";
                        System.out.print("Masukkan Tipe Kendaraan: ");
                        filterValue = scanner.nextLine().trim();
                    }
                    case "4" -> {
                        sql += "AND k.plat_nomor LIKE ? ";
                        System.out.print("Masukkan Plat Nomor: ");
                        filterValue = scanner.nextLine().trim();
                    }
                    default -> {
                        System.out.println("Pilihan filter tidak valid.");
                        continue;
                    }
                }

                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, tglMulai + " 00:00:00");
                stmt.setString(2, tglAkhir + " 23:59:59");
                if (filterValue != null) {
                    stmt.setString(3, "%" + filterValue + "%");
                }

                ResultSet rs = stmt.executeQuery();
                System.out.println("\n--- DATA RESERVASI ---");
                boolean adaData = false;
                while (rs.next()) {
                    adaData = true;
                    System.out.printf("ID Reservasi: %d | Nama User: %s | ID Kendaraan: %d | ID Slot: %s%n",
                            rs.getInt("id_reservasi"), rs.getString("nama"),
                            rs.getInt("id_kendaraan"), rs.getString("id_slot"));
                    System.out.printf("Plat: %s | Merk: %s | Tipe: %s%n",
                            rs.getString("plat_nomor"), rs.getString("merk"), rs.getString("tipe"));
                    System.out.printf("Waktu Masuk: %s | Waktu Keluar: %s | Total Biaya: %s%n\n",
                            rs.getTimestamp("waktu_masuk"),
                            rs.getTimestamp("waktu_keluar"),
                            rs.getString("total_biaya"));
                }
                if (!adaData) {
                    System.out.println("Tidak ada data reservasi yang ditemukan.");
                }

                System.out.print("\nKetik 'K' untuk kembali ke menu admin, atau tekan ENTER untuk filter lagi: ");
                String kembali = scanner.nextLine().trim();
                if (kembali.equalsIgnoreCase("K")) {
                    break;
                }
            } catch (SQLException e) {
                System.out.println("Error melihat reservasi: " + e.getMessage());
                break;
            }
        }
    }

    private static void lihatKendaraan() {
        while (true) {
            System.out.println("\nPilih filter untuk kendaraan:");
            System.out.println("1. Nama Pengguna");
            System.out.println("2. Merk Kendaraan");
            System.out.println("3. Tipe Kendaraan");
            System.out.println("4. Plat Nomor");
            System.out.println("K. Kembali ke menu admin");
            System.out.print("Masukkan pilihan filter (1-4) atau 'K' untuk kembali: ");
            String pilihanFilter = scanner.nextLine().trim();

            if (pilihanFilter.equalsIgnoreCase("K")) {
                break;
            }

            String sql = "SELECT k.id_kendaraan, k.id_user, u.nama, k.merk, k.tipe, k.plat_nomor " +
                         "FROM kendaraan k JOIN user u ON k.id_user = u.id_user ";
            String filterValue = null;

            switch (pilihanFilter) {
                case "1" -> {
                    sql += "WHERE u.nama LIKE ? ";
                    System.out.print("Masukkan Nama Pengguna: ");
                    filterValue = scanner.nextLine().trim();
                }
                case "2" -> {
                    sql += "WHERE k.merk LIKE ? ";
                    System.out.print("Masukkan Merk Kendaraan: ");
                    filterValue = scanner.nextLine().trim();
                }
                case "3" -> {
                    sql += "WHERE k.tipe LIKE ? ";
                    System.out.print("Masukkan Tipe Kendaraan: ");
                    filterValue = scanner.nextLine().trim();
                }
                case "4" -> {
                    sql += "WHERE k.plat_nomor LIKE ? ";
                    System.out.print("Masukkan Plat Nomor: ");
                    filterValue = scanner.nextLine().trim();
                }
                default -> {
                    System.out.println("Pilihan filter tidak valid. Silakan coba lagi.");
                    continue;
                }
            }

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, "%" + filterValue + "%");
                ResultSet rs = stmt.executeQuery();

                System.out.println("\n--- DATA KENDARAAN ---");
                boolean adaData = false;
                while (rs.next()) {
                    adaData = true;
                    System.out.printf("ID Kendaraan: %d | ID User: %d | Nama User: %s | Merk: %s | Tipe: %s | Plat: %s%n",
                            rs.getInt("id_kendaraan"), rs.getInt("id_user"),
                            rs.getString("nama"), rs.getString("merk"),
                            rs.getString("tipe"), rs.getString("plat_nomor"));
                }
                if (!adaData) {
                    System.out.println("Tidak ada data kendaraan yang ditemukan.");
                }
            } catch (SQLException e) {
                System.out.println("Error melihat kendaraan: " + e.getMessage());
            }

            System.out.print("\nKetik 'K' untuk kembali ke menu admin, atau tekan ENTER untuk filter lagi: ");
            String kembali = scanner.nextLine().trim();
            if (kembali.equalsIgnoreCase("K")) {
                break;
            }
        }
    }

    private static void lihatDataUser() {
        while (true) {
            System.out.println("\nFilter Data User:");
            System.out.println("1. Nama");
            System.out.println("2. Username");
            System.out.println("3. Email");
            System.out.println("4. Nomor HP");
            System.out.println("K. Kembali ke menu admin");
            System.out.print("Pilih filter (1-4) atau K untuk kembali: ");
            String pilihan = scanner.nextLine().trim();

            if (pilihan.equalsIgnoreCase("K")) {
                break;
            }

            String sql = "SELECT id_user, nama, username, email, no_hp, role FROM user ";
            String filterValue = null;

            switch (pilihan) {
                case "1" -> {
                    sql += "WHERE nama LIKE ? ";
                    System.out.print("Masukkan Nama: ");
                    filterValue = scanner.nextLine().trim();
                }
                case "2" -> {
                    sql += "WHERE username LIKE ? ";
                    System.out.print("Masukkan Username: ");
                    filterValue = scanner.nextLine().trim();
                }
                case "3" -> {
                    sql += "WHERE email LIKE ? ";
                    System.out.print("Masukkan Email: ");
                    filterValue = scanner.nextLine().trim();
                }
                case "4" -> {
                    sql += "WHERE no_hp LIKE ? ";
                    System.out.print("Masukkan Nomor HP: ");
                    filterValue = scanner.nextLine().trim();
                }
                default -> {
                    System.out.println("Pilihan filter tidak valid.");
                    continue;
                }
            }

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, "%" + filterValue + "%");
                ResultSet rs = stmt.executeQuery();

                System.out.println("\n--- DATA USER ---");
                boolean adaData = false;
                while (rs.next()) {
                    adaData = true;
                    System.out.printf("ID User: %d | Nama: %s | Username: %s | Email: %s | No HP: %s | Role: %s%n",
                            rs.getInt("id_user"), rs.getString("nama"),
                            rs.getString("username"), rs.getString("email"),
                            rs.getString("no_hp"), rs.getString("role"));
                }
                if (!adaData) {
                    System.out.println("Tidak ada data user yang ditemukan.");
                }
            } catch (SQLException e) {
                System.out.println("Error melihat data user: " + e.getMessage());
            }

            System.out.print("\nKetik 'K' untuk kembali ke menu admin, atau tekan ENTER untuk filter lagi: ");
            String kembali = scanner.nextLine().trim();
            if (kembali.equalsIgnoreCase("K")) {
                break;
            }
        }
    }

   

    private static void laporanPemakaianSlot() {
        while (true) {
            try (Connection conn = DBConnection.getConnection()) {
                System.out.println("\n--- LAPORAN PEMAKAIAN SLOT PARKIR ---");
                System.out.print("Masukkan tanggal mulai (yyyy-MM-dd): ");
                String tglMulai = scanner.nextLine().trim();
                System.out.print("Masukkan tanggal akhir (yyyy-MM-dd): ");
                String tglAkhir = scanner.nextLine().trim();

                String sql = "SELECT id_slot, COUNT(*) AS jumlah_penggunaan " +
                             "FROM reservasi_parkir " +
                             "WHERE waktu_masuk BETWEEN ? AND ? " +
                             "GROUP BY id_slot ORDER BY jumlah_penggunaan DESC";

                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, tglMulai + " 00:00:00");
                stmt.setString(2, tglAkhir + " 23:59:59");

                ResultSet rs = stmt.executeQuery();
                boolean adaData = false;
                while (rs.next()) {
                    adaData = true;
                    System.out.printf("ID Slot: %s | Jumlah Penggunaan: %d%n",
                            rs.getString("id_slot"), rs.getInt("jumlah_penggunaan"));
                }
                if (!adaData) {
                    System.out.println("Tidak ada data pemakaian slot pada rentang tanggal tersebut.");
                }

                System.out.print("\nKetik 'K' untuk kembali ke menu admin, atau tekan ENTER untuk lihat lagi: ");
                String kembali = scanner.nextLine().trim();
                if (kembali.equalsIgnoreCase("K")) {
                    break;
                }
            } catch (SQLException e) {
                System.out.println("Error laporan pemakaian slot: " + e.getMessage());
                break;
            }
        }
    }

    private static void laporanPendapatan() {
    try (Connection conn = DBConnection.getConnection()) {
        System.out.println("\n--- LAPORAN PENDAPATAN ---");
        System.out.print("Masukkan tanggal mulai (yyyy-MM-dd): ");
        String tglMulai = scanner.nextLine().trim();
        System.out.print("Masukkan tanggal akhir (yyyy-MM-dd): ");
        String tglAkhir = scanner.nextLine().trim();

        String sql = "SELECT DATE(waktu_keluar) AS tanggal, SUM(total_biaya) AS pendapatan_harian " +
                     "FROM reservasi_parkir " +
                     "WHERE waktu_keluar IS NOT NULL AND waktu_keluar BETWEEN ? AND ? " +
                     "GROUP BY DATE(waktu_keluar) " +
                     "ORDER BY tanggal ASC";

        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, tglMulai + " 00:00:00");
        stmt.setString(2, tglAkhir + " 23:59:59");

        ResultSet rs = stmt.executeQuery();

        boolean adaData = false;
        double totalKeseluruhan = 0;

        System.out.println("\nTanggal\t\tPendapatan Harian");
        System.out.println("-----------------------------------");

        while (rs.next()) {
            adaData = true;
            String tanggal = rs.getString("tanggal");
            double pendapatan = rs.getDouble("pendapatan_harian");
            totalKeseluruhan += pendapatan;

            System.out.printf("%s\tRp%.2f%n", tanggal, pendapatan);
        }

        if (adaData) {
            System.out.println("-----------------------------------");
            System.out.printf("Total Pendapatan: Rp%.2f%n", totalKeseluruhan);
        } else {
            System.out.println("Tidak ada data pendapatan pada rentang tanggal tersebut.");
        }

    } catch (SQLException e) {
        System.out.println("Terjadi kesalahan saat mengambil data pendapatan: " + e.getMessage());
            }
        }

    }

