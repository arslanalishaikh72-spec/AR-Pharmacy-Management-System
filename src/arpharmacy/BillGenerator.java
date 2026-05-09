package arpharmacy;

import javax.swing.*;
import java.awt.*;
import java.awt.print.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * BillGenerator – generates a pharmacy bill as a printable PDF-style document.
 * Uses Java's built-in Graphics2D + PrinterJob (no external library needed).
 * Also saves an HTML bill that can be opened in any browser and printed to PDF.
 */
public class BillGenerator {

    // ── Bill data model ───────────────────────────────────────────────────────
    public static class BillItem {
        public final String name;
        public final double unitPrice;
        public final int    qty;
        public final double subtotal;
        public BillItem(String name, double unitPrice, int qty) {
            this.name      = name;
            this.unitPrice = unitPrice;
            this.qty       = qty;
            this.subtotal  = unitPrice * qty;
        }
    }

    public static class BillData {
        public int            billId;
        public String         customerName;
        public String         customerUsername;
        public String         customerPhone;
        public String         customerAddress;
        public LocalDateTime  dateTime;
        public List<BillItem> items;
        public double         total;

        public BillData(int billId, String name, String username,
                        String phone, String address,
                        LocalDateTime dt, List<BillItem> items, double total) {
            this.billId           = billId;
            this.customerName     = name;
            this.customerUsername = username;
            this.customerPhone    = phone;
            this.customerAddress  = address;
            this.dateTime         = dt;
            this.items            = items;
            this.total            = total;
        }
    }

