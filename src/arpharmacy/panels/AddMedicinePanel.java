package arpharmacy.panels;

import arpharmacy.Theme;
import arpharmacy.UIHelper;
import arpharmacy.db.DBConnection;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;

/**
 * AddMedicinePanel – only adds a new medicine. No update, no delete.
 */
public class AddMedicinePanel extends JPanel {

    private final CardLayout cardLayout;
    private final JPanel     container;

    private JTextField txtName, txtCategory, txtPrice, txtQty, txtMinStock;

    public AddMedicinePanel(CardLayout cardLayout, JPanel container) {
        this.cardLayout = cardLayout;
        this.container  = container;
        setName("ADD_MED");
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 248, 250));
        add(UIHelper.createHeader("AR Pharmacy System", "Add New Medicine"), BorderLayout.NORTH);

        // ── Form card ─────────────────────────────────────────────────────────
        JPanel card = buildCard();
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(new Color(245, 248, 250));
        wrapper.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));
        wrapper.add(card);
        add(wrapper, BorderLayout.CENTER);
    }

    private JPanel buildCard() {
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));
                // top accent bar
                g2.setColor(new Color(0, 150, 90));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), 6, 4, 4));
                g2.dispose();
            }
            @Override public boolean isOpaque() { return false; }
        };
        card.setPreferredSize(new Dimension(480, 460));
        card.setBorder(BorderFactory.createCompoundBorder(
            new ShadowBorder(),
            BorderFactory.createEmptyBorder(28, 36, 28, 36)));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(6, 0, 6, 0);

        // Header icon + title
        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2; gc.anchor = GridBagConstraints.CENTER;
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        titleRow.setOpaque(false);
        JLabel icon = new JLabel("💊");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
        JLabel title = new JLabel("Add New Medicine");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(0, 120, 70));
        titleRow.add(icon); titleRow.add(title);
        card.add(titleRow, gc);

        gc.gridy = 1; gc.insets = new Insets(2, 0, 18, 0);
        JLabel sub = new JLabel("Fill in medicine details and click Save", SwingConstants.CENTER);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(120, 135, 128));
        card.add(sub, gc);

        gc.gridwidth = 1; gc.anchor = GridBagConstraints.WEST; gc.insets = new Insets(6, 0, 2, 0);

        txtName     = styledField("e.g. Paracetamol 500mg");
        txtCategory = styledField("e.g. Analgesic");
        txtPrice    = styledField("e.g. 15.00");
        txtQty      = styledField("e.g. 100");
        txtMinStock = styledField("e.g. 20  (default: 10)");

        addFormRow(card, gc, 2,  "Medicine Name *",    txtName);
        addFormRow(card, gc, 4,  "Category",           txtCategory);
        addFormRow(card, gc, 6,  "Price (Rs) *",       txtPrice);
        addFormRow(card, gc, 8,  "Quantity *",         txtQty);
        addFormRow(card, gc, 10, "Min Stock Alert",    txtMinStock);

        // Buttons row
        gc.gridy = 12; gc.gridx = 0; gc.gridwidth = 1;
        gc.insets = new Insets(22, 0, 0, 8);
        JButton btnSave = actionBtn("💾  Save Medicine", new Color(0, 150, 90), Color.WHITE);
        card.add(btnSave, gc);

        gc.gridx = 1; gc.insets = new Insets(22, 8, 0, 0);
        JButton btnBack = actionBtn("← Back to Admin", new Color(100, 110, 106), Color.WHITE);
        card.add(btnBack, gc);

        btnSave.addActionListener(e -> saveMedicine());
        btnBack.addActionListener(e -> { clearForm(); cardLayout.show(container, "ADMIN"); });

        return card;
    }

    private void saveMedicine() {
        String name  = txtName.getText().trim();
        String cat   = txtCategory.getText().trim();
        String prStr = txtPrice.getText().trim();
        String qtStr = txtQty.getText().trim();
        String msStr = txtMinStock.getText().trim();

        if (name.isEmpty() || prStr.isEmpty() || qtStr.isEmpty()) {
            UIHelper.error(this, "Name, Price and Quantity are required."); return;
        }
        try {
            double price = Double.parseDouble(prStr);
            int    qty   = Integer.parseInt(qtStr);
            int    minSt = msStr.isEmpty() ? 10 : Integer.parseInt(msStr);
            if (price < 0 || qty < 0) { UIHelper.error(this, "Values must be ≥ 0."); return; }

            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO medicines(name,category,price,quantity,min_stock) VALUES(?,?,?,?,?)");
            ps.setString(1, name);
            ps.setString(2, cat.isEmpty() ? "General" : cat);
            ps.setDouble(3, price);
            ps.setInt(4, qty);
            ps.setInt(5, minSt);
            ps.executeUpdate(); ps.close();

            UIHelper.success(this, "✅  Medicine \"" + name + "\" added successfully!");
            clearForm();
        } catch (NumberFormatException ex) {
            UIHelper.error(this, "Price must be a decimal, Qty and Min Stock must be integers.");
        } catch (SQLException ex) {
            UIHelper.error(this, "DB Error: " + ex.getMessage());
        }
    }

    private void clearForm() {
        txtName.setText(""); txtCategory.setText("");
        txtPrice.setText(""); txtQty.setText(""); txtMinStock.setText("");
    }

    private void addFormRow(JPanel p, GridBagConstraints gc, int row, String label, JTextField field) {
        gc.gridy = row; gc.gridx = 0; gc.weightx = 0; gc.gridwidth = 2;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(60, 80, 70));
        p.add(lbl, gc);
        gc.gridy = row + 1;
        field.setPreferredSize(new Dimension(380, 38));
        p.add(field, gc);
    }

    private JTextField styledField(String placeholder) {
        JTextField f = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(new Color(180, 190, 185));
                    g2.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                    g2.drawString(placeholder, 10, getHeight() / 2 + 5);
                    g2.dispose();
                }
            }
        };
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 215, 208), 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        f.setBackground(Color.WHITE);
        return f;
    }

    private JButton actionBtn(String text, Color bg, Color fg) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = getModel().isRollover() ? bg.brighter() : getModel().isPressed() ? bg.darker() : bg;
                g2.setColor(base);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
            @Override protected void paintBorder(Graphics g) {}
            @Override public boolean isOpaque() { return false; }
        };
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(186, 40));
        return b;
    }

    static class ShadowBorder extends AbstractBorder {
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (int i = 4; i >= 0; i--) {
                g2.setColor(new Color(0, 60, 30, 18 - i * 3));
                g2.draw(new RoundRectangle2D.Double(x+i, y+i, w-i*2-1, h-i*2-1, 16, 16));
            }
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(5,5,5,5); }
    }
}
