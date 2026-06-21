import { decodeClaims, isExpired } from './jwt.util';

// Monta um JWT de mentira (header.payload.assinatura) com o payload em base64url.
// A assinatura é irrelevante: o decode não a verifica.
function makeToken(payload: object): string {
  const header = btoa(JSON.stringify({ alg: 'RS256', typ: 'JWT' }));
  const body = btoa(JSON.stringify(payload))
    .replace(/\+/g, '-')
    .replace(/\//g, '_')
    .replace(/=+$/, '');
  return `${header}.${body}.assinatura`;
}

describe('jwt.util', () => {
  describe('decodeClaims', () => {
    it('decodifica um token bem formado', () => {
      const token = makeToken({
        sub: '1',
        upn: 'admin@unifor.br',
        groups: ['admin'],
        mustChangePassword: true,
        exp: 1893456000,
      });

      const claims = decodeClaims(token);

      expect(claims).not.toBeNull();
      expect(claims!.sub).toBe('1');
      expect(claims!.upn).toBe('admin@unifor.br');
      expect(claims!.groups).toEqual(['admin']);
      expect(claims!.mustChangePassword).toBeTrue();
      expect(claims!.exp).toBe(1893456000);
    });

    it('normaliza groups ausente para uma lista vazia', () => {
      const token = makeToken({ sub: '1', upn: 'x', mustChangePassword: false, exp: 1893456000 });
      expect(decodeClaims(token)!.groups).toEqual([]);
    });

    it('devolve null quando o token não tem três partes', () => {
      expect(decodeClaims('abc')).toBeNull();
      expect(decodeClaims('a.b')).toBeNull();
    });

    it('devolve null quando o payload não é JSON válido', () => {
      expect(decodeClaims('aaa.@@@.bbb')).toBeNull();
    });
  });

  describe('isExpired', () => {
    it('é true quando exp está no passado', () => {
      const claims = { sub: '1', upn: 'x', groups: [], mustChangePassword: false, exp: 1000 };
      expect(isExpired(claims, 2000)).toBeTrue();
    });

    it('é false quando exp está no futuro', () => {
      const claims = { sub: '1', upn: 'x', groups: [], mustChangePassword: false, exp: 5000 };
      expect(isExpired(claims, 2000)).toBeFalse();
    });
  });
});
