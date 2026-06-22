export const environment = {
  // URL base da API em produção. O front é um container próprio, cross-origin com a API,
  // então o browser chama a API na porta publicada pelo compose. Este é só o default: no
  // build via Docker, o Dockerfile substitui este valor pelo arg API_BASE_URL (montado a
  // partir de APP_PORT no compose), então mudar APP_PORT no .env já propaga no --build. Em
  // desenvolvimento vale o environment.development.ts (trocado pelo angular.json).
  apiUrl: 'http://localhost:8080',
};
