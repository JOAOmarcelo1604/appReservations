package br.com.dev.jm.web.reservas.security;


public class UsuarioToken {

    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UsuarioToken(String token) {
        this.token = token;
    }
}