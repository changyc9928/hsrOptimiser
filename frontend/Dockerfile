FROM node:20-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . ./
ARG VITE_INVENTORY_URL=/api/inventory
ARG VITE_ENGINE_URL=/api/engine
ENV VITE_INVENTORY_URL=${VITE_INVENTORY_URL}
ENV VITE_ENGINE_URL=${VITE_ENGINE_URL}
RUN npm run build

FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
