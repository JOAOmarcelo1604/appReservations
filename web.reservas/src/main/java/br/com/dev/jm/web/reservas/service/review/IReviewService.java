package br.com.dev.jm.web.reservas.service.review;

import br.com.dev.jm.web.reservas.dto.ReviewDTO;
import br.com.dev.jm.web.reservas.entity.Review;

import java.util.List;

public interface IReviewService {

    Review saveReview(ReviewDTO dto, String emailUserLogado);
    public List<Review> getReviewsByUnit(Long unitId);
}
