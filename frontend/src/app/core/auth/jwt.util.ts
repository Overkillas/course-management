import { AuthClaims } from './auth.models';

// Decodifica o payload (segunda parte) de um JWT para os claims que o front usa.
// Função pura, sem dependência de Angular, para ser fácil de testar. Devolve null
// se o token não for um JWT de três partes ou se o payload não for o esperado.
// Serve apenas para a UI ler o papel, o mustChangePassword e a expiração.
export function decodeClaims(token: string): AuthClaims | null {
  const parts = token.split('.');
  if (parts.length !== 3) {
    return null;
  }

  try {
    const raw = JSON.parse(base64UrlDecode(parts[1]));
    if (typeof raw.sub !== 'string' || typeof raw.exp !== 'number') {
      return null;
    }
    return {
      sub: raw.sub,
      upn: typeof raw.upn === 'string' ? raw.upn : '',
      groups: Array.isArray(raw.groups) ? raw.groups : [],
      mustChangePassword: raw.mustChangePassword === true,
      exp: raw.exp,
    };
  } catch {
    return null;
  }
}

// Verifica se o token já expirou, comparando o claim exp (em segundos) com agora.
export function isExpired(
  claims: AuthClaims,
  nowSeconds: number = Math.floor(Date.now() / 1000),
): boolean {
  return claims.exp <= nowSeconds;
}

// Decodifica uma string base64url (variante do base64 usada em JWT) para texto,
// tratando o conteúdo como UTF-8.
function base64UrlDecode(input: string): string {
  const base64 = input.replace(/-/g, '+').replace(/_/g, '/');
  const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=');
  const binary = atob(padded);
  const bytes = Uint8Array.from(binary, (char) => char.charCodeAt(0));
  return new TextDecoder().decode(bytes);
}
