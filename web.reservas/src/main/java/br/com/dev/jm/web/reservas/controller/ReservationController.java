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
    public ResponseEntity<Reservation> updateReservation(@PathVariable Long id, @RequestBody Reservation reservationUpdate){
        reservationUpdate.setId(id);
        Reservation res = service.update(id,reservationUpdate);
            return ResponseEntity.status(200).body(res);

    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Reservation> cancelReservation(@PathVariable Long id) {
        return ResponseEntity.ok(service.canceled(id));
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
}
