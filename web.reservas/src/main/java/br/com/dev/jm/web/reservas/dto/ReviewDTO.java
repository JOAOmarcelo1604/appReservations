package br.com.dev.jm.web.reservas.dto;

import lombok.Data;

@Data
public class ReviewDTO {
    private Long unitId;
    private Integer rating;
    private String comment;
}