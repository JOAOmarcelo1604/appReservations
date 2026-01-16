package br.com.dev.jm.web.reservas.repository;

import br.com.dev.jm.web.reservas.entity.Reservation;
import br.com.dev.jm.web.reservas.entity.Unit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface ReservationDAO extends JpaRepository<Reservation, Long> {
    // Busca qualquer reserva ATIVA (não cancelada) que caia nas mesmas datas para uma unidade específica
    @Query("SELECT r FROM Reservation r WHERE r.unit.id = :unitId " +
            "AND r.status <> 'CANCELED' " +
            "AND (r.checkIn < :checkOut AND r.checkOut > :checkIn)")
    List<Reservation> findConflictingReservations(Long unitId, LocalDate checkIn, LocalDate checkOut);
    // No UnitRepository.java

}
