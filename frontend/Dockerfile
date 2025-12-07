FROM node:20-alpine AS build
WORKDIR /app
ARG API_URL=http://localhost:8080/api
ENV API_URL=$API_URL
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build -- --configuration production

FROM nginx:alpine
COPY --from=build /app/dist/rate-limiter-frontend /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]

