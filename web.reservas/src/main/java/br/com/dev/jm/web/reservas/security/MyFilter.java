package br.com.dev.jm.web.reservas.security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class MyFilter extends OncePerRequestFilter {


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if (request.getHeader("Authorization") != null) {
            Authentication auth = TokenUtil.decode(request);
            if (auth != null) {
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
            else {
                System.out.println("Deu ruim!!!");
                response.setStatus(401);
                return;
            }
        }
        //nao faz nada.. .apenas encaminha a requisicao
        filterChain.doFilter(request, response);
    }

}
