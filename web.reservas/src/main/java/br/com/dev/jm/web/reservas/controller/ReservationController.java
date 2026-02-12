package br.com.dev.jm.web.reservas.controller;

import br.com.dev.jm.web.reservas.dto.ReservationDTO;
import br.com.dev.jm.web.reservas.entity.Customer;
import br.com.dev.jm.web.reservas.entity.Reservation;
import br.com.dev.jm.web.reservas.entity.Unit;
import br.com.dev.jm.web.reservas.repository.CustomerDAO;
import br.com.dev.jm.web.reservas.repository.ReservationDAO;
import br.com.dev.jm.web.reservas.service.customer.ICustomerService;
import br.com.dev.jm.web.reservas.service.reservation.IReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final IReservationService service;

    private final ReservationDAO dto;

    private final CustomerDAO customer;

    @GetMapping
    public ResponseEntity<List<Reservation>> findAll(){
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> findById(@PathVariable Long id ){
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/my-reservations") // <--- O Spring prefere rotas exatas do que variáveis
    public ResponseEntity<List<Reservation>> getMyReservations(Authentication auth) {
        String email = auth.getName();
        List<Reservation> reservas = service.getMinhasReservas(email);
        return ResponseEntity.ok(reservas);
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
    public ResponseEntity<ReservationDTO> updateReservation(@PathVariable Long id, @RequestBody ReservationDTO dto) {
        Reservation reservation = service.findById(id);
        if (reservation == null) throw new RuntimeException("Reserva não encontrada");

        // 1. Atualiza Preço
        if (dto.getTotalAmount() != null) {
            reservation.setTotalAmount(dto.getTotalAmount());
        }

        // 2. LÓGICA DE CLIENTE
        if (dto.getCustomerId() != null) {
            // CENÁRIO A: Cliente do Site (Existente)
            Customer clienteExistente = customer.findById(dto.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
            reservation.setCustomer(clienteExistente);

            // Limpa dados de hóspede avulso
            reservation.setGuestName(null);
            reservation.setGuestPhone(null);
            reservation.setGuestEmail(null); // Limpa e-mail avulso

        } else if (dto.getNewCustomerName() != null && !dto.getNewCustomerName().isBlank()) {
            // CENÁRIO B: Hóspede Airbnb/Vrbo (Avulso)

            // Vincula ao Robô (ID 3) para satisfazer o banco
            Customer robo = customer.findById(3L).orElseThrow();
            reservation.setCustomer(robo);

            // Salva os dados reais NA RESERVA
            reservation.setGuestName(dto.getNewCustomerName());
            reservation.setGuestPhone(dto.getNewCustomerPhone());
            reservation.setGuestEmail(dto.getNewCustomerEmail()); // <--- SALVA O E-MAIL AQUI
        }

        Reservation updated = service.update(id, reservation);
        return ResponseEntity.ok(toDTO(updated));
    }
    // Método auxiliar para converter Entidade -> DTO (Caso você não tenha Mapper)
    private ReservationDTO toDTO(Reservation res) {
        ReservationDTO dto = new ReservationDTO();

        // Campos básicos
        dto.setCheckIn(res.getCheckIn());
        dto.setCheckOut(res.getCheckOut());
        dto.setOrigin(res.getOrigin());
        dto.setTotalAmount(res.getTotalAmount());
        dto.setUnitId(res.getUnit() != null ? res.getUnit().getId() : null);

        // --- LÓGICA DE EXIBIÇÃO INTELIGENTE ---

        // 1. O ID do Customer sempre vai (seja o real ou o ID 3 do robô)
        if (res.getCustomer() != null) {
            dto.setCustomerId(res.getCustomer().getId());
        }

        // 2. Decidir qual NOME mostrar para o usuário
        if (res.getGuestName() != null && !res.getGuestName().isBlank()) {
            // Se tiver nome avulso (Airbnb), usa ele
            dto.setNewCustomerName(res.getGuestName());
            dto.setNewCustomerPhone(res.getGuestPhone());
            dto.setNewCustomerEmail(res.getGuestEmail());
            // Dica: Você pode criar um campo 'displayName' no DTO para facilitar
        } else if (res.getCustomer() != null) {
            // Se não, usa o nome do cadastro oficial
            dto.setNewCustomerName(res.getCustomer().getFullName());
            dto.setNewCustomerPhone(res.getCustomer().getPhoneNumber());
            dto.setNewCustomerEmail(res.getCustomer().getEmail());
        }

        // Lógica do isImported
        dto.setImported(res.getOrigin() != null && !"SITE".equalsIgnoreCase(res.getOrigin()));

        return dto;
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

    @GetMapping("/occupied-dates")
    public ResponseEntity<List<LocalDate>> getOccupiedDates(@RequestParam Long unitId) {
        List<LocalDate> dates = service.getOccupiedDates(unitId);
        return ResponseEntity.ok(dates);
    }

}
