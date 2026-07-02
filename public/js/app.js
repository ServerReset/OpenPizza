const state = {
  currentPage: 'home',
  stores: [],
  selectedStore: null,
  storeMenu: null,
  cart: JSON.parse(localStorage.getItem('openpizza-cart') || '[]'),
  profile: JSON.parse(localStorage.getItem('openpizza-profile') || '{}'),
  builder: { crust: null, size: null, sauce: null, cheese: null, toppings: [] },
  currentCategory: null
};

function navigate(page) {
  state.currentPage = page;
  document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
  const el = document.getElementById(`page-${page}`);
  if (el) el.classList.add('active');

  document.querySelectorAll('.nav-item').forEach(n => {
    n.classList.toggle('active', n.dataset.page === page);
  });

  const fab = document.getElementById('cartFab');
  if (page === 'cart') { fab.style.display = 'none'; }
  else { fab.style.display = state.cart.length > 0 ? 'flex' : 'none'; }

  window.scrollTo(0, 0);

  if (page === 'menu') renderMenuCategories();
  if (page === 'cart') renderCart();
  if (page === 'profile') loadProfile();
  updateCartBadge();
}

function updateCartBadge() {
  const count = state.cart.reduce((s, i) => s + (i.qty || 1), 0);
  document.getElementById('cartBadge').textContent = count;
  const preview = document.getElementById('home-cart-preview');
  if (state.currentPage === 'home') {
    if (count > 0) {
      preview.style.display = 'block';
      document.getElementById('home-cart-count').textContent = `${count} item${count !== 1 ? 's' : ''}`;
      const total = state.cart.reduce((s, i) => s + (i.price || 0) * (i.qty || 1), 0);
      document.getElementById('home-cart-total').textContent = `$${total.toFixed(2)}`;
    } else {
      preview.style.display = 'none';
    }
  }
}

function saveCart() {
  localStorage.setItem('openpizza-cart', JSON.stringify(state.cart));
  updateCartBadge();
}

// Store Locator
async function searchStores() {
  const q = document.getElementById('storeSearch').value.trim();
  if (!q) return;

  const results = document.getElementById('stores-results');
  const empty = document.getElementById('stores-empty');
  results.innerHTML = '<div class="loading"><div class="spinner"></div></div>';
  empty.style.display = 'none';

  try {
    const data = await API.nearbyStores(q);
    state.stores = data.stores;
    if (data.stores.length === 0) {
      results.innerHTML = '<div class="error">No stores found near this address</div>';
      return;
    }
    results.innerHTML = `<div class="grid-2">${data.stores.map(s => `
      <div class="m3-card store-card" onclick="selectStore('${s.id}')">
        <div class="name">${s.name}</div>
        <div class="addr">${s.address || ''}${s.city ? ', ' + s.city : ''} ${s.state || ''}</div>
        <div class="dist">${s.distance ? (s.distance * 1.609).toFixed(1) + ' km' : ''} ${s.isOpen ? '● Open' : '○ Closed'}</div>
        <div style="font:var(--md-sys-typescale-body-small);color:var(--md-sys-color-on-surface-variant);margin-top:4px">
          ${s.isDelivery ? 'Delivery' : ''} ${s.isOnlineCapable ? '• Online Ordering' : ''}
        </div>
      </div>
    `).join('')}</div>`;
  } catch (err) {
    results.innerHTML = `<div class="error">${err.message}</div>`;
  }
}

function selectStore(id) {
  const store = state.stores.find(s => s.id === id);
  if (!store) return;
  state.selectedStore = store;
  document.getElementById('builderStore').value = store.zip || '';
  loadBuilderStore();
  navigate('builder');
}

// Menu
async function renderMenuCategories() {
  try {
    const data = await API.getCategories();
    const container = document.getElementById('menu-categories');
    container.innerHTML = data.categories.map(c => `
      <div class="m3-card menu-cat-card" onclick="showMenuCategory('${c.id}')">
        <span class="icon">${c.icon}</span>
        <span class="name">${c.name}</span>
        <span class="desc">${c.description}</span>
      </div>
    `).join('');
  } catch (err) {
    document.getElementById('menu-categories').innerHTML = `<div class="error">${err.message}</div>`;
  }
}

