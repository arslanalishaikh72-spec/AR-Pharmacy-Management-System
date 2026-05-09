package arpharmacy.panels;

import arpharmacy.Theme;
import arpharmacy.UIHelper;
import arpharmacy.db.DBConnection;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;

/**
 * DeleteMedicinePanel – select a row, click Delete. No add, no update.
 */
public class DeleteMedicinePanel extends JPanel {

    private final CardLayout      cardLayout;
    private final JPanel          container;
    private DefaultTableModel     tableModel;
    private JTable                table;
    private JLabel                lblPreview;
    private JButton               btnDelete;

    public DeleteMedicinePanel(CardLayout cardLayout, JPanel container) {
        this.cardLayout = cardLayout;
        this.container  = container;
        setName("DELETE_MED");
        buildUI();
    }

    public void loadData() {
        tableModel.setRowCount(0);
        try {
            Connection conn = DBConnection.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT med_id, name, category, price, quantity, min_stock FROM medicines ORDER BY name");
            while (rs.next()) {
                int qty = rs.getInt("quantity");
                int min = rs.getInt("min_stock");
                tableModel.addRow(new Object[]{
                    rs.getInt("med_id"),
                    rs.getString("name"),
                    rs.getString("category"),
                    String.format("Rs %.2f", rs.getDouble("price")),
                    qty, min,
                    qty == 0 ? "OUT" : qty < min ? "LOW" : "OK"
                });
            }
            rs.close();
        } catch (SQLException ex) {
            UIHelper.error(this, "DB Error: " + ex.getMessage());
        }
        lblPreview.setText("← Select a medicine to delete");
        btnDelete.setEnabled(false);
    }

