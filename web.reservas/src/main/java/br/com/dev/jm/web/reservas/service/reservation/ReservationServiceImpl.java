package br.com.dev.jm.web.reservas.service.reservation;

import br.com.dev.jm.web.reservas.entity.Customer;
import br.com.dev.jm.web.reservas.entity.Reservation;
import br.com.dev.jm.web.reservas.entity.Unit;
import br.com.dev.jm.web.reservas.repository.CustomerDAO;
import br.com.dev.jm.web.reservas.repository.ReservationDAO;

import br.com.dev.jm.web.reservas.repository.UnitDAO;
import br.com.dev.jm.web.reservas.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements IReservationService {

    private final ReservationDAO reservationRepository;
    private final UnitDAO unitRepository; // Necessário para checar a hierarquia
    private final CustomerDAO customerRepository;
    private final EmailService emailService;

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

        validarConflitos(unidadeAlvo, novaReserva);

       // --- 3. NOVO: Carregar o Cliente Completo ---
        // Se não fizer isso, o getEmail() retorna null
        if (novaReserva.getCustomer() != null && novaReserva.getCustomer().getId() != null) {
            Customer clienteAlvo = customerRepository.findById(novaReserva.getCustomer().getId())
                    .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
            novaReserva.setCustomer(clienteAlvo);
        } else {
            throw new IllegalArgumentException("É obrigatório informar o ID do cliente.");
        }

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

        Reservation savedReservation = reservationRepository.save(novaReserva);

        try {
            emailService.sendReservationConfirmation(
                    savedReservation.getCustomer().getEmail(),
                    savedReservation.getCustomer().getFullName(),
                    savedReservation.getUnit().getName(),
                    savedReservation.getCheckIn().toString(),
                    savedReservation.getCheckOut().toString(),
                    savedReservation.getTotalAmount()
                    );
        }  catch (Exception e) {
            // Logar o erro de email, mas não impedir a reserva
            System.err.println("Erro ao enviar email: " + e.getMessage());
        }
        return savedReservation;

    }



    private void validarConflitos(Unit unidade, Reservation r) {

        // --- (REMOVIDO BLOCO A) ---
        // Removemos a verificação "temReservaNessePeriodo" para a própria unidade,
        // pois ela não sabe ignorar o ID no update. Deixamos essa checagem para o final.

        // B. Checar conflito com o PAI (Propriedade maior)
        // Aqui mantemos, pois se o Pai estiver ocupado, é por OUTRA reserva (outro ID), então é conflito real.
        if (unidade.getParent() != null) {
            if (temReservaNessePeriodo(unidade.getParent().getId(), r)) {
                throw new IllegalArgumentException("Não é possível reservar: A propriedade principal (Pai) já está alugada.");
            }
        }

        // C. Checar conflito com FILHOS (Sub-unidades)
        List<Unit> unidadesFilhas = unitRepository.findByParentId(unidade.getId());
        for (Unit filho : unidadesFilhas) {
            if (temReservaNessePeriodo(filho.getId(), r)) {
                throw new IllegalArgumentException("Não é possível reservar a casa inteira: O quarto " + filho.getName() + " já está reservado.");
            }
        }

        // D. Validação Final da Própria Unidade (Substitui o bloco A)
        List<Reservation> conflitos = reservationRepository.findConflictingReservations(
                unidade.getId(), r.getCheckIn(), r.getCheckOut());

        // --- FILTRAGEM INTELIGENTE ---
        // Se for uma EDIÇÃO (Update), removemos a própria reserva da lista de conflitos
        if (r.getId() != null) {
            conflitos.removeIf(res -> res.getId().equals(r.getId()));
        }

        // Se sobrou alguém na lista, aí sim é um conflito real (outra pessoa)
        if (!conflitos.isEmpty()) {
            System.out.println("🚨 CONFLITO REAL DETECTADO! A unidade já tem outra reserva.");
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

        // -----------------------------------------------------------------------
        // 1. ATUALIZAÇÃO MANUAL (Preço, Cliente e Notas)
        // Isso garante que a alteração seja salva mesmo se as datas NÃO mudarem.
        // -----------------------------------------------------------------------

        // Se o usuário mandou um preço manual, atualizamos e marcamos uma flag
        boolean precoManualFoiEnviado = false;
        if (reservaAtualizada.getTotalAmount() != null) {
            reservaExistente.setTotalAmount(reservaAtualizada.getTotalAmount());
            precoManualFoiEnviado = true;
        }

        // Se mandou um cliente novo, atualiza
        if (reservaAtualizada.getCustomer() != null) {
            reservaExistente.setCustomer(reservaAtualizada.getCustomer());
        }

        // Se mandou status novo, atualiza
        if (reservaAtualizada.getStatus() != null) {
            reservaExistente.setStatus(reservaAtualizada.getStatus());
        }

        // -----------------------------------------------------------------------
        // 2. LÓGICA DE DATAS E UNIDADE
        // -----------------------------------------------------------------------

        // Proteção contra NullPointerException nas datas
        LocalDate checkInNovo = reservaAtualizada.getCheckIn() != null ? reservaAtualizada.getCheckIn() : reservaExistente.getCheckIn();
        LocalDate checkOutNovo = reservaAtualizada.getCheckOut() != null ? reservaAtualizada.getCheckOut() : reservaExistente.getCheckOut();

        boolean datasMudaram = !reservaExistente.getCheckIn().equals(checkInNovo) ||
                !reservaExistente.getCheckOut().equals(checkOutNovo);

        boolean unidadeMudou = false;
        if (reservaAtualizada.getUnit() != null && reservaAtualizada.getUnit().getId() != null) {
            unidadeMudou = !reservaExistente.getUnit().getId().equals(reservaAtualizada.getUnit().getId());
        }

        if (datasMudaram || unidadeMudou) {
            // Atualiza datas
            reservaExistente.setCheckIn(checkInNovo);
            reservaExistente.setCheckOut(checkOutNovo);

            // Se mudou unidade, carrega a nova
            if (unidadeMudou) {
                Unit novaUnidade = unitRepository.findById(reservaAtualizada.getUnit().getId())
                        .orElseThrow(() -> new RuntimeException("Nova unidade não encontrada"));
                reservaExistente.setUnit(novaUnidade);
            }

            // Valida conflitos (ignorando o próprio ID)
            validarConflitos(reservaExistente.getUnit(), reservaExistente);

            // --- LÓGICA INTELIGENTE DE PREÇO ---
            // Só recalculamos o preço automaticamente (Regra da Unidade)
            // SE o usuário NÃO tiver enviado um preço manual agora.
            // Se ele mandou 4000, respeitamos o 4000. Se não mandou nada, calculamos.
            if (!precoManualFoiEnviado) {
                long dias = ChronoUnit.DAYS.between(reservaExistente.getCheckIn(), reservaExistente.getCheckOut());
                if (dias < 1) throw new IllegalArgumentException("A reserva deve ter no mínimo 1 diária.");

                BigDecimal precoBase = reservaExistente.getUnit().getDefaultPrice();
                if (precoBase != null) {
                    BigDecimal novoValorCalculado = precoBase.multiply(BigDecimal.valueOf(dias));
                    reservaExistente.setTotalAmount(novoValorCalculado);
                }
            }
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

    @Override
    public List<LocalDate> getOccupiedDates(Long unitId) {
        // 1. Busca reservas ativas
        List<Reservation> reservations = reservationRepository.findByUnitIdAndStatusNot(unitId, "CANCELLED");

        List<LocalDate> occupiedDates = new ArrayList<>();
        LocalDate hoje = LocalDate.now(); // Data de hoje para filtrar o passado

        for (Reservation res : reservations) {
            // Só nos interessa se a reserva termina DEPOIS de hoje
            if (res.getCheckOut().isAfter(hoje)) {
                LocalDate start = res.getCheckIn();
                LocalDate end = res.getCheckOut();

                // Adiciona os dias na lista
                start.datesUntil(end).forEach(date -> {
                    // Opcional: Só adiciona se o dia for futuro (para não bloquear o passado no calendário)
                    if (!date.isBefore(hoje)) {
                        occupiedDates.add(date);
                    }
                });
            }
        }

        // 2. Limpeza Final: Remove duplicados e Ordena
        return occupiedDates.stream()
                .distinct() // Remove datas repetidas (se houver overbooking)
                .sorted()   // Ordena (fica bonito no JSON)
                .collect(Collectors.toList());
    }

    @Override
    public List<Reservation> getMinhasReservas(String emailCliente) {
        // 1. Acha o cliente pelo email (quem está logado)
        Customer customer = customerRepository.findByEmail(emailCliente)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        // 2. Busca as reservas dele
        return reservationRepository.findByCustomerId(customer.getId());
    }
}