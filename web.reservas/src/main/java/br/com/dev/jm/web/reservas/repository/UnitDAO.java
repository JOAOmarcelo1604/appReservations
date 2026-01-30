package br.com.dev.jm.web.reservas.repository;

import br.com.dev.jm.web.reservas.entity.Unit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UnitDAO extends JpaRepository<Unit,Long> {
    List<Unit> findByParentId(Long parentId);
    List<Unit> findByCity(String city);
}
