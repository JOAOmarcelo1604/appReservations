package br.com.dev.jm.web.reservas.security;


import br.com.dev.jm.web.reservas.entity.Customer;
import jakarta.servlet.http.Cookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.io.IOException;
import java.security.Key;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import io.jsonwebtoken.ExpiredJwtException;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

public class TokenUtil {

    // aqui vamos definir alguns parametros de configuracao do token
    // - duracao
    // - emissor
    // - chave

    public static final long SEGUNDOS   = 1000;
    public static final long MINUTOS    = 60 * SEGUNDOS;
    public static final long HORAS      = 60 * MINUTOS;
    public static final long DIAS       = 24 * HORAS;
    public static final long EXPIRATION = 3 * HORAS;

    public static final String ISSUER   = "*IsiFLIX*";

    public static final String SECRET_KEY = "0123456789012345678901234567890123";

    public static final String PREFIX = "Bearer ";


    public static UsuarioToken encode(Customer customer) {
        Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
        String jws = Jwts.builder()
                .setSubject(customer.getEmail())
                .setIssuer(ISSUER)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // --- CORREÇÃO AQUI ---
        // Em vez de 'new UsuarioToken(...)', usamos o Builder para preencher tudo
        return UsuarioToken.builder()
                .token(PREFIX + jws)
                .customerId(customer.getId())
                .fullName(customer.getFullName())
                .email(customer.getEmail())
                .phoneNumber(customer.getPhoneNumber())
                // Converte Role para String com segurança (caso seja null)
                .role(customer.getRole() != null ? customer.getRole().toString() : null)
                .build();
    }



    public static Authentication decode(HttpServletRequest request) {
        String token = null;

        // 1. TENTATIVA 1: Buscar no Cookie (Prioridade para o Browser)
        if (request.getCookies() != null) {
            token = Arrays.stream(request.getCookies())
                    .filter(c -> "auth_token".equals(c.getName())) // Procura o cookie 'auth_token'
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }

        // 2. TENTATIVA 2: Buscar no Header (Fallback para Insomnia/Mobile)
        if (token == null) {
            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith(PREFIX)) {
                token = header.replace(PREFIX, ""); // Remove "Bearer "
            }
        }

        // Se não achou token em lugar nenhum, retorna null
        if (token == null) {
            return null;
        }

        // 3. Chama o método que faz a validação real
        return getAuthentication(token);
    }

    // Método auxiliar que valida a String do token (independente de onde veio)
    public static Authentication getAuthentication(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY.getBytes())
                    .build()
                    .parseClaimsJws(token);

            String subject = claims.getBody().getSubject();
            String issuer = claims.getBody().getIssuer();
            Date exp = claims.getBody().getExpiration();

            if (isValid(subject, issuer, exp)) {
                // Sucesso! Retorna o usuário autenticado
                return new UsernamePasswordAuthenticationToken(subject, null, Collections.emptyList());
            }
        } catch (ExpiredJwtException e) {
            // CASO 1: Token Vencido.
            // Isso é normal (sessão expirou). Não fazemos print no console.
            // Apenas retornamos null, e o filtro entenderá que é um usuário anônimo.
            return null;

        } catch (Exception e) {
            // CASO 2: Token Falso, Assinatura Inválida ou Malformado.
            // Isso pode ser uma tentativa de ataque, então logamos.
            System.out.println("Falha de segurança no token: " + e.getMessage());
        }
        return null;
    }



    public static boolean isValid(String subject, String issuer, Date exp) {
        return subject != null && subject.length() > 0 && issuer.equals(ISSUER) && exp.after(new Date(System.currentTimeMillis()));
    }
}
