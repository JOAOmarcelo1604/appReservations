package br.com.dev.jm.web.reservas.repository;

import br.com.dev.jm.web.reservas.entity.Unit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnitDAO extends JpaRepository<Unit,Long> {
}
