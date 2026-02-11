package br.com.dev.jm.web.reservas.service.claudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public String uploadFile(MultipartFile file) {
        try {
            // Envia para a nuvem
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());

            // Retorna a URL segura (https) da imagem
            return uploadResult.get("secure_url").toString();

        } catch (IOException e) {
            throw new RuntimeException("Erro ao enviar imagem para o Cloudinary", e);
        }
    }
}