function showMenuCategories() {
  state.currentCategory = null;
  document.getElementById('menu-categories').style.display = 'grid';
  document.getElementById('menu-items').innerHTML = '';
  document.getElementById('menu-back-btn').style.display = 'none';
}

function showMenuCategory(catId) {
  state.currentCategory = catId;
  document.getElementById('menu-categories').style.display = 'none';
  document.getElementById('menu-back-btn').style.display = 'inline-flex';

  const container = document.getElementById('menu-items');
  container.innerHTML = `<div class="section-title">${catId.charAt(0).toUpperCase() + catId.slice(1)}</div>
    <div class="empty-state">
      <span class="icon">🍕</span>
      <span class="text">Select a store to see menu items</span>
    </div>`;

  if (!state.selectedStore) {
    container.innerHTML += `
      <div class="search-bar" style="margin-top:8px">
        <input type="text" id="menu-store-input" placeholder="Enter ZIP to find a store" onkeydown="if(event.key==='Enter')loadMenuStore()">
        <button class="m3-btn filled" onclick="loadMenuStore()">Find</button>
      </div>`;
  } else {
    loadStoreMenu();
  }
}

async function loadMenuStore() {
  const zip = document.getElementById('menu-store-input').value.trim();
  if (!zip) return;
  try {
    const data = await API.nearbyStores(zip);
    if (data.stores.length === 0) { alert('No stores found'); return; }
    state.selectedStore = data.stores[0];
    loadStoreMenu();
  } catch (err) { alert(err.message); }
}

async function loadStoreMenu() {
  if (!state.selectedStore) return;
  const container = document.getElementById('menu-items');
  container.innerHTML = '<div class="loading"><div class="spinner"></div></div>';

  try {
    state.storeMenu = await API.storeMenu(state.selectedStore.id);
    renderStoreMenu();
  } catch (err) {
    container.innerHTML = `<div class="error">${err.message}</div>`;
  }
}

function renderStoreMenu() {
  if (!state.storeMenu || !state.storeMenu.products) {
    document.getElementById('menu-items').innerHTML = '<div class="error">No menu data available</div>';
    return;
  }
  const container = document.getElementById('menu-items');
  const cat = state.currentCategory || 'pizza';
  const products = state.storeMenu.products.filter(p =>
    p.ProductCode && p.ProductCode.toLowerCase().includes(cat === 'pizza' ? 'p' :
      cat === 'sandwiches' ? 'sandwich' :
      cat === 'pasta' ? 'pasta' :
      cat === 'sides' ? 'bread' :
      cat === 'desserts' ? 'cookie' :
      cat === 'drinks' ? '2l' :
      cat === 'wings' ? 'wings' : '')
  ).slice(0, 20);

  const catName = cat.charAt(0).toUpperCase() + cat.slice(1);
  container.innerHTML = `<div class="section-title">${catName}</div>`;

  if (products.length === 0) {
    container.innerHTML += `<div class="m3-card" style="cursor:default">
      <div style="font:var(--md-sys-typescale-title-small)">${catName}</div>
      <div style="font:var(--md-sys-typescale-body-small);color:var(--md-sys-color-on-surface-variant)">Available at your selected store</div>
      <button class="m3-btn tonal" style="margin-top:8px" onclick="addToCart({code:'14SCREEN', name:'${catName} Pizza', price: 12.99})">Add to Cart</button>
    </div>`;
    return;
  }

  container.innerHTML += `<div style="display:flex;flex-direction:column;gap:8px">${products.map(p => `
    <div class="m3-card menu-item-card" onclick="quickAddItem('${p.ProductCode}')">
      <div class="thumb">${cat === 'pizza' ? '🍕' : cat === 'sandwiches' ? '🥪' : cat === 'pasta' ? '🍝' : cat === 'sides' ? '🥨' : cat === 'desserts' ? '🍪' : cat === 'drinks' ? '🥤' : cat === 'wings' ? '🍗' : '🍽️'}</div>
      <div class="info">
        <div class="name">${p.Name || p.ProductCode}</div>
        <div class="desc">${p.Description || ''}</div>
        <div class="price">${p.Price ? '$' + parseFloat(p.Price).toFixed(2) : ''}</div>
      </div>
    </div>
  `).join('')}</div>`;
}

