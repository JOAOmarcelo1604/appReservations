package br.com.dev.jm.web.reservas.service.ticket;

import br.com.dev.jm.web.reservas.dto.TicketDTO;
import br.com.dev.jm.web.reservas.entity.Reservation;
import br.com.dev.jm.web.reservas.entity.Ticket;
import br.com.dev.jm.web.reservas.enums.TicketStatus;
import br.com.dev.jm.web.reservas.repository.ReservationDAO;
import br.com.dev.jm.web.reservas.repository.TicketDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements ITicketService {

    private final TicketDAO ticketRepository;
    private final ReservationDAO reservationRepository;

    @Override
    @Transactional
    public Ticket createTicket(TicketDTO dto) {
        // 1. Busca a Reserva pelo ID que veio no JSON
        Reservation res = reservationRepository.findById(dto.getReservationId())
                .orElseThrow(() -> new RuntimeException("Reserva ID " + dto.getReservationId() + " não encontrada"));

        // 2. Monta o Ticket
        Ticket ticket = new Ticket();
        ticket.setTitle(dto.getTitle());
        ticket.setDescription(dto.getDescription());
        ticket.setPriority(dto.getPriority());
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setReservation(res); // Vincula a reserva encontrada
        ticket.setCreatedAt(LocalDateTime.now());

        // 3. Salva
        return ticketRepository.save(ticket);
    }

    @Override
    @Transactional
    public Ticket updateTicket(Long id, TicketDTO dto) {
        Ticket ticket = findById(id);

        if (dto.getStatus() != null) ticket.setStatus(dto.getStatus());
        if (dto.getAdminResponse() != null) ticket.setAdminResponse(dto.getAdminResponse());
        if (dto.getStatus() == TicketStatus.RESOLVED) ticket.setResolvedAt(LocalDateTime.now());

        return ticketRepository.save(ticket);
    }

    @Override
    public Ticket findById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket não encontrado"));
    }

    @Override
    public List<TicketDTO> findAll() {
        return ticketRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    // Método auxiliar público para converter Entity -> DTO
    @Override
    public TicketDTO toDTO(Ticket t) {
        TicketDTO dto = new TicketDTO();
        dto.setId(t.getId());
        dto.setTitle(t.getTitle());
        dto.setDescription(t.getDescription());
        dto.setStatus(t.getStatus());
        dto.setPriority(t.getPriority());
        dto.setCreatedAt(t.getCreatedAt());
        dto.setAdminResponse(t.getAdminResponse());

        // Dados da Reserva
        if (t.getReservation() != null) {
            dto.setReservationId(t.getReservation().getId());

            // Dados extras para o Front (Unit Name e Customer Name)
            if (t.getReservation().getUnit() != null) {
                dto.setUnitName(t.getReservation().getUnit().getName());
            }

            // Lógica do Nome (Airbnb vs Site)
            if (t.getReservation().getGuestName() != null) {
                dto.setCustomerName(t.getReservation().getGuestName() + " (Externo)");
            } else if (t.getReservation().getCustomer() != null) {
                dto.setCustomerName(t.getReservation().getCustomer().getFullName());
            }
        }
        return dto;
    }
}