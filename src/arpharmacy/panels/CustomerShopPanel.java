package arpharmacy.panels;

import arpharmacy.BillGenerator;
import arpharmacy.Theme;
import arpharmacy.UIHelper;
import arpharmacy.db.DBConnection;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;

/**
 * CustomerShopPanel v3 – POS-style interface.
 *
 * LEFT  : Searchable item list with checkboxes + inline qty spinner
 * RIGHT : Live order summary table (auto-updates on every check/uncheck/qty change)
 *         Running total box at bottom
 *         Generate Bill button
 */
public class CustomerShopPanel extends JPanel {

    // ── Session ───────────────────────────────────────────────────────────────
    private int    currentUserId   = -1;
    private String currentUserName = "";
    private String currentUserPhone = "";
    private String currentUserAddr  = "";

    private final CardLayout cardLayout;
    private final JPanel     container;

    // ── Item list data ────────────────────────────────────────────────────────
    /** All medicines loaded from DB */
    private final List<MedItem> allItems    = new ArrayList<>();
    /** Subset shown after search filter */
    private final List<MedItem> visibleItems = new ArrayList<>();

    // ── Left panel components ─────────────────────────────────────────────────
    private JPanel      itemListPanel;   // scrollable list of MedItemRow cards
    private JTextField  txtSearch;
    private JLabel      lblFound;

    // ── Right panel (order summary) ───────────────────────────────────────────
    private DefaultTableModel orderModel;
    private JTable            orderTable;

    // ── Footer totals ─────────────────────────────────────────────────────────
    private JLabel  lblItemCount;
    private JLabel  lblSubtotal;
    private JLabel  lblTotalBig;
    private JButton btnGenerateBill;
    private JButton btnClearAll;
    private JLabel  lblWelcome;
    private JLabel  lblStatus;

    // ── Data model for one medicine ───────────────────────────────────────────
    static class MedItem {
        int    id;
        String name;
        String category;
        double price;
        int    stock;
        // live selection state
        boolean  selected = false;
        int      qty      = 1;
        MedItemRow rowUI;   // back-reference to the UI card

        MedItem(int id, String name, String category, double price, int stock) {
            this.id = id; this.name = name;
            this.category = category; this.price = price; this.stock = stock;
        }
        double subtotal() { return selected ? price * qty : 0; }
    }

    // ── UI card representing one medicine in the list ─────────────────────────
    class MedItemRow extends JPanel {

        private final MedItem    item;
        private final JCheckBox  chk;
        private final JSpinner   spinQty;
        private final JLabel     lblSubtotal;
        private boolean suspendEvents = false;

        MedItemRow(MedItem item) {
            this.item = item;
            item.rowUI = this;

            setLayout(new BorderLayout(8, 0));
            setBackground(Color.WHITE);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
            setPreferredSize(new Dimension(100, 58));
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(235, 240, 238)),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            // LEFT: checkbox + name/category
            chk = new JCheckBox();
            chk.setSelected(item.selected);
            chk.setOpaque(false);
            chk.setFocusPainted(false);

            JPanel nameBlock = new JPanel(new GridLayout(2, 1, 0, 1));
            nameBlock.setOpaque(false);
            JLabel lblName = new JLabel(item.name);
            lblName.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblName.setForeground(item.stock <= 0 ? Color.GRAY : Theme.TEXT_DARK);
            JLabel lblCat = new JLabel(item.category + "  •  Rs " + String.format("%.2f", item.price) + " each");
            lblCat.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lblCat.setForeground(item.stock <= 0 ? Color.GRAY : Theme.TEXT_MID);
            nameBlock.add(lblName);
            nameBlock.add(lblCat);

            JPanel leftBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            leftBox.setOpaque(false);
            leftBox.add(chk);
            leftBox.add(nameBlock);

            // RIGHT: stock badge + qty spinner + subtotal
            JPanel rightBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            rightBox.setOpaque(false);