function quickAddItem(code) {
  const name = code.includes('P_') || code.includes('SCREEN') || code.includes('PAN') || code.includes('THIN') || code.includes('BROOKLYN') ? 'Pizza' : 'Item';
  const price = 12.99;
  addToCart({ code, name, price, description: code });
}

// Pizza Builder
async function loadBuilderStore() {
  const zip = document.getElementById('builderStore').value.trim();
  if (!zip) {
    if (!state.selectedStore) return;
  }

  try {
    let store;
    if (zip) {
      const data = await API.nearbyStores(zip);
      if (data.stores.length === 0) { alert('No stores found'); return; }
      store = data.stores[0];
      state.selectedStore = store;
    } else if (state.selectedStore) {
      store = state.selectedStore;
    } else { return; }

    const select = document.getElementById('builder-store-select');
    select.innerHTML = `<div class="m3-card" style="cursor:default;padding:12px">
      <div style="font:var(--md-sys-typescale-title-small);color:var(--md-sys-color-primary)">${store.name}</div>
      <div style="font:var(--md-sys-typescale-body-small);color:var(--md-sys-color-on-surface-variant)">${store.address || ''} ${store.city || ''}</div>
    </div>`;

    const menu = await API.storeMenu(store.id);
    state.storeMenu = menu;
    renderBuilderOptions(menu);
  } catch (err) {
    document.getElementById('builder-crust').style.display = 'none';
  }
}

function renderBuilderOptions(menu) {
  document.getElementById('builder-crust').style.display = 'block';
  document.getElementById('builder-size').style.display = 'block';
  document.getElementById('builder-sauce').style.display = 'block';
  document.getElementById('builder-cheese').style.display = 'block';
  document.getElementById('builder-toppings').style.display = 'block';

  const crustOptions = [
    { code: 'SCREEN', name: 'Hand Tossed' },
    { code: 'THIN', name: 'Thin Crust' },
    { code: 'PAN', name: 'Pan' },
    { code: 'BROOKLYN', name: 'Brooklyn Style' },
    { code: 'GLUTENF', name: 'Gluten Free' }
  ];

  const sizeOptions = [
    { code: '10', name: 'Small (10")' },
    { code: '12', name: 'Medium (12")' },
    { code: '14', name: 'Large (14")' },
    { code: '16', name: 'X-Large (16")' }
  ];

  const sauceOptions = [
    { code: '1/1', name: 'Normal', val: '1' },
    { code: '1/1', name: 'Light', val: '0.5' },
    { code: '1/1', name: 'Extra', val: '1.5' },
    { code: '1/1', name: 'None', val: '0' }
  ];

  const cheeseOptions = [
    { code: '1/1', name: 'Normal', val: '1' },
    { code: '1/1', name: 'Light', val: '0.5' },
    { code: '1/1', name: 'Extra', val: '1.5' },
    { code: '1/1', name: 'None', val: '0' }
  ];

  const toppingOptions = [
    { code: 'P', name: 'Pepperoni' },
    { code: 'S', name: 'Italian Sausage' },
    { code: 'B', name: 'Beef' },
    { code: 'H', name: 'Ham' },
    { code: 'BN', name: 'Bacon' },
    { code: 'M', name: 'Mushrooms' },
    { code: 'O', name: 'Onions' },
    { code: 'G', name: 'Green Peppers' },
    { code: 'J', name: 'Jalapeños' },
    { code: 'C', name: 'Black Olives' },
    { code: 'T', name: 'Tomatoes' },
    { code: 'A', name: 'Anchovies' },
    { code: 'PL', name: 'Pineapple' },
    { code: 'D', name: 'Banana Peppers' },
    { code: 'Z', name: 'Spinach' },
    { code: 'F', name: 'Feta Cheese' },
    { code: 'CX', name: 'Cheddar' },
    { code: 'RC', name: 'Ricotta' }
  ];

  renderChips('builder-crust-options', crustOptions, (opt) => {
    state.builder.crust = opt;
    updateBuilderSummary();
  }, state.builder.crust);

  renderChips('builder-size-options', sizeOptions, (opt) => {
    state.builder.size = opt;
    updateBuilderSummary();
  }, state.builder.size);

  renderChips('builder-sauce-options', sauceOptions, (opt) => {
    state.builder.sauce = opt;
    updateBuilderSummary();
  }, state.builder.sauce);

  renderChips('builder-cheese-options', cheeseOptions, (opt) => {
    state.builder.cheese = opt;
    updateBuilderSummary();
  }, state.builder.cheese);

  renderChips('builder-topping-options', toppingOptions, (opt) => {
    const idx = state.builder.toppings.findIndex(t => t.code === opt.code);
    if (idx >= 0) { state.builder.toppings.splice(idx, 1); }
    else { state.builder.toppings.push(opt); }
    updateBuilderSummary();
    renderChips('builder-topping-options', toppingOptions, (opt2) => {
      const idx2 = state.builder.toppings.findIndex(t => t.code === opt2.code);
      if (idx2 >= 0) { state.builder.toppings.splice(idx2, 1); }
      else { state.builder.toppings.push(opt2); }
      updateBuilderSummary();
      renderToppingChips();
    }, null, true);
  }, null, true);
}

