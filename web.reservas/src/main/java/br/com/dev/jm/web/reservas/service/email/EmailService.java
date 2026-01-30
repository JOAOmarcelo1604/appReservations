package br.com.dev.jm.web.reservas.service.email;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;

@Service
public class EmailService {

    @Autowired
    private SendGrid sendGrid;

    @Value("${app.sendgrid.from-email}")
    private String remetenteVerificado;

    public void sendReservationConfirmation(String toEmail, String nomeCliente, String nomeUnidade,
                                            String checkIn, String checkOut, BigDecimal total) {

        // 1. Cria o conteúdo do email
        Email from = new Email(remetenteVerificado);
        String subject = "Confirmação de Reserva - " + nomeUnidade;
        Email to = new Email(toEmail);

        // Pode usar HTML aqui
        String corpoTexto = String.format(
                "Olá %s,\n\nSua reserva em %s foi confirmada!\nCheck-in: %s\nCheck-out: %s\nValor: R$ %s",
                nomeCliente, nomeUnidade, checkIn, checkOut, total
        );

        Content content = new Content("text/plain", corpoTexto);

        // 2. Monta o objeto Mail
        Mail mail = new Mail(from, subject, to, content);

        // 3. Envia a Requisição
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sendGrid.api(request);

            // Debug: Verifica se deu certo (Status 2xx é sucesso)
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                System.out.println("E-mail enviado com sucesso!");
            } else {
                System.err.println("Erro SendGrid: " + response.getBody());
            }

        } catch (IOException ex) {
            System.err.println("Erro de conexão com SendGrid: " + ex.getMessage());
        }
    }
}