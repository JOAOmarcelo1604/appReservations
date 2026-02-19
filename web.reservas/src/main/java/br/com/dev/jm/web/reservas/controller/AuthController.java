package br.com.dev.jm.web.reservas.controller;

import br.com.dev.jm.web.reservas.dto.LoginDTO;
import br.com.dev.jm.web.reservas.security.TokenUtil;
import br.com.dev.jm.web.reservas.security.UsuarioToken;
import br.com.dev.jm.web.reservas.service.auth.IAuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDto) {
        // 1. Realiza a lógica de login (busca usuário e gera token)
        UsuarioToken usuarioToken = authService.realizarLogin(loginDto);

        if (usuarioToken != null) {

            // 1. Limpa o token (Remove "Bearer " e espaços extras)
            String tokenLimpo = usuarioToken.getToken();
            if (tokenLimpo != null) {
                if (tokenLimpo.startsWith("Bearer ")) {
                    tokenLimpo = tokenLimpo.substring(7);
                }
                tokenLimpo = tokenLimpo.trim(); // Garante que não sobrou nenhum espaço no final
            }

            // 2. Cria o Cookie usando a variável TRATADA (tokenLimpo)
            ResponseCookie jwtCookie = ResponseCookie.from("auth_token", tokenLimpo) // <--- CORREÇÃO AQUI
                    .httpOnly(true)
                    .secure(false) // Mude para true quando tiver HTTPS
                    .path("/")
                    .maxAge(7 * 24 * 60 * 60)
                    .sameSite("Lax")
                    .build();

            // 3. Limpa o objeto de retorno
            usuarioToken.setToken(null);

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                    .body(usuarioToken);
        }

        return ResponseEntity.status(401).build();
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioToken> getMe() {
        // Agora o SecurityContextHolder vai ter o usuário!
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            String email = (String) auth.getPrincipal();
            UsuarioToken usuario = authService.buscarUsuarioPorEmail(email);
            return ResponseEntity.ok(usuario);
        }

        return ResponseEntity.status(401).build();
    }

    // Opcional: Endpoint de Logout para limpar o cookie
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        ResponseCookie cleanCookie = ResponseCookie.from("auth_token", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0) // Expira agora
                .build();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cleanCookie.toString()).build();
    }
}