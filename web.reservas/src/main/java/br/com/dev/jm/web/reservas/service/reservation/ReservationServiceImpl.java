package br.com.dev.jm.web.reservas.service.reservation;

import br.com.dev.jm.web.reservas.entity.Reservation;
import br.com.dev.jm.web.reservas.entity.Unit;
import br.com.dev.jm.web.reservas.repository.ReservationDAO;

import br.com.dev.jm.web.reservas.repository.UnitDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements IReservationService {

    private final ReservationDAO reservationRepository;
    private final UnitDAO unitRepository; // Necessário para checar a hierarquia

    @Override
    @Transactional
    public Reservation save(Reservation novaReserva) {
        // 1. Validações Básicas
        if (novaReserva.getCheckOut().isBefore(novaReserva.getCheckIn())) {
            throw new IllegalArgumentException("Data de Check-out não pode ser antes do Check-in");
        }

        // 2. Carregar a Unidade completa (para saber se tem Pai)
        Unit unidadeAlvo = unitRepository.findById(novaReserva.getUnit().getId())
                .orElseThrow(() -> new RuntimeException("Unidade não encontrada"));
        novaReserva.setUnit(unidadeAlvo);

        // 3. VALIDAR DISPONIBILIDADE (O Coração do Sistema)
        validarConflitos(unidadeAlvo, novaReserva);

        // 4. Preparar para salvar
        novaReserva.setBookingDate(LocalDateTime.now());
        novaReserva.setStatus("CONFIRMED"); // Começa confirmada ou PENDING

        return reservationRepository.save(novaReserva);
    }

    /**
     * Lógica Mágica:
     * Verifica se a unidade, o pai dela, ou os filhos dela estão ocupados.
     */
    private void validarConflitos(Unit unidade, Reservation r) {
        // A. Checar conflito direto (Alguém já reservou ESSA unidade?)
        if (temReservaNessePeriodo(unidade.getId(), r)) {
            throw new IllegalArgumentException("Esta unidade já está reservada para estas datas.");
        }

        // B. Checar conflito com o PAI (Se tento alugar o Quarto, a Casa toda está alugada?)
        if (unidade.getParent() != null) {
            if (temReservaNessePeriodo(unidade.getParent().getId(), r)) {
                throw new IllegalArgumentException("Não é possível reservar: A propriedade principal (Pai) já está alugada.");
            }
        }

        // C. Checar conflito com FILHOS (Se tento alugar a Casa toda, algum quarto está alugado?)
        List<Unit> unidadesFilhas = unitRepository.findByParentId(unidade.getId());
        for (Unit filho : unidadesFilhas) {
            if (temReservaNessePeriodo(filho.getId(), r)) {
                throw new IllegalArgumentException("Não é possível reservar a casa inteira: O quarto " + filho.getName() + " já está reservado.");
            }
        }
    }

    private boolean temReservaNessePeriodo(Long unitId, Reservation r) {
        List<Reservation> conflitos = reservationRepository.findConflictingReservations(
                unitId, r.getCheckIn(), r.getCheckOut());

        // Se for update, precisamos ignorar a própria reserva da lista de conflitos
        if (r.getId() != null) {
            conflitos.removeIf(res -> res.getId().equals(r.getId()));
        }

        return !conflitos.isEmpty();
    }

    @Override
    @Transactional
    public Reservation canceled(Long id) {
        Reservation reserva = findById(id);

        // REGRA: Não deletamos, apenas mudamos o status
        if ("CANCELED".equals(reserva.getStatus())) {
            throw new IllegalArgumentException("Esta reserva já está cancelada.");
        }

        reserva.setStatus("CANCELED");
        return reservationRepository.save(reserva);
    }

    @Override
    @Transactional
    public Reservation update(Long id, Reservation reservation) {
        Reservation existente = findById(id);

        // Atualiza dados
        existente.setCheckIn(reservation.getCheckIn());
        existente.setCheckOut(reservation.getCheckOut());
        existente.setTotalAmount(reservation.getTotalAmount());
        // ... outros campos ...

        // Revalida conflitos com as novas datas
        validarConflitos(existente.getUnit(), existente);

        return reservationRepository.save(existente);
    }

    @Override
    public Reservation findById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva não encontrada ID: " + id));
    }

    @Override
    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }
}