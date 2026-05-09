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
 * UpdateMedicinePanel – select a medicine from the table, edit fields, click Update.
 * No add, no delete.
 */
public class UpdateMedicinePanel extends JPanel {

    private final CardLayout      cardLayout;
    private final JPanel          container;

    private DefaultTableModel     tableModel;
    private JTable                table;
    private JTextField            txtMedId, txtName, txtCategory, txtPrice, txtQty, txtMinStock;
    private JLabel                lblSelected;

    public UpdateMedicinePanel(CardLayout cardLayout, JPanel container) {
        this.cardLayout = cardLayout;
        this.container  = container;
        setName("UPDATE_MED");
        buildUI();
    }

    public void loadData() {
        tableModel.setRowCount(0);
        try {
            Connection conn = DBConnection.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT * FROM medicines ORDER BY name");
            while (rs.next()) {
                int qty = rs.getInt("quantity");
                int min = rs.getInt("min_stock");
                tableModel.addRow(new Object[]{
                    rs.getInt("med_id"),
                    rs.getString("name"),
                    rs.getString("category"),
                    rs.getDouble("price"),
                    qty, min,
                    qty == 0 ? "OUT" : qty < min ? "LOW" : "OK"
                });
            }
            rs.close();
        } catch (SQLException ex) {
            UIHelper.error(this, "DB Error: " + ex.getMessage());
        }
    }

