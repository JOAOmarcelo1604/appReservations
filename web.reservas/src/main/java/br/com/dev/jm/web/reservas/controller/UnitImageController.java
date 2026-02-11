package br.com.dev.jm.web.reservas.controller;

import br.com.dev.jm.web.reservas.service.claudinary.CloudinaryService;
import br.com.dev.jm.web.reservas.service.unit.IUnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/units")
@RequiredArgsConstructor
public class UnitImageController {

    private final CloudinaryService cloudinaryService; // Mudou aqui
    private final IUnitService unitService;

    @PostMapping("/{unitId}/photos")
    public ResponseEntity<String> uploadPhoto(@PathVariable Long unitId,
                                              @RequestParam("file") MultipartFile file) {

        // 1. Sobe pro Cloudinary
        String photoUrl = cloudinaryService.uploadFile(file);

        // 2. Salva no banco (Lógica que já tínhamos)
        unitService.addImageToUnit(unitId, photoUrl);

        return ResponseEntity.ok(photoUrl);
    }
}