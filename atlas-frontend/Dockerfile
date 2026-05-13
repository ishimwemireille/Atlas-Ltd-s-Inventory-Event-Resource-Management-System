# ── Stage 1: Build the React app with Node ────────────────────────────────────
FROM node:20-alpine AS build

WORKDIR /app

# Copy package files first so npm install layer is cached
COPY package*.json ./
RUN npm install

# Pass API URL as a build argument (Nginx proxy uses relative /api)
ARG VITE_API_URL=/api
ENV VITE_API_URL=$VITE_API_URL

# Copy source and build
COPY . .
RUN npm run build

# ── Stage 2: Serve the built app with Nginx ───────────────────────────────────
FROM nginx:alpine

# Copy the built React files
COPY --from=build /app/dist /usr/share/nginx/html

# Copy custom Nginx config (handles React Router + /api proxy)
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
