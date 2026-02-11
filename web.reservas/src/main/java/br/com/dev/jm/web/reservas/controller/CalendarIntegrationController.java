package br.com.dev.jm.web.reservas.controller;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;
import br.com.dev.jm.web.reservas.entity.Reservation;
import br.com.dev.jm.web.reservas.repository.ReservationDAO;
import br.com.dev.jm.web.reservas.service.airbnb.AirbnbSyncService;
import br.com.dev.jm.web.reservas.service.vrbo.VrboSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/integration") // Mudei para 'integration' para ficar mais genérico
@RequiredArgsConstructor
public class CalendarIntegrationController {

    // Injeção dos Serviços (Para os botões de Sync)
    private final AirbnbSyncService airbnbSyncService;
    private final VrboSyncService vrboSyncService;

    // Injeção do DAO (Para a exportação)
    private final ReservationDAO reservationRepository;

    // =================================================================================
    // 1. ROTAS DE SINCRONIZAÇÃO MANUAL (Botões do Front-end)
    // =================================================================================

    @PostMapping("/airbnb/sync")
    public ResponseEntity<String> forceSyncAirbnb() {
        airbnbSyncService.syncAllUnits();
        return ResponseEntity.ok("Sincronização Airbnb iniciada!");
    }

    @PostMapping("/vrbo/sync")
    public ResponseEntity<String> forceSyncVrbo() {
        vrboSyncService.syncAllUnits();
        return ResponseEntity.ok("Sincronização Vrbo iniciada!");
    }

    // =================================================================================
    // 2. ROTAS DE EXPORTAÇÃO (Links para colar no Airbnb/Vrbo)
    // =================================================================================

    /**
     * Link para colar no AIRBNB.
     * Filtra reservas que já vieram do Airbnb para evitar loop.
     * URL: /api/integration/unit/{id}/airbnb.ics
     */
    @GetMapping(value = "/unit/{unitId}/airbnb.ics", produces = "text/calendar")
    public ResponseEntity<String> exportForAirbnb(@PathVariable Long unitId) {
        return generateIcal(unitId, "AIRBNB"); // Ignora reservas do Airbnb
    }

    /**
     * Link para colar no VRBO.
     * Filtra reservas que já vieram do Vrbo.
     * URL: /api/integration/unit/{id}/vrbo.ics
     */
    @GetMapping(value = "/unit/{unitId}/vrbo.ics", produces = "text/calendar")
    public ResponseEntity<String> exportForVrbo(@PathVariable Long unitId) {
        return generateIcal(unitId, "VRBO"); // Ignora reservas do Vrbo
    }

    /**
     * Método auxiliar que gera o arquivo iCal
     * @param ignoreOrigin A origem que devemos ignorar (para não devolver pro site o que veio dele)
     */
    private ResponseEntity<String> generateIcal(Long unitId, String ignoreOrigin) {
        // Busca reservas ativas (não canceladas)
        List<Reservation> reservations = reservationRepository.findByUnitIdAndStatusNot(unitId, "CANCELLED");

        ICalendar ical = new ICalendar();
        ical.setProductId("-//SeuProjeto//Reservas " + ignoreOrigin + "//PT");

        for (Reservation res : reservations) {
            // A MÁGICA: Se a reserva veio do Airbnb, NÃO mandamos de volta pro Airbnb
            if (!res.getOrigin().equalsIgnoreCase(ignoreOrigin)) {

                VEvent event = new VEvent();
                event.setSummary("Ocupado"); // Não exponha dados sensíveis

                Date start = Date.from(res.getCheckIn().atStartOfDay(ZoneId.systemDefault()).toInstant());
                Date end = Date.from(res.getCheckOut().atStartOfDay(ZoneId.systemDefault()).toInstant());

                event.setDateStart(start);
                event.setDateEnd(end);
                event.setUid("res-" + res.getId() + "@seuprojeto.com");

                ical.addEvent(event);
            }
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=calendar.ics")
                .contentType(MediaType.parseMediaType("text/calendar"))
                .body(Biweekly.write(ical).go());
    }
}