    private void buildUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(245, 248, 250));
        add(UIHelper.createHeader("AR Pharmacy System", "Update Medicine"), BorderLayout.NORTH);

        // ── Table (left) ──────────────────────────────────────────────────────
        String[] cols = {"ID","Medicine Name","Category","Price","Stock","Min","Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UIHelper.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] widths = {40,180,100,70,55,55,60};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        styleStatusCol();

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(new Color(245,248,250));
        tablePanel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 8));

        JLabel tblHdr = sectionHdr("📋  Medicine List — Click a row to select");
        tablePanel.add(tblHdr, BorderLayout.NORTH);
        tablePanel.add(scroll, BorderLayout.CENTER);

        // ── Form (right) ──────────────────────────────────────────────────────
        JPanel formPanel = new JPanel(new BorderLayout());
        formPanel.setBackground(new Color(245,248,250));
        formPanel.setBorder(BorderFactory.createEmptyBorder(16, 8, 16, 20));

        JLabel formHdr = sectionHdr("✏️  Edit Details");
        formPanel.add(formHdr, BorderLayout.NORTH);
        formPanel.add(buildForm(), BorderLayout.CENTER);

        // ── Split ─────────────────────────────────────────────────────────────
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tablePanel, formPanel);
        split.setDividerLocation(520);
        split.setDividerSize(4);
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setBackground(new Color(245,248,250));
        add(split, BorderLayout.CENTER);

        // ── Bottom toolbar ────────────────────────────────────────────────────
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        bar.setBackground(new Color(245,248,250));
        bar.setBorder(BorderFactory.createMatteBorder(1,0,0,0,new Color(200,215,208)));
        JButton btnRef  = roundBtn("🔄  Refresh", new Color(30,110,200), Color.WHITE);
        JButton btnBack = roundBtn("←  Back to Admin", new Color(100,110,106), Color.WHITE);
        bar.add(btnRef); bar.add(btnBack);
        add(bar, BorderLayout.SOUTH);

        // ── Listeners ─────────────────────────────────────────────────────────
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillForm();
        });
        btnRef.addActionListener(e  -> loadData());
        btnBack.addActionListener(e -> cardLayout.show(container, "ADMIN"));
    }

    private JPanel buildForm() {
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 14, 14));
                g2.setColor(new Color(200, 130, 0));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), 5, 4, 4));
                g2.dispose();
            }
            @Override public boolean isOpaque() { return false; }
        };
        card.setBorder(BorderFactory.createCompoundBorder(
            new AddMedicinePanel.ShadowBorder(),
            BorderFactory.createEmptyBorder(24, 28, 24, 28)));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;

        // Selected label
        gc.gridx=0; gc.gridy=0; gc.gridwidth=2; gc.insets=new Insets(0,0,14,0);
        lblSelected = new JLabel("← Select a medicine from the list");
        lblSelected.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblSelected.setForeground(new Color(140, 155, 148));
        card.add(lblSelected, gc);

        txtMedId    = hiddenField();
        txtName     = formField();
        txtCategory = formField();
        txtPrice    = formField();
        txtQty      = formField();
        txtMinStock = formField();

        addRow(card, gc, 1,  "Med ID",            txtMedId);
        addRow(card, gc, 3,  "Medicine Name *",   txtName);
        addRow(card, gc, 5,  "Category",          txtCategory);
        addRow(card, gc, 7,  "Price (Rs) *",      txtPrice);
        addRow(card, gc, 9,  "Quantity *",        txtQty);
        addRow(card, gc, 11, "Min Stock Alert",   txtMinStock);

        // Single update button
        gc.gridy=13; gc.insets=new Insets(22,0,0,0);
        JButton btnUpdate = roundBtn("✏️  Update Medicine", new Color(200, 130, 0), Color.WHITE);
        btnUpdate.setPreferredSize(new Dimension(280, 42));
        btnUpdate.setFont(new Font("Segoe UI", Font.BOLD, 14));
        card.add(btnUpdate, gc);

        btnUpdate.addActionListener(e -> doUpdate());
        return card;
    }

    private void fillForm() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        txtMedId.setText(tableModel.getValueAt(row,0).toString());
        txtName.setText(tableModel.getValueAt(row,1).toString());
        txtCategory.setText(tableModel.getValueAt(row,2).toString());
        txtPrice.setText(tableModel.getValueAt(row,3).toString());
        txtQty.setText(tableModel.getValueAt(row,4).toString());
        txtMinStock.setText(tableModel.getValueAt(row,5).toString());
        lblSelected.setText("Editing:  " + tableModel.getValueAt(row,1));
        lblSelected.setForeground(new Color(0, 130, 80));
        lblSelected.setFont(new Font("Segoe UI", Font.BOLD, 12));
    }

    private void doUpdate() {
        String idStr = txtMedId.getText().trim();
        if (idStr.isEmpty()) { UIHelper.error(this, "Select a medicine from the list first."); return; }
        String name = txtName.getText().trim();
        if (name.isEmpty()) { UIHelper.error(this, "Medicine name is required."); return; }
        try {
            int    medId = Integer.parseInt(idStr);
            String cat   = txtCategory.getText().trim();
            double price = Double.parseDouble(txtPrice.getText().trim());
            int    qty   = Integer.parseInt(txtQty.getText().trim());
            int    minSt = txtMinStock.getText().trim().isEmpty() ? 10 : Integer.parseInt(txtMinStock.getText().trim());

            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE medicines SET name=?,category=?,price=?,quantity=?,min_stock=? WHERE med_id=?");
            ps.setString(1,name); ps.setString(2,cat.isEmpty()?"General":cat);
            ps.setDouble(3,price); ps.setInt(4,qty); ps.setInt(5,minSt); ps.setInt(6,medId);
            int rows = ps.executeUpdate(); ps.close();

            if (rows == 0) UIHelper.error(this, "Medicine not found.");
            else { UIHelper.success(this, "✅  Medicine updated successfully!"); loadData(); }
        } catch (NumberFormatException ex) {
            UIHelper.error(this, "Invalid number format in Price/Qty/Min Stock.");
        } catch (SQLException ex) {
            UIHelper.error(this, "DB Error: " + ex.getMessage());
        }
    }

    // helpers
    private void styleStatusCol() {
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
    }

    private void addRow(JPanel p, GridBagConstraints gc, int row, String label, JTextField field) {
        gc.gridy=row; gc.gridx=0; gc.gridwidth=2; gc.insets=new Insets(6,0,2,0);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(new Color(80,95,88));
        p.add(lbl, gc);
        gc.gridy=row+1; gc.insets=new Insets(0,0,4,0);
        field.setPreferredSize(new Dimension(280, 36));
        p.add(field, gc);
    }

    private JTextField formField() {
        JTextField f = new JTextField();
        f.setFont(new Font("Segoe UI",Font.PLAIN,13));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200,215,208),1,true),
            BorderFactory.createEmptyBorder(6,10,6,10)));
        return f;
    }

    private JTextField hiddenField() {
        JTextField f = formField();
        f.setEditable(false);
        f.setBackground(new Color(245,248,246));
        f.setForeground(new Color(120,135,128));
        return f;
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
                g2.dispose();
                super.paintComponent(g);
            }
            @Override protected void paintBorder(Graphics g) {}
            @Override public boolean isOpaque() { return false; }
        };
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(160, 34));
        return b;
    }
}