            // Stock badge
            JLabel stockBadge;
            if (item.stock <= 0) {
                stockBadge = badge("Out", new Color(255, 230, 230), new Color(180, 30, 30));
            } else if (item.stock <= 10) {
                stockBadge = badge("Low:" + item.stock, new Color(255, 245, 215), new Color(160, 100, 0));
            } else {
                stockBadge = badge("Stock:" + item.stock, new Color(225, 248, 235), new Color(0, 130, 60));
            }
            rightBox.add(stockBadge);

            // Qty spinner (disabled when out of stock)
            spinQty = new JSpinner(new SpinnerNumberModel(item.qty, 1, Math.max(1, item.stock), 1));
            spinQty.setPreferredSize(new Dimension(62, 28));
            spinQty.setFont(new Font("Segoe UI", Font.BOLD, 12));
            spinQty.setEnabled(item.stock > 0);
            JLabel qtyLabel = new JLabel("Qty:");
            qtyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            qtyLabel.setForeground(Theme.TEXT_MID);
            rightBox.add(qtyLabel);
            rightBox.add(spinQty);

            // Subtotal label
            lblSubtotal = new JLabel(item.selected ? "Rs " + String.format("%.2f", item.subtotal()) : "");
            lblSubtotal.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblSubtotal.setForeground(Theme.PRIMARY_DARK);
            lblSubtotal.setPreferredSize(new Dimension(88, 20));
            lblSubtotal.setHorizontalAlignment(SwingConstants.RIGHT);
            rightBox.add(lblSubtotal);

            add(leftBox,  BorderLayout.CENTER);
            add(rightBox, BorderLayout.EAST);

            // ── Disable if out of stock ───────────────────────────────────────
            if (item.stock <= 0) {
                setBackground(new Color(252, 252, 252));
                chk.setEnabled(false);
                return;
            }

            // ── Checkbox listener ─────────────────────────────────────────────
            chk.addActionListener(e -> {
                if (suspendEvents) return;
                item.selected = chk.isSelected();
                item.qty      = (int) spinQty.getValue();
                refreshRowAppearance();
                rebuildOrder();
                recalcTotal();
            });

            // ── Spinner listener ──────────────────────────────────────────────
            spinQty.addChangeListener(e -> {
                if (suspendEvents) return;
                item.qty = (int) spinQty.getValue();
                if (item.selected) {
                    refreshRowAppearance();
                    rebuildOrder();
                    recalcTotal();
                }
            });

