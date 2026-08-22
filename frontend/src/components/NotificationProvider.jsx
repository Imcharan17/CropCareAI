import { Alert, Snackbar } from '@mui/material';
import { createContext, useCallback, useContext, useMemo, useState } from 'react';

const NotificationContext = createContext(null);

export function getApiErrorMessage(error, fallback = 'Something went wrong. Please try again.') {
  const { data, status } = error?.response || {};

  if (typeof data?.message === 'string') return data.message;
  if (typeof data === 'string' && data.trim()) return data;
  if (status === 502 || status === 503 || status === 504) {
    return 'The server is starting. Please wait a moment and try again.';
  }
  if (!error?.response && error?.message === 'Network Error') {
    return 'Unable to reach the server. Check your connection and try again.';
  }

  return fallback;
}

export function NotificationProvider({ children }) {
  const [notification, setNotification] = useState(null);

  const notify = useCallback((message, severity = 'info') => {
    setNotification({ id: Date.now(), message, severity });
  }, []);

  const value = useMemo(() => ({ notify }), [notify]);

  return (
    <NotificationContext.Provider value={value}>
      {children}
      <Snackbar
        anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
        autoHideDuration={5000}
        key={notification?.id}
        open={Boolean(notification)}
        onClose={(_, reason) => reason !== 'clickaway' && setNotification(null)}
      >
        <Alert
          elevation={6}
          onClose={() => setNotification(null)}
          severity={notification?.severity || 'info'}
          variant="filled"
          sx={{ maxWidth: 420, alignItems: 'center' }}
        >
          {notification?.message}
        </Alert>
      </Snackbar>
    </NotificationContext.Provider>
  );
}

export function useNotification() {
  const context = useContext(NotificationContext);

  if (!context) {
    throw new Error('useNotification must be used inside NotificationProvider');
  }

  return context;
}
