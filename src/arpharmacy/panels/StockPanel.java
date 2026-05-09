package arpharmacy.panels;

import arpharmacy.Theme;
import arpharmacy.UIHelper;
import arpharmacy.db.DBConnection;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

/**
 * StockPanel – shows all medicines with colour-coded stock status.
 *   Green  = OK (qty >= min_stock)
 *   Orange = Low stock (0 < qty < min_stock)
 *   Red    = Out of stock (qty == 0)
 */
public class StockPanel extends JPanel {

    private final CardLayout      cardLayout;
    private final JPanel          container;
    private DefaultTableModel     model;
    private JTable                table;
    private JLabel                lblSummary;

    private static final Color OK_BG   = new Color(230, 255, 240);
    private static final Color LOW_BG  = new Color(255, 245, 220);
    private static final Color OUT_BG  = new Color(255, 230, 230);
    private static final Color OK_FG   = new Color(0, 130, 60);
    private static final Color LOW_FG  = new Color(160, 100, 0);
    private static final Color OUT_FG  = new Color(180, 30, 30);

    public StockPanel(CardLayout cardLayout, JPanel container) {
        this.cardLayout = cardLayout;
        this.container  = container;
        setName("STOCK");
        buildUI();
    }

    public void loadData() {
        model.setRowCount(0);
        int total = 0, ok = 0, low = 0, out = 0;
        try {
            Connection conn = DBConnection.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT med_id, name, category, price, quantity, min_stock FROM medicines ORDER BY name");
            while (rs.next()) {
                int qty   = rs.getInt("quantity");
                int minSt = rs.getInt("min_stock");
                String status;
                if      (qty == 0)        status = "OUT OF STOCK";
                else if (qty < minSt)     status = "LOW STOCK";
                else                      status = "IN STOCK";

                model.addRow(new Object[]{
                    rs.getInt("med_id"),
                    rs.getString("name"),
                    rs.getString("category"),
                    String.format("Rs %.2f", rs.getDouble("price")),
                    qty,
                    minSt,
                    qty - minSt,     // surplus/deficit
                    status
                });
                total++;
                if      (qty == 0)    out++;
                else if (qty < minSt) low++;
                else                  ok++;
            }
            rs.close();
            lblSummary.setText(
                "Total: " + total + "   |   " +
                "In Stock: " + ok + "   |   " +
                "Low Stock: " + low + "   |   " +
                "Out of Stock: " + out
            );
        } catch (SQLException ex) {
            UIHelper.error(this, "DB Error: " + ex.getMessage());
        }
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG_MAIN);

        add(UIHelper.createHeader("AR Pharmacy System", "Stock Status Monitor"), BorderLayout.NORTH);

        // Legend bar
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 8));
        legend.setBackground(new Color(245, 250, 248));
        legend.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER));
        legend.add(legendChip("IN STOCK",    OK_BG,  OK_FG));
        legend.add(legendChip("LOW STOCK",   LOW_BG, LOW_FG));
        legend.add(legendChip("OUT OF STOCK",OUT_BG, OUT_FG));
        legend.add(Box.createHorizontalStrut(20));
        lblSummary = new JLabel("Loading...");
        lblSummary.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSummary.setForeground(Theme.PRIMARY_DARK);
        legend.add(lblSummary);

        // Table
        String[] cols = {"ID", "Medicine Name", "Category", "Price", "Qty", "Min Stock", "Surplus/Deficit", "Status"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        UIHelper.styleTable(table);
        int[] widths = {40, 200, 100, 90, 60, 80, 110, 120};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Custom renderer
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                if (!sel) {
                    String status = (String) model.getValueAt(row, 7);
                    switch (status) {
                        case "OUT OF STOCK": c.setBackground(OUT_BG); c.setForeground(OUT_FG); break;
                        case "LOW STOCK":    c.setBackground(LOW_BG); c.setForeground(LOW_FG); break;
                        default:             c.setBackground(OK_BG);  c.setForeground(OK_FG);  break;
                    }
                }
                // Status column bold
                if (col == 7) ((JLabel)c).setFont(new Font("Segoe UI", Font.BOLD, 12));
                else          ((JLabel)c).setFont(Theme.FONT_TABLE);
                ((JLabel)c).setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return c;
            }
        };
        table.setDefaultRenderer(Object.class, renderer);

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        toolbar.setBackground(Theme.BG_MAIN);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER),
                BorderFactory.createEmptyBorder(4, 16, 4, 16)));
        JButton btnRefresh = UIHelper.accentBtn("Refresh Stock");
        JButton btnBack    = UIHelper.grayBtn("Back to Admin");
        toolbar.add(btnRefresh);
        toolbar.add(btnBack);

        JPanel centre = new JPanel(new BorderLayout());
        centre.setBackground(Theme.BG_MAIN);
        centre.add(legend, BorderLayout.NORTH);
        centre.add(scroll, BorderLayout.CENTER);
        add(centre,  BorderLayout.CENTER);
        add(toolbar, BorderLayout.SOUTH);

        btnRefresh.addActionListener(e -> loadData());
        btnBack.addActionListener(e    -> cardLayout.show(container, "ADMIN"));
    }

    private JPanel legendChip(String text, Color bg, Color fg) {
        JPanel chip = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        chip.setBackground(bg);
        chip.setBorder(BorderFactory.createLineBorder(fg, 1, true));
        chip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(fg, 1, true),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)));
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(fg);
        chip.add(lbl);
        return chip;
    }
}
