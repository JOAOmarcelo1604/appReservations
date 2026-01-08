package br.com.dev.jm.web.reservas.entity; // Ajustado para .entity

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "daily_pricing", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"date", "unit_id"}) // Garante a regra de unicidade do banco
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyPricing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price; // Dinheiro sempre BigDecimal

    @Column(name = "is_available", nullable = false)
    @Builder.Default
    private Boolean isAvailable = true;

    @Column(name = "competitor_price", precision = 10, scale = 2)
    private BigDecimal competitorPrice;

    @Column(length = 50)
    private String reason;

    // Relacionamento com a Tabela Units
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;
}