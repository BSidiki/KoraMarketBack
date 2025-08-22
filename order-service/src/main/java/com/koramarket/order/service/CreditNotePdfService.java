package com.koramarket.order.service;

import com.koramarket.common.exceptions.BusinessException;
import com.koramarket.order.model.CreditNote;
import com.koramarket.order.repository.CreditNoteRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreditNotePdfService {

    private final CreditNoteRepository creditNoteRepository;

    public byte[] renderById(UUID id) {
        CreditNote cn = creditNoteRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Avoir introuvable"));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 36, 36, 48, 48);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            // ---- Styles ----
            Font title = new Font(Font.HELVETICA, 16, Font.BOLD);
            Font h2    = new Font(Font.HELVETICA, 12, Font.BOLD);
            Font text  = new Font(Font.HELVETICA, 11, Font.NORMAL);

            // ---- En-tête ----
            Paragraph pTitle = new Paragraph("AVOIR " + cn.getCreditNumber(), title);
            pTitle.setAlignment(Element.ALIGN_LEFT);
            doc.add(pTitle);

            doc.add(new Paragraph("Date : " + fmtDate(cn.getIssuedAt() != null ? cn.getIssuedAt() : cn.getCreatedAt()), text));
            doc.add(Chunk.NEWLINE);

            // ---- Références ----
            PdfPTable refs = new PdfPTable(2);
            refs.setWidthPercentage(100);
            refs.setSpacingBefore(5f);
            refs.setWidths(new float[]{35f, 65f});

            addKV(refs, "Commande",  cn.getOrder().getOrderNumber(), h2, text);
            addKV(refs, "Facture",   cn.getInvoice() != null ? cn.getInvoice().getInvoiceNumber() : "-", h2, text);
            addKV(refs, "Motif",     nz(cn.getReason()), h2, text);
            addKV(refs, "Devise",    nz(cn.getCurrency()), h2, text);
            doc.add(refs);

            doc.add(Chunk.NEWLINE);

            // ---- Montant ----
            Paragraph lbl = new Paragraph("Montant remboursé", h2);
            doc.add(lbl);
            Paragraph amt = new Paragraph(fmtXof(cn.getAmount()), new Font(Font.HELVETICA, 14, Font.BOLD));
            doc.add(amt);

            doc.add(Chunk.NEWLINE);

            // ---- Pied de page simple ----
            Paragraph foot = new Paragraph("Document généré automatiquement - Kora Market", text);
            foot.setAlignment(Element.ALIGN_RIGHT);
            doc.add(foot);

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new BusinessException("Erreur génération PDF Avoir: " + e.getMessage());
        }
    }

    /* ---------------- helpers ---------------- */

    private static void addKV(PdfPTable table, String k, String v, Font keyFont, Font valFont) {
        PdfPCell c1 = new PdfPCell(new Phrase(nz(k), keyFont));
        PdfPCell c2 = new PdfPCell(new Phrase(nz(v), valFont));
        c1.setBorder(Rectangle.NO_BORDER);
        c2.setBorder(Rectangle.NO_BORDER);
        table.addCell(c1);
        table.addCell(c2);
    }

    private static String nz(String s) { return (s == null || s.isBlank()) ? "-" : s; }

    private static String fmtDate(java.time.Instant instant) {
        if (instant == null) return "-";
        var fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withLocale(Locale.FRANCE)
                .withZone(ZoneId.systemDefault());
        return fmt.format(instant);
    }

    // Montants internes en centimes -> XOF sans décimales
    private static String fmtXof(Long cents) {
        if (cents == null) return "0 XOF";
        long units = Math.round(cents / 100.0);
        DecimalFormatSymbols sym = new DecimalFormatSymbols(Locale.FRANCE);
        sym.setGroupingSeparator('\u202F'); // espace fine insécable
        DecimalFormat df = new DecimalFormat("#,##0", sym);
        return df.format(units) + " XOF";
    }
}
