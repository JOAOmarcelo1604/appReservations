package br.com.dev.jm.web.reservas.entity;

import br.com.dev.jm.web.reservas.enums.TicketPriority;
import br.com.dev.jm.web.reservas.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.Data;


import java.time.LocalDateTime;

@Entity
@Data // Lombok
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;       // Ex: "Chuveiro não esquenta"

    @Column(length = 1000)
    private String description; // Ex: "Tentei ligar e sai apenas água fria..."

    @Enumerated(EnumType.STRING)
    private TicketStatus status = TicketStatus.OPEN;

    @Enumerated(EnumType.STRING)
    private TicketPriority priority = TicketPriority.MEDIUM;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime resolvedAt;

    // VÍNCULO IMPORTANTE: De qual reserva é esse problema?
    @ManyToOne
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    // Resposta do Admin (para não criar tabela de comentários agora)
    private String adminResponse;
}