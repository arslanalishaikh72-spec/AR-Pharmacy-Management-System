package arpharmacy.panels;

import arpharmacy.Theme;
import arpharmacy.UIHelper;
import arpharmacy.db.DBConnection;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

/**
 * MedicinePanel v2 - full admin CRUD with category, min_stock + icon buttons.
 */
public class MedicinePanel extends JPanel {

    private final CardLayout cardLayout;
    private final JPanel     container;
    private final boolean    adminMode;

    private DefaultTableModel tableModel;
    private JTable            table;

    // Form fields
    private JTextField txtMedId, txtName, txtCategory, txtPrice, txtQty, txtMinStock;

    public MedicinePanel(CardLayout cardLayout, JPanel container, boolean adminMode) {
        this.cardLayout = cardLayout;
        this.container  = container;
        this.adminMode  = adminMode;
        setName(adminMode ? "MEDICINES" : "MED_CUSTOMER");
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
                String status = qty == 0 ? "OUT" : qty < min ? "LOW" : "OK";
                tableModel.addRow(new Object[]{
                    rs.getInt("med_id"),
                    rs.getString("name"),
                    rs.getString("category"),
                    rs.getDouble("price"),
                    qty,
                    min,
                    status
                });
            }
            rs.close();
        } catch (SQLException ex) {
            UIHelper.error(this, "DB Error: " + ex.getMessage());
        }
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG_MAIN);
        String subtitle = adminMode ? "Medicine Management (Admin - Add/Update/Delete)" : "Available Medicines";
        add(UIHelper.createHeader("AR Pharmacy System", subtitle), BorderLayout.NORTH);

        // Table columns
        String[] cols = {"ID","Medicine Name","Category","Price","Stock","Min Stock","Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UIHelper.styleTable(table);
        int[] widths = {40, 200, 100, 80, 60, 80, 70};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Status column color
        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t,val,sel,focus,row,col);
                String s = (String) val;
                if (!sel) {
                    if      ("OUT".equals(s)) { c.setBackground(new Color(255,230,230)); c.setForeground(new Color(180,30,30)); }
                    else if ("LOW".equals(s)) { c.setBackground(new Color(255,245,220)); c.setForeground(new Color(160,100,0)); }
                    else                      { c.setBackground(new Color(230,255,240)); c.setForeground(new Color(0,130,60)); }
                }
                ((JLabel)c).setFont(new Font("Segoe UI",Font.BOLD,11));
                ((JLabel)c).setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Color.WHITE);

        if (adminMode) {
            JPanel form = buildAdminForm();
            JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scroll, form);
            split.setDividerLocation(540);
            split.setDividerSize(5);
            split.setBorder(BorderFactory.createEmptyBorder(14, 20, 0, 20));
            add(split, BorderLayout.CENTER);
        } else {
            JPanel c = new JPanel(new BorderLayout());
            c.setBackground(Theme.BG_MAIN);
            c.setBorder(BorderFactory.createEmptyBorder(14, 20, 0, 20));
            c.add(scroll, BorderLayout.CENTER);
            add(c, BorderLayout.CENTER);
        }

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        toolbar.setBackground(Theme.BG_MAIN);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1,0,0,0,Theme.BORDER),
            BorderFactory.createEmptyBorder(4,16,4,16)));
        JButton btnRef  = UIHelper.accentBtn("Refresh");
        JButton btnBack = UIHelper.grayBtn("Back");
        toolbar.add(btnRef); toolbar.add(btnBack);
        add(toolbar, BorderLayout.SOUTH);

        btnRef.addActionListener(e  -> loadData());
        btnBack.addActionListener(e -> cardLayout.show(container, adminMode ? "ADMIN" : "CUSTOMER"));
    }

    private JPanel buildAdminForm() {
        JPanel card = UIHelper.createCard();
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(320, 500));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(5, 5, 5, 5);

        gc.gridx=0; gc.gridy=0; gc.gridwidth=2;
        JLabel hdr = new JLabel("Medicine Form", SwingConstants.CENTER);
        hdr.setFont(new Font("Segoe UI",Font.BOLD,14));
        hdr.setForeground(Theme.PRIMARY_DARK);
        card.add(hdr, gc);

        gc.gridwidth=1;
        txtMedId    = UIHelper.createField(8);
        txtName     = UIHelper.createField(18);
        txtCategory = UIHelper.createField(14);
        txtPrice    = UIHelper.createField(10);
        txtQty      = UIHelper.createField(8);
        txtMinStock = UIHelper.createField(8);

        addRow(card, gc, 1, "Med ID (update/del):", txtMedId);
        addRow(card, gc, 2, "Name:",                txtName);
        addRow(card, gc, 3, "Category:",            txtCategory);
        addRow(card, gc, 4, "Price (Rs):",          txtPrice);
        addRow(card, gc, 5, "Quantity:",            txtQty);
        addRow(card, gc, 6, "Min Stock Alert:",     txtMinStock);

        // Fill from table
        gc.gridy=7; gc.gridx=0; gc.gridwidth=2; gc.insets=new Insets(12,5,4,5);
        JButton btnFill = UIHelper.accentBtn("Fill from Selected Row");
        btnFill.setPreferredSize(new Dimension(280, 32));
        card.add(btnFill, gc);

        // Action buttons with icons
        String[][] btnDefs = {
            {"Add Medicine",    "#00966a"},
            {"Update Medicine", "#c88200"},
            {"Delete Medicine", "#d22d2d"},
            {"Clear Form",      "#6e7870"}
        };
        int[] colors = {
            new Color(0,150,100).getRGB(),
            new Color(200,130,0).getRGB(),
            new Color(210,45,45).getRGB(),
            new Color(110,120,115).getRGB()
        };
        Color[] btnColors = {
            new Color(0,150,100),
            new Color(200,130,0),
            new Color(210,45,45),
            new Color(110,120,115)
        };
        String[] btnLabels = {"Add Medicine","Update Medicine","Delete Medicine","Clear Form"};

        for (int i = 0; i < btnLabels.length; i++) {
            gc.gridy = 8 + i;
            gc.insets = new Insets(3, 5, 3, 5);
            JButton btn = UIHelper.createButton(btnLabels[i], btnColors[i], Color.WHITE);
            btn.setPreferredSize(new Dimension(280, 36));
            final int idx = i;
            btn.addActionListener(e -> {
                switch (idx) {
                    case 0: addMedicine();    break;
                    case 1: updateMedicine(); break;
                    case 2: deleteMedicine(); break;
                    case 3: clearForm();      break;
                }
            });
            card.add(btn, gc);
        }

        btnFill.addActionListener(e -> fillFromTable());
        return card;
    }

    private void addRow(JPanel p, GridBagConstraints gc, int row, String label, JTextField field) {
        gc.gridy = row; gc.gridx = 0; gc.weightx = 0; gc.insets = new Insets(5,5,5,4);
        p.add(UIHelper.fieldLabel(label), gc);
        gc.gridx = 1; gc.weightx = 1; gc.insets = new Insets(5,4,5,5);
        field.setPreferredSize(new Dimension(145, 32));
        p.add(field, gc);
    }

    private void fillFromTable() {
        int row = table.getSelectedRow();
        if (row < 0) { UIHelper.error(this, "Select a row first."); return; }
        txtMedId.setText(tableModel.getValueAt(row,0).toString());
        txtName.setText(tableModel.getValueAt(row,1).toString());
        txtCategory.setText(tableModel.getValueAt(row,2).toString());
        txtPrice.setText(tableModel.getValueAt(row,3).toString());
        txtQty.setText(tableModel.getValueAt(row,4).toString());
        txtMinStock.setText(tableModel.getValueAt(row,5).toString());
    }

    private void addMedicine() {
        String name = txtName.getText().trim();
        String cat  = txtCategory.getText().trim();
        String pr   = txtPrice.getText().trim();
        String qt   = txtQty.getText().trim();
        String ms   = txtMinStock.getText().trim();
        if (name.isEmpty() || pr.isEmpty() || qt.isEmpty()) {
            UIHelper.error(this,"Name, Price and Qty are required."); return;
        }
        try {
            double price = Double.parseDouble(pr);
            int    qty   = Integer.parseInt(qt);
            int    minSt = ms.isEmpty() ? 10 : Integer.parseInt(ms);
            if (price < 0 || qty < 0) { UIHelper.error(this,"Values must be non-negative."); return; }
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO medicines(name,category,price,quantity,min_stock) VALUES(?,?,?,?,?)");
            ps.setString(1,name); ps.setString(2,cat.isEmpty()?"General":cat);
            ps.setDouble(3,price); ps.setInt(4,qty); ps.setInt(5,minSt);
            ps.executeUpdate(); ps.close();
            UIHelper.success(this,"Medicine added."); loadData(); clearForm();
        } catch (NumberFormatException ex) {
            UIHelper.error(this,"Invalid number format.");
        } catch (SQLException ex) {
            UIHelper.error(this,"DB Error: "+ex.getMessage());
        }
    }

    private void updateMedicine() {
        String idStr = txtMedId.getText().trim();
        if (idStr.isEmpty()) { UIHelper.error(this,"Enter Med ID."); return; }
        try {
            int    medId = Integer.parseInt(idStr);
            String name  = txtName.getText().trim();
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
            if (rows == 0) UIHelper.error(this,"No medicine with ID "+medId);
            else { UIHelper.success(this,"Medicine updated."); loadData(); clearForm(); }
        } catch (NumberFormatException ex) {
            UIHelper.error(this,"Invalid number.");
        } catch (SQLException ex) {
            UIHelper.error(this,"DB Error: "+ex.getMessage());
        }
    }

    private void deleteMedicine() {
        String idStr = txtMedId.getText().trim();
        if (idStr.isEmpty()) { UIHelper.error(this,"Enter Med ID."); return; }
        if (!UIHelper.confirm(this,"Delete this medicine?")) return;
        try {
            int medId = Integer.parseInt(idStr);
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement("DELETE FROM medicines WHERE med_id=?");
            ps.setInt(1,medId); ps.executeUpdate(); ps.close();
            UIHelper.success(this,"Deleted."); loadData(); clearForm();
        } catch (NumberFormatException ex) {
            UIHelper.error(this,"Invalid Med ID.");
        } catch (SQLException ex) {
            UIHelper.error(this,"DB Error: "+ex.getMessage());
        }
    }

    private void clearForm() {
        txtMedId.setText(""); txtName.setText(""); txtCategory.setText("");
        txtPrice.setText(""); txtQty.setText(""); txtMinStock.setText("");
    }
}
