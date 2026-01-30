package br.com.dev.jm.web.reservas.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource())).authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/customers/**").permitAll()
                       // .requestMatchers(HttpMethod.GET, "/customers/**").permitAll()
                       // .requestMatchers(HttpMethod.PUT, "/customers/**").permitAll()
                        // .requestMatchers(HttpMethod.POST, "/reservations/**").permitAll()
                        //.requestMatchers(HttpMethod.GET, "/reservations/**").permitAll()
                        //.requestMatchers(HttpMethod.PUT, "/reservations/**").permitAll()
                        //.requestMatchers(HttpMethod.DELETE, "/reservations/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/units/**").permitAll()
                      //  .requestMatchers(HttpMethod.GET, "/units/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/swagger-ui/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v3/**").permitAll()
                        //.requestMatchers(HttpMethod.POST, "/api/sync/**").permitAll()




                        .anyRequest().authenticated()

                );

        http.addFilterBefore(new MyFilter(), UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }

    @Bean
    public org.springframework.security.crypto.password.PasswordEncoder passwordEncoder() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Permite requisições do seu frontend
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));

        // Métodos permitidos
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Cabeçalhos permitidos
        configuration.setAllowedHeaders(List.of("*"));

        // Permite envio de credenciais (cookies, etc.)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Aplica esta configuração a todos os paths da sua API
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}