package com.koramarket.order.service;

import com.koramarket.common.exceptions.BusinessException;
import com.koramarket.order.model.Invoice;
import com.koramarket.order.model.Order;
import com.koramarket.order.repository.InvoiceRepository;
import com.koramarket.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvoicePdfService {

    private final InvoiceRepository invoiceRepo;
    private final OrderRepository orderRepo;

    @Transactional(readOnly = true)
    public byte[] renderByInvoiceId(UUID invoiceId) {
        Invoice inv = invoiceRepo.findById(invoiceId)
                .orElseThrow(() -> new BusinessException("Facture introuvable"));

        // Charge la commande + items si nécessaire
        Order order = orderRepo.findOneWithItems(inv.getOrder().getId())
                .orElseGet(() -> orderRepo.findById(inv.getOrder().getId())
                        .orElseThrow(() -> new BusinessException("Commande introuvable")));

        return render(inv, order);
    }

    private byte[] render(Invoice inv, Order o) {
        try {
            var baos = new ByteArrayOutputStream();
            var doc = new com.lowagie.text.Document(com.lowagie.text.PageSize.A4);
            com.lowagie.text.pdf.PdfWriter.getInstance(doc, baos);
            doc.open();

            // Titre
            var title = new com.lowagie.text.Paragraph("FACTURE " + inv.getInvoiceNumber(),
                    new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 18, com.lowagie.text.Font.BOLD));
            title.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
            doc.add(title);
            doc.add(new com.lowagie.text.Paragraph(" "));

            // Infos facture
            var fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            doc.add(new com.lowagie.text.Paragraph("Date émission : " + inv.getIssuedAt()));
            doc.add(new com.lowagie.text.Paragraph("Commande     : " + o.getOrderNumber()));
            doc.add(new com.lowagie.text.Paragraph("Client (UUID): " + o.getUserIdExt()));
            doc.add(new com.lowagie.text.Paragraph("Devise       : " + o.getCurrency()));
            doc.add(new com.lowagie.text.Paragraph(" "));

            // Tableau items
            var table = new com.lowagie.text.pdf.PdfPTable(6);
            table.setWidthPercentage(100);
            table.addCell("Produit");
            table.addCell("SKU");
            table.addCell("Qté");
            table.addCell("PU (centimes)");
            table.addCell("TVA (centimes)");
            table.addCell("Total ligne");

            for (var it : o.getItems()) {
                table.addCell(nvl(it.getProductNameSnap(), "-"));
                table.addCell(nvl(it.getProductSkuSnap(), "-"));
                table.addCell(String.valueOf(it.getQuantity()));
                table.addCell(String.valueOf(it.getUnitPriceAmount()));
                table.addCell(String.valueOf(it.getTaxAmount()));
                table.addCell(String.valueOf(it.getLineTotalAmount()));
            }
            doc.add(table);
            doc.add(new com.lowagie.text.Paragraph(" "));

            // Totaux
            doc.add(new com.lowagie.text.Paragraph("Sous-total : " + o.getSubtotalAmount()));
            doc.add(new com.lowagie.text.Paragraph("TVA total  : " + o.getTaxTotalAmount()));
            doc.add(new com.lowagie.text.Paragraph("Livraison  : " + o.getShippingTotalAmount()));
            doc.add(new com.lowagie.text.Paragraph("Remise     : " + o.getDiscountTotalAmount()));
            doc.add(new com.lowagie.text.Paragraph("TOTAL TTC  : " + o.getGrandTotalAmount() + " " + o.getCurrency()));

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new BusinessException("Erreur génération PDF: " + e.getMessage());
        }
    }

    private static String nvl(String s, String d) { return (s == null || s.isBlank()) ? d : s; }
}