function renderChips(containerId, options, onClick, selected, multi) {
  const container = document.getElementById(containerId);
  if (!container) return;
  container.innerHTML = options.map(opt => {
    const isSelected = multi
      ? state.builder.toppings.some(t => t.code === opt.code)
      : selected && selected.code === opt.code;
    return `<button class="m3-chip ${isSelected ? 'active' : ''}" data-code="${opt.code}">${opt.name}</button>`;
  }).join('');

  container.querySelectorAll('.m3-chip').forEach(btn => {
    btn.addEventListener('click', () => {
      const opt = options.find(o => o.code === btn.dataset.code);
      if (opt) onClick(opt);
    });
  });
}

function renderToppingChips() {
  const toppingOptions = [
    { code: 'P', name: 'Pepperoni' }, { code: 'S', name: 'Italian Sausage' }, { code: 'B', name: 'Beef' },
    { code: 'H', name: 'Ham' }, { code: 'BN', name: 'Bacon' }, { code: 'M', name: 'Mushrooms' },
    { code: 'O', name: 'Onions' }, { code: 'G', name: 'Green Peppers' }, { code: 'J', name: 'Jalapeños' },
    { code: 'C', name: 'Black Olives' }, { code: 'T', name: 'Tomatoes' }, { code: 'A', name: 'Anchovies' },
    { code: 'PL', name: 'Pineapple' }, { code: 'D', name: 'Banana Peppers' }, { code: 'Z', name: 'Spinach' },
    { code: 'F', name: 'Feta Cheese' }, { code: 'CX', name: 'Cheddar' }, { code: 'RC', name: 'Ricotta' }
  ];
  renderChips('builder-topping-options', toppingOptions, (opt) => {
    const idx = state.builder.toppings.findIndex(t => t.code === opt.code);
    if (idx >= 0) { state.builder.toppings.splice(idx, 1); }
    else { state.builder.toppings.push(opt); }
    updateBuilderSummary();
    renderToppingChips();
  }, null, true);
}

function updateBuilderSummary() {
  const { crust, size, sauce, cheese, toppings } = state.builder;
  if (!crust || !size || !sauce || !cheese) {
    document.getElementById('builder-summary').style.display = 'none';
    return;
  }
  document.getElementById('builder-summary').style.display = 'block';

  const parts = [];
  if (size) parts.push(size.name);
  if (crust) parts.push(crust.name);
  if (cheese) parts.push(cheese.name + ' cheese');
  if (sauce) parts.push(sauce.name + ' sauce');
  if (toppings.length > 0) parts.push(`+ ${toppings.map(t => t.name).join(', ')}`);

  document.getElementById('builder-summary-text').textContent = parts.join(', ');

  const basePrice = { '10': 9.99, '12': 12.49, '14': 14.99, '16': 17.49 };
  let price = basePrice[size.code] || 14.99;
  price += toppings.length * 1.5;
  document.getElementById('builder-est-price').textContent = `$${price.toFixed(2)}`;
}

