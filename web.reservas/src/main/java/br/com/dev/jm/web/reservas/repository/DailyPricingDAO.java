package br.com.dev.jm.web.reservas.repository;

import br.com.dev.jm.web.reservas.entity.DailyPricing;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyPricingDAO extends JpaRepository<DailyPricing, Long> {
}
