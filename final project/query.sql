USE magood;

CREATE TABLE users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  email VARCHAR(100) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  nama VARCHAR(100) NOT NULL,
  alamat TEXT NOT NULL,
  no_hp VARCHAR(20) NOT NULL
);

CREATE TABLE contact_messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    email VARCHAR(100),
    message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE checkout (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT,
  nama VARCHAR(100),
  email VARCHAR(100),
  alamat TEXT,
  telepon VARCHAR(20),
  grand_total INT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id)
);

ALTER TABLE checkout DROP COLUMN users;


CREATE TABLE checkout_items (
  id INT AUTO_INCREMENT PRIMARY KEY,
  checkout_id INT,
  product_name VARCHAR(100),
  quantity INT,
  total_price INT,
  FOREIGN KEY (checkout_id) REFERENCES checkout(id)
);
