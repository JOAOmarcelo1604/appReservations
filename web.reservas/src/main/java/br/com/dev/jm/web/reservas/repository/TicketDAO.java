package br.com.dev.jm.web.reservas.repository;

import br.com.dev.jm.web.reservas.entity.Ticket;
import br.com.dev.jm.web.reservas.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketDAO extends JpaRepository<Ticket, Long> {
    // Listar tickets de uma reserva específica (para o hóspede ver os dele)
    List<Ticket> findByReservationId(Long reservationId);

    // Listar tickets por status (para o admin ver o que está pendente)
    List<Ticket> findByStatus(TicketStatus status);
}
