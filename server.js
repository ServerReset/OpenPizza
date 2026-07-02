import express from 'express';
import cors from 'cors';
import { Address, NearbyStores, Store, Menu, Customer, Item, Order, Payment, Tracking, Image } from 'dominos';

const app = express();
const PORT = process.env.PORT || 3000;

app.use(cors());
app.use(express.json());
app.use(express.static('public'));

app.post('/api/stores/nearby', async (req, res) => {
  try {
    const { address } = req.body;
    if (!address) return res.status(400).json({ error: 'Address is required' });

    const nearbyStores = await new NearbyStores(address);
    const stores = nearbyStores.stores.map(s => ({
      id: s.StoreID,
      name: s.StoreName || `Store #${s.StoreID}`,
      address: s.AddressDescription || s.StreetName,
      city: s.City,
      state: s.Region,
      zip: s.PostalCode,
      phone: s.Phone,
      hours: s.OpeningTime,
      isDelivery: s.IsDeliveryStore,
      isOpen: s.IsOpen,
      isOnlineCapable: s.IsOnlineCapable,
      serviceOpen: s.ServiceIsOpen,
      distance: s.MinDistance,
      lat: s.Latitude,
      lng: s.Longitude
    }));

    res.json({ stores });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.get('/api/stores/:id/menu', async (req, res) => {
  try {
    const menu = await new Menu(req.params.id);
    res.json(menu);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.get('/api/stores/:id/info', async (req, res) => {
  try {
    const store = await new Store(req.params.id);
    res.json(store);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.post('/api/order/validate', async (req, res) => {
  try {
    const { customer, storeID, items } = req.body;
    if (!customer || !storeID || !items?.length) {
      return res.status(400).json({ error: 'Customer, storeID, and items are required' });
    }

    const cust = new Customer(customer);
    const order = new Order(cust);
    order.storeID = storeID;

    for (const item of items) {
      const pizza = new Item({ code: item.code, options: item.options || {} });
      order.addItem(pizza);
    }

    await order.validate();
    await order.price();

    res.json({
      validated: true,
      amounts: order.amountsBreakdown,
      order: order
    });
  } catch (err) {
    res.status(500).json({ error: err.message, response: err.validationResponse || err.priceResponse });
  }
});

app.post('/api/order/place', async (req, res) => {
  try {
    const { customer, storeID, items, payment } = req.body;
    if (!customer || !storeID || !items?.length || !payment) {
      return res.status(400).json({ error: 'Customer, storeID, items, and payment are required' });
    }

    const cust = new Customer(customer);
    const order = new Order(cust);
    order.storeID = storeID;

    for (const item of items) {
      const pizza = new Item({ code: item.code, options: item.options || {} });
      order.addItem(pizza);
    }

    await order.validate();
    await order.price();

    const card = new Payment({
      amount: order.amountsBreakdown.customer,
      number: payment.number,
      expiration: payment.expiration,
      securityCode: payment.securityCode,
      postalCode: payment.postalCode,
      tipAmount: payment.tipAmount || 0
    });

    order.payments.push(card);
    await order.place();

    res.json({
      placed: true,
      order: order,
      amounts: order.amountsBreakdown
    });
  } catch (err) {
    res.status(500).json({
      error: err.message,
      response: err.placeOrderResponse || err.validationResponse || err.priceResponse
    });
  }
});

app.post('/api/tracking', async (req, res) => {
  try {
    const { phone } = req.body;
    if (!phone) return res.status(400).json({ error: 'Phone number is required' });

    const tracking = new Tracking();
    const result = await tracking.byPhone(phone);
    res.json({ tracking: result });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.get('/api/products/:code/image', async (req, res) => {
  try {
    const img = await new Image(req.params.code);
    res.json({ code: req.params.code, image: img.base64Image });
  } catch (err) {
    res.status(404).json({ error: 'Image not found' });
  }
});

app.get('/api/menu/categories', async (req, res) => {
  const categories = [
    { id: 'pizza', name: 'Pizza', icon: '🍕', description: 'Custom & specialty pizzas' },
    { id: 'sandwiches', name: 'Sandwiches', icon: '🥪', description: 'Oven-baked sandwiches' },
    { id: 'pasta', name: 'Pasta', icon: '🍝', description: 'Pasta bowls' },
    { id: 'sides', name: 'Sides', icon: '🥨', description: 'Sides & bread' },
    { id: 'desserts', name: 'Desserts', icon: '🍪', description: 'Sweet treats' },
    { id: 'drinks', name: 'Drinks', icon: '🥤', description: 'Beverages' },
    { id: 'salads', name: 'Salads', icon: '🥗', description: 'Fresh salads' },
    { id: 'wings', name: 'Wings', icon: '🍗', description: 'Chicken wings' }
  ];
  res.json({ categories });
});

app.listen(PORT, () => {
  console.log(`OpenPizza server running on http://localhost:${PORT}`);
});
