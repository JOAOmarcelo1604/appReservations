package br.com.dev.jm.web.reservas.controller;

import br.com.dev.jm.web.reservas.dto.UnitDTO;
import br.com.dev.jm.web.reservas.entity.Customer;
import br.com.dev.jm.web.reservas.entity.Unit;
import br.com.dev.jm.web.reservas.service.unit.IUnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/units")
@RequiredArgsConstructor
public class UnitController {

    private final IUnitService service;

    @PostMapping
    public ResponseEntity<Unit> create(@RequestBody UnitDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(dto));
    }

    @GetMapping
    public ResponseEntity<List<Unit>> listAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Unit> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }
}