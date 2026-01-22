package br.com.dev.jm.web.reservas.controller;

import br.com.dev.jm.web.reservas.dto.LoginDTO;
import br.com.dev.jm.web.reservas.security.UsuarioToken;
import br.com.dev.jm.web.reservas.service.auth.IAuthService;
import lombok.RequiredArgsConstructor; // Importante para injetar o Service
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

    // A anotação @PostMapping vem logo acima do método, não da variável
    @PostMapping("/login")
    public ResponseEntity<UsuarioToken> login(@RequestBody LoginDTO loginDto) {
        UsuarioToken token = authService.realizarLogin(loginDto);

        if (token != null) {
            return ResponseEntity.ok(token);
        }

        // Retorna 403 Forbidden ou 401 Unauthorized se falhar
        return ResponseEntity.status(401).build();
    }
}