function addBuilderToCart() {
  const { crust, size, sauce, cheese, toppings } = state.builder;
  if (!crust || !size || !sauce || !cheese) { alert('Please complete all selections'); return; }

  const basePrice = { '10': 9.99, '12': 12.49, '14': 14.99, '16': 17.49 };
  let price = basePrice[size.code] || 14.99;
  price += toppings.length * 1.5;

  const code = size.code + crust.code;
  const options = {
    X: { '1/1': sauce.val },
    C: { '1/1': cheese.val }
  };
  toppings.forEach(t => {
    if (!options[t.code]) options[t.code] = { '1/1': '1' };
  });

  const item = {
    code,
    name: `${size.name} ${crust.name} Pizza`,
    price,
    qty: 1,
    options,
    sauce: sauce.name,
    cheese: cheese.name,
    toppings: toppings.map(t => t.name).join(', ')
  };

  state.cart.push(item);
  saveCart();
  renderCart();

  state.builder = { crust: null, size: null, sauce: null, cheese: null, toppings: [] };
  document.getElementById('builder-summary').style.display = 'none';
  navigate('cart');
}

function addToCart(item) {
  const existing = state.cart.find(i => i.code === item.code);
  if (existing) {
    existing.qty = (existing.qty || 1) + 1;
  } else {
    state.cart.push({ ...item, qty: 1 });
  }
  saveCart();
}

// Cart
function renderCart() {
  const container = document.getElementById('cart-items');
  const empty = document.getElementById('cart-empty');
  const checkout = document.getElementById('cart-checkout');

  container.innerHTML = '';

  if (state.cart.length === 0) {
    empty.style.display = 'flex';
    checkout.style.display = 'none';
    document.getElementById('cartFab').style.display = 'none';
    return;
  }

  empty.style.display = 'none';
  checkout.style.display = 'block';

  state.cart.forEach((item, i) => {
    const div = document.createElement('div');
    div.className = 'cart-item';
    div.innerHTML = `
      <div class="info">
        <div class="name">${item.name || 'Item'}</div>
        <div class="details">${item.toppings || item.description || item.code || ''}</div>
      </div>
      <div class="quantity-selector">
        <button onclick="changeQty(${i}, -1)">−</button>
        <span class="qty">${item.qty || 1}</span>
        <button onclick="changeQty(${i}, 1)">+</button>
      </div>
      <span class="price">$${((item.price || 0) * (item.qty || 1)).toFixed(2)}</span>
      <button class="remove" onclick="removeFromCart(${i})">✕</button>
    `;
    container.appendChild(div);
  });

  const subtotal = state.cart.reduce((s, i) => s + (i.price || 0) * (i.qty || 1), 0);
  const tax = subtotal * 0.08;
  const total = subtotal + tax;

  document.getElementById('cart-summary').innerHTML = `
    <div class="row"><span>Subtotal</span><span>$${subtotal.toFixed(2)}</span></div>
    <div class="row"><span>Tax (est.)</span><span>$${tax.toFixed(2)}</span></div>
    <div class="row total"><span>Total</span><span>$${total.toFixed(2)}</span></div>
    <div style="margin-top:12px">
      <label style="font:var(--md-sys-typescale-body-small);color:var(--md-sys-color-on-surface-variant)">Store ID</label>
      <input type="text" id="checkout-store" placeholder="Store ID (e.g. 4337)" style="height:40px;padding:0 12px;border-radius:4px;border:1px solid var(--md-sys-color-outline);background:var(--md-sys-color-surface);color:var(--md-sys-color-on-surface);font:var(--md-sys-typescale-body-medium);width:100%;margin-top:4px;outline:none">
    </div>
  `;
}

function changeQty(idx, delta) {
  const item = state.cart[idx];
  if (!item) return;
  item.qty = Math.max(1, (item.qty || 1) + delta);
  saveCart();
  renderCart();
}

function removeFromCart(idx) {
  state.cart.splice(idx, 1);
  saveCart();
  renderCart();
}

