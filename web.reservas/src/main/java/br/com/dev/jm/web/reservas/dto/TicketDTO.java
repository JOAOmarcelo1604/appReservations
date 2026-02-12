package br.com.dev.jm.web.reservas.dto;

import br.com.dev.jm.web.reservas.enums.TicketPriority;
import br.com.dev.jm.web.reservas.enums.TicketStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TicketDTO {
    private Long id;
    private String title;
    private String description;
    private TicketStatus status;
    private TicketPriority priority;
    private Long reservationId; // O ID da reserva é obrigatório para abrir
    private String adminResponse;
    private LocalDateTime createdAt;

    // Campos informativos para facilitar a leitura no Front
    private String unitName;     // Nome da casa (para o admin saber onde ir)
    private String customerName; // Quem reclamou
}
