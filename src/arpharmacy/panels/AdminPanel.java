package arpharmacy.panels;

import arpharmacy.Theme;
import arpharmacy.UIHelper;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

/**
 * AdminPanel v3 – modern card-style hub with pharmacy logo,
 * separated medicine panels, and bills management.
 */
public class AdminPanel extends JPanel {

    private final CardLayout cardLayout;
    private final JPanel     container;
    private JLabel           lblWelcome;

    // Sub-panels (lazy init)
    private ViewUsersPanel    viewUsersPanel;
    private AddUserPanel      addUserPanel;
    private UpdateRolePanel   updateRolePanel;
    private AddMedicinePanel  addMedPanel;
    private UpdateMedicinePanel updateMedPanel;
    private DeleteMedicinePanel deleteMedPanel;
    private StockPanel        stockPanel;
    private AdminBillsPanel   billsPanel;

    public AdminPanel(CardLayout cardLayout, JPanel container) {
        this.cardLayout = cardLayout;
        this.container  = container;
        setName("ADMIN");
        buildUI();
    }

    public void refresh(String name) {
        lblWelcome.setText("Welcome back,  " + name);
    }

    private void buildUI() {
        setLayout(new BorderLayout(0,0));
        setBackground(new Color(240, 244, 248));

        // ── Top header with pharmacy logo ─────────────────────────────────────
        add(buildLogoHeader(), BorderLayout.NORTH);

        // ── Main scrollable content ───────────────────────────────────────────
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(new Color(240, 244, 248));
        content.setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));

        // Welcome row
        lblWelcome = new JLabel("Welcome, Admin");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblWelcome.setForeground(new Color(50, 70, 60));
        lblWelcome.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblWelcome.setBorder(BorderFactory.createEmptyBorder(0,0,18,0));
        content.add(lblWelcome);

        // ── Section: User Management ──────────────────────────────────────────
        content.add(sectionLabel("👤  User Management"));
        content.add(Box.createVerticalStrut(10));
        JPanel userRow = buttonRow(
            navCard("➕", "Add User",       "Create a new account",          new Color(0,160,100),   e -> showAddUser()),
            navCard("📋", "View Users",     "Browse all registered users",   new Color(30,110,200),  e -> showViewUsers()),
            navCard("🔄", "Update Role",    "Change a user's access role",   new Color(200,130,0),   e -> showUpdateRole()),
            navCard("🗑",  "Delete User",   "Remove a user account",         new Color(210,45,45),   e -> doDeleteUser())
        );
        content.add(userRow);
        content.add(Box.createVerticalStrut(22));

        // ── Section: Medicine Management ──────────────────────────────────────
        content.add(sectionLabel("💊  Medicine Management"));
        content.add(Box.createVerticalStrut(10));
        JPanel medRow = buttonRow(
            navCard("➕", "Add Medicine",   "Add a new medicine to stock",   new Color(0,140,120),   e -> showAddMed()),
            navCard("✏️", "Update Medicine","Edit medicine details or price", new Color(60,90,200),   e -> showUpdateMed()),
            navCard("🗑",  "Delete Medicine","Remove a medicine from stock",  new Color(160,50,160),  e -> showDeleteMed()),
            navCard("📊", "View Stock",     "Check inventory & stock levels", new Color(20,150,60),   e -> showStock())
        );
        content.add(medRow);
        content.add(Box.createVerticalStrut(22));

        // ── Section: Reports & System ─────────────────────────────────────────
        content.add(sectionLabel("📋  Reports & System"));
        content.add(Box.createVerticalStrut(10));
        JPanel sysRow = buttonRow(
            navCard("🧾", "View Bills",     "All customer purchase bills",   new Color(80,60,200),   e -> showBills()),
            navCard("🚪", "Logout",         "Sign out of admin panel",       new Color(90,95,92),    e -> logout())
        );
        content.add(sysRow);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(new Color(240,244,248));
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        add(scrollPane, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBackground(new Color(230,236,232));
        footer.setBorder(BorderFactory.createMatteBorder(1,0,0,0,new Color(200,215,208)));
        JLabel ftl = new JLabel("✚  AR Pharmacy System  •  Admin Dashboard  •  v3.0  •  All Rights Reserved");
        ftl.setFont(new Font("Segoe UI",Font.PLAIN,11));
        ftl.setForeground(new Color(110,130,120));
        footer.add(ftl);
        add(footer, BorderLayout.SOUTH);
    }

    // ── Pharmacy logo header ──────────────────────────────────────────────────
    private JPanel buildLogoHeader() {
        JPanel header = new JPanel(new BorderLayout(0,0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Deep green gradient
                GradientPaint gp = new GradientPaint(0,0, new Color(0,100,60),
                                                     getWidth(),0, new Color(0,70,42));
                g2.setPaint(gp);
                g2.fillRect(0,0,getWidth(),getHeight());
                // Subtle circular glow top-right
                g2.setColor(new Color(255,255,255,12));
                g2.fillOval(getWidth()-120,-60,200,200);
                g2.dispose();
            }
        };
        header.setPreferredSize(new Dimension(0, 78));
        header.setBorder(BorderFactory.createEmptyBorder(0,24,0,24));

        // LEFT: Logo block
        JPanel logoBlock = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        logoBlock.setOpaque(false);

        // Cross icon circle
        JPanel crossCircle = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255,255,255,30));
                g2.fillOval(0,0,getWidth()-1,getHeight()-1);
                g2.setColor(new Color(180,255,210,120));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(1,1,getWidth()-3,getHeight()-3);
                g2.dispose();
                super.paintComponent(g);
            }
            @Override public boolean isOpaque(){return false;}
        };
        crossCircle.setPreferredSize(new Dimension(52,52));
        JLabel crossLbl = new JLabel("✚");
        crossLbl.setFont(new Font("Segoe UI",Font.BOLD,26));
        crossLbl.setForeground(new Color(180,255,210));
        crossCircle.add(crossLbl);

        // Title + subtitle
        JPanel titleBlock = new JPanel(new GridLayout(2,1,0,2));
        titleBlock.setOpaque(false);
        JLabel appName = new JLabel("AR Pharmacy System");
        appName.setFont(new Font("Segoe UI",Font.BOLD,20));
        appName.setForeground(Color.WHITE);
        JLabel appSub = new JLabel("Admin Control Panel  •  Your Health, Our Priority");
        appSub.setFont(new Font("Segoe UI",Font.PLAIN,11));
        appSub.setForeground(new Color(180,240,210));
        titleBlock.add(appName);
        titleBlock.add(appSub);

        logoBlock.add(crossCircle);
        logoBlock.add(titleBlock);
        header.add(logoBlock, BorderLayout.WEST);

        // RIGHT: pill icon + tagline
        JPanel rightBlock = new JPanel(new GridLayout(2,1,0,2));
        rightBlock.setOpaque(false);
        JLabel pill = new JLabel("💊  AR Pharmacy", SwingConstants.RIGHT);
        pill.setFont(new Font("Segoe UI Emoji",Font.BOLD,13));
        pill.setForeground(new Color(180,240,210));
        JLabel copy = new JLabel("© 2025 All rights reserved", SwingConstants.RIGHT);
        copy.setFont(new Font("Segoe UI",Font.PLAIN,10));
        copy.setForeground(new Color(140,200,170));
        rightBlock.add(pill);
        rightBlock.add(copy);
        header.add(rightBlock, BorderLayout.EAST);

        return header;
    }

    // ── Section label ─────────────────────────────────────────────────────────
    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI",Font.BOLD,13));
        l.setForeground(new Color(70,90,80));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0,0,1,0,new Color(200,215,208)),
            BorderFactory.createEmptyBorder(0,0,6,0)));
        return l;
    }

    // ── Row of nav cards ──────────────────────────────────────────────────────
    private JPanel buttonRow(JPanel... cards) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (JPanel c : cards) row.add(c);
        return row;
    }

    // ── Modern nav card ───────────────────────────────────────────────────────
    private JPanel navCard(String emoji, String title, String desc, Color accent, ActionListener al) {
        JPanel card = new JPanel(new BorderLayout(0, 6)) {
            boolean hover = false;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e){ hover=true; repaint(); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); }
                public void mouseExited(MouseEvent e) { hover=false; repaint(); }
                public void mouseClicked(MouseEvent e){ al.actionPerformed(null); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Card bg
                g2.setColor(hover ? new Color(250,252,251) : Color.WHITE);
                g2.fill(new RoundRectangle2D.Double(0,0,getWidth(),getHeight(),14,14));
                // Left accent bar
                g2.setColor(accent);
                g2.fill(new RoundRectangle2D.Double(0,0,5,getHeight(),4,4));
                // Border
                g2.setColor(hover ? accent : new Color(220,230,226));
                g2.setStroke(new BasicStroke(hover ? 1.5f : 1f));
                g2.draw(new RoundRectangle2D.Double(0,0,getWidth()-1,getHeight()-1,14,14));
                // Bottom shadow
                g2.setColor(new Color(0,60,30, hover ? 18 : 10));
                g2.fill(new RoundRectangle2D.Double(2,getHeight()-4,getWidth()-4,4,4,4));
                g2.dispose();
            }
            @Override public boolean isOpaque(){return false;}
        };
        card.setPreferredSize(new Dimension(200, 86));
        card.setBorder(BorderFactory.createEmptyBorder(12,14,10,14));

        // Icon area
        JPanel iconCircle = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),22);
                g2.setColor(bg);
                g2.fillOval(0,0,getWidth(),getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
            @Override public boolean isOpaque(){return false;}
        };
        iconCircle.setPreferredSize(new Dimension(38,38));
        JLabel emojiLbl = new JLabel(emoji);
        emojiLbl.setFont(new Font("Segoe UI Emoji",Font.PLAIN,18));
        iconCircle.add(emojiLbl);

        // Text block
        JPanel textBlock = new JPanel(new GridLayout(2,1,0,2));
        textBlock.setOpaque(false);
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI",Font.BOLD,13));
        titleLbl.setForeground(new Color(30,45,38));
        JLabel descLbl = new JLabel(desc);
        descLbl.setFont(new Font("Segoe UI",Font.PLAIN,10));
        descLbl.setForeground(new Color(110,130,120));
        textBlock.add(titleLbl);
        textBlock.add(descLbl);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        top.setOpaque(false);
        top.add(iconCircle);
        top.add(textBlock);

        // Arrow indicator
        JLabel arrow = new JLabel("→");
        arrow.setFont(new Font("Segoe UI",Font.BOLD,12));
        arrow.setForeground(new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),160));
        arrow.setHorizontalAlignment(SwingConstants.RIGHT);

        card.add(top,   BorderLayout.CENTER);
        card.add(arrow, BorderLayout.SOUTH);

        // Also fire action on card click
        for (Component child : getAllComponents(card)) {
            child.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) { al.actionPerformed(null); }
            });
        }

        return card;
    }

    private java.util.List<Component> getAllComponents(Container c) {
        java.util.List<Component> list = new java.util.ArrayList<>();
        for (Component comp : c.getComponents()) {
            list.add(comp);
            if (comp instanceof Container) list.addAll(getAllComponents((Container)comp));
        }
        return list;
    }

    // ── Navigation methods ────────────────────────────────────────────────────
    private void showAddUser() {
        if (addUserPanel == null) { addUserPanel = new AddUserPanel(cardLayout, container); container.add(addUserPanel,"ADD_USER"); }
        cardLayout.show(container,"ADD_USER");
    }
    private void showViewUsers() {
        if (viewUsersPanel == null) { viewUsersPanel = new ViewUsersPanel(cardLayout, container); container.add(viewUsersPanel,"VIEW_USERS"); }
        viewUsersPanel.loadData();
        cardLayout.show(container,"VIEW_USERS");
    }
    private void showUpdateRole() {
        if (updateRolePanel == null) { updateRolePanel = new UpdateRolePanel(cardLayout, container); container.add(updateRolePanel,"UPDATE_ROLE"); }
        updateRolePanel.loadUsers();
        cardLayout.show(container,"UPDATE_ROLE");
    }
    private void doDeleteUser() {
        if (viewUsersPanel == null) { viewUsersPanel = new ViewUsersPanel(cardLayout, container); container.add(viewUsersPanel,"VIEW_USERS"); }
        viewUsersPanel.loadData();
        viewUsersPanel.enableDeleteMode(true);
        cardLayout.show(container,"VIEW_USERS");
    }
    private void showAddMed() {
        if (addMedPanel == null) { addMedPanel = new AddMedicinePanel(cardLayout, container); container.add(addMedPanel,"ADD_MED"); }
        cardLayout.show(container,"ADD_MED");
    }
    private void showUpdateMed() {
        if (updateMedPanel == null) { updateMedPanel = new UpdateMedicinePanel(cardLayout, container); container.add(updateMedPanel,"UPDATE_MED"); }
        updateMedPanel.loadData();
        cardLayout.show(container,"UPDATE_MED");
    }
    private void showDeleteMed() {
        if (deleteMedPanel == null) { deleteMedPanel = new DeleteMedicinePanel(cardLayout, container); container.add(deleteMedPanel,"DELETE_MED"); }
        deleteMedPanel.loadData();
        cardLayout.show(container,"DELETE_MED");
    }
    private void showStock() {
        if (stockPanel == null) { stockPanel = new StockPanel(cardLayout, container); container.add(stockPanel,"STOCK"); }
        stockPanel.loadData();
        cardLayout.show(container,"STOCK");
    }
    private void showBills() {
        if (billsPanel == null) { billsPanel = new AdminBillsPanel(cardLayout, container); container.add(billsPanel,"ADMIN_BILLS"); }
        billsPanel.loadBills();
        cardLayout.show(container,"ADMIN_BILLS");
    }
    private void logout() { cardLayout.show(container,"LOGIN"); }
}
