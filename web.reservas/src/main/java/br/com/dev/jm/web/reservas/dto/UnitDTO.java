package br.com.dev.jm.web.reservas.dto;

import br.com.dev.jm.web.reservas.entity.UnitImage;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class UnitDTO {
    private String name;
    private Integer capacity;
    private BigDecimal defaultPrice;
    private String description;
    private String city;
    private String state;
    private String address;

    // O pulo do gato: Se for null, é Casa Principal.
    // Se tiver número, é um Quarto filho dessa casa.
    private Long parentId;
    private List<UnitImage> images;
    private BigDecimal areaM2;
    private String vrboUrl;
    private String airbnbUrl;
}