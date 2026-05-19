import { useEffect, useMemo, useState } from 'react';

type RuntimeContextPayload = {
  exports?: {
    orderId?: string | number;
    userId?: string | number;
    status?: string;
  };
};

type Product = {
  id: string;
  name: string;
  description: string;
  price: string;
};

const RUNTIME_CONTEXT_URL =
  '/test-data/runtime/poc-runtime-bridge/cypress-runtime-context-poc-REQ-POC-001.json';

const DEMO_PRODUCTS: Product[] = [
  {
    id: 'keyboard-pro',
    name: 'Keyboard Pro Mechanical',
    description: 'Compact keyboard with tactile switches and a soft wrist rest.',
    price: '$89.00'
  },
  {
    id: 'mouse-air',
    name: 'Mouse Air Wireless',
    description: 'Lightweight wireless mouse with silent clicks.',
    price: '$39.00'
  },
  {
    id: 'monitor-plus',
    name: 'Monitor Plus 4K',
    description: '27-inch 4K display with crisp color and slim bezels.',
    price: '$299.00'
  },
  {
    id: 'dock-core',
    name: 'Dock Core USB-C',
    description: 'Single-cable dock with power passthrough and extra ports.',
    price: '$129.00'
  },
  {
    id: 'desk-mat',
    name: 'Desk Mat Studio',
    description: 'Large desk mat with stitched edges and a soft finish.',
    price: '$24.00'
  }
];

type CartState = Record<string, number>;

function toDisplayValue(value: string | number | undefined, fallback: string): string {
  if (value === undefined || value === null) {
    return fallback;
  }

  const text = String(value).trim();
  return text.length > 0 ? text : fallback;
}

