FROM node:22-alpine AS build
WORKDIR /build
COPY package*.json ./
RUN npm ci --ignore-scripts
COPY . .
RUN npm run typecheck && npm run build
FROM nginx:alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /build/dist /usr/share/nginx/html
EXPOSE 80
