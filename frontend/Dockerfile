FROM node:22-alpine AS build
WORKDIR /app
COPY package.json ./
RUN npm install --no-package-lock
COPY . .
RUN npm run build

FROM nginx:1.27-alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf.template /etc/nginx/templates/default.conf.template
EXPOSE 10000
