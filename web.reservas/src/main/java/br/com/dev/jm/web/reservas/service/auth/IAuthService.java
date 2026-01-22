package br.com.dev.jm.web.reservas.service.auth;

import br.com.dev.jm.web.reservas.dto.LoginDTO;
import br.com.dev.jm.web.reservas.entity.Customer;
import br.com.dev.jm.web.reservas.security.UsuarioToken;

public interface IAuthService {

    public Customer criarUsuario(Customer novo);
    UsuarioToken realizarLogin(LoginDTO dadosLogin);
}
