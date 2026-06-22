# ---- Build ----
# Builda os estáticos do Angular dentro do container, para que `docker compose up --build`
# faça tudo do zero, sem exigir um npm install/ng build local antes.
FROM node:22-alpine AS build

# URL da API "assada" no build. O compose passa http://localhost:${APP_PORT}; o default
# cobre um build local sem arg.
ARG API_BASE_URL=http://localhost:8080

WORKDIR /build
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
# Injeta a URL da API no environment de produção antes do build, para o front respeitar
# o APP_PORT do .env.
RUN sed -i "s|apiUrl: '.*'|apiUrl: '${API_BASE_URL}'|" src/environments/environment.ts
RUN npm run build

# ---- Runtime ----
# Serve os estáticos com nginx. A config de SPA devolve o index.html para qualquer rota,
# para que as rotas client-side do Angular funcionem ao recarregar a página.
FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /build/dist/frontend/browser /usr/share/nginx/html
EXPOSE 80
