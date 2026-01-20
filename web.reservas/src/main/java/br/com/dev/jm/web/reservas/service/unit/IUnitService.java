package br.com.dev.jm.web.reservas.service.unit;

import br.com.dev.jm.web.reservas.dto.UnitDTO;
import br.com.dev.jm.web.reservas.entity.Customer;
import br.com.dev.jm.web.reservas.entity.Reservation;
import br.com.dev.jm.web.reservas.entity.Unit;

import java.util.List;

public interface IUnitService {

    public Unit save(UnitDTO dto);
    List<Unit> findAll();
    public Unit findById(Long Id);

}