            // ── Click anywhere on row to toggle ──────────────────────────────
            MouseAdapter toggleRow = new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    if (item.stock <= 0) return;
                    // don't re-trigger if click was directly on spinner
                    if (e.getSource() instanceof JSpinner ||
                        e.getSource() instanceof JSpinner.DefaultEditor) return;
                    chk.doClick();
                }
            };
            addMouseListener(toggleRow);
            leftBox.addMouseListener(toggleRow);
            nameBlock.addMouseListener(toggleRow);
            lblName.addMouseListener(toggleRow);
            lblCat.addMouseListener(toggleRow);
        }

        void refreshRowAppearance() {
            boolean sel = item.selected;
            setBackground(sel ? new Color(232, 250, 240) : Color.WHITE);
            lblSubtotal.setText(sel ? "Rs " + String.format("%.2f", item.subtotal()) : "");
            setBorder(BorderFactory.createCompoundBorder(
                sel ? BorderFactory.createMatteBorder(0, 3, 1, 0, Theme.PRIMARY)
                    : BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(235, 240, 238)),
                BorderFactory.createEmptyBorder(6, sel ? 9 : 12, 6, 12)));
        }

        /** Called when order rebuilt externally (e.g. clear all) */
        void setSelectedSilent(boolean v) {
            suspendEvents = true;
            item.selected = v;
            chk.setSelected(v);
            if (!v) item.qty = 1;
            spinQty.setValue(item.qty);
            refreshRowAppearance();
            suspendEvents = false;
        }

        private JLabel badge(String text, Color bg, Color fg) {
            JLabel l = new JLabel(text);
            l.setFont(new Font("Segoe UI", Font.BOLD, 10));
            l.setForeground(fg);
            l.setBackground(bg);
            l.setOpaque(true);
            l.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(fg, 1, true),
                BorderFactory.createEmptyBorder(2, 6, 2, 6)));
            return l;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Constructor
    // ─────────────────────────────────────────────────────────────────────────
    public CustomerShopPanel(CardLayout cardLayout, JPanel container) {
        this.cardLayout = cardLayout;
        this.container  = container;
        setName("CUSTOMER_SHOP");
        buildUI();
    }

    public void init(String name, int userId) {
        currentUserName = name;
        currentUserId   = userId;
        lblWelcome.setText("Welcome, " + name);
        loadUserDetails();
        loadMedicines();
    }

    private void loadUserDetails() {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT phone, address FROM users WHERE user_id=?");
            ps.setInt(1, currentUserId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                currentUserPhone = rs.getString("phone") != null ? rs.getString("phone") : "";
                currentUserAddr  = rs.getString("address") != null ? rs.getString("address") : "";
            }
            rs.close(); ps.close();
        } catch (SQLException ignored) {}
    }

    // ── Load medicines from DB and build item row cards ───────────────────────
    public void loadMedicines() {
        allItems.clear();
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) { lblStatus.setText("DB not connected"); return; }
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT med_id, name, category, price, quantity FROM medicines ORDER BY name");
            while (rs.next()) {
                allItems.add(new MedItem(
                    rs.getInt("med_id"),
                    rs.getString("name"),
                    rs.getString("category") != null ? rs.getString("category") : "General",
                    rs.getDouble("price"),
                    rs.getInt("quantity")
                ));
            }
            rs.close();
        } catch (SQLException ex) {
            lblStatus.setText("DB Error: " + ex.getMessage());
            return;
        }
        applySearch();
        rebuildOrder();
        recalcTotal();
    }

    // ── Filter list based on search text ─────────────────────────────────────
    private void applySearch() {
        String q = txtSearch.getText().trim().toLowerCase();
        visibleItems.clear();
        for (MedItem m : allItems) {
            if (q.isEmpty() || m.name.toLowerCase().contains(q)
                    || m.category.toLowerCase().contains(q)) {
                visibleItems.add(m);
            }
        }
        renderItemList();
        lblFound.setText(visibleItems.size() + " item(s)");
    }

    // ── Render item row cards into scrollable list ────────────────────────────
    private void renderItemList() {
        itemListPanel.removeAll();
        for (MedItem item : visibleItems) {
            MedItemRow row = new MedItemRow(item);
            itemListPanel.add(row);
        }
        // Filler at bottom
        JPanel filler = new JPanel();
        filler.setOpaque(false);
        itemListPanel.add(filler);
        itemListPanel.revalidate();
        itemListPanel.repaint();
    }

    // ── Rebuild right-side order summary table ────────────────────────────────
    private void rebuildOrder() {
        orderModel.setRowCount(0);
        for (MedItem m : allItems) {
            if (m.selected && m.qty > 0) {
                orderModel.addRow(new Object[]{
                    m.name,
                    m.qty,
                    String.format("Rs %.2f", m.price),
                    String.format("Rs %.2f", m.subtotal())
                });
            }
        }
    }

    // ── Recalculate and display running total ─────────────────────────────────
    private void recalcTotal() {
        double total   = 0;
        int    selCount = 0;
        for (MedItem m : allItems) {
            if (m.selected) { total += m.subtotal(); selCount++; }
        }
        lblItemCount.setText(selCount + " item(s) selected");
        lblSubtotal.setText("Subtotal:  Rs " + String.format("%.2f", total));
        lblTotalBig.setText("Rs " + String.format("%.2f", total));
        btnGenerateBill.setEnabled(selCount > 0);
        btnClearAll.setEnabled(selCount > 0);
    }

    // ── Clear all selections ──────────────────────────────────────────────────
    private void clearAll() {
        for (MedItem m : allItems) {
            m.selected = false; m.qty = 1;
            if (m.rowUI != null) m.rowUI.setSelectedSilent(false);
        }
        rebuildOrder();
        recalcTotal();
        lblStatus.setText("Cart cleared.");
    }

    // ── Checkout & generate bill ──────────────────────────────────────────────
    private void generateBill() {
        List<MedItem> chosen = new ArrayList<>();
        double total = 0;
        for (MedItem m : allItems) {
            if (m.selected && m.qty > 0) { chosen.add(m); total += m.subtotal(); }
        }
        if (chosen.isEmpty()) { UIHelper.error(this, "Select at least one item."); return; }

        // Confirm dialog
        StringBuilder sb = new StringBuilder("Confirm Purchase:\n\n");
        for (MedItem m : chosen)
            sb.append(String.format("  %-28s x%d  =  Rs %.2f%n", m.name, m.qty, m.subtotal()));
        sb.append(String.format("%n  TOTAL:  Rs %.2f", total));
        if (!UIHelper.confirm(this, sb.toString())) return;

        try {
            Connection conn = DBConnection.getConnection();

            // Insert bill header
            PreparedStatement psBill = conn.prepareStatement(
                "INSERT INTO bills(user_id, bill_date, total_amount) VALUES(?,NOW(),?)",
                Statement.RETURN_GENERATED_KEYS);
            psBill.setInt(1, currentUserId);
            psBill.setDouble(2, total);
            psBill.executeUpdate();
            ResultSet keys = psBill.getGeneratedKeys();
            int billId = keys.next() ? keys.getInt(1) : 0;
            keys.close(); psBill.close();

            // Insert line items + deduct stock + record sale
            List<BillGenerator.BillItem> billItems = new ArrayList<>();
            for (MedItem m : chosen) {
                PreparedStatement psItem = conn.prepareStatement(
                    "INSERT INTO bill_items(bill_id,med_id,med_name,unit_price,quantity,subtotal) VALUES(?,?,?,?,?,?)");
                psItem.setInt(1,billId); psItem.setInt(2,m.id);
                psItem.setString(3,m.name); psItem.setDouble(4,m.price);
                psItem.setInt(5,m.qty); psItem.setDouble(6,m.subtotal());
                psItem.executeUpdate(); psItem.close();

                PreparedStatement psStk = conn.prepareStatement(
                    "UPDATE medicines SET quantity=quantity-? WHERE med_id=?");
                psStk.setInt(1,m.qty); psStk.setInt(2,m.id);
                psStk.executeUpdate(); psStk.close();

                PreparedStatement psSale = conn.prepareStatement(
                    "INSERT INTO sales(user_id,med_id,quantity,sale_date) VALUES(?,?,?,CURDATE())");
                psSale.setInt(1,currentUserId); psSale.setInt(2,m.id);
                psSale.setInt(3,m.qty); psSale.executeUpdate(); psSale.close();

                billItems.add(new BillGenerator.BillItem(m.name, m.price, m.qty));
            }

            // Generate HTML bill
            BillGenerator.BillData bd = new BillGenerator.BillData(
                billId, currentUserName, "", currentUserPhone, currentUserAddr,
                LocalDateTime.now(), billItems, total);

            try {
                String path = BillGenerator.generateHtmlBill(bd);
                int opt = JOptionPane.showOptionDialog(this,
                    "Purchase complete!\nBill #" + String.format("%05d", billId) +
                    "  —  Total: Rs " + String.format("%.2f", total),
                    "Success", JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE, null,
                    new String[]{"Open Bill (PDF)", "Close"}, "Open Bill (PDF)");
                if (opt == 0) java.awt.Desktop.getDesktop().open(new File(path));
            } catch (Exception ex) {
                UIHelper.success(this, "Purchase complete! Bill #" + billId);
            }

            // Reset
            clearAll();
            loadMedicines();
            lblStatus.setText("Bill #" + String.format("%05d", billId) + " generated.");

        } catch (SQLException ex) {
            UIHelper.error(this, "DB Error: " + ex.getMessage());
        }
    }

    // ── View bill history dialog ──────────────────────────────────────────────
    private void viewMyBills() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "My Bills", true);
        dlg.setSize(760, 480);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        String[] cols = {"Bill #","Date & Time","Items","Total (Rs)","View"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return c == 4; }
        };
        JTable tbl = new JTable(model);
        UIHelper.styleTable(tbl);
        tbl.setRowHeight(32);
        tbl.getColumnModel().getColumn(4).setCellRenderer(new BtnCellRenderer());
        tbl.getColumnModel().getColumn(4).setCellEditor(new BtnCellEditor(tbl, model, this));

        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT b.bill_id, b.bill_date, b.total_amount," +
                "(SELECT COUNT(*) FROM bill_items WHERE bill_id=b.bill_id) AS cnt" +
                " FROM bills b WHERE b.user_id=? ORDER BY b.bill_date DESC");
            ps.setInt(1, currentUserId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    String.format("#%05d", rs.getInt("bill_id")),
                    rs.getString("bill_date").substring(0,16),
                    rs.getInt("cnt") + " item(s)",
                    "Rs " + String.format("%.2f", rs.getDouble("total_amount")),
                    "View Bill"
                });
            }
            rs.close(); ps.close();
        } catch (SQLException ex) {
            UIHelper.error(this, "DB Error: " + ex.getMessage());
        }

        dlg.add(UIHelper.createHeader("AR Pharmacy", "My Purchase History"), BorderLayout.NORTH);
        dlg.add(new JScrollPane(tbl), BorderLayout.CENTER);
        JButton close = UIHelper.grayBtn("Close");
        close.addActionListener(e -> dlg.dispose());
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,8));
        bar.add(close);
        dlg.add(bar, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    void reprintBill(int billId) {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement psH = conn.prepareStatement(
                "SELECT b.*,u.name,u.phone,u.address FROM bills b" +
                " JOIN users u ON b.user_id=u.user_id WHERE b.bill_id=?");
            psH.setInt(1, billId);
            ResultSet rsH = psH.executeQuery();
            if (!rsH.next()) { UIHelper.error(this,"Bill not found."); return; }
            double total = rsH.getDouble("total_amount");
            String dt    = rsH.getString("bill_date");
            String cName = rsH.getString("name");
            String cPh   = rsH.getString("phone");
            String cAddr = rsH.getString("address");
            rsH.close(); psH.close();

            PreparedStatement psI = conn.prepareStatement(
                "SELECT * FROM bill_items WHERE bill_id=?");
            psI.setInt(1, billId);
            ResultSet rsI = psI.executeQuery();
            List<BillGenerator.BillItem> items = new ArrayList<>();
            while (rsI.next())
                items.add(new BillGenerator.BillItem(rsI.getString("med_name"),
                    rsI.getDouble("unit_price"), rsI.getInt("quantity")));
            rsI.close(); psI.close();

            LocalDateTime ldt = LocalDateTime.parse(dt.replace(" ","T"));
            BillGenerator.BillData bd = new BillGenerator.BillData(
                billId, cName, "", cPh!=null?cPh:"", cAddr!=null?cAddr:"", ldt, items, total);
            String path = BillGenerator.generateHtmlBill(bd);
            java.awt.Desktop.getDesktop().open(new File(path));
        } catch (Exception ex) {
            UIHelper.error(this, "Error: " + ex.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  UI Construction
    // ─────────────────────────────────────────────────────────────────────────
    private void buildUI() {
        setLayout(new BorderLayout(0,0));
        setBackground(Theme.BG_MAIN);

        // ── Top header ────────────────────────────────────────────────────────
        add(UIHelper.createHeader("AR Pharmacy System", "Medicine Shop"), BorderLayout.NORTH);

        // ── Welcome bar ───────────────────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout(10,0));
        topBar.setBackground(new Color(235,250,242));
        topBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0,0,1,0,Theme.BORDER),
            BorderFactory.createEmptyBorder(8,20,8,20)));

        lblWelcome = new JLabel("Welcome!");
        lblWelcome.setFont(new Font("Segoe UI",Font.BOLD,14));
        lblWelcome.setForeground(Theme.PRIMARY_DARK);

        lblStatus = new JLabel(" ");
        lblStatus.setFont(Theme.FONT_SMALL);
        lblStatus.setForeground(Theme.TEXT_MID);

        JPanel topLeft = new JPanel(new GridLayout(2,1,0,2));
        topLeft.setOpaque(false);
        topLeft.add(lblWelcome);
        topLeft.add(lblStatus);

        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));
        topRight.setOpaque(false);
        JButton btnMyBills = UIHelper.accentBtn("My Bills");
        JButton btnLogout  = UIHelper.grayBtn("Logout");
        btnMyBills.setPreferredSize(new Dimension(110,30));
        btnLogout.setPreferredSize(new Dimension(90,30));
        topRight.add(btnMyBills);
        topRight.add(btnLogout);
        topBar.add(topLeft,  BorderLayout.WEST);
        topBar.add(topRight, BorderLayout.EAST);

        // ════════════════════════════════════════════════════════════════════
        //  LEFT PANEL – Item list
        // ════════════════════════════════════════════════════════════════════
        JPanel leftOuter = new JPanel(new BorderLayout());
        leftOuter.setBackground(Color.WHITE);
        leftOuter.setBorder(BorderFactory.createMatteBorder(0,0,0,1,Theme.BORDER));

        // Search bar
        JPanel searchBar = new JPanel(new BorderLayout(8,0));
        searchBar.setBackground(new Color(245,250,248));
        searchBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0,0,1,0,Theme.BORDER),
            BorderFactory.createEmptyBorder(10,14,10,14)));

        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setFont(new Font("Segoe UI Emoji",Font.PLAIN,16));
        txtSearch = new JTextField();
        txtSearch.setFont(new Font("Segoe UI",Font.PLAIN,13));
        txtSearch.setBorder(BorderFactory.createEmptyBorder(4,8,4,8));
        txtSearch.setBackground(Color.WHITE);
        ((AbstractDocument)txtSearch.getDocument()).addDocumentListener(new DocumentListener(){
            public void insertUpdate(DocumentEvent e){ applySearch(); }
            public void removeUpdate(DocumentEvent e){ applySearch(); }
            public void changedUpdate(DocumentEvent e){ applySearch(); }
        });

        lblFound = new JLabel("0 item(s)");
        lblFound.setFont(new Font("Segoe UI",Font.PLAIN,11));
        lblFound.setForeground(Theme.TEXT_MID);

        searchBar.add(searchIcon, BorderLayout.WEST);
        searchBar.add(txtSearch,  BorderLayout.CENTER);
        searchBar.add(lblFound,   BorderLayout.EAST);

        // Item list (BoxLayout for vertical stacking)
        itemListPanel = new JPanel();
        itemListPanel.setLayout(new BoxLayout(itemListPanel, BoxLayout.Y_AXIS));
        itemListPanel.setBackground(Color.WHITE);

        JScrollPane listScroll = new JScrollPane(itemListPanel);
        listScroll.setBorder(BorderFactory.createEmptyBorder());
        listScroll.getViewport().setBackground(Color.WHITE);
        listScroll.getVerticalScrollBar().setUnitIncrement(20);
        listScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Left header label
        JPanel leftHeader = new JPanel(new BorderLayout());
        leftHeader.setBackground(new Color(0,130,75));
        leftHeader.setBorder(BorderFactory.createEmptyBorder(7,14,7,14));
        JLabel lhTitle = new JLabel("💊  Medicine List");
        lhTitle.setFont(new Font("Segoe UI",Font.BOLD,13));
        lhTitle.setForeground(Color.WHITE);
        JLabel lhHint = new JLabel("Click item or checkbox to select");
        lhHint.setFont(new Font("Segoe UI",Font.PLAIN,11));
        lhHint.setForeground(new Color(200,240,220));
        leftHeader.add(lhTitle,  BorderLayout.WEST);
        leftHeader.add(lhHint,   BorderLayout.EAST);

        leftOuter.add(leftHeader, BorderLayout.NORTH);
        leftOuter.add(searchBar,  BorderLayout.CENTER);
        // wrap: header (north=searchBar, center=list)
        JPanel listWrapper = new JPanel(new BorderLayout());
        listWrapper.add(searchBar,  BorderLayout.NORTH);
        listWrapper.add(listScroll, BorderLayout.CENTER);
        leftOuter.add(listWrapper, BorderLayout.CENTER);

        // ════════════════════════════════════════════════════════════════════
        //  RIGHT PANEL – Order summary + total
        // ════════════════════════════════════════════════════════════════════
        JPanel rightOuter = new JPanel(new BorderLayout());
        rightOuter.setBackground(Theme.BG_MAIN);

        // Right header
        JPanel rightHeader = new JPanel(new BorderLayout());
        rightHeader.setBackground(new Color(0,100,60));
        rightHeader.setBorder(BorderFactory.createEmptyBorder(7,14,7,14));
        JLabel rhTitle = new JLabel("🛒  Order Summary");
        rhTitle.setFont(new Font("Segoe UI",Font.BOLD,13));
        rhTitle.setForeground(Color.WHITE);
        rightHeader.add(rhTitle, BorderLayout.WEST);

        // Order table
        String[] oCols = {"Medicine","Qty","Unit Price","Subtotal"};
        orderModel = new DefaultTableModel(oCols,0){
            public boolean isCellEditable(int r,int c){ return false; }
        };
        orderTable = new JTable(orderModel);
        UIHelper.styleTable(orderTable);
        orderTable.setRowHeight(28);
        int[] ow = {180,45,90,90};
        for(int i=0;i<ow.length;i++) orderTable.getColumnModel().getColumn(i).setPreferredWidth(ow[i]);

        JScrollPane orderScroll = new JScrollPane(orderTable);
        orderScroll.setBorder(BorderFactory.createEmptyBorder());
        orderScroll.getViewport().setBackground(Color.WHITE);

        // ── Total box ─────────────────────────────────────────────────────────
        JPanel totalBox = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0,0,new Color(0,130,80),getWidth(),0,new Color(0,100,60));
                g2.setPaint(gp);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),0,0);
                g2.dispose();
            }
        };
        totalBox.setBorder(BorderFactory.createEmptyBorder(14,16,14,16));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill=GridBagConstraints.HORIZONTAL; gc.insets=new Insets(3,4,3,4);

        lblItemCount = new JLabel("0 item(s) selected");
        lblItemCount.setFont(new Font("Segoe UI",Font.PLAIN,12));
        lblItemCount.setForeground(new Color(200,240,220));
        gc.gridx=0; gc.gridy=0; gc.gridwidth=2; gc.weightx=1;
        totalBox.add(lblItemCount, gc);

        lblSubtotal = new JLabel("Subtotal:  Rs 0.00");
        lblSubtotal.setFont(new Font("Segoe UI",Font.PLAIN,13));
        lblSubtotal.setForeground(new Color(220,245,232));
        gc.gridy=1;
        totalBox.add(lblSubtotal, gc);

        // Big total amount display
        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setOpaque(false);
        JLabel totalLabel = new JLabel("TOTAL");
        totalLabel.setFont(new Font("Segoe UI",Font.BOLD,14));
        totalLabel.setForeground(new Color(180,240,210));
        lblTotalBig = new JLabel("Rs 0.00");
        lblTotalBig.setFont(new Font("Segoe UI",Font.BOLD,26));
        lblTotalBig.setForeground(Color.WHITE);
        lblTotalBig.setHorizontalAlignment(SwingConstants.RIGHT);
        totalRow.add(totalLabel,  BorderLayout.WEST);
        totalRow.add(lblTotalBig, BorderLayout.EAST);
        gc.gridy=2; gc.insets=new Insets(8,4,8,4);
        totalBox.add(totalRow, gc);

        // Divider
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(255,255,255,60));
        gc.gridy=3; gc.insets=new Insets(2,4,10,4);
        totalBox.add(sep, gc);

        // Buttons
        btnGenerateBill = UIHelper.createButton("🧾  Generate Bill", new Color(255,210,0), new Color(50,40,0));
        btnGenerateBill.setFont(new Font("Segoe UI",Font.BOLD,14));
        btnGenerateBill.setPreferredSize(new Dimension(240,42));
        btnGenerateBill.setEnabled(false);
        gc.gridy=4; gc.insets=new Insets(4,4,4,4);
        totalBox.add(btnGenerateBill, gc);

        btnClearAll = UIHelper.createButton("✖  Clear All Selections", new Color(255,255,255,40), Color.WHITE);
        btnClearAll.setFont(new Font("Segoe UI",Font.PLAIN,12));
        btnClearAll.setPreferredSize(new Dimension(240,34));
        btnClearAll.setEnabled(false);
        gc.gridy=5;
        totalBox.add(btnClearAll, gc);

        rightOuter.add(rightHeader, BorderLayout.NORTH);
        rightOuter.add(orderScroll, BorderLayout.CENTER);
        rightOuter.add(totalBox,    BorderLayout.SOUTH);

        // ── Split pane ────────────────────────────────────────────────────────
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftOuter, rightOuter);
        split.setDividerLocation(560);
        split.setDividerSize(4);
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setBackground(Theme.BG_MAIN);

        JPanel centre = new JPanel(new BorderLayout());
        centre.setBackground(Theme.BG_MAIN);
        centre.add(topBar, BorderLayout.NORTH);
        centre.add(split,  BorderLayout.CENTER);
        add(centre, BorderLayout.CENTER);

        // ── Wire actions ──────────────────────────────────────────────────────
        btnGenerateBill.addActionListener(e -> generateBill());
        btnClearAll.addActionListener(e     -> clearAll());
        btnMyBills.addActionListener(e      -> viewMyBills());
        btnLogout.addActionListener(e       -> cardLayout.show(container, "LOGIN"));
    }

    // ── Button cell renderer/editor for bill history dialog ───────────────────
    static class BtnCellRenderer extends JButton implements TableCellRenderer {
        BtnCellRenderer() { setOpaque(true); }
        public Component getTableCellRendererComponent(JTable t,Object v,
                boolean s,boolean f,int r,int c) {
            setText(v==null?"":v.toString());
            setBackground(Theme.PRIMARY); setForeground(Color.WHITE);
            setFont(new Font("Segoe UI",Font.BOLD,11));
            return this;
        }
    }
    static class BtnCellEditor extends DefaultCellEditor {
        private final DefaultTableModel model;
        private final CustomerShopPanel parent;
        private final JTable tbl;
        private final JButton btn;
        BtnCellEditor(JTable tbl, DefaultTableModel m, CustomerShopPanel p) {
            super(new JCheckBox());
            this.tbl=tbl; this.model=m; this.parent=p;
            btn = new JButton();
            btn.setOpaque(true);
            btn.setBackground(Theme.PRIMARY); btn.setForeground(Color.WHITE);
            btn.setFont(new Font("Segoe UI",Font.BOLD,11));
            btn.addActionListener(e -> fireEditingStopped());
        }
        public Component getTableCellEditorComponent(JTable t,Object v,boolean s,int r,int c) {
            btn.setText(v==null?"":v.toString()); return btn;
        }
        public Object getCellEditorValue() {
            int row = tbl.getSelectedRow();
            if (row >= 0) {
                String bs = (String) model.getValueAt(row,0);
                try { parent.reprintBill(Integer.parseInt(bs.replace("#","").trim())); }
                catch (Exception ignored) {}
            }
            return btn.getText();
        }
    }
}
