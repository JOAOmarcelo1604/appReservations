package br.com.dev.jm.web.reservas.exception; // Ajuste o pacote se precisar

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Captura o IllegalArgumentException (que você lançou no Service)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        // Retorna Erro 409 (Conflict) ou 400 (Bad Request)
        // Monta um JSON simples com a mensagem
        Map<String, String> erro = new HashMap<>();
        erro.put("error", "Erro de Validação");
        erro.put("message", ex.getMessage()); // <--- AQUI VAI O SEU TEXTO ("Já existe um cliente...")

        return ResponseEntity.status(HttpStatus.CONFLICT).body(erro);
    }

    // 2. Captura erros genéricos (NullPointer, etc)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneralException(Exception ex) {
        Map<String, String> erro = new HashMap<>();
        erro.put("error", "Erro Interno");
        erro.put("message", "Ocorreu um erro inesperado: " + ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        // Pega todos os campos que deram erro (ex: cpf inválido, email vazio)
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
}