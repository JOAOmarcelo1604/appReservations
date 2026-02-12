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


    List<Reservation> findByUnitIdAndStatusNot(Long unitId, String status);

    List<Reservation> findAllByUnitId(Long unitId);

    List<Reservation> findByCustomerId(Long customerId);

    // Método NOVO: Busca conflitos ignorando o ID da própria reserva (para Updates)
    @Query("SELECT r FROM Reservation r WHERE r.unit.id = :unitId " +
            "AND r.status <> 'CANCELLED' " +
            "AND (r.checkIn < :checkOut AND r.checkOut > :checkIn) " +
            "AND r.id <> :excludeId") // <--- O PULO DO GATO: Ignora este ID
    List<Reservation> findConflictingReservationsExcludingId(
            @Param("unitId") Long unitId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("excludeId") Long excludeId);


    // 1. Apaga TUDO do futuro se o calendário vier vazio
    @Modifying
    @Query("DELETE FROM Reservation r WHERE r.unit.id = :unitId AND r.origin = :origin AND r.checkIn >= :now")
    void deleteAllFutureByOrigin(@Param("unitId") Long unitId, @Param("now") LocalDateTime now, @Param("origin") String origin);

    // 2. Apaga "Órfãos" (Reservas que existem no banco mas não vieram na lista do iCal)
    @Modifying
    @Query("DELETE FROM Reservation r WHERE r.unit.id = :unitId AND r.origin = :origin AND r.checkIn >= :now AND r.externalUid NOT IN :activeUids")
    void deleteOrphansByOrigin(@Param("unitId") Long unitId, @Param("now") LocalDateTime now, @Param("activeUids") List<String> activeUids, @Param("origin") String origin);
}

