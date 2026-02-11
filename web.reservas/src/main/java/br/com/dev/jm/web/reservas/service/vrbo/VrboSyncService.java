package br.com.dev.jm.web.reservas.service.vrbo;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;
import br.com.dev.jm.web.reservas.entity.Customer;
import br.com.dev.jm.web.reservas.entity.Reservation;
import br.com.dev.jm.web.reservas.entity.Unit;
import br.com.dev.jm.web.reservas.repository.CustomerDAO;
import br.com.dev.jm.web.reservas.repository.ReservationDAO;
import br.com.dev.jm.web.reservas.repository.UnitDAO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VrboSyncService {

    private final UnitDAO unitRepository;
    private final ReservationDAO reservationRepository;
    private final CustomerDAO customerRepository;

    // Roda a cada 30 min
    @Scheduled(fixedRate = 1800000)
    @Transactional
    public void syncAllUnits() {
        List<Unit> units = unitRepository.findAll();

        for (Unit unit : units) {
            // Verifica se tem URL do Vrbo configurada
            if (unit.getVrboUrl() != null && !unit.getVrboUrl().isEmpty()) {
                System.out.println("Sincronizando Vrbo para unidade: " + unit.getName());
                importCalendar(unit);
            }
        }
    }

    private void importCalendar(Unit unit) {
        try {
            Customer vrboCustomer = getOrCreateVrboCustomer();

            InputStream in = new URL(unit.getVrboUrl()).openStream();
            ICalendar ical = Biweekly.parse(in).first();

            if (ical == null) return;

            List<String> activeUids = new ArrayList<>();

            for (VEvent event : ical.getEvents()) {
                // Vrbo as vezes não manda UID limpo, mas o Biweekly resolve
                String uid = event.getUid().getValue();
                if (uid == null) continue;

                activeUids.add(uid);

                Date start = event.getDateStart().getValue();
                Date end = event.getDateEnd().getValue();

                // Conversão segura de Date para LocalDate
                LocalDate checkIn = start.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                LocalDate checkOut = end.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                // Verifica se já existe pelo UID externo
                Optional<Reservation> existing = reservationRepository.findByExternalUid(uid);

                if (existing.isPresent()) {
                    // ATUALIZAÇÃO: Se mudou a data, atualiza
                    Reservation res = existing.get();
                    if (!res.getCheckIn().isEqual(checkIn) || !res.getCheckOut().isEqual(checkOut)) {
                        res.setCheckIn(checkIn);
                        res.setCheckOut(checkOut);
                        reservationRepository.save(res);
                        System.out.println("Reserva Vrbo Atualizada: " + uid);
                    }
                } else {
                    // CRIAÇÃO: Nova reserva
                    Reservation newRes = new Reservation();
                    newRes.setUnit(unit);
                    newRes.setCustomer(vrboCustomer);
                    newRes.setCheckIn(checkIn);
                    newRes.setCheckOut(checkOut);
                    newRes.setTotalAmount(BigDecimal.ZERO);
                    newRes.setStatus("CONFIRMED");
                    newRes.setPaymentStatus("PAID");
                    newRes.setOrigin("VRBO"); // <--- Importante: Origem VRBO
                    newRes.setExternalUid(uid);

                    reservationRepository.save(newRes);
                    System.out.println("Nova Reserva Vrbo Importada: " + uid);
                }
            }

            // LIMPEZA DE CANCELAMENTOS (A Mágica)
            LocalDateTime hoje = LocalDateTime.now();

            if (activeUids.isEmpty()) {
                // Se o calendário veio vazio, apaga tudo que é futuro do Vrbo
                reservationRepository.deleteAllFutureByOrigin(unit.getId(), hoje, "VRBO");
            } else {
                // Apaga o que está no banco mas NÃO veio no iCal (Cancelados)
                reservationRepository.deleteOrphansByOrigin(unit.getId(), hoje, activeUids, "VRBO");
            }

        } catch (Exception e) {
            System.err.println("Erro ao sincronizar Vrbo unidade " + unit.getName() + ": " + e.getMessage());
        }
    }

    private Customer getOrCreateVrboCustomer() {
        return customerRepository.findByEmail("sistema@vrbo.com")
                .orElseGet(() -> {
                    Customer c = new Customer();
                    c.setFullName("Vrbo Guest (Importado)");
                    c.setEmail("sistema@vrbo.com");
                    c.setPassword("SENHA_SISTEMA_VRBO");
                    c.setRole("USER");
                    c.setBirthDate(LocalDate.of(2000, 1, 1));
                    c.setPhoneNumber("0000000000");
                    c.setCpf("00000000000");
                    c.setCountryOrigin("BR");
                    return customerRepository.save(c);
                });
    }
}