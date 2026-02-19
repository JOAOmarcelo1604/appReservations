package br.com.dev.jm.web.reservas.controller;

import br.com.dev.jm.web.reservas.dto.UnitDTO;
import br.com.dev.jm.web.reservas.service.auth.IAuthService;
import br.com.dev.jm.web.reservas.service.favorite.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private IAuthService authService;

    // POST: Favoritar ou Desfavoritar (Toggle)
    @PostMapping("/{unitId}")
    public ResponseEntity<Void> toggleFavorite(@PathVariable Long unitId) {
        // Pega o ID do usuário logado do Contexto de Segurança
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        // Você precisará de um método para buscar ID pelo email, ou injetar o ID no token
        // Supondo que você busque o usuário pelo email:
        Long userId = authService.buscarUsuarioPorEmail(email).getCustomerId();

        favoriteService.toggleFavorite(userId, unitId);
        return ResponseEntity.ok().build();
    }

    // GET: Listar meus favoritos
    @GetMapping
    public ResponseEntity<List<UnitDTO>> getMyFavorites() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = authService.buscarUsuarioPorEmail(email).getCustomerId();

        return ResponseEntity.ok(favoriteService.getMyFavorites(userId));
    }
}
