import React from 'react';
import ReactDOM from 'react-dom/client';
import { Provider } from 'react-redux';
import { BrowserRouter } from 'react-router-dom';
import { CssBaseline, ThemeProvider, createTheme } from '@mui/material';
import App from './App.jsx';
import { store } from './app/store.js';
import { NotificationProvider } from './components/NotificationProvider.jsx';
import './styles/index.css';

const theme = createTheme({
  palette: {
    mode: 'dark',
    primary: { main: '#22c55e' },
    background: { default: '#06110c', paper: 'rgba(8, 17, 13, .72)' },
  },
  shape: { borderRadius: 8 },
  typography: { fontFamily: 'Inter, system-ui, sans-serif' },
});

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <Provider store={store}>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <NotificationProvider>
          <BrowserRouter>
            <App />
          </BrowserRouter>
        </NotificationProvider>
      </ThemeProvider>
    </Provider>
  </React.StrictMode>,
);
