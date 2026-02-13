package br.com.dev.jm.web.reservas.security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

public class MyFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 1. Tenta pegar o token dos Cookies
        String token = null;
        if (request.getCookies() != null) {
            token = Arrays.stream(request.getCookies())
                    .filter(c -> "auth_token".equals(c.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }

        // (Opcional) Fallback: Se não achar no cookie, tenta no Header (para facilitar testes no Insomnia)
        if (token == null && request.getHeader("Authorization") != null) {
            token = request.getHeader("Authorization").replace("Bearer ", "");
        }

        // 2. Se achou token, valida
        if (token != null) {
            // OBS: Você precisará ajustar o TokenUtil.decode para aceitar a String token
            // Se o TokenUtil.decode receber (request), você terá que sobrecarregá-lo ou alterar a lógica interna dele.
            // Assumindo que você pode alterar o TokenUtil para receber String:
            Authentication auth = TokenUtil.getAuthentication(token);

            if (auth != null) {
                SecurityContextHolder.getContext().setAuthentication(auth);
            } else {
                // Token inválido/expirado
                response.setStatus(401);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}