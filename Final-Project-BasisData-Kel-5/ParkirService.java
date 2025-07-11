import java.sql.*;
import java.util.Scanner;

public class ParkirService {
    static Scanner scanner = new Scanner(System.in);

    public static void reservasiParkir(int idUser) {
    try (Connection conn = DBConnection.getConnection()) {
        System.out.print("Plat Nomor: ");
        String plat = scanner.nextLine();

        int idKendaraan;

        // Cek apakah kendaraan sudah ada berdasarkan plat
        PreparedStatement cekKendaraan = conn.prepareStatement(
            "SELECT id_kendaraan FROM kendaraan WHERE plat_nomor = ?");
        cekKendaraan.setString(1, plat);
        ResultSet rsCek = cekKendaraan.executeQuery();

        if (rsCek.next()) {
            // Kendaraan sudah ada
            idKendaraan = rsCek.getInt("id_kendaraan");
            System.out.println("Kendaraan sudah terdaftar. Data akan digunakan untuk reservasi.");
        } else {
            // Kendaraan belum ada, minta input merk dan tipe
            System.out.print("Merk Kendaraan: ");
            String merk = scanner.nextLine();
            System.out.print("Tipe Kendaraan: ");
            String tipe = scanner.nextLine();

            // Insert kendaraan baru
            PreparedStatement insertKendaraan = conn.prepareStatement(
                "INSERT INTO kendaraan (id_user, merk, tipe, plat_nomor) VALUES (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);
            insertKendaraan.setInt(1, idUser);
            insertKendaraan.setString(2, merk);
            insertKendaraan.setString(3, tipe);
            insertKendaraan.setString(4, plat);
            insertKendaraan.executeUpdate();

            ResultSet rsKendaraan = insertKendaraan.getGeneratedKeys();
            rsKendaraan.next();
            idKendaraan = rsKendaraan.getInt(1);
        }

        // Tampilkan slot tersedia
        System.out.println("\n--- SLOT PARKIR TERSEDIA ---");
        PreparedStatement stmt = conn.prepareStatement(
            "SELECT id_slot, lantai, lokasi FROM slot_parkir WHERE status = 'tersedia'");
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            System.out.printf("ID: %s | Lantai: %d | Lokasi: %s%n",
                rs.getString("id_slot"), rs.getInt("lantai"), rs.getString("lokasi"));
        }

        // Pilih slot
        System.out.print("\nPilih ID Slot Parkir: ");
        String idSlot = scanner.nextLine();

        // Input waktu masuk
        System.out.print("Waktu Masuk (format yyyy-MM-dd HH:mm:ss): ");
        String waktuMasukStr = scanner.nextLine();
        Timestamp waktuMasuk = Timestamp.valueOf(waktuMasukStr);

        // Insert reservasi
        PreparedStatement reservasi = conn.prepareStatement(
            "INSERT INTO reservasi_parkir (id_user, id_kendaraan, id_slot, waktu_masuk) VALUES (?, ?, ?, ?)");
        reservasi.setInt(1, idUser);
        reservasi.setInt(2, idKendaraan);
        reservasi.setString(3, idSlot);
        reservasi.setTimestamp(4, waktuMasuk);
        reservasi.executeUpdate();

        // Update status slot
        PreparedStatement updateSlot = conn.prepareStatement(
            "UPDATE slot_parkir SET status = 'terisi' WHERE id_slot = ?");
        updateSlot.setString(1, idSlot);
        updateSlot.executeUpdate();

        System.out.println("Reservasi berhasil!");
    } catch (SQLException e) {
        System.out.println("Gagal reservasi: " + e.getMessage());
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
            while (rs.next()) {
                String status = rs.getString("status");
                String plat = rs.getString("plat_nomor");
                System.out.printf("Slot: %s | Lantai: %d | Lokasi: %s | Status: %s",
                    rs.getString("id_slot"), rs.getInt("lantai"),
                    rs.getString("lokasi"), status);

                if ("terisi".equalsIgnoreCase(status) && plat != null) {
                    System.out.printf(" | Plat: %s", plat);
                }
                System.out.println();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void bayarParkir(int idUser) {
    try (Connection conn = DBConnection.getConnection()) {
        PreparedStatement stmt = conn.prepareStatement(
            "SELECT r.id_reservasi, k.merk, k.tipe, k.plat_nomor, r.waktu_masuk " +
            "FROM reservasi_parkir r JOIN kendaraan k ON r.id_kendaraan = k.id_kendaraan " +
            "WHERE r.id_user = ? AND r.waktu_keluar IS NULL");
        stmt.setInt(1, idUser);
        ResultSet rs = stmt.executeQuery();

        if (!rs.next()) {
            System.out.println("Tidak ada reservasi aktif.");
            return;
        }

        int idReservasi = rs.getInt("id_reservasi");
        String merk = rs.getString("merk");
        String tipe = rs.getString("tipe");
        String plat = rs.getString("plat_nomor");
        Timestamp masuk = rs.getTimestamp("waktu_masuk");
        // Input waktu keluar manual
        System.out.print("Waktu Keluar (format yyyy-MM-dd HH:mm:ss): ");
        String waktuKeluarStr = scanner.nextLine();
        Timestamp keluar = Timestamp.valueOf(waktuKeluarStr);

        // Hitung biaya
        CallableStatement cs = conn.prepareCall("{CALL hitung_biaya(?, ?, ?)}");
        cs.setTimestamp(1, masuk);
        cs.setTimestamp(2, keluar);
        cs.registerOutParameter(3, Types.DECIMAL);
        cs.execute();
        double biaya = cs.getDouble(3);

        // Input uang dan validasi
        double uang;
        do {
            System.out.printf("Total biaya parkir: Rp %.0f%n", biaya);
            System.out.print("Masukkan uang: Rp ");
            uang = scanner.nextDouble();
            scanner.nextLine(); // consume newline
            if (uang < biaya) {
                System.out.println("Uang yang dimasukkan kurang. Silakan coba lagi.");
            }
        } while (uang < biaya);

        double kembali = uang - biaya;

        // Update reservasi
        PreparedStatement update = conn.prepareStatement(
            "UPDATE reservasi_parkir SET waktu_keluar=?, total_biaya=? WHERE id_reservasi=?");
        update.setTimestamp(1, keluar);
        update.setDouble(2, biaya);
        update.setInt(3, idReservasi);
        update.executeUpdate();

        // Kosongkan slot parkir
        PreparedStatement kosongkan = conn.prepareStatement(
            "UPDATE slot_parkir SET status='tersedia' WHERE id_slot = " +
            "(SELECT id_slot FROM reservasi_parkir WHERE id_reservasi=?)");
        kosongkan.setInt(1, idReservasi);
        kosongkan.executeUpdate();

        // Output struk parkir rapi dan jelas
        System.out.println("\n==================================");
        System.out.println("          STRUK PARKIR");
        System.out.println("==================================");
        System.out.printf("Merk Kendaraan  : %s%n", merk);
        System.out.printf("Tipe Kendaraan  : %s%n", tipe);
        System.out.printf("Plat Nomor      : %s%n", plat);
        System.out.printf("Waktu Masuk     : %s%n", masuk.toString());
        System.out.printf("Waktu Keluar    : %s%n", keluar.toString());
        System.out.println("----------------------------------");
        System.out.printf("Total Biaya     : Rp %.0f%n", biaya);
        System.out.printf("Uang Tunai      : Rp %.0f%n", uang);
        System.out.printf("Kembalian       : Rp %.0f%n", kembali);
        System.out.println("==================================");
        System.out.println("Terima kasih telah menggunakan layanan kami!");
        System.out.println("==================================\n");

    } catch (SQLException e) {
        e.printStackTrace();
    }
}

}
