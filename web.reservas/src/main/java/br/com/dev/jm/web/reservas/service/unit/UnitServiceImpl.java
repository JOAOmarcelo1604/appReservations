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
    private final br.com.dev.jm.web.reservas.repository.UnitImageDAO unitImageRepository;

    @Override
    @Transactional
    public Unit save(UnitDTO dto) {
        Unit unit = Unit.builder()
                .name(dto.getName())
                .bathrooms(dto.getBathrooms())
                .bedrooms(dto.getBedrooms())
                .beds(dto.getBeds())
                .capacity(dto.getCapacity())
                .areaM2(dto.getAreaM2())
                .defaultPrice(dto.getDefaultPrice())
                .description(dto.getDescription())
                .city(dto.getCity())
                .state(dto.getState())
                .address(dto.getAddress())
                .wifi(dto.getWifi())
                .pool(dto.getPool())
                .parking(dto.getParking())
                .airConditioning(dto.getAirConditioning())
                .bbqGrill(dto.getBbqGrill())
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

    @Override
    @Transactional // Importante para garantir que salva no banco
    public void addImageToUnit(Long unitId, String photoUrl) {
        // A. Busca a Unidade
        Unit unit = repository.findById(unitId)
                .orElseThrow(() -> new RuntimeException("Imóvel não encontrado"));

        // B. Cria o objeto da Imagem
        br.com.dev.jm.web.reservas.entity.UnitImage newImage = new br.com.dev.jm.web.reservas.entity.UnitImage();
        newImage.setUnit(unit);
        newImage.setUrl(photoUrl);

        // C. Salva na tabela unit_images
        unitImageRepository.save(newImage);
    }

    @Override
    public Unit update(Long id, UnitDTO dto) {
        // 1. Busca o imóvel existente (ou lança erro se não achar)
        Unit unit = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Imóvel não encontrado com ID: " + id));

        // 2. Só atualiza o Nome se o DTO trouxe um nome novo
        if (dto.getName() != null && !dto.getName().isEmpty()) {
            unit.setName(dto.getName());
        }

        // 3. Só atualiza a Descrição se vier preenchida
        if (dto.getDescription() != null) {
            unit.setDescription(dto.getDescription());
        }

        // 4. Só atualiza o Endereço se vier preenchido
        if (dto.getAddress() != null) {
            unit.setAddress(dto.getAddress());
        }

        // 5. Só atualiza o Preço se vier preenchido
        if (dto.getDefaultPrice() != null) {
            unit.setDefaultPrice(dto.getDefaultPrice());
        }

        // 3. Atualiza os detalhes (Quartos, Banheiros, Camas)
        if (dto.getBedrooms() != null) unit.setBedrooms(dto.getBedrooms());
        if (dto.getBathrooms() != null) unit.setBathrooms(dto.getBathrooms());
        if (dto.getBeds() != null) unit.setBeds(dto.getBeds());

        // 4. Atualiza os Links Externos (Airbnb / Vrbo)
        // Certifique-se que esses campos existem no seu UnitDTO e Entity Unit
        if (dto.getAirbnbUrl() != null) unit.setAirbnbUrl(dto.getAirbnbUrl());
        if (dto.getVrboUrl() != null) unit.setVrboUrl(dto.getVrboUrl());

        // 5. Salva e retorna
        return repository.save(unit);
    }
}