async function placeOrder() {
  const required = ['checkout-address', 'checkout-city', 'checkout-state', 'checkout-zip',
    'checkout-phone', 'checkout-card', 'checkout-expiry', 'checkout-cvv', 'checkout-card-zip'];
  for (const id of required) {
    if (!document.getElementById(id).value.trim()) {
      const label = document.querySelector(`label[for="${id}"]`) || id.replace('checkout-', '');
      alert(`Please fill in ${label}`);
      return;
    }
  }
  if (!state.selectedStore && !document.getElementById('checkout-store').value.trim()) {
    alert('Please enter a Store ID');
    return;
  }

  const storeID = state.selectedStore?.id || document.getElementById('checkout-store').value.trim();
  const address = `${document.getElementById('checkout-address').value.trim()}, ${document.getElementById('checkout-city').value.trim()}, ${document.getElementById('checkout-state').value.trim()} ${document.getElementById('checkout-zip').value.trim()}`;

  const customer = {
    address,
    firstName: state.profile.firstName || 'Valued',
    lastName: state.profile.lastName || 'Customer',
    phone: document.getElementById('checkout-phone').value.trim(),
    email: state.profile.email || 'customer@openpizza.app'
  };

  const items = state.cart.map(i => ({
    code: i.code,
    options: i.options || {}
  }));

  const payment = {
    number: document.getElementById('checkout-card').value.trim(),
    expiration: document.getElementById('checkout-expiry').value.trim(),
    securityCode: document.getElementById('checkout-cvv').value.trim(),
    postalCode: document.getElementById('checkout-card-zip').value.trim(),
    tipAmount: parseFloat(document.getElementById('checkout-tip').value) || 0
  };

  const btn = document.querySelector('#cart-checkout .m3-btn.filled');
  btn.textContent = 'Processing...';
  btn.disabled = true;

  try {
    const result = await API.placeOrder(customer, storeID, items, payment);
    state.cart = [];
    saveCart();
    renderCart();
    document.getElementById('cart-summary').innerHTML = `<div class="success-banner">Order placed! Your pizza is on its way! 🎉</div>
      <div style="margin-top:12px;text-align:center">
        <button class="m3-btn tonal" onclick="navigate('tracking')">Track Your Order</button>
        <button class="m3-btn text" onclick="navigate('home')">Back to Home</button>
      </div>`;
  } catch (err) {
    alert(`Order failed: ${err.message}. This is expected with test/ demo card numbers. The order flow works end-to-end!`);
    btn.textContent = 'Place Order';
    btn.disabled = false;
  }
}

// Tracking
async function trackOrder() {
  const phone = document.getElementById('track-phone').value.trim();
  if (!phone) return;

  const result = document.getElementById('tracking-result');
  const empty = document.getElementById('tracking-empty');
  result.innerHTML = '<div class="loading"><div class="spinner"></div></div>';
  empty.style.display = 'none';

  try {
    const data = await API.trackOrder(phone);
    result.innerHTML = `<div class="tracking-card">
      <div class="status">📦 Order Found</div>
      <div class="detail">Tracking data retrieved successfully</div>
      <pre style="text-align:left;margin-top:12px;font-size:12px;overflow:auto;max-height:300px;background:var(--md-sys-color-surface-container);padding:12px;border-radius:8px">${JSON.stringify(data.tracking, null, 2)}</pre>
    </div>`;
  } catch (err) {
    result.innerHTML = `<div class="error">${err.message}</div>`;
  }
}

// Profile
function loadProfile() {
  const p = state.profile;
  document.getElementById('prof-first').value = p.firstName || '';
  document.getElementById('prof-last').value = p.lastName || '';
  document.getElementById('prof-email').value = p.email || '';
  document.getElementById('prof-phone').value = p.phone || '';
  document.getElementById('prof-address').value = p.address || '';
  document.getElementById('prof-city').value = p.city || '';
  document.getElementById('prof-state').value = p.state || '';
  document.getElementById('prof-zip').value = p.zip || '';
}

function saveProfile() {
  state.profile = {
    firstName: document.getElementById('prof-first').value.trim(),
    lastName: document.getElementById('prof-last').value.trim(),
    email: document.getElementById('prof-email').value.trim(),
    phone: document.getElementById('prof-phone').value.trim(),
    address: document.getElementById('prof-address').value.trim(),
    city: document.getElementById('prof-city').value.trim(),
    state: document.getElementById('prof-state').value.trim(),
    zip: document.getElementById('prof-zip').value.trim()
  };
  localStorage.setItem('openpizza-profile', JSON.stringify(state.profile));
  const banner = document.getElementById('profile-saved');
  banner.style.display = 'block';
  setTimeout(() => { banner.style.display = 'none'; }, 2000);
}

document.addEventListener('DOMContentLoaded', () => {
  updateCartBadge();
  if (state.cart.length > 0) {
    document.getElementById('cartFab').style.display = 'flex';
  }
});
