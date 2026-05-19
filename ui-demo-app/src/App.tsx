import { useEffect, useState } from 'react';

type RuntimeContextPayload = {
  exports?: {
    orderId?: string | number;
    userId?: string | number;
    status?: string;
  };
};

const RUNTIME_CONTEXT_URL = '/test-data/runtime/poc-runtime-bridge/cypress-runtime-context-poc-REQ-POC-001.json';

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

  return (
    <div
      style={{
        fontFamily: 'Arial, sans-serif',
        padding: '40px',
        backgroundColor: '#0f172a',
        minHeight: '100vh',
        color: '#e2e8f0'
      }}
    >
      <h1 data-testid="page-title">Runtime Context Order Viewer</h1>

      <div
        data-testid="order-card"
        style={{
          marginTop: '24px',
          padding: '24px',
          borderRadius: '12px',
          backgroundColor: '#1e293b',
          width: '400px'
        }}
      >
        {isLoading ? (
          <p data-testid="runtime-state">Loading runtime context...</p>
        ) : missingFile ? (
          <p data-testid="runtime-state">Runtime context file is not available.</p>
        ) : (
          <p data-testid="runtime-state">Runtime context loaded.</p>
        )}

        <p>
          <strong>Order ID:</strong>{' '}
          <span data-testid="order-id">{orderId}</span>
        </p>

        <p>
          <strong>User ID:</strong>{' '}
          <span data-testid="user-id">{userId}</span>
        </p>

        <p>
          <strong>Status:</strong>{' '}
          <span data-testid="order-status">{status}</span>
        </p>
      </div>
    </div>
  );
}

export default App;
