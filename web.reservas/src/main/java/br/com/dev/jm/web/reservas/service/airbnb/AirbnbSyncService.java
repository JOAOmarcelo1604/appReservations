package br.com.dev.jm.web.reservas.service.airbnb;

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
public class AirbnbSyncService {

    private final UnitDAO unitRepository;
    private final ReservationDAO reservationRepository;
    private final CustomerDAO customerRepository;

    @Transactional
    public void syncAllUnits() {
        // 1. Busca todas as unidades que têm link do Airbnb configurado
        List<Unit> units = unitRepository.findAll();

        for (Unit unit : units) {
            if (unit.getAirbnbUrl() != null && !unit.getAirbnbUrl().isEmpty()) {
                System.out.println("Sincronizando unidade: " + unit.getName());
                importCalendar(unit);
            }
        }
    }

    private void importCalendar(Unit unit) {
        try {
            // 2. Garante que existe um usuário "Airbnb" no banco
            Customer airbnbCustomer = getOrCreateAirbnbCustomer();

            // 3. Lê o arquivo .ics direto da URL
            InputStream in = new URL(unit.getAirbnbUrl()).openStream();
            ICalendar ical = Biweekly.parse(in).first();

            if (ical == null) return;

            List<String> activeUids = new ArrayList<>();

            // 4. Varre todos os eventos do calendário
            for (VEvent event : ical.getEvents()) {
                String uid = event.getUid().getValue();
                activeUids.add(uid);


                // Conversão de data (Date -> LocalDateTime)
                Date start = event.getDateStart().getValue();
                Date end = event.getDateEnd().getValue();
                LocalDate checkIn = start.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                LocalDate checkOut = end.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                // 5. Verifica se já existe essa reserva
                Optional<Reservation> existing = reservationRepository.findByExternalUid(uid);

                if (existing.isPresent()) {
                    // ATUALIZAÇÃO: Se as datas mudaram, atualiza
                    Reservation res = existing.get();
                    if (!res.getCheckIn().isEqual(checkIn) || !res.getCheckOut().isEqual(checkOut)) {
                        res.setCheckIn(checkIn);
                        res.setCheckOut(checkOut);
                        reservationRepository.save(res);
                        System.out.println("Reserva Airbnb Atualizada: " + uid);
                    }
                } else {
                    // CRIAÇÃO: Nova reserva
                    Reservation newRes = new Reservation();
                    newRes.setUnit(unit);
                    newRes.setCustomer(airbnbCustomer); // Usa o cliente fantasma
                    newRes.setCheckIn(checkIn);
                    newRes.setCheckOut(checkOut);
                    newRes.setTotalAmount(BigDecimal.ZERO); // Sem valor financeiro
                    newRes.setStatus("CONFIRMED"); // Já vem confirmada
                    newRes.setOrigin("AIRBNB");
                    newRes.setExternalUid(uid);

                    reservationRepository.save(newRes);
                    System.out.println("Nova Reserva Airbnb Importada: " + uid);
                }
            }

            LocalDateTime hoje = LocalDateTime.now();

            if (activeUids.isEmpty()) {
                // Se a lista está vazia, significa que não tem nenhuma reserva no Airbnb.
                // Apaga tudo o que for futuro no nosso banco.
                reservationRepository.deleteAllFutureAirbnb(unit.getId(), hoje);
                System.out.println("Limpeza completa: Nenhuma reserva ativa no Airbnb.");
            } else {
                // Se tem reservas, apaga só as que NÃO vieram nessa lista (as canceladas).
                reservationRepository.deleteOrphans(unit.getId(), hoje, activeUids);
                // Obs: Não imprimimos log aqui porque o deleteOrphans roda no banco direto
            }

            System.out.println("Sincronização com cancelamento finalizada para: " + unit.getName());

        } catch (Exception e) {
            System.err.println("Erro ao sincronizar unidade " + unit.getName() + ": " + e.getMessage());
        }
    }

    // Método auxiliar para não dar erro de "Customer Null"
    private Customer getOrCreateAirbnbCustomer() {
        return customerRepository.findByEmail("sistema@airbnb.com")
                .orElseGet(() -> {
                    Customer c = new Customer();
                    c.setFullName("Airbnb Guest (Importado)");
                    c.setEmail("sistema@airbnb.com");
                    c.setPassword("SENHA_GERADA_AUTO_BLOQUEADA");
                    c.setRole("USER");

                    // --- CORREÇÃO AQUI ---
                    // Preenchendo campos obrigatórios com dados fictícios para passar no banco
                    c.setBirthDate(LocalDate.of(2000, 1, 1));
                    c.setPhoneNumber("0000000000"); // Coloque um dummy se for obrigatório
                    c.setCpf("00000000000");        // Coloque um dummy se for obrigatório
                    c.setCountryOrigin("BR");       // Coloque um dummy se for obrigatório
                    // ---------------------

                    return customerRepository.save(c);
                });
    }
}

