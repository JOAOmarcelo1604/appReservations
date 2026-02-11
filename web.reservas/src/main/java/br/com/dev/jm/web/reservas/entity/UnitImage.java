package br.com.dev.jm.web.reservas.entity;


import br.com.dev.jm.web.reservas.entity.Unit;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.Id;

@Entity
@Table(name = "unit_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UnitImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String url; // O link da foto (ex: https://meubucket.s3.../foto1.jpg)

    @JsonIgnore // Para não criar loop infinito no JSON
    @ManyToOne
    @JoinColumn(name = "unit_id") // Chave estrangeira
    private Unit unit;
}