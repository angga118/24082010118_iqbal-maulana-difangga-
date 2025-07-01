let cart = [];
const savedCart = localStorage.getItem('cart');
if (savedCart) cart = JSON.parse(savedCart);

document.querySelectorAll('.cart-btn').forEach((btn) => {
  btn.addEventListener('click', () => {
    const text = btn.parentElement.querySelector('p').innerText;
    const lines = text.split('\n');
    const name = lines[0].trim();
    const priceText = lines[lines.length - 1];
    const price = parseInt(priceText.replace(/[^\d]/g, ''));

    const existing = cart.find(item => item.name === name);
    if (existing) {
      existing.qty += 1;
      existing.total += price;
    } else {
      cart.push({ name, price, qty: 1, total: price });
    }

    localStorage.setItem('cart', JSON.stringify(cart));
    alert("Produk ditambahkan ke keranjang!");
  });
});

function checkoutItems() {
  if (cart.length === 0) {
    alert("Keranjang masih kosong!");
    return;
  }

  const form = document.createElement("form");
  form.method = "POST";
  form.action = "checkout.php";

  const input = document.createElement("input");
  input.type = "hidden";
  input.name = "cart";
  input.value = JSON.stringify(cart);

  form.appendChild(input);
  document.body.appendChild(form);
  form.submit();
}

document.querySelectorAll('.faq-item').forEach((item) => {
  item.addEventListener('click', () => {
    const active = document.querySelector('.faq-item.active');
    if (active && active !== item) {
      active.classList.remove('active');
      active.nextElementSibling?.remove();
    }

    if (!item.classList.contains('active')) {
      item.classList.add('active');
      const answer = document.createElement('div');
      answer.className = 'faq-answer';

      if (item.textContent.includes("mati")) {
        answer.textContent = "Maggot bisa mati karena suhu terlalu tinggi, media terlalu basah, atau makanan membusuk.";
      } else if (item.textContent.includes("bau")) {
        answer.textContent = "Media yang bau biasanya karena terlalu lembap atau fermentasi berlebihan. Tambahkan serbuk gergaji atau kertas.";
      } else if (item.textContent.includes("tidak mau makan")) {
        answer.textContent = "Periksa apakah pakan masih segar dan tidak berjamur. Maggot tidak suka makanan basi atau tercemar.";
      } else if (item.textContent.includes("alat")) {
        answer.textContent = "Anda butuh wadah, media organik, telur maggot, dan panduan budidaya. Bisa beli di MaGood Mart.";
      } else {
        answer.textContent = "Jawaban belum tersedia.";
      }

      item.insertAdjacentElement('afterend', answer);
    } else {
      item.classList.remove('active');
      item.nextElementSibling?.remove();
    }
  });
});

const searchInput = document.querySelector('.search-bar input');
if (searchInput) {
  searchInput.addEventListener('input', () => {
    const query = searchInput.value.toLowerCase();
    document.querySelectorAll('.faq-item').forEach((item) => {
      const match = item.textContent.toLowerCase().includes(query);
      item.style.display = match ? 'block' : 'none';
      if (item.classList.contains('active')) {
        item.classList.remove('active');
        item.nextElementSibling?.remove();
      }
    });
  });
}
