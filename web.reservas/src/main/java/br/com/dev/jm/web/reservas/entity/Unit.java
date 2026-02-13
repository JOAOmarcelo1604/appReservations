package br.com.dev.jm.web.reservas.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "units")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Unit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    // Auto-relacionamento (Pai/Filho)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Unit parent;

    @Column(nullable = false)
    private Integer capacity;

    @Column(name = "default_price", precision = 10, scale = 2)
    private BigDecimal defaultPrice;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "airbnb_url", length = 255)
    private String airbnbUrl;

    @Column(name = "address", unique = true)
    private String address;

    @Column(name = "bedrooms")
    private Integer bedrooms; // Integer aceita null (bom para cadastro parcial)

    @Column(name = "bathrooms")
    private Integer bathrooms;

    @Column(name = "beds")
    private Integer beds;

    private Boolean wifi;

    private Boolean pool; // Piscina

    private Boolean parking; // Estacionamento

    @Column(name = "air_conditioning")
    private Boolean airConditioning;

    @Column(name = "bbq_grill")
    private Boolean bbqGrill;

    @Column(name = "vrbo_url")
    private String vrboUrl;

    @Column(name = "area_m2") // O nome exato da coluna no banco
    private BigDecimal areaM2;

    @OneToMany(mappedBy = "unit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UnitImage> images = new ArrayList<>();
}