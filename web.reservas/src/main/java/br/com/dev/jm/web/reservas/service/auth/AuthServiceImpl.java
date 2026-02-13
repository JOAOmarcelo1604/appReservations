package br.com.dev.jm.web.reservas.service.auth;

import br.com.dev.jm.web.reservas.dto.LoginDTO;
import br.com.dev.jm.web.reservas.entity.Customer;
import br.com.dev.jm.web.reservas.repository.CustomerDAO;
import br.com.dev.jm.web.reservas.security.TokenUtil;
import br.com.dev.jm.web.reservas.security.UsuarioToken;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor // 1. Isso cria o construtor para os campos 'final' automaticamente
public class AuthServiceImpl implements IAuthService {

    private final CustomerDAO customerRepository;
    private final PasswordEncoder passwordEncoder; // 2. Injete o encoder, não use 'new'

    @Override
    public UsuarioToken realizarLogin(LoginDTO dadosLogin) {
        // 1. Busca o usuário no banco pelo E-mail
        Customer cliente = customerRepository.findByEmail(dadosLogin.getEmail())
                .orElse(null);

        if (cliente != null) {
            // 3. Usa o encoder injetado para verificar a senha
            if (passwordEncoder.matches(dadosLogin.getPassword(), cliente.getPassword())) {
                return TokenUtil.encode(cliente);
            }
        }

        return null;
    }

    @Override
    public UsuarioToken buscarUsuarioPorEmail(String email) {
        // 1. Busca o usuário no banco pelo e-mail (vindo do Cookie/Contexto)
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + email));

        // 2. Converte para o DTO de resposta
        UsuarioToken userDto = new UsuarioToken();

        userDto.setCustomerId(customer.getId());
        userDto.setFullName(customer.getFullName());
        userDto.setEmail(customer.getEmail());
        userDto.setPhoneNumber(customer.getPhoneNumber()); // Se tiver esse campo no DTO

        // Converte Role (Enum ou String) para String
        if (customer.getRole() != null) {
            userDto.setRole(customer.getRole().toString());
        }

        // IMPORTANTE: Token vai nulo, pois o navegador já tem o Cookie
        userDto.setToken(null);

        return userDto;
    }
}