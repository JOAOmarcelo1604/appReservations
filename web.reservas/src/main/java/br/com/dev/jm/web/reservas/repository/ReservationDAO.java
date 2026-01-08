package br.com.dev.jm.web.reservas.repository;

import br.com.dev.jm.web.reservas.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationDAO extends JpaRepository<Reservation, Long> {

}
