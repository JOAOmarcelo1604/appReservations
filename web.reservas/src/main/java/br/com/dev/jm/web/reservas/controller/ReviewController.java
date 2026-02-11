package br.com.dev.jm.web.reservas.controller;

import br.com.dev.jm.web.reservas.dto.ReviewDTO;
import br.com.dev.jm.web.reservas.entity.Review;

import br.com.dev.jm.web.reservas.service.review.IReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final IReviewService reviewService;

    @PostMapping
    public ResponseEntity<Review> create(@RequestBody ReviewDTO dto, Authentication auth) {
        // Pega o e-mail do token JWT automaticamente
        String email = auth.getName();
        Review novaReview = reviewService.saveReview(dto, email);
        return ResponseEntity.ok(novaReview);
    }

    @GetMapping("/unit/{unitId}")
    public ResponseEntity<List<Review>> listByUnit(@PathVariable Long unitId) {
        return ResponseEntity.ok(reviewService.getReviewsByUnit(unitId));
    }
}