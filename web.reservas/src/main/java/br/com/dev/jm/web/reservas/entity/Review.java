package br.com.dev.jm.web.reservas.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Getter @Setter
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Min(1) @Max(5) // Garante que a nota seja entre 1 e 5
    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT") // Permite textos longos
    private String comment;

    private LocalDateTime createdAt = LocalDateTime.now();

    // Relacionamentos
    @ManyToOne
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    // Opcional: Ligar à reserva para garantir que ele realmente se hospedou
    // @OneToOne
    // private Reservation reservation;
}