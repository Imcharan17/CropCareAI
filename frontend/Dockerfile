FROM node:22-alpine AS build
WORKDIR /app
COPY package.json ./
RUN npm install --no-package-lock
COPY . .
RUN npm run build

FROM nginx:1.27-alpine
ENV NGINX_ENTRYPOINT_LOCAL_RESOLVERS=1
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf.template /etc/nginx/templates/default.conf.template
EXPOSE 10000
