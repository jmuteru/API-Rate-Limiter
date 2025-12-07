function getApiUrl(): string {
  if ((window as any).__APP_CONFIG__?.apiUrl) {
    return (window as any).__APP_CONFIG__.apiUrl;
  }
  return 'https://rate-limiter-backend.onrender.com/api';
}

export const environment = {
  production: true,
  apiUrl: getApiUrl()
};

