package br.com.dev.jm.web.reservas.service.reservation;

import br.com.dev.jm.web.reservas.entity.Reservation;
import br.com.dev.jm.web.reservas.entity.Unit;
import br.com.dev.jm.web.reservas.repository.ReservationDAO;

import br.com.dev.jm.web.reservas.repository.UnitDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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

        long dias = ChronoUnit.DAYS.between(novaReserva.getCheckIn(), novaReserva.getCheckOut());
        if (dias < 1) {
            throw new IllegalArgumentException("A reserva deve ter no mínimo 1 diária.");
        }
        // B. Pega o preço padrão da unidade (BigDecimal)
        if (unidadeAlvo.getDefaultPrice() == null) {
            throw new IllegalStateException("Esta unidade não tem um preço base configurado.");
        }
        // C. Multiplica: Dias * Preço
        BigDecimal valorTotal = unidadeAlvo.getDefaultPrice().multiply(BigDecimal.valueOf(dias));
        // D. Seta o valor na reserva antes de salvar
        novaReserva.setTotalAmount(valorTotal);

        // 4. Preparar para salvar
        novaReserva.setBookingDate(LocalDateTime.now());
        novaReserva.setStatus("CONFIRMED"); // Começa confirmada ou PENDING

        if (novaReserva.getPaymentStatus() == null) {
            novaReserva.setPaymentStatus("UNPAID");
        }

        return reservationRepository.save(novaReserva);
    }


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
        List<Reservation> conflitos = reservationRepository.findConflictingReservations(
                unidade.getId(), r.getCheckIn(), r.getCheckOut());

        // --- ADICIONE ESTE BLOCO DE DEBUG ---
        if (!conflitos.isEmpty()) {
            System.out.println("🚨 CONFLITO ENCONTRADO! Detalhes:");
            for (Reservation conf : conflitos) {
                System.out.println("ID: " + conf.getId() +
                        " | Status no Java: '" + conf.getStatus() + "'" +
                        " | Tamanho: " + conf.getStatus().length());
            }
        }
        // ------------------------------------

        // Se for update, remove a própria reserva da lista...
        if (r.getId() != null) {
            conflitos.removeIf(res -> res.getId().equals(r.getId()));
        }

        if (!conflitos.isEmpty()) {
            throw new IllegalArgumentException("Esta unidade já está reservada para estas datas.");
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
    public Reservation update(Long id, Reservation reservaAtualizada) {
        Reservation reservaExistente = findById(id);

        // Verifica se houve mudança nas datas ou na unidade
        boolean datasMudaram = !reservaExistente.getCheckIn().equals(reservaAtualizada.getCheckIn()) ||
                !reservaExistente.getCheckOut().equals(reservaAtualizada.getCheckOut());

        boolean unidadeMudou = false;

        if (reservaAtualizada.getUnit() != null && reservaAtualizada.getUnit().getId() != null) {
            unidadeMudou = !reservaExistente.getUnit().getId().equals(reservaAtualizada.getUnit().getId());
        }

        // Se mudou algo crítico, precisamos revalidar e recalcular
        if (datasMudaram || unidadeMudou) {

            // 1. Atualiza os campos no objeto existente
            reservaExistente.setCheckIn(reservaAtualizada.getCheckIn());
            reservaExistente.setCheckOut(reservaAtualizada.getCheckOut());

            // Se mudou a unidade, buscamos a nova no banco
            if (unidadeMudou) {
                Unit novaUnidade = unitRepository.findById(reservaAtualizada.getUnit().getId())
                        .orElseThrow(() -> new RuntimeException("Nova unidade não encontrada"));
                reservaExistente.setUnit(novaUnidade);
            }

            // 2. REVALIDA CONFLITOS (Importante!)
            // Passamos a reservaExistente, pois o método validarConflitos sabe ignorar o ID dela mesma
            validarConflitos(reservaExistente.getUnit(), reservaExistente);

            // 3. RECALCULA O PREÇO (Cópia da lógica do save)
            long dias = java.time.temporal.ChronoUnit.DAYS.between(
                    reservaExistente.getCheckIn(),
                    reservaExistente.getCheckOut());

            if (dias < 1) {
                throw new IllegalArgumentException("A reserva deve ter no mínimo 1 diária.");
            }

            BigDecimal precoBase = reservaExistente.getUnit().getDefaultPrice();
            if (precoBase == null) {
                throw new IllegalStateException("Unidade sem preço configurado.");
            }

            BigDecimal novoValorTotal = precoBase.multiply(BigDecimal.valueOf(dias));
            reservaExistente.setTotalAmount(novoValorTotal);
        }

        // Se o status veio no update (ex: CONFIRMED), atualizamos. Se não, mantemos o atual.
        if (reservaAtualizada.getStatus() != null) {
            reservaExistente.setStatus(reservaAtualizada.getStatus());
        }

        return reservationRepository.save(reservaExistente);
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

    @Override
    @Transactional
    public void delete(Long id) {
        Reservation reserva = findById(id);
        // Aqui removemos fisicamente do banco
        reservationRepository.delete(reserva);
    }
}