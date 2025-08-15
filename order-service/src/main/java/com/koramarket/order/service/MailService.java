package com.koramarket.order.service;

import com.koramarket.order.model.Invoice;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.*;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender sender;

    @Value("${invoice.mail.to-debug:}")
    private String toDebug;

    public void sendInvoice(Invoice inv, byte[] pdf, List<String> to) {
        try {
            MimeMessage msg = sender.createMimeMessage();
            var helper = new MimeMessageHelper(msg, true, "UTF-8");
            List<String> recipients = (to == null || to.isEmpty())
                    ? java.util.List.of(toDebug)
                    : to;
            helper.setTo(recipients.toArray(String[]::new));
            helper.setSubject("Facture " + inv.getInvoiceNumber());
            helper.setText("Veuillez trouver ci-joint votre facture " + inv.getInvoiceNumber(), false);
            helper.addAttachment(inv.getInvoiceNumber() + ".pdf", new ByteArrayResource(pdf));
            sender.send(msg);
        } catch (Exception e) {
            // log soft-fail
            org.slf4j.LoggerFactory.getLogger(getClass()).warn("Envoi email facture KO: {}", e.toString());
        }
    }
}
