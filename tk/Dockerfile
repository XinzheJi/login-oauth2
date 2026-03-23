ARG NODE_IMAGE=node:18-alpine
ARG NGINX_IMAGE=nginx:1.25-alpine

FROM ${NODE_IMAGE} AS build
WORKDIR /app

COPY package*.json ./
RUN npm install && chmod +x node_modules/.bin/*

COPY . .
RUN npm run build

FROM ${NGINX_IMAGE}
WORKDIR /usr/share/nginx/html

COPY --from=build /app/dist ./

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
