package br.com.dev.jm.web.reservas.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ReservationDTO {

    // O usuário só manda o ID, não o objeto completo
    private Long customerId;
    private Long unitId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private String origin;
}