function App() {
  const [isLoading, setIsLoading] = useState(true);
  const [missingFile, setMissingFile] = useState(false);
  const [orderId, setOrderId] = useState('Unavailable');
  const [userId, setUserId] = useState('Unavailable');
  const [status, setStatus] = useState('CREATED');
  const [searchTerm, setSearchTerm] = useState('');
  const [cart, setCart] = useState<CartState>({});

  const normalizedSearch = searchTerm.trim().toLowerCase();

  const filteredProducts = useMemo(() => {
    if (!normalizedSearch) {
      return DEMO_PRODUCTS;
    }

    return DEMO_PRODUCTS.filter((product) => {
      return (
        product.name.toLowerCase().includes(normalizedSearch) ||
        product.description.toLowerCase().includes(normalizedSearch)
      );
    });
  }, [normalizedSearch]);

  const cartEntries = useMemo(() => {
    return DEMO_PRODUCTS
      .filter((product) => (cart[product.id] ?? 0) > 0)
      .map((product) => ({
        product,
        quantity: cart[product.id]
      }))
      .filter((entry): entry is { product: Product; quantity: number } => Boolean(entry.quantity));
  }, [cart]);

  const cartCount = useMemo(() => {
    return Object.values(cart).reduce((total, quantity) => total + quantity, 0);
  }, [cart]);

  useEffect(() => {
    let cancelled = false;

    async function loadRuntimeContext() {
      setIsLoading(true);
      setMissingFile(false);

      try {
        const response = await fetch(RUNTIME_CONTEXT_URL, {
          cache: 'no-store'
        });

        if (!response.ok) {
          if (response.status === 404) {
            if (!cancelled) {
              setMissingFile(true);
              setOrderId('Unavailable');
              setUserId('Unavailable');
              setStatus('CREATED');
              setIsLoading(false);
            }
            return;
          }

          throw new Error(`Unexpected response status: ${response.status}`);
        }

        const payload = (await response.json()) as RuntimeContextPayload;

        if (!cancelled) {
          setOrderId(toDisplayValue(payload.exports?.orderId, 'Unavailable'));
          setUserId(toDisplayValue(payload.exports?.userId, 'Unavailable'));
          setStatus(toDisplayValue(payload.exports?.status, 'CREATED'));
          setIsLoading(false);
        }
      } catch {
        if (!cancelled) {
          setMissingFile(true);
          setOrderId('Unavailable');
          setUserId('Unavailable');
          setStatus('CREATED');
          setIsLoading(false);
        }
      }
    }

    void loadRuntimeContext();

    return () => {
      cancelled = true;
    };
  }, []);

  function addToCart(productId: string) {
    setCart((current) => ({
      ...current,
      [productId]: (current[productId] ?? 0) + 1
    }));
  }

  return (
    <div className="page-shell">
      <header className="hero">
        <div className="hero-copy">
          <p className="eyebrow">Deterministic demo shop</p>
          <h1 data-testid="page-title">Unified QA demo shop</h1>
          <p className="hero-text">
            A small, local, visually watchable UI target for Cypress tests and runtime bridge checks.
          </p>
        </div>
        <div className="hero-metrics" data-testid="demo-metrics">
          <div className="metric-card">
            <span className="metric-label">Products</span>
            <strong>{DEMO_PRODUCTS.length}</strong>
          </div>
          <div className="metric-card">
            <span className="metric-label">Search term</span>
            <strong>{searchTerm.trim() || 'All products'}</strong>
          </div>
          <div className="metric-card">
            <span className="metric-label">Cart items</span>
            <strong data-testid="cart-count">{cartCount}</strong>
          </div>
        </div>
      </header>

      <main className="content-grid">
        <section className="panel demo-panel" data-testid="demo-shop">
          <div className="panel-header">
            <div>
              <p className="section-label">Demo shop</p>
              <h2>Search and add a product</h2>
            </div>
            <div className="search-wrap">
              <label className="sr-only" htmlFor="demo-search-input">
                Search products
              </label>
              <input
                id="demo-search-input"
                data-testid="demo-search-input"
                type="search"
                value={searchTerm}
                onChange={(event) => setSearchTerm(event.target.value)}
                placeholder="Search products"
              />
            </div>
          </div>

          <div className="demo-layout">
            <div className="product-grid" data-testid="product-grid">
              {filteredProducts.length === 0 ? (
                <div className="empty-state" data-testid="product-empty-state">
                  No products match your search.
                </div>
              ) : (
                filteredProducts.map((product) => (
                  <article
                    key={product.id}
                    className="product-card"
                    data-testid={`product-card-${product.id}`}
                  >
                    <div className="product-copy">
                      <h3 data-testid={`product-name-${product.id}`}>{product.name}</h3>
                      <p>{product.description}</p>
                    </div>
                    <div className="product-footer">
                      <strong>{product.price}</strong>
                      <button
                        type="button"
                        data-testid={`add-to-cart-${product.id}`}
                        onClick={() => addToCart(product.id)}
                      >
                        Add to cart
                      </button>
                    </div>
                  </article>
                ))
              )}
            </div>

            <aside className="cart-panel" data-testid="cart-summary">
              <div className="panel-header cart-header">
                <div>
                  <p className="section-label">Cart summary</p>
                  <h2>Current selection</h2>
                </div>
                <span className="cart-pill" data-testid="cart-item-total">
                  {cartCount} item{cartCount === 1 ? '' : 's'}
                </span>
              </div>

              {cartEntries.length === 0 ? (
                <p className="empty-state" data-testid="cart-summary-empty">
                  Your cart is empty.
                </p>
              ) : (
                <ul className="cart-lines" data-testid="cart-summary-lines">
                  {cartEntries.map(({ product, quantity }) => (
                    <li key={product.id} data-testid={`cart-line-${product.id}`}>
                      <span>{product.name}</span>
                      <span>Qty {quantity}</span>
                      <span>{product.price}</span>
                    </li>
                  ))}
                </ul>
              )}
            </aside>
          </div>
        </section>

        <section
          data-testid="order-card"
          className="panel runtime-panel"
          aria-label="Runtime context viewer"
        >
          <div className="panel-header">
            <div>
              <p className="section-label">Runtime bridge</p>
              <h2>Hydrated order context</h2>
            </div>
          </div>

          {isLoading ? (
            <p data-testid="runtime-state">Loading runtime context...</p>
          ) : missingFile ? (
            <p data-testid="runtime-state">Runtime context file is not available.</p>
          ) : (
            <p data-testid="runtime-state">Runtime context loaded.</p>
          )}

          <dl className="runtime-details">
            <div>
              <dt>Order ID</dt>
              <dd data-testid="order-id">{orderId}</dd>
            </div>
            <div>
              <dt>User ID</dt>
              <dd data-testid="user-id">{userId}</dd>
            </div>
            <div>
              <dt>Status</dt>
              <dd data-testid="order-status">{status}</dd>
            </div>
          </dl>
        </section>
      </main>
    </div>
  );
}

export default App;