    // ── Generate HTML bill (opens in browser, print → Save as PDF) ────────────
    public static String generateHtmlBill(BillData bill) throws IOException {
        String fileName = "Bill_" + bill.billId + "_" +
                bill.dateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".html";
        String filePath = System.getProperty("user.home") + File.separator + fileName;

        DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("dd-MMM-yyyy  hh:mm a");

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
        sb.append("<title>AR Pharmacy Bill #").append(bill.billId).append("</title>");
        sb.append("<style>");
        sb.append("*{margin:0;padding:0;box-sizing:border-box;font-family:'Segoe UI',Arial,sans-serif;}");
        sb.append("body{background:#f0f0f0;padding:20px;}");
        sb.append(".bill{background:white;max-width:720px;margin:0 auto;padding:0;");
        sb.append("       box-shadow:0 4px 20px rgba(0,0,0,0.15);border-radius:8px;overflow:hidden;}");

        // Header
        sb.append(".header{background:linear-gradient(135deg,#00966a,#006644);color:white;padding:30px 36px;}");
        sb.append(".header h1{font-size:28px;letter-spacing:1px;}");
        sb.append(".header .tagline{font-size:12px;opacity:0.8;margin-top:4px;}");
        sb.append(".header .bill-no{font-size:22px;font-weight:bold;float:right;margin-top:-30px;}");
        sb.append(".header .bill-date{font-size:11px;float:right;clear:right;opacity:0.85;}");

        // Cross logo
        sb.append(".logo{display:inline-flex;align-items:center;gap:12px;}");
        sb.append(".cross{font-size:36px;color:#7fffc0;}");

        // Info section
        sb.append(".info-section{display:flex;gap:0;border-bottom:2px solid #e0e0e0;}");
        sb.append(".info-box{flex:1;padding:20px 28px;border-right:1px solid #eee;}");
        sb.append(".info-box:last-child{border-right:none;}");
        sb.append(".info-box h3{font-size:11px;text-transform:uppercase;color:#009966;letter-spacing:1px;margin-bottom:8px;}");
        sb.append(".info-box p{font-size:13px;color:#333;line-height:1.7;}");
        sb.append(".info-box .val{font-weight:600;color:#111;}");

        // Table
        sb.append("table{width:100%;border-collapse:collapse;}");
        sb.append("thead{background:#006644;color:white;}");
        sb.append("thead th{padding:12px 16px;text-align:left;font-size:12px;text-transform:uppercase;letter-spacing:.5px;}");
        sb.append("thead th:last-child{text-align:right;}");
        sb.append("tbody tr:nth-child(even){background:#f7fdf9;}");
        sb.append("tbody tr:hover{background:#e6f7ef;}");
        sb.append("tbody td{padding:11px 16px;font-size:13px;color:#333;border-bottom:1px solid #eee;}");
        sb.append("tbody td:last-child{text-align:right;font-weight:600;}");
        sb.append(".table-wrap{padding:0 0 0 0;}");

        // Totals
        sb.append(".totals{padding:16px 28px;background:#f9fdf9;}");
        sb.append(".total-row{display:flex;justify-content:flex-end;gap:40px;margin-bottom:6px;font-size:13px;color:#555;}");
        sb.append(".total-row.grand{font-size:16px;font-weight:700;color:#006644;border-top:2px solid #009966;padding-top:10px;margin-top:10px;}");
        sb.append(".total-label{min-width:120px;text-align:right;}");
        sb.append(".total-val{min-width:80px;text-align:right;}");

        // Footer
        sb.append(".footer{background:#006644;color:white;padding:16px 28px;text-align:center;font-size:11px;opacity:.9;}");
        sb.append(".stamp{display:inline-block;border:2px solid #00cc88;border-radius:50%;padding:8px 14px;");
        sb.append("  color:#00cc88;font-weight:bold;font-size:13px;margin:10px 0;}");

        sb.append("@media print{body{background:white;padding:0;}.bill{box-shadow:none;max-width:100%;}}");
        sb.append("</style></head><body>");

        sb.append("<div class='bill'>");

        // ── Header ─────────────────────────────────────────────────────────
        sb.append("<div class='header'>");
        sb.append("<div style='overflow:hidden'>");
        sb.append("<div class='logo'><span class='cross'>&#10010;</span>");
        sb.append("<div><h1>AR Pharmacy System</h1>");
        sb.append("<div class='tagline'>Your Health, Our Priority &nbsp;|&nbsp; Quality Medicines Since 2024</div></div></div>");
        sb.append("<div class='bill-no'>BILL #").append(String.format("%05d", bill.billId)).append("</div>");
        sb.append("<div class='bill-date'>").append(bill.dateTime.format(dtFmt)).append("</div>");
        sb.append("</div></div>");

        // ── Customer + Pharmacy Info ────────────────────────────────────────
        sb.append("<div class='info-section'>");
        sb.append("<div class='info-box'><h3>&#128100; Customer Information</h3>");
        sb.append("<p>Name: <span class='val'>").append(esc(bill.customerName)).append("</span></p>");
        sb.append("<p>Username: <span class='val'>").append(esc(bill.customerUsername)).append("</span></p>");
        if (!bill.customerPhone.isEmpty())
            sb.append("<p>Phone: <span class='val'>").append(esc(bill.customerPhone)).append("</span></p>");
        if (!bill.customerAddress.isEmpty())
            sb.append("<p>Address: <span class='val'>").append(esc(bill.customerAddress)).append("</span></p>");
        sb.append("</div>");

        sb.append("<div class='info-box'><h3>&#128138; Pharmacy Information</h3>");
        sb.append("<p>Name: <span class='val'>AR Pharmacy System</span></p>");
        sb.append("<p>Address: <span class='val'>Main Market, Sukkur, Sindh</span></p>");
        sb.append("<p>Phone: <span class='val'>+92-71-1234567</span></p>");
        sb.append("<p>Email: <span class='val'>info@arpharmacy.pk</span></p>");
        sb.append("</div>");

        sb.append("<div class='info-box'><h3>&#128203; Bill Details</h3>");
        sb.append("<p>Bill No: <span class='val'>#").append(String.format("%05d", bill.billId)).append("</span></p>");
        sb.append("<p>Date: <span class='val'>").append(bill.dateTime.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"))).append("</span></p>");
        sb.append("<p>Time: <span class='val'>").append(bill.dateTime.format(DateTimeFormatter.ofPattern("hh:mm a"))).append("</span></p>");
        sb.append("<p>Items: <span class='val'>").append(bill.items.size()).append("</span></p>");
        sb.append("</div>");
        sb.append("</div>");

        // ── Items Table ─────────────────────────────────────────────────────
        sb.append("<div class='table-wrap'>");
        sb.append("<table><thead><tr>");
        sb.append("<th>#</th><th>Medicine</th><th>Unit Price (Rs)</th><th>Qty</th><th>Subtotal (Rs)</th>");
        sb.append("</tr></thead><tbody>");

        int idx = 1;
        for (BillItem item : bill.items) {
            sb.append("<tr>");
            sb.append("<td>").append(idx++).append("</td>");
            sb.append("<td>&#128138; ").append(esc(item.name)).append("</td>");
            sb.append("<td>").append(String.format("%.2f", item.unitPrice)).append("</td>");
            sb.append("<td>").append(item.qty).append("</td>");
            sb.append("<td>Rs ").append(String.format("%.2f", item.subtotal)).append("</td>");
            sb.append("</tr>");
        }
        sb.append("</tbody></table></div>");

        // ── Totals ──────────────────────────────────────────────────────────
        double tax = bill.total * 0.00;  // 0% for now – adjust if needed
        sb.append("<div class='totals'>");
        sb.append("<div class='total-row'><span class='total-label'>Subtotal:</span>");
        sb.append("<span class='total-val'>Rs ").append(String.format("%.2f", bill.total)).append("</span></div>");
        sb.append("<div class='total-row'><span class='total-label'>Tax (0%):</span>");
        sb.append("<span class='total-val'>Rs 0.00</span></div>");
        sb.append("<div class='total-row grand'><span class='total-label'>TOTAL AMOUNT:</span>");
        sb.append("<span class='total-val'>Rs ").append(String.format("%.2f", bill.total)).append("</span></div>");
        sb.append("</div>");

        // ── Footer ──────────────────────────────────────────────────────────
        sb.append("<div style='text-align:center;padding:16px;'>");
        sb.append("<div class='stamp'>&#10004; PAID</div>");
        sb.append("</div>");
        sb.append("<div class='footer'>");
        sb.append("Thank you for choosing AR Pharmacy System &nbsp;|&nbsp; ");
        sb.append("Get well soon! &nbsp;|&nbsp; ");
        sb.append("Please keep this bill for your records.<br>");
        sb.append("<small>&#10010; AR Pharmacy &bull; Your Health, Our Priority &bull; Sukkur, Sindh, Pakistan</small>");
        sb.append("</div>");

        sb.append("</div>");  // .bill

        // Print button
        sb.append("<div style='text-align:center;margin:16px;'>");
        sb.append("<button onclick='window.print()' style='background:#006644;color:white;border:none;");
        sb.append("padding:12px 32px;font-size:14px;border-radius:6px;cursor:pointer;'>");
        sb.append("&#128438; Print / Save as PDF</button></div>");

        sb.append("</body></html>");

        // Write file
        try (FileWriter fw = new FileWriter(filePath)) {
            fw.write(sb.toString());
        }
        return filePath;
    }

    // ── Print bill via Java PrinterJob ────────────────────────────────────────
    public static void printBill(BillData bill, Component parent) {
        PrinterJob job = PrinterJob.getPrinterJob();
        PageFormat pf = job.defaultPage();
        pf.setOrientation(PageFormat.PORTRAIT);

        job.setPrintable((graphics, pageFormat, pageIndex) -> {
            if (pageIndex > 0) return Printable.NO_SUCH_PAGE;
            Graphics2D g2 = (Graphics2D) graphics;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

            float pw = (float) pageFormat.getImageableWidth();
            float y  = 0;

            // Header background
            g2.setColor(new Color(0, 102, 68));
            g2.fillRoundRect(0, 0, (int)pw, 70, 8, 8);

            // Title
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 20));
            g2.drawString("✚  AR Pharmacy System", 16, 28);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2.setColor(new Color(180, 240, 210));
            g2.drawString("Your Health, Our Priority  |  Bill #" + String.format("%05d", bill.billId), 16, 46);

            DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("dd-MMM-yyyy  hh:mm a");
            g2.drawString(bill.dateTime.format(dtFmt), 16, 62);
            y = 82;

            // Customer info box
            g2.setColor(new Color(240, 250, 245));
            g2.fillRoundRect(0, (int)y, (int)pw, 50, 6, 6);
            g2.setColor(new Color(0, 120, 70));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            g2.drawString("CUSTOMER", 12, (int)y + 14);
            g2.setColor(new Color(30, 40, 35));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g2.drawString(bill.customerName + "  |  " + bill.customerUsername +
                    (bill.customerPhone.isEmpty() ? "" : "  |  " + bill.customerPhone), 12, (int)y + 30);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2.setColor(new Color(90, 105, 98));
            g2.drawString("AR Pharmacy  |  Main Market, Sukkur, Sindh  |  +92-71-1234567", 12, (int)y + 44);
            y += 60;

            // Table header
            g2.setColor(new Color(0, 100, 60));
            g2.fillRect(0, (int)y, (int)pw, 22);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            float[] colX = {6, 30, pw*0.55f, pw*0.68f, pw*0.80f};
            g2.drawString("#",           colX[0], (int)y+15);
            g2.drawString("Medicine",    colX[1], (int)y+15);
            g2.drawString("Unit Price",  colX[2], (int)y+15);
            g2.drawString("Qty",         colX[3], (int)y+15);
            g2.drawString("Subtotal",    colX[4], (int)y+15);
            y += 24;

            // Table rows
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            int rowNum = 1;
            for (BillItem item : bill.items) {
                if (rowNum % 2 == 0) {
                    g2.setColor(new Color(240, 252, 246));
                    g2.fillRect(0, (int)y-12, (int)pw, 18);
                }
                g2.setColor(new Color(30, 40, 35));
                g2.drawString(String.valueOf(rowNum++), colX[0], (int)y);
                g2.drawString(item.name.length() > 28 ? item.name.substring(0,28) + "…" : item.name, colX[1], (int)y);
                g2.drawString(String.format("Rs %.2f", item.unitPrice), colX[2], (int)y);
                g2.drawString(String.valueOf(item.qty), colX[3], (int)y);
                g2.drawString(String.format("Rs %.2f", item.subtotal), colX[4], (int)y);
                y += 18;
            }

            // Separator
            g2.setColor(new Color(0, 140, 80));
            g2.drawLine(0, (int)y+2, (int)pw, (int)y+2);
            y += 10;

            // Total
            g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
            g2.setColor(new Color(0, 100, 60));
            String totalStr = "TOTAL:  Rs " + String.format("%.2f", bill.total);
            g2.drawString(totalStr, pw - g2.getFontMetrics().stringWidth(totalStr) - 8, (int)y+14);
            y += 30;

            // Footer
            g2.setColor(new Color(0, 102, 68));
            g2.fillRoundRect(0, (int)y, (int)pw, 30, 6, 6);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            String footer = "Thank you for choosing AR Pharmacy  ★  Get Well Soon!";
            g2.drawString(footer, (pw - g2.getFontMetrics().stringWidth(footer)) / 2, (int)y + 19);

            return Printable.PAGE_EXISTS;
        }, pf);

        if (job.printDialog()) {
            try { job.print(); }
            catch (PrinterException ex) {
                JOptionPane.showMessageDialog(parent, "Print error: " + ex.getMessage(),
                        "Print Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }
}
