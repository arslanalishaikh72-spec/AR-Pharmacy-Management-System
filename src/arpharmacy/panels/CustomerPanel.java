package arpharmacy.panels;

import arpharmacy.Theme;
import arpharmacy.UIHelper;

import javax.swing.*;
import java.awt.*;

/**
 * Customer Panel hub with icon buttons.
 */
public class CustomerPanel extends JPanel {

    private final CardLayout cardLayout;
    private final JPanel     container;
    private JLabel           lblWelcome;
    private CustomerShopPanel shopPanel;

    public CustomerPanel(CardLayout cardLayout, JPanel container) {
        this.cardLayout = cardLayout;
        this.container  = container;
        setName("CUSTOMER");
        buildUI();
    }

    public void refresh(String name, int userId) {
        lblWelcome.setText("Welcome, " + name + "  \u2713");
        if (shopPanel == null) {
            shopPanel = new CustomerShopPanel(cardLayout, container);
            container.add(shopPanel, "CUSTOMER_SHOP");
        }
        shopPanel.init(name, userId);
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG_MAIN);

        add(UIHelper.createHeader("AR Pharmacy System", "Customer Portal"), BorderLayout.NORTH);

        JPanel centre = new JPanel(new GridBagLayout());
        centre.setBackground(Theme.BG_MAIN);
        centre.setBorder(BorderFactory.createEmptyBorder(30, 60, 30, 60));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets  = new Insets(6, 14, 6, 14);
        gc.fill    = GridBagConstraints.BOTH;
        gc.weightx = 1; gc.weighty = 0;

        lblWelcome = new JLabel("Welcome!", SwingConstants.CENTER);
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblWelcome.setForeground(Theme.PRIMARY_DARK);
        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2;
        centre.add(lblWelcome, gc);

        gc.gridy = 1;
        JLabel hint = new JLabel("What would you like to do today?", SwingConstants.CENTER);
        hint.setFont(Theme.FONT_SMALL);
        hint.setForeground(Theme.TEXT_MID);
        centre.add(hint, gc);

        gc.gridwidth = 1; gc.weighty = 1;
        gc.gridy = 2; gc.insets = new Insets(16, 14, 6, 14);

        // Browse & Buy
        JButton btnShop = UIHelper.createIconButton("\ud83d\udc8a", "Browse & Buy", new Color(0,150,90));
        btnShop.setPreferredSize(new Dimension(180, 100));
        btnShop.addActionListener(e -> goToShop());
        gc.gridx = 0;
        centre.add(btnShop, gc);

        // Logout
        JButton btnLogout = UIHelper.createIconButton("\ud83d\udeaa", "Logout", new Color(90,90,90));
        btnLogout.setPreferredSize(new Dimension(180, 100));
        btnLogout.addActionListener(e -> logout());
        gc.gridx = 1;
        centre.add(btnLogout, gc);

        add(centre, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBackground(Theme.BG_MAIN);
        JLabel ftl = new JLabel("\ud83d\udc8a  AR Pharmacy System  \u2022  Customer Portal  \u2022  v2.0");
        ftl.setFont(Theme.FONT_SMALL);
        ftl.setForeground(Theme.TEXT_MID);
        footer.add(ftl);
        add(footer, BorderLayout.SOUTH);
    }

    private void goToShop() {
        if (shopPanel != null) { shopPanel.loadMedicines(); cardLayout.show(container, "CUSTOMER_SHOP"); }
    }
    private void logout() { cardLayout.show(container, "LOGIN"); }
}
