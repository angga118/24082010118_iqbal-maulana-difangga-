import java.sql.*;

public class ReportService {

    public static void laporanCrosstab() {
        String query = """
            SELECT 
                id_slot,
                SUM(CASE WHEN waktu_keluar IS NOT NULL THEN 1 ELSE 0 END) AS selesai,
                SUM(CASE WHEN waktu_keluar IS NULL THEN 1 ELSE 0 END) AS pending
            FROM reservasi_parkir
            GROUP BY id_slot
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("\n=== Laporan Crosstab Pemakaian Slot ===");
            System.out.printf("%-10s | %-8s | %-8s%n", "ID Slot", "Selesai", "Pending");
            System.out.println("-----------------------------");

            while (rs.next()) {
                String idSlot = rs.getString("id_slot");
                int selesai = rs.getInt("selesai");
                int pending = rs.getInt("pending");

                System.out.printf("%-10s | %-8d | %-8d%n", idSlot, selesai, pending);
            }

        } catch (SQLException e) {
            System.out.println("Gagal memuat laporan crosstab: " + e.getMessage());
        }
    }

    public static void laporanCTE() {
        System.out.println("=== Laporan CTE: Waktu Terakhir Reservasi Setiap User ===");
        String query = """
            WITH TerakhirReservasi AS (
              SELECT id_user, MAX(waktu_masuk) AS waktu_terakhir
              FROM reservasi_parkir
              GROUP BY id_user
            )
            SELECT u.nama, t.waktu_terakhir
            FROM user u
            JOIN TerakhirReservasi t ON u.id_user = t.id_user
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                System.out.println("Nama: " + rs.getString("nama") +
                        " | Terakhir Masuk: " + rs.getTimestamp("waktu_terakhir"));
            }

        } catch (SQLException e) {
            System.out.println("Gagal menjalankan laporan CTE: " + e.getMessage());
        }
    }

    public static void laporanSubquery() {
        System.out.println("=== Laporan SUBQUERY: User Paling Sering Reservasi ===");
        String query = """
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
            )
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                System.out.println("Nama: " + rs.getString("nama") +
                        " | Jumlah Reservasi: " + rs.getInt("jumlah"));
            }

        } catch (SQLException e) {
            System.out.println("Gagal menjalankan laporan Subquery: " + e.getMessage());
        }
    }
}
