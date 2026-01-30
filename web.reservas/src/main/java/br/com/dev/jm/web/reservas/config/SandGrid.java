package br.com.dev.jm.web.reservas.config;

import com.sendgrid.SendGrid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SandGrid {

    @Value("${spring.sendgrid.api-key}")
    private String sendGridApiKey;

    @Bean
    public SendGrid sendGrid() {
        // Se a chave não for encontrada, isso pode lançar erro na inicialização.
        // Garanta que está no application.properties
        return new SendGrid(sendGridApiKey);
    }
}