const API = {
  async nearbyStores(address) {
    const r = await fetch('/api/stores/nearby', {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ address })
    });
    if (!r.ok) { const e = await r.json(); throw new Error(e.error); }
    return r.json();
  },

  async storeMenu(id) {
    const r = await fetch(`/api/stores/${id}/menu`);
    if (!r.ok) throw new Error('Failed to load menu');
    return r.json();
  },

  async storeInfo(id) {
    const r = await fetch(`/api/stores/${id}/info`);
    if (!r.ok) throw new Error('Failed to load store');
    return r.json();
  },

  async validateOrder(customer, storeID, items) {
    const r = await fetch('/api/order/validate', {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ customer, storeID, items })
    });
    if (!r.ok) { const e = await r.json(); throw new Error(e.error); }
    return r.json();
  },

  async placeOrder(customer, storeID, items, payment) {
    const r = await fetch('/api/order/place', {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ customer, storeID, items, payment })
    });
    if (!r.ok) { const e = await r.json(); throw new Error(e.error); }
    return r.json();
  },

  async trackOrder(phone) {
    const r = await fetch('/api/tracking', {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ phone })
    });
    if (!r.ok) { const e = await r.json(); throw new Error(e.error); }
    return r.json();
  },

  async getCategories() {
    const r = await fetch('/api/menu/categories');
    return r.json();
  }
};
