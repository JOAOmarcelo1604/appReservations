package br.com.dev.jm.web.reservas.service.ticket;

import br.com.dev.jm.web.reservas.dto.TicketDTO; // Importe seu DTO
import br.com.dev.jm.web.reservas.entity.Ticket;
import java.util.List;

public interface ITicketService {
    Ticket createTicket(TicketDTO dto); // Mudamos de Ticket para TicketDTO
    Ticket updateTicket(Long id, TicketDTO dto);
    Ticket findById(Long id);

    List<TicketDTO> findAll(); // Retornar DTOs na lista é melhor
    TicketDTO toDTO(Ticket ticket); // Vamos expor o conversor para o Controller usar
}