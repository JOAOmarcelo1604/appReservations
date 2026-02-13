package br.com.dev.jm.web.reservas.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor // Cria o construtor vazio necessário
@AllArgsConstructor // Cria o construtor com tudo
public class UsuarioToken {

    private String token; // O token JWT (que agora ficará nulo no retorno)

    // --- Adicione estes novos campos para o erro sumir ---
    private Long customerId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String role;
}