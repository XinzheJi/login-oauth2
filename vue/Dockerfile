FROM node:18-alpine AS build
WORKDIR /app

COPY package*.json ./
RUN npm install && chmod +x node_modules/.bin/*

COPY . .
RUN npm run build

FROM nginx:1.25-alpine
WORKDIR /usr/share/nginx/html

COPY --from=build /app/dist ./
COPY ./nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]

