import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("==========================================");
        System.out.println("     SISTEM RESERVASI PARKIR GEDUNG       ");
        System.out.println("==========================================");
        System.out.println("Selamat datang! Silakan pilih peran Anda.");
        System.out.println("1. Masuk sebagai Pengguna");
        System.out.println("2. Masuk sebagai Admin");
        System.out.print("Pilih: ");
        int peran = Integer.parseInt(scanner.nextLine());

        if (peran == 1) {
            System.out.println("\n=== PENGGUNA ===");
            System.out.println("1. Login");
            System.out.println("2. Daftar");
            System.out.print("Pilih: ");
            int pilih = Integer.parseInt(scanner.nextLine());

            if (pilih == 2) {
                UserService.registerUser();
            }

            int userId = UserService.loginUser();

            while (true) {
            System.out.println("\n--- MENU PENGGUNA ---");
            System.out.println("1. Reservasi Parkir");
            System.out.println("2. Lihat Slot Parkir");
            System.out.println("3. Bayar Parkir");
            System.out.println("4. Keluar");
            System.out.println("5. Ubah Username/Password"); // Tambahan
            System.out.print("Pilih: ");
            int menu = Integer.parseInt(scanner.nextLine());

            switch (menu) {
                case 1 -> ParkirService.reservasiParkir(userId);
                case 2 -> ParkirService.lihatSlot();
                case 3 -> ParkirService.bayarParkir(userId);
                case 4 -> {
                    System.out.println("Keluar...");
                    return;
                }
                case 5 -> UserService.ubahUsernamePassword(userId); // Tambahan
                default -> System.out.println("Pilihan tidak valid.");
            }
        }

        } else if (peran == 2) {
            int adminId = UserService.loginAdmin();
            if (adminId != -1) {
                AdminService.menuAdmin();
            }
        } else {
            System.out.println("Peran tidak valid. Keluar...");
        }
    }
}
