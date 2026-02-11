package br.com.dev.jm.web.reservas.service.review;


import br.com.dev.jm.web.reservas.dto.ReviewDTO;
import br.com.dev.jm.web.reservas.entity.Customer;
import br.com.dev.jm.web.reservas.entity.Review;
import br.com.dev.jm.web.reservas.entity.Unit;
import br.com.dev.jm.web.reservas.repository.CustomerDAO; // Ou Repository
import br.com.dev.jm.web.reservas.repository.ReviewRepositoryDAO;
import br.com.dev.jm.web.reservas.repository.ReviewRepositoryDAO;
import br.com.dev.jm.web.reservas.repository.UnitDAO; // Ou Repository
import br.com.dev.jm.web.reservas.service.review.IReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements IReviewService {

    private final ReviewRepositoryDAO reviewRepository;
    private final UnitDAO unitRepository;
    private final CustomerDAO customerRepository;

    public Review saveReview(ReviewDTO dto, String emailUserLogado) {
        // 1. Busca quem está avaliando
        Customer customer = customerRepository.findByEmail(emailUserLogado)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // 2. Busca o imóvel
        Unit unit = unitRepository.findById(dto.getUnitId())
                .orElseThrow(() -> new RuntimeException("Imóvel não encontrado"));

        // --- REGRA DE OURO (FUTURO) ---
        // Aqui você verificaria se existe uma reserva CONCLUÍDA para este usuário nesta unidade.
        // if (!reservationRepository.existsByCustomerAndUnitAndStatus(customer, unit, "FINISHED")) {
        //     throw new RuntimeException("Você precisa ter se hospedado para avaliar.");
        // }

        // 3. Monta e Salva
        Review review = new Review();
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        review.setUnit(unit);
        review.setCustomer(customer);

        return reviewRepository.save(review);
    }

    public List<Review> getReviewsByUnit(Long unitId) {
        return reviewRepository.findByUnitId(unitId);
    }
}