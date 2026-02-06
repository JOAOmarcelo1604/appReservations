package br.com.dev.jm.web.reservas.controller;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.property.Summary;
import br.com.dev.jm.web.reservas.entity.Reservation;
import br.com.dev.jm.web.reservas.entity.Unit;
import br.com.dev.jm.web.reservas.repository.ReservationDAO;
import br.com.dev.jm.web.reservas.repository.UnitDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/ical")
@RequiredArgsConstructor
public class IcalExportController {

    private final UnitDAO unitRepository;
    private final ReservationDAO reservationRepository;

    @GetMapping(value = "/unit/{unitId}.ics", produces = "text/calendar")
    public ResponseEntity<String> exportUnitCalendar(@PathVariable Long unitId) {

        // 1. Busca a Unidade
        Unit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new RuntimeException("Unidade não encontrada"));

        // 2. Busca TODAS as reservas confirmadas dessa unidade
        // (Você precisa garantir que seu DAO tenha um método para buscar por unidade)
        List<Reservation> reservations = reservationRepository.findAllByUnitId(unitId);

        // 3. Cria o objeto Calendário
        ICalendar ical = new ICalendar();
        ical.setProductId("-//SeuProjeto//Reservas//PT");

        // 4. Converte cada reserva do banco para um evento iCal
        for (Reservation res : reservations) {
            // Só exporta se NÃO estiver cancelada
            if (!"CANCELLED".equals(res.getStatus())) {
                VEvent event = new VEvent();

                // Define o Título (Airbnb esconde, mas é bom por)
                Summary summary = event.setSummary("Reservado");

                // Converte LocalDateTime para Date (Legacy do iCal)
                Date start = Date.from(res.getCheckIn().atStartOfDay(ZoneId.systemDefault()).toInstant());
                Date end = Date.from(res.getCheckOut().atStartOfDay(ZoneId.systemDefault()).toInstant());

                event.setDateStart(start);
                event.setDateEnd(end);

                // Usa o ID da reserva como UID (importante para atualizações)
                event.setUid("reserva-" + res.getId() + "@seuprojeto.com");

                ical.addEvent(event);
            }
        }

        // 5. Gera o texto final
        String icalString = Biweekly.write(ical).go();

        // 6. Retorna como arquivo para download
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=agenda_unidade_" + unitId + ".ics")
                .contentType(MediaType.parseMediaType("text/calendar"))
                .body(icalString);
    }
}