    private void buildUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(245, 248, 250));
        add(UIHelper.createHeader("AR Pharmacy System", "Delete Medicine"), BorderLayout.NORTH);

        // ── Table ─────────────────────────────────────────────────────────────
        String[] cols = {"ID","Medicine Name","Category","Price","Stock","Min","Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UIHelper.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] widths = {40,200,110,90,60,55,70};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Status renderer
        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t,v,sel,foc,r,c);
                if (!sel) {
                    String s = (String)v;
                    if ("OUT".equals(s))      { comp.setBackground(new Color(255,230,230)); comp.setForeground(new Color(180,30,30)); }
                    else if ("LOW".equals(s)) { comp.setBackground(new Color(255,245,215)); comp.setForeground(new Color(160,100,0)); }
                    else                      { comp.setBackground(new Color(230,255,240)); comp.setForeground(new Color(0,130,60)); }
                }
                ((JLabel)comp).setFont(new Font("Segoe UI",Font.BOLD,11));
                ((JLabel)comp).setHorizontalAlignment(SwingConstants.CENTER);
                return comp;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        // ── Bottom confirmation panel ──────────────────────────────────────────
        JPanel confirmPanel = buildConfirmPanel();

        JPanel centre = new JPanel(new BorderLayout(0,0));
        centre.setBackground(new Color(245,248,250));
        centre.setBorder(BorderFactory.createEmptyBorder(16,20,0,20));

        JLabel tableHdr = sectionHdr("🗑  Select a medicine to delete");
        centre.add(tableHdr, BorderLayout.NORTH);
        centre.add(scroll,   BorderLayout.CENTER);
        centre.add(confirmPanel, BorderLayout.SOUTH);
        add(centre, BorderLayout.CENTER);

        // ── Toolbar ───────────────────────────────────────────────────────────
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT,12,8));
        bar.setBackground(new Color(245,248,250));
        bar.setBorder(BorderFactory.createMatteBorder(1,0,0,0,new Color(200,215,208)));
        JButton btnRef  = roundBtn("🔄  Refresh", new Color(30,110,200), Color.WHITE);
        JButton btnBack = roundBtn("←  Back to Admin", new Color(100,110,106), Color.WHITE);
        bar.add(btnRef); bar.add(btnBack);
        add(bar, BorderLayout.SOUTH);

        // Listeners
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onRowSelect();
        });
        btnRef.addActionListener(e  -> loadData());
        btnBack.addActionListener(e -> cardLayout.show(container, "ADMIN"));
    }

    private JPanel buildConfirmPanel() {
        JPanel panel = new JPanel(new BorderLayout(14, 0));
        panel.setBackground(new Color(255, 245, 245));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(210, 45, 45, 60)),
            BorderFactory.createEmptyBorder(14, 16, 14, 16)));

        JPanel infoBox = new JPanel(new GridLayout(2,1,0,3));
        infoBox.setOpaque(false);
        lblPreview = new JLabel("← Select a medicine to delete");
        lblPreview.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPreview.setForeground(new Color(100, 40, 40));
        JLabel hint = new JLabel("The medicine will be permanently removed from the database.");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hint.setForeground(new Color(160, 80, 80));
        infoBox.add(lblPreview);
        infoBox.add(hint);
        panel.add(infoBox, BorderLayout.CENTER);

        btnDelete = new JButton("🗑  Delete Medicine") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = !isEnabled() ? new Color(200,200,200)
                           : getModel().isRollover() ? new Color(190,30,30)
                           : getModel().isPressed() ? new Color(150,20,20)
                           : new Color(210,45,45);
                g2.setColor(base);
                g2.fill(new RoundRectangle2D.Double(0,0,getWidth(),getHeight(),10,10));
                g2.dispose();
                super.paintComponent(g);
            }
            @Override protected void paintBorder(Graphics g) {}
            @Override public boolean isOpaque() { return false; }
        };
        btnDelete.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setFocusPainted(false);
        btnDelete.setContentAreaFilled(false);
        btnDelete.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnDelete.setPreferredSize(new Dimension(180, 40));
        btnDelete.setEnabled(false);
        btnDelete.addActionListener(e -> doDelete());
        panel.add(btnDelete, BorderLayout.EAST);
        return panel;
    }

    private void onRowSelect() {
        int row = table.getSelectedRow();
        if (row < 0) {
            lblPreview.setText("← Select a medicine to delete");
            btnDelete.setEnabled(false);
        } else {
            String name = (String) tableModel.getValueAt(row, 1);
            lblPreview.setText("Selected:  " + name);
            btnDelete.setEnabled(true);
        }
    }

    private void doDelete() {
        int row = table.getSelectedRow();
        if (row < 0) { UIHelper.error(this, "Select a medicine first."); return; }
        int    medId = (int)    tableModel.getValueAt(row, 0);
        String name  = (String) tableModel.getValueAt(row, 1);
        if (!UIHelper.confirm(this, "Permanently delete \"" + name + "\"?\n\nThis cannot be undone.")) return;
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement("DELETE FROM medicines WHERE med_id=?");
            ps.setInt(1, medId); ps.executeUpdate(); ps.close();
            UIHelper.success(this, "✅  \"" + name + "\" deleted successfully.");
            loadData();
        } catch (SQLException ex) {
            UIHelper.error(this, "DB Error: " + ex.getMessage());
        }
    }

    private JLabel sectionHdr(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(new Color(60,80,70));
        l.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0,0,1,0,new Color(200,215,208)),
            BorderFactory.createEmptyBorder(0,0,8,0)));
        return l;
    }

    private JButton roundBtn(String text, Color bg, Color fg) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover()?bg.brighter():getModel().isPressed()?bg.darker():bg);
                g2.fill(new RoundRectangle2D.Double(0,0,getWidth(),getHeight(),10,10));
                g2.dispose(); super.paintComponent(g);
            }
            @Override protected void paintBorder(Graphics g) {}
            @Override public boolean isOpaque() { return false; }
        };
        b.setFont(new Font("Segoe UI",Font.BOLD,12));
        b.setForeground(fg); b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(160, 34));
        return b;
    }
}
