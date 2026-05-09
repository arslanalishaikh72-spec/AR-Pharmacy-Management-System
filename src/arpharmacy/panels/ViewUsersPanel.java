package arpharmacy.panels;

import arpharmacy.Theme;
import arpharmacy.UIHelper;
import arpharmacy.db.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

/**
 * View Users Panel – shows all users in a JTable.
 * When deleteMode is true a Delete button is shown per-selection.
 */
public class ViewUsersPanel extends JPanel {

    private final CardLayout cardLayout;
    private final JPanel     container;

    private DefaultTableModel tableModel;
    private JTable            table;
    private JButton           btnDelete;
    private boolean           deleteMode = false;

    public ViewUsersPanel(CardLayout cardLayout, JPanel container) {
        this.cardLayout = cardLayout;
        this.container  = container;
        setName("VIEW_USERS");
        buildUI();
    }

    public void enableDeleteMode(boolean on) {
        this.deleteMode = on;
        btnDelete.setVisible(on);
    }

    public void loadData() {
        tableModel.setRowCount(0);
        try {
            Connection conn = DBConnection.getConnection();
            Statement  st   = conn.createStatement();
            ResultSet  rs   = st.executeQuery("SELECT * FROM users ORDER BY user_id");
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("user_id"),
                    rs.getString("name"),
                    rs.getString("username"),
                    "••••••",
                    rs.getString("role")
                });
            }
            rs.close(); st.close();
        } catch (SQLException ex) {
            UIHelper.error(this, "DB Error: " + ex.getMessage());
        }
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG_MAIN);

        add(UIHelper.createHeader("AR Pharmacy System", "User Management"), BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Full Name", "Username", "Password", "Role"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UIHelper.styleTable(table);

        // Column widths
        int[] widths = {50, 180, 140, 100, 100};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 24));
        scroll.getViewport().setBackground(Theme.BG_CARD);

        JPanel centre = new JPanel(new BorderLayout());
        centre.setBackground(Theme.BG_MAIN);
        centre.setBorder(BorderFactory.createEmptyBorder(18, 0, 18, 0));
        centre.add(scroll, BorderLayout.CENTER);
        add(centre, BorderLayout.CENTER);

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        toolbar.setBackground(Theme.BG_MAIN);
        toolbar.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));

        JButton btnRefresh = UIHelper.accentBtn("🔄  Refresh");
        btnDelete = UIHelper.dangerBtn("🗑  Delete Selected");
        JButton btnBack = UIHelper.grayBtn("← Back");
        btnDelete.setVisible(false);

        toolbar.add(btnRefresh);
        toolbar.add(btnDelete);
        toolbar.add(btnBack);

        add(toolbar, BorderLayout.SOUTH);

        // Actions
        btnRefresh.addActionListener(e -> loadData());
        btnDelete.addActionListener(e  -> deleteSelected());
        btnBack.addActionListener(e    -> { enableDeleteMode(false); cardLayout.show(container, "ADMIN"); });
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { UIHelper.error(this, "Please select a user to delete."); return; }

        int    userId   = (int)    tableModel.getValueAt(row, 0);
        String username = (String) tableModel.getValueAt(row, 2);

        if ("admin".equalsIgnoreCase((String) tableModel.getValueAt(row, 4))) {
            UIHelper.error(this, "Cannot delete the admin user.");
            return;
        }
        if (!UIHelper.confirm(this, "Delete user '" + username + "'?")) return;

        try {
            Connection      conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE user_id=?");
            ps.setInt(1, userId);
            ps.executeUpdate();
            ps.close();
            UIHelper.success(this, "User deleted.");
            loadData();
        } catch (SQLException ex) {
            UIHelper.error(this, "DB Error: " + ex.getMessage());
        }
    }
}
