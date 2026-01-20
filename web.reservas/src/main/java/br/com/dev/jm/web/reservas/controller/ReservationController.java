package br.com.dev.jm.web.reservas.controller;

import br.com.dev.jm.web.reservas.dto.ReservationDTO;
import br.com.dev.jm.web.reservas.entity.Customer;
import br.com.dev.jm.web.reservas.entity.Reservation;
import br.com.dev.jm.web.reservas.entity.Unit;
import br.com.dev.jm.web.reservas.service.reservation.IReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final IReservationService service;

    @GetMapping
    public ResponseEntity<List<Reservation>> findAll(){
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> findById(@PathVariable Long id ){
        return ResponseEntity.ok(service.findById(id));
    }

    // --- MUDANÇA AQUI: Recebe DTO ---
    @PostMapping
    public ResponseEntity<Reservation> createReservation(@RequestBody ReservationDTO dto){
        // 1. Converter DTO -> Entity
        Reservation novaReserva = converterParaEntidade(dto);

        // 2. Chamar o Service (que vai calcular preço e validar conflitos)
        Reservation res = service.save(novaReserva);

        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }
    @PutMapping("/{id}")
    public ResponseEntity<Reservation> updateReservation(@PathVariable Long id, @RequestBody ReservationDTO dto) {
        // 1. Converter DTO para Entidade
        Reservation reservationUpdate = new Reservation();

        reservationUpdate.setCheckIn(dto.getCheckIn());
        reservationUpdate.setCheckOut(dto.getCheckOut());

        // AQUI ESTÁ O SEGREDO: Transformar o long unitId em Objeto Unit
        if (dto.getUnitId() != null) {
            Unit u = new Unit();
            u.setId(dto.getUnitId()); // Seta o ID 2 aqui
            reservationUpdate.setUnit(u);
        }

        if (dto.getCustomerId() != null) {
            Customer c = new Customer();
            c.setId(dto.getCustomerId());
            reservationUpdate.setCustomer(c);
        }

        // 2. Agora o Service recebe uma reserva com Unit preenchida!
        Reservation res = service.update(id, reservationUpdate);

        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build(); // Retorna 204 No Content
    }

    // --- Método Auxiliar de Conversão ---
    private Reservation converterParaEntidade(ReservationDTO dto) {
        // Criamos objetos "dummy" apenas com o ID para o Hibernate entender a relação
        Customer cliente = new Customer();
        cliente.setId(dto.getCustomerId());

        Unit unidade = new Unit();
        unidade.setId(dto.getUnitId());

        return Reservation.builder()
                .customer(cliente)
                .unit(unidade)
                .checkIn(dto.getCheckIn())
                .checkOut(dto.getCheckOut())
                .build();
    }

    @PatchMapping("/{id}/pay")
    public ResponseEntity<Reservation> confirmPayment(@PathVariable Long id) {
        // Você poderia criar esse método no Service
        Reservation reserva = service.findById(id);

        if ("CANCELED".equals(reserva.getStatus())) {
            return ResponseEntity.badRequest().body(null); // Não pode pagar cancelada
        }

        reserva.setPaymentStatus("PAID");
        reserva.setStatus("CONFIRMED"); // Se pagou, a reserva está garantida!

        service.save(reserva); // O save simples serve, ou crie um updateStatus específico

        return ResponseEntity.ok(reserva);
    }
}
