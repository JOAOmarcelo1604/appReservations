package br.com.dev.jm.web.reservas.service.unit;

import br.com.dev.jm.web.reservas.dto.UnitDTO;
import br.com.dev.jm.web.reservas.entity.Unit;
import br.com.dev.jm.web.reservas.repository.UnitDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UnitServiceImpl implements IUnitService {

    private final UnitDAO repository;

    @Override
    @Transactional
    public Unit save(UnitDTO dto) {
        Unit unit = Unit.builder()
                .name(dto.getName())
                .capacity(dto.getCapacity())
                .defaultPrice(dto.getDefaultPrice())
                .description(dto.getDescription())
                .build();

        // Lógica de Hierarquia
        if (dto.getParentId() != null) {
            Unit pai = repository.findById(dto.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("Unidade Pai não encontrada"));
            unit.setParent(pai);
        }

        return repository.save(unit);
    }

    @Override
    public List<Unit> findAll() {
        return repository.findAll();
    }

    @Override
    public Unit findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Unidade não encontrada"));
    }
}