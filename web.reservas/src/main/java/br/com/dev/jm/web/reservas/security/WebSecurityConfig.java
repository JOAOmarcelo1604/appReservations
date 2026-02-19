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
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/customers/**").permitAll()
                       //.requestMatchers(HttpMethod.GET, "/customers/**").permitAll()
                       //.requestMatchers(HttpMethod.PUT, "/customers/**").permitAll()
                        // .requestMatchers(HttpMethod.POST, "/reservations/**").permitAll()
                        //.requestMatchers(HttpMethod.GET, "/reservations/**").permitAll()
                        //.requestMatchers(HttpMethod.PUT, "/reservations/**").permitAll()
                        //.requestMatchers(HttpMethod.DELETE, "/reservations/**").permitAll()
                        //equestMatchers(HttpMethod.POST, "/units/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/units/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**").permitAll()
                        //.requestMatchers("/api/sync/**").permitAll()
                        //.requestMatchers(HttpMethod.POST, "/review").permitAll()
                        .requestMatchers(HttpMethod.GET, "/reviews/**").permitAll()
                        .requestMatchers("/api/sync/**", "/api/vrbo/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/reservations/occupied-dates/**").permitAll()



                        .requestMatchers("/error").permitAll()




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

        // ATENÇÃO: Para Cookies, NÃO use "*". Coloque a URL EXATA do seu front.
        // Se estiver testando no Insomnia/Postman, isso não afeta, mas no navegador sim.
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:5173"));

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));

        // Headers permitidos
        configuration.setAllowedHeaders(List.of("*"));

        // 3. Libera os cabeçalhos (inclusive Content-Type e Authorization)
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "x-auth-token"));

        // 4. O MAIS IMPORTANTE: Permite Credenciais (Cookies)
        configuration.setAllowCredentials(true);

        // OBRIGATÓRIO PARA COOKIES: Permite enviar credenciais
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}