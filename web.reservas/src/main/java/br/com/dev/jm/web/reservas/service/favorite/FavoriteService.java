package br.com.dev.jm.web.reservas.service.favorite;

import br.com.dev.jm.web.reservas.dto.UnitDTO;
import br.com.dev.jm.web.reservas.entity.Customer;
import br.com.dev.jm.web.reservas.entity.Unit;
import br.com.dev.jm.web.reservas.repository.CustomerDAO;
import br.com.dev.jm.web.reservas.repository.UnitDAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoriteService {

    @Autowired
    private CustomerDAO customerRepository;

    @Autowired
    private UnitDAO unitRepository;

    @Transactional
    public void toggleFavorite(Long userId, Long unitId) {
        Customer customer = customerRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Unit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new RuntimeException("Imóvel não encontrado"));

        if (customer.getFavorites().contains(unit)) {
            customer.getFavorites().remove(unit); // Remove se já existe
        } else {
            customer.getFavorites().add(unit); // Adiciona se não existe
        }

        customerRepository.save(customer);
    }

    @Transactional(readOnly = true)
    public List<UnitDTO> getMyFavorites(Long userId) {
        Customer customer = customerRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Aqui você converte a lista de Entidades para DTOs
        return customer.getFavorites().stream()
                .map(this::convertToDTO) // Supondo que você tenha esse método ou use um Mapper
                .collect(Collectors.toList());
    }

    // Método auxiliar corrigido
    private UnitDTO convertToDTO(Unit unit) {
        UnitDTO dto = new UnitDTO();

        dto.setId(unit.getId());
        dto.setName(unit.getName());
        dto.setDefaultPrice(unit.getDefaultPrice());
        dto.setAddress(unit.getAddress());
        dto.setAirbnbUrl(unit.getAirbnbUrl());
        dto.setVrboUrl(unit.getVrboUrl());

        // Se tiver foto, adicione também:
        // dto.setPhoto(unit.getPhotos().isEmpty() ? null : unit.getPhotos().get(0));

        return dto;
    }
}