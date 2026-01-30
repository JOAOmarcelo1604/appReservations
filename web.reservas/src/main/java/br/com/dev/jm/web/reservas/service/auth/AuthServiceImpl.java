package br.com.dev.jm.web.reservas.service.auth;

import br.com.dev.jm.web.reservas.dto.LoginDTO;
import br.com.dev.jm.web.reservas.entity.Customer;
import br.com.dev.jm.web.reservas.repository.CustomerDAO;
import br.com.dev.jm.web.reservas.security.TokenUtil;
import br.com.dev.jm.web.reservas.security.UsuarioToken;
import br.com.dev.jm.web.reservas.service.auth.IAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;


@Service
public class AuthServiceImpl implements IAuthService {

    @Autowired
    private CustomerDAO repository; // Use o Repository, não o DAO (se possível)
/*
    @Override
    public Customer criarUsuario(Customer novo) {
        //criptografar a senha ANTES de salvar
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // A senha deve vir preenchida do Controller/DTO
        if(novo.getPassword() == null) {
            throw new IllegalArgumentException("Senha é obrigatória");
        }

        String senhaCriptografada = encoder.encode(novo.getPassword());
        novo.setPassword(senhaCriptografada);

        return repository.save(novo);
    }
*/
    @Override
    public UsuarioToken realizarLogin(LoginDTO dadosLogin) { // Mudei para receber DTO
        // 1. Busca o usuário no banco pelo E-mail
        Customer cliente = repository.findByEmail(dadosLogin.getEmail())
                .orElse(null); // ou lançar exceção

        if (cliente != null) {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

            // 2. Compara a senha que chegou (dadosLogin) com a do banco (cliente)
            // O encoder.matches faz a mágica de comparar Texto Puro com Hash
            if (encoder.matches(dadosLogin.getPassword(), cliente.getPassword())) {
                return TokenUtil.encode(cliente);
            }
        }

        return null; // Ou lançar "Credenciais Inválidas"
    }
}