<?php
$host = "localhost";
$user = "root";
$pass = ""; // kosong (jangan isi password apapun)
$db   = "magood"; // pastikan nama database benar

$conn = new mysqli($host, $user, $pass, $db);

// Cek koneksi
if ($conn->connect_error) {
    die("Koneksi gagal: " . $conn->connect_error);
}
?>
