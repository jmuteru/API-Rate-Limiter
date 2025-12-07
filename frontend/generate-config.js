

const fs = require('fs');
const path = require('path');

const apiUrl = process.env.API_URL || 'https://rate-limiter-backend.onrender.com/api';
const publicDir = path.join(__dirname, 'public');
const configFile = path.join(publicDir, 'config.js');

// ensure public directory exists
if (!fs.existsSync(publicDir)) {
  fs.mkdirSync(publicDir, { recursive: true });
}

const configContent = `// Runtime configuration for the application
// This file is generated automatically from environment variables
window.__APP_CONFIG__ = window.__APP_CONFIG__ || {
  apiUrl: '${apiUrl}'
};
`;

fs.writeFileSync(configFile, configContent, 'utf8');
console.log(`Generated config.js with API URL: ${apiUrl}`);

