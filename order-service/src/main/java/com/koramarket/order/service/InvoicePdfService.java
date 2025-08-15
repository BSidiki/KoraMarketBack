package com.koramarket.order.service;

import com.koramarket.common.exceptions.BusinessException;
import com.koramarket.order.model.Invoice;
import com.koramarket.order.model.Order;
import com.koramarket.order.repository.InvoiceRepository;
import com.koramarket.order.repository.OrderRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvoicePdfService {

    private final InvoiceRepository invoiceRepo;
    private final OrderRepository orderRepo;

    @Value("${company.name:}")
    private String companyName;
    @Value("${company.rc:}")
    private String companyRc;
    @Value("${company.ifu:}")
    private String companyIfu;
    @Value("${company.address:}")
    private String companyAddress;
    @Value("${company.phone:}")
    private String companyPhone;
    @Value("${company.email:}")
    private String companyEmail;
    @Value("${company.logo-url:}")
    private String logoUrl;

    @Transactional(readOnly = true)
    public byte[] renderByInvoiceId(UUID invoiceId) {
        Invoice inv = invoiceRepo.findById(invoiceId)
                .orElseThrow(() -> new BusinessException("Facture introuvable"));

        Order order = orderRepo.findOneWithItems(inv.getOrder().getId())
                .orElseGet(() -> orderRepo.findById(inv.getOrder().getId())
                        .orElseThrow(() -> new BusinessException("Commande introuvable")));

        return render(inv, order);
    }

    private byte[] render(Invoice inv, Order o) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            // --------- En-tête société ----------
            PdfPTable header = new PdfPTable(new float[]{1f, 2.5f});
            header.setWidthPercentage(100);

            PdfPCell logoCell = new PdfPCell();
            logoCell.setBorder(Rectangle.NO_BORDER);
            Image logo = tryLoadLogo();
            if (logo != null) {
                logo.scaleToFit(90, 90);
                logoCell.addElement(logo);
            } else {
                Paragraph p = new Paragraph(nvl(companyName, "Kora Market"),
                        new Font(Font.HELVETICA, 16, Font.BOLD));
                logoCell.addElement(p);
            }
            header.addCell(logoCell);

            PdfPCell coCell = new PdfPCell();
            coCell.setBorder(Rectangle.NO_BORDER);
            coCell.addElement(pBold(nvl(companyName, "Kora Market"), 14));
            coCell.addElement(pSmall(nvl(companyRc, "")));
            coCell.addElement(pSmall(nvl(companyIfu, "")));
            coCell.addElement(pSmall(nvl(companyAddress, "")));
            coCell.addElement(pSmall(nvl(companyPhone, "")));
            coCell.addElement(pSmall(nvl(companyEmail, "")));
            header.addCell(coCell);

            doc.add(header);
            doc.add(spacer(10));

            // --------- Titre facture ----------
            Paragraph title = new Paragraph("FACTURE " + inv.getInvoiceNumber(),
                    new Font(Font.HELVETICA, 16, Font.BOLD));
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);
            doc.add(spacer(6));

            // --------- Infos commande ----------
            PdfPTable meta = new PdfPTable(new float[]{1.2f, 2.2f, 1.2f, 2.2f});
            meta.setWidthPercentage(100);
            meta.addCell(kv("Date émission", String.valueOf(inv.getIssuedAt())));
            meta.addCell(kv("N° Commande", o.getOrderNumber()));
            meta.addCell(kv("Client (UUID)", String.valueOf(o.getUserIdExt())));
            meta.addCell(kv("Devise", nvl(o.getCurrency(), "XOF")));
            doc.add(meta);
            doc.add(spacer(8));

            // --------- Tableau des articles ----------
            PdfPTable table = new PdfPTable(new float[]{3.5f, 1.6f, 0.9f, 1.6f, 1.2f, 1.8f});
            table.setWidthPercentage(100);
            addHeader(table, "Produit", "SKU", "Qté", "PU (XOF)", "TVA", "Total (XOF)");

            long sub = nz(o.getSubtotalAmount());
            long tax = nz(o.getTaxTotalAmount());
            long ship = nz(o.getShippingTotalAmount());
            long disc = nz(o.getDiscountTotalAmount());
            long grand = nz(o.getGrandTotalAmount());

            o.getItems().forEach(it -> {
                table.addCell(cellLeft(nvl(it.getProductNameSnap(), "-")));
                table.addCell(cellLeft(nvl(it.getProductSkuSnap(), "-")));
                table.addCell(cellRight(String.valueOf(nzInt(it.getQuantity()))));
                table.addCell(cellRight(fmtXof(nz(it.getUnitPriceAmount()))));
                table.addCell(cellRight(fmtXof(nz(it.getTaxAmount()))));
                table.addCell(cellRight(fmtXof(nz(it.getLineTotalAmount()))));
            });
            doc.add(table);
            doc.add(spacer(8));

            // --------- Totaux ----------
            PdfPTable totals = new PdfPTable(new float[]{3.7f, 1.6f});
            totals.setWidthPercentage(45);
            totals.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totals.addCell(totalRow("Sous-total", fmtXof(sub), false));
            totals.addCell(totalRow("TVA", fmtXof(tax), false));
            totals.addCell(totalRow("Livraison", fmtXof(ship), false));
            totals.addCell(totalRow("Remise", "-" + fmtXof(disc), false));
            totals.addCell(totalRow("TOTAL TTC", fmtXof(grand), true));
            doc.add(totals);

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new BusinessException("Erreur génération PDF: " + e.getMessage());
        }
    }

    /* ===== Helpers style & format ===== */

    private static Paragraph pBold(String txt, int size) {
        return new Paragraph(txt, new Font(Font.HELVETICA, size, Font.BOLD));
    }
    private static Paragraph pSmall(String txt) {
        return new Paragraph(txt, new Font(Font.HELVETICA, 10));
    }
    private static Paragraph spacer(float h) {
        Paragraph p = new Paragraph(" ");
        p.setSpacingBefore(h);
        return p;
    }

    private PdfPCell kv(String k, String v) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        Paragraph p = new Paragraph(k + " : " + nvl(v, "-"), new Font(Font.HELVETICA, 10));
        cell.addElement(p);
        return cell;
    }

    private static void addHeader(PdfPTable t, String... labels) {
        for (String l : labels) {
            PdfPCell c = new PdfPCell(new Phrase(l, new Font(Font.HELVETICA, 10, Font.BOLD)));
            c.setHorizontalAlignment(Element.ALIGN_LEFT);
            c.setGrayFill(0.93f);
            c.setPadding(6);
            t.addCell(c);
        }
    }

    private static PdfPCell cellLeft(String s) {
        PdfPCell c = new PdfPCell(new Phrase(s, new Font(Font.HELVETICA, 10)));
        c.setHorizontalAlignment(Element.ALIGN_LEFT);
        c.setPadding(5);
        return c;
    }
    private static PdfPCell cellRight(String s) {
        PdfPCell c = new PdfPCell(new Phrase(s, new Font(Font.HELVETICA, 10)));
        c.setHorizontalAlignment(Element.ALIGN_RIGHT);
        c.setPadding(5);
        return c;
    }

    private static PdfPCell totalRow(String label, String value, boolean bold) {
        PdfPCell l = new PdfPCell(new Phrase(label, new Font(Font.HELVETICA, bold?11:10, bold?Font.BOLD:Font.NORMAL)));
        l.setBorder(Rectangle.NO_BORDER);
        l.setHorizontalAlignment(Element.ALIGN_LEFT);
        l.setPadding(4);
        PdfPCell v = new PdfPCell(new Phrase(value, new Font(Font.HELVETICA, bold?11:10, bold?Font.BOLD:Font.NORMAL)));
        v.setBorder(Rectangle.NO_BORDER);
        v.setHorizontalAlignment(Element.ALIGN_RIGHT);
        v.setPadding(4);
        PdfPTable row = new PdfPTable(new float[]{1f, 1f});
        row.setWidthPercentage(100);
        row.addCell(l); row.addCell(v);
        PdfPCell wrap = new PdfPCell(row);
        wrap.setBorder(Rectangle.NO_BORDER);
        return wrap;
    }

    private static String nvl(String s, String d) { return (s == null || s.isBlank()) ? d : s; }
    private static int nzInt(Integer i) { return i == null ? 0 : i; }
    private static long nz(Long l) { return l == null ? 0L : l; }

    private static String fmtXof(long cents) {
        // On affiche en XOF entiers (pas de décimales) : cents / 100
        long units = BigDecimal.valueOf(cents).divide(BigDecimal.valueOf(100)).longValue();
        DecimalFormatSymbols sym = new DecimalFormatSymbols(Locale.FRANCE);
        sym.setGroupingSeparator('\u202F'); // Espace fine insécable  (U+202F)
        DecimalFormat df = new DecimalFormat("#,##0", sym);
        return df.format(units) + " XOF";
    }

    private Image tryLoadLogo() {
        try {
            if (logoUrl != null && !logoUrl.isBlank()) {
                return Image.getInstance(logoUrl);
            }
        } catch (Exception ignored) { }
        return null;
    }
}
