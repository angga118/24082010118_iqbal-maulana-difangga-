import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("==========================================");
            System.out.println("     SISTEM RESERVASI PARKIR GEDUNG       ");
            System.out.println("==========================================");
            System.out.println("Selamat datang! Silakan pilih peran Anda.");
            System.out.println("1. Masuk sebagai Pengguna");
            System.out.println("2. Masuk sebagai Admin");
            System.out.println("0. Keluar");
            System.out.print("Pilih: ");
            int peran = Integer.parseInt(scanner.nextLine());

            if (peran == 1) {
                System.out.println("\n=== PENGGUNA ===");
                System.out.println("1. Login");
                System.out.println("2. Daftar");
                System.out.println("0. Kembali");
                System.out.print("Pilih: ");
                int pilih = Integer.parseInt(scanner.nextLine());

                if (pilih == 0) {
                    continue;
                }

                if (pilih == 2) {
                    UserService.registerUser();
                }

                int userId = UserService.loginUser();
                if (userId != -1) {
                    while (true) {
                        System.out.println("\n--- MENU PENGGUNA ---");
                        System.out.println("1. Reservasi Parkir");
                        System.out.println("2. Lihat Slot Parkir");
                        System.out.println("3. Bayar Parkir");
                        System.out.println("4. Logout");
                        System.out.println("5. Ubah Username/Password");
                        System.out.print("Pilih: ");
                        int menu = Integer.parseInt(scanner.nextLine());

                        switch (menu) {
                            case 1 -> ParkirService.reservasiParkir(userId);
                            case 2 -> ParkirService.lihatSlot();
                            case 3 -> ParkirService.bayarParkir(userId);
                            case 4 -> {
                                System.out.println("Logout berhasil. Kembali ke menu utama.");
                                break;
                            }
                            case 5 -> UserService.ubahUsernamePassword(userId);
                            default -> System.out.println("Pilihan tidak valid.");
                        }

                        if (menu == 4) break;
                    }
                }

            } else if (peran == 2) {
                int adminId = UserService.loginAdmin();
                if (adminId != -1) {
                    AdminService.menuAdmin();
                }
            } else if (peran == 0) {
                System.out.println("Terima kasih! Program selesai.");
                break;
            } else {
                System.out.println("Peran tidak valid. Silakan coba lagi.");
            }
        }

        scanner.close();
    }
}
