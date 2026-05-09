package arpharmacy;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Factory helpers that produce consistently styled Swing components.
 */
public class UIHelper {

    // ── Icon Button (big icon above text – for hub panels) ────────────────────
    public static JButton createIconButton(String emoji, String label, Color bg) {
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = getModel().isPressed()  ? bg.darker().darker()
                           : getModel().isRollover() ? bg.brighter()
                           : bg;
                // Gradient fill
                GradientPaint gp = new GradientPaint(0, 0, base.brighter(), 0, getHeight(), base.darker());
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));
                // Subtle top shine
                g2.setColor(new Color(255,255,255,40));
                g2.fill(new RoundRectangle2D.Double(4, 4, getWidth()-8, getHeight()/2, 10, 10));
                g2.dispose();

                // Draw emoji icon
                FontMetrics fmIcon = g.getFontMetrics(new Font("Segoe UI Emoji", Font.PLAIN, 28));
                g.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
                g.setColor(Color.WHITE);
                int iconX = (getWidth() - fmIcon.stringWidth(emoji)) / 2;
                g.drawString(emoji, iconX, 42);

                // Draw label
                g.setFont(new Font("Segoe UI", Font.BOLD, 13));
                FontMetrics fmLbl = g.getFontMetrics();
                g.setColor(new Color(255,255,255,230));
                int lblX = (getWidth() - fmLbl.stringWidth(label)) / 2;
                g.drawString(label, lblX, 65);
            }
            @Override protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0,0,0,40));
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Double(0,0,getWidth()-1,getHeight()-1,16,16));
                g2.dispose();
            }
            @Override public boolean isOpaque() { return false; }
        };
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(160, 80));
        btn.setToolTipText(label);
        return btn;
    }

    // ── Inline rounded button (icon + text side by side) ─────────────────────
    public static JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = getModel().isPressed()  ? bg.darker()
                           : getModel().isRollover() ? bg.brighter()
                           : bg;
                GradientPaint gp = new GradientPaint(0, 0, base.brighter(), 0, getHeight(), base);
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
            @Override protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0,0,0,30));
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Double(0,0,getWidth()-1,getHeight()-1,10,10));
                g2.dispose();
            }
            @Override public boolean isOpaque() { return false; }
        };
        btn.setFont(Theme.FONT_BTN);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(Theme.BTN_SIZE);
        return btn;
    }

    public static JButton primaryBtn(String text) {
        return createButton(text, Theme.PRIMARY, Theme.TEXT_WHITE);
    }
    public static JButton dangerBtn(String text) {
        return createButton(text, Theme.DANGER, Theme.TEXT_WHITE);
    }
    public static JButton accentBtn(String text) {
        return createButton(text, Theme.ACCENT, Theme.TEXT_WHITE);
    }
    public static JButton warningBtn(String text) {
        return createButton(text, Theme.WARNING, Theme.TEXT_WHITE);
    }
    public static JButton grayBtn(String text) {
        return createButton(text, new Color(110,120,115), Theme.TEXT_WHITE);
    }

    // ── Login button (large, full width) ──────────────────────────────────────
    public static JButton loginBtn(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = getModel().isPressed()  ? Theme.PRIMARY_DARK
                           : getModel().isRollover() ? new Color(0,170,100)
                           : Theme.PRIMARY;
                GradientPaint gp = new GradientPaint(0,0, base.brighter(), 0, getHeight(), base.darker());
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Double(0,0,getWidth(),getHeight(),12,12));
                g2.dispose();
                super.paintComponent(g);
            }
            @Override protected void paintBorder(Graphics g) {}
            @Override public boolean isOpaque() { return false; }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ── Text Field ────────────────────────────────────────────────────────────
    public static JTextField createField(int columns) {
        JTextField f = new JTextField(columns);
        f.setFont(Theme.FONT_FIELD);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        f.setBackground(Theme.BG_CARD);
        return f;
    }

    public static JPasswordField createPasswordField(int columns) {
        JPasswordField f = new JPasswordField(columns);
        f.setFont(Theme.FONT_FIELD);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        f.setBackground(Theme.BG_CARD);
        return f;
    }

    // ── Labels ────────────────────────────────────────────────────────────────
    public static JLabel titleLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_TITLE);
        l.setForeground(Theme.TEXT_WHITE);
        return l;
    }
    public static JLabel bodyLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_BODY);
        l.setForeground(Theme.TEXT_DARK);
        return l;
    }
    public static JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_BODY);
        l.setForeground(Theme.TEXT_MID);
        return l;
    }

    // ── ComboBox ──────────────────────────────────────────────────────────────
    public static JComboBox<String> createCombo(String... items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(Theme.FONT_FIELD);
        cb.setBackground(Theme.BG_CARD);
        cb.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1, true));
        return cb;
    }

    // ── Styled JTable ─────────────────────────────────────────────────────────
    public static void styleTable(JTable table) {
        table.setFont(Theme.FONT_TABLE);
        table.setRowHeight(30);
        table.setGridColor(Theme.BORDER);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setSelectionBackground(Theme.PRIMARY_LIGHT);
        table.setSelectionForeground(Theme.TEXT_DARK);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(Theme.FONT_TABLE_HDR);
        header.setBackground(Theme.TABLE_HEADER);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 34));
        header.setReorderingAllowed(false);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t,val,sel,focus,row,col);
                if (!sel) c.setBackground(row%2==0 ? Theme.TABLE_EVEN : Theme.TABLE_ODD);
                ((JLabel)c).setBorder(BorderFactory.createEmptyBorder(0,8,0,8));
                return c;
            }
        });
    }

    // ── Card panel ────────────────────────────────────────────────────────────
    public static JPanel createCard() {
        JPanel p = new JPanel();
        p.setBackground(Theme.BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(20, 24, 20, 24)));
        return p;
    }

    // ── Header bar ────────────────────────────────────────────────────────────
    public static JPanel createHeader(String title, String subtitle) {
        JPanel header = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0,0, Theme.BG_HEADER.brighter(),
                                                     getWidth(),0, Theme.BG_HEADER.darker());
                g2.setPaint(gp);
                g2.fillRect(0,0,getWidth(),getHeight());
                g2.dispose();
            }
        };
        header.setBorder(BorderFactory.createEmptyBorder(14, 24, 14, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        // Cross/plus logo
        JLabel icon = new JLabel("✚");
        icon.setFont(new Font("Segoe UI", Font.BOLD, 30));
        icon.setForeground(new Color(180,255,210));
        icon.setBorder(BorderFactory.createEmptyBorder(0,0,0,14));
        gbc.gridx=0; gbc.gridy=0; gbc.gridheight=2; gbc.weightx=0;
        header.add(icon, gbc);

        gbc.gridheight=1; gbc.gridx=1; gbc.weightx=1;
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLbl.setForeground(Color.WHITE);
        header.add(titleLbl, gbc);

        gbc.gridy=1;
        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(Theme.FONT_SMALL);
        subLbl.setForeground(new Color(200,240,220));
        header.add(subLbl, gbc);

        // Right-side pharmacy logo text
        gbc.gridx=2; gbc.gridy=0; gbc.gridheight=2; gbc.weightx=0; gbc.anchor=GridBagConstraints.EAST;
        JLabel rightLogo = new JLabel("💊 AR Pharmacy");
        rightLogo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        rightLogo.setForeground(new Color(200,240,220));
        header.add(rightLogo, gbc);

        return header;
    }

    public static JSeparator separator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(Theme.BORDER);
        return sep;
    }

    public static void success(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    public static void error(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
    public static boolean confirm(Component parent, String msg) {
        return JOptionPane.showConfirmDialog(parent, msg, "Confirm",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    private UIHelper() {}
}
