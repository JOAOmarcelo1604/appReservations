package br.com.dev.jm.web.reservas.repository;

import br.com.dev.jm.web.reservas.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepositoryDAO extends JpaRepository<Review, Long> {
    // Buscar todas as avaliações de uma casa específica
    List<Review> findByUnitId(Long unitId);


}