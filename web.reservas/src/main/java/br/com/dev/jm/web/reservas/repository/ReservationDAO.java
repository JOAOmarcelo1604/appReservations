package br.com.dev.jm.web.reservas.repository;

import br.com.dev.jm.web.reservas.entity.Reservation;
import br.com.dev.jm.web.reservas.entity.Unit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationDAO extends JpaRepository<Reservation, Long> {
    // Busca qualquer reserva ATIVA (não cancelada) que caia nas mesmas datas para uma unidade específica
    @Query("SELECT r FROM Reservation r WHERE r.unit.id = :unitId " +
            "AND r.status <> 'CANCELED' " +
            "AND (r.checkIn < :checkOut AND r.checkOut > :checkIn)")
    List<Reservation> findConflictingReservations(Long unitId, LocalDate checkIn, LocalDate checkOut);

    Optional<Reservation> findByExternalUid(String externalUid);

    @Modifying
    @Query("DELETE FROM Reservation r WHERE r.unit.id = :unitId " +
            "AND r.origin = 'AIRBNB' " +
            "AND r.checkIn >= :hoje " + // Só apaga reservas futuras
            "AND r.externalUid NOT IN :uidsAtivos")
    void deleteOrphans(@Param("unitId") Long unitId,
                       @Param("hoje") LocalDateTime hoje,
                       @Param("uidsAtivos") List<String> uidsAtivos);

    // CASO 2: O arquivo do Airbnb veio vazio (ex: dono cancelou tudo). Apaga tudo futuro.
    @Modifying
    @Query("DELETE FROM Reservation r WHERE r.unit.id = :unitId " +
            "AND r.origin = 'AIRBNB' " +
            "AND r.checkIn >= :hoje")
    void deleteAllFutureAirbnb(@Param("unitId") Long unitId,
                               @Param("hoje") LocalDateTime hoje);

}
