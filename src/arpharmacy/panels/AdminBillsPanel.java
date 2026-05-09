package arpharmacy.panels;

import arpharmacy.BillGenerator;
import arpharmacy.Theme;
import arpharmacy.UIHelper;
import arpharmacy.db.DBConnection;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;

/**
 * AdminBillsPanel – admin sees all bills with customer info, items, totals.
 * Can delete selected bill or delete all bills.
 */
public class AdminBillsPanel extends JPanel {

    private final CardLayout      cardLayout;
    private final JPanel          container;

    private DefaultTableModel     billModel;
    private JTable                billTable;
    private DefaultTableModel     itemModel;
    private JTable                itemTable;

    private JLabel                lblBillInfo;
    private JLabel                lblSummary;
    private JButton               btnDeleteBill;

    public AdminBillsPanel(CardLayout cardLayout, JPanel container) {
        this.cardLayout = cardLayout;
        this.container  = container;
        setName("ADMIN_BILLS");
        buildUI();
    }

    public void loadBills() {
        billModel.setRowCount(0);
        itemModel.setRowCount(0);
        lblBillInfo.setText("← Select a bill to see items");
        btnDeleteBill.setEnabled(false);

        try {
            Connection conn = DBConnection.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT b.bill_id, u.name AS cname, u.username, u.phone, " +
                "b.bill_date, b.total_amount, " +
                "(SELECT COUNT(*) FROM bill_items WHERE bill_id=b.bill_id) AS items " +
                "FROM bills b JOIN users u ON b.user_id=u.user_id " +
                "ORDER BY b.bill_date DESC");
            int total = 0; double totalAmt = 0;
            while (rs.next()) {
                billModel.addRow(new Object[]{
                    rs.getInt("bill_id"),
                    rs.getString("cname"),
                    rs.getString("username"),
                    rs.getString("phone") != null ? rs.getString("phone") : "—",
                    rs.getString("bill_date").substring(0,16),
                    rs.getInt("items"),
                    String.format("Rs %.2f", rs.getDouble("total_amount"))
                });
                total++; totalAmt += rs.getDouble("total_amount");
            }
            rs.close();
            lblSummary.setText(total + " bills  |  Total Revenue: Rs " + String.format("%.2f", totalAmt));
        } catch (SQLException ex) {
            UIHelper.error(this, "DB Error: " + ex.getMessage());
        }
    }

    private void loadBillItems(int billId, String customerInfo) {
        itemModel.setRowCount(0);
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM bill_items WHERE bill_id=?");
            ps.setInt(1, billId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                itemModel.addRow(new Object[]{
                    rs.getString("med_name"),
                    rs.getInt("quantity"),
                    String.format("Rs %.2f", rs.getDouble("unit_price")),
                    String.format("Rs %.2f", rs.getDouble("subtotal"))
                });
            }
            rs.close(); ps.close();
            lblBillInfo.setText("Bill #" + String.format("%05d",billId) + "  |  " + customerInfo);
        } catch (SQLException ex) {
            UIHelper.error(this, "DB Error: " + ex.getMessage());
        }
    }

    private void deleteBill() {
        int row = billTable.getSelectedRow();
        if (row < 0) { UIHelper.error(this, "Select a bill to delete."); return; }
        int    billId = (int) billModel.getValueAt(row, 0);
        String cname  = (String) billModel.getValueAt(row, 1);
        if (!UIHelper.confirm(this, "Delete Bill #" + String.format("%05d",billId) +
                " for " + cname + "?\n\nThis will also delete all line items.")) return;
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement("DELETE FROM bills WHERE bill_id=?");
            ps.setInt(1, billId); ps.executeUpdate(); ps.close();
            UIHelper.success(this, "Bill deleted.");
            loadBills();
        } catch (SQLException ex) {
            UIHelper.error(this, "DB Error: " + ex.getMessage());
        }
    }

    private void deleteAllBills() {
        if (billModel.getRowCount() == 0) { UIHelper.error(this, "No bills to delete."); return; }
        if (!UIHelper.confirm(this,
                "Delete ALL " + billModel.getRowCount() + " bills?\n\n" +
                "This will permanently erase all purchase history.")) return;
        try {
            Connection conn = DBConnection.getConnection();
            conn.createStatement().executeUpdate("DELETE FROM bills");
            UIHelper.success(this, "All bills deleted.");
            loadBills();
        } catch (SQLException ex) {
            UIHelper.error(this, "DB Error: " + ex.getMessage());
        }
    }

    private void viewBillPdf() {
        int row = billTable.getSelectedRow();
        if (row < 0) { UIHelper.error(this,"Select a bill first."); return; }
        int billId = (int) billModel.getValueAt(row,0);
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement psH = conn.prepareStatement(
                "SELECT b.*,u.name,u.phone,u.address FROM bills b" +
                " JOIN users u ON b.user_id=u.user_id WHERE b.bill_id=?");
            psH.setInt(1,billId);
            ResultSet rsH = psH.executeQuery();
            if (!rsH.next()) return;
            double total=rsH.getDouble("total_amount");
            String dt=rsH.getString("bill_date");
            String cName=rsH.getString("name");
            String cPh=rsH.getString("phone");
            String cAddr=rsH.getString("address");
            rsH.close(); psH.close();

            PreparedStatement psI=conn.prepareStatement("SELECT * FROM bill_items WHERE bill_id=?");
            psI.setInt(1,billId);
            ResultSet rsI=psI.executeQuery();
            List<BillGenerator.BillItem> items=new ArrayList<>();
            while(rsI.next())
                items.add(new BillGenerator.BillItem(rsI.getString("med_name"),
                    rsI.getDouble("unit_price"),rsI.getInt("quantity")));
            rsI.close(); psI.close();

            LocalDateTime ldt=LocalDateTime.parse(dt.replace(" ","T"));
            BillGenerator.BillData bd=new BillGenerator.BillData(
                billId,cName,"",cPh!=null?cPh:"",cAddr!=null?cAddr:"",ldt,items,total);
            String path=BillGenerator.generateHtmlBill(bd);
            java.awt.Desktop.getDesktop().open(new File(path));
        } catch(Exception ex) {
            UIHelper.error(this,"Error: "+ex.getMessage());
        }
    }

    private void buildUI() {
        setLayout(new BorderLayout(0,0));
        setBackground(new Color(245,248,250));
        add(UIHelper.createHeader("AR Pharmacy System","Bill Management — All Customer Bills"), BorderLayout.NORTH);

        // ── Summary bar ───────────────────────────────────────────────────────
        JPanel summaryBar = new JPanel(new BorderLayout(12,0));
        summaryBar.setBackground(new Color(235,248,242));
        summaryBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0,0,1,0,new Color(200,220,210)),
            BorderFactory.createEmptyBorder(8,20,8,20)));
        lblSummary = new JLabel("Loading...");
        lblSummary.setFont(new Font("Segoe UI",Font.BOLD,13));
        lblSummary.setForeground(new Color(0,100,60));
        summaryBar.add(lblSummary, BorderLayout.WEST);

        // ── Bills table (top) ─────────────────────────────────────────────────
        String[] bCols = {"Bill #","Customer","Username","Phone","Date","Items","Total"};
        billModel = new DefaultTableModel(bCols,0) {
            public boolean isCellEditable(int r,int c){ return false; }
        };
        billTable = new JTable(billModel);
        UIHelper.styleTable(billTable);
        billTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] bw = {65,150,110,110,140,60,100};
        for(int i=0;i<bw.length;i++) billTable.getColumnModel().getColumn(i).setPreferredWidth(bw[i]);

        JScrollPane billScroll = new JScrollPane(billTable);
        billScroll.getViewport().setBackground(Color.WHITE);
        billScroll.setBorder(BorderFactory.createEmptyBorder());

        // ── Items table (bottom) ──────────────────────────────────────────────
        String[] iCols = {"Medicine Name","Qty","Unit Price","Subtotal"};
        itemModel = new DefaultTableModel(iCols,0) {
            public boolean isCellEditable(int r,int c){ return false; }
        };
        itemTable = new JTable(itemModel);
        UIHelper.styleTable(itemTable);
        int[] iw = {220,60,110,110};
        for(int i=0;i<iw.length;i++) itemTable.getColumnModel().getColumn(i).setPreferredWidth(iw[i]);

        JScrollPane itemScroll = new JScrollPane(itemTable);
        itemScroll.getViewport().setBackground(Color.WHITE);
        itemScroll.setBorder(BorderFactory.createEmptyBorder());

        // ── Labels ────────────────────────────────────────────────────────────
        JLabel billsHdr = sectionHdr("🧾  All Bills");
        lblBillInfo = new JLabel("← Select a bill to see items");
        lblBillInfo.setFont(new Font("Segoe UI",Font.BOLD,12));
        lblBillInfo.setForeground(new Color(0,100,60));
        lblBillInfo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0,0,1,0,new Color(200,215,208)),
            BorderFactory.createEmptyBorder(0,0,6,0)));

        JPanel topSection = new JPanel(new BorderLayout(0,6));
        topSection.setBackground(new Color(245,248,250));
        topSection.add(billsHdr,  BorderLayout.NORTH);
        topSection.add(billScroll, BorderLayout.CENTER);

        JPanel botSection = new JPanel(new BorderLayout(0,6));
        botSection.setBackground(new Color(245,248,250));
        botSection.add(lblBillInfo, BorderLayout.NORTH);
        botSection.add(itemScroll,  BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topSection, botSection);
        split.setDividerLocation(280);
        split.setDividerSize(5);
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setBackground(new Color(245,248,250));

        JPanel centre = new JPanel(new BorderLayout(0,0));
        centre.setBackground(new Color(245,248,250));
        centre.setBorder(BorderFactory.createEmptyBorder(12,20,0,20));
        centre.add(summaryBar, BorderLayout.NORTH);
        centre.add(split,      BorderLayout.CENTER);
        add(centre, BorderLayout.CENTER);

        // ── Toolbar ───────────────────────────────────────────────────────────
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT,12,8));
        toolbar.setBackground(new Color(245,248,250));
        toolbar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1,0,0,0,new Color(200,215,208)),
            BorderFactory.createEmptyBorder(4,16,4,16)));

        JButton btnRef       = roundBtn("🔄  Refresh",         new Color(30,110,200),  Color.WHITE);
        btnDeleteBill        = roundBtn("🗑  Delete Bill",      new Color(210,45,45),   Color.WHITE);
        JButton btnDeleteAll = roundBtn("🗑  Delete All Bills", new Color(160,30,30),   Color.WHITE);
        JButton btnView      = roundBtn("📄  View Bill PDF",    new Color(0,120,80),    Color.WHITE);
        JButton btnBack      = roundBtn("←  Back to Admin",    new Color(100,110,106), Color.WHITE);

        btnDeleteBill.setEnabled(false);
        toolbar.add(btnRef);
        toolbar.add(btnDeleteBill);
        toolbar.add(btnDeleteAll);
        toolbar.add(btnView);
        toolbar.add(btnBack);
        add(toolbar, BorderLayout.SOUTH);

        // Listeners
        billTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = billTable.getSelectedRow();
            if (row >= 0) {
                int    billId  = (int)    billModel.getValueAt(row,0);
                String cname   = (String) billModel.getValueAt(row,1);
                String uname   = (String) billModel.getValueAt(row,2);
                String phone   = (String) billModel.getValueAt(row,3);
                String date    = (String) billModel.getValueAt(row,4);
                String total   = (String) billModel.getValueAt(row,6);
                loadBillItems(billId, cname + " (@" + uname + ")  |  " + phone + "  |  " + date + "  |  " + total);
                btnDeleteBill.setEnabled(true);
            } else {
                itemModel.setRowCount(0);
                lblBillInfo.setText("← Select a bill to see items");
                btnDeleteBill.setEnabled(false);
            }
        });

        btnRef.addActionListener(e       -> loadBills());
        btnDeleteBill.addActionListener(e -> deleteBill());
        btnDeleteAll.addActionListener(e  -> deleteAllBills());
        btnView.addActionListener(e       -> viewBillPdf());
        btnBack.addActionListener(e       -> cardLayout.show(container,"ADMIN"));
    }

    private JLabel sectionHdr(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI",Font.BOLD,12));
        l.setForeground(new Color(60,80,70));
        l.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0,0,1,0,new Color(200,215,208)),
            BorderFactory.createEmptyBorder(0,0,6,0)));
        return l;
    }

    private JButton roundBtn(String text, Color bg, Color fg) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled()
                    ?(getModel().isRollover()?bg.brighter():getModel().isPressed()?bg.darker():bg)
                    :new Color(190,190,190));
                g2.fill(new RoundRectangle2D.Double(0,0,getWidth(),getHeight(),10,10));
                g2.dispose(); super.paintComponent(g);
            }
            @Override protected void paintBorder(Graphics g){}
            @Override public boolean isOpaque(){return false;}
        };
        b.setFont(new Font("Segoe UI",Font.BOLD,12));
        b.setForeground(fg); b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(160,34));
        return b;
    }
}
