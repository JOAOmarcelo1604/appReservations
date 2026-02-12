package br.com.dev.jm.web.reservas.controller; // Ajuste seu pacote

import br.com.dev.jm.web.reservas.dto.TicketDTO;
import br.com.dev.jm.web.reservas.entity.Ticket;
import br.com.dev.jm.web.reservas.service.ticket.ITicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final ITicketService service;

    @PostMapping
    public ResponseEntity<TicketDTO> openTicket(@RequestBody TicketDTO dto) {
        // 1. Cria o ticket
        Ticket savedTicket = service.createTicket(dto);

        // 2. Converte para DTO para retornar preenchido (AQUI ESTAVA O ERRO ANTES)
        return ResponseEntity.ok(service.toDTO(savedTicket));
    }

    @GetMapping
    public ResponseEntity<List<TicketDTO>> listAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<TicketDTO> update(@PathVariable Long id, @RequestBody TicketDTO dto) {
        Ticket t = service.updateTicket(id, dto);
        return ResponseEntity.ok(service.toDTO(t));
    }
}