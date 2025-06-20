import java.sql.*;

public class SlotTersediaView {
    private String idSlot;      
    private String lokasi;
    private int lantai;

    public SlotTersediaView() {}

    public SlotTersediaView(String idSlot, String lokasi, int lantai) {
        this.idSlot = idSlot;
        this.lokasi = lokasi;
        this.lantai = lantai;
    }

    public void tampilkanSlotTersedia() {
        String query = "SELECT * FROM ViewSlotTersedia";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("=== Slot Tersedia ===");
            while (rs.next()) {
                String id = rs.getString("id_slot");     
                String lokasi = rs.getString("lokasi");
                int lantai = rs.getInt("lantai");

                System.out.println("ID: " + id + " | Lokasi: " + lokasi + " | Lantai: " + lantai);
            }

        } catch (SQLException e) {
            System.out.println("Gagal mengambil data dari view: " + e.getMessage());
        }
    }
}
