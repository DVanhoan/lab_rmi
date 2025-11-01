import javax.swing.*;
import java.awt.*;

public class Home extends JFrame {
    public JPanel mainPanel, panelInfo, panelNorth, panelHeader, panelButton;
    public JLabel lblUsername, lblBalance, lblHello;
    public JButton btnWithdraw, btnTransfer, btnRecharge, btnChangePass;

    private Interf bank;
    private String username;

    public Home(Interf bank, String username) {
        this.bank = bank;
        this.username = username;
        init();
        loadData();
        initEvents();
    }

    public void init() {
        setTitle("ATM");
        setSize(400, 500);
        add(getContainer());
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public JPanel getContainer() {
        mainPanel = new JPanel(new GridLayout(2, 1));

        Font font = new Font("Arial", Font.BOLD, 18);
        Font font2 = new Font("Arial", Font.PLAIN, 13);

        // NORTH
        panelNorth = new JPanel(new BorderLayout());

        panelHeader = new JPanel(new GridLayout(2, 1));
        panelHeader.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        lblHello = new JLabel("Hello, welcome to Hoan ATM", JLabel.CENTER);

        panelInfo = new JPanel(new GridLayout(2, 1));
        lblUsername = new JLabel("Username: " + username);
        lblBalance = new JLabel("Balance: ...");

        lblBalance.setFont(font);
        lblUsername.setFont(font);
        lblHello.setFont(font);

        panelInfo.add(lblUsername);
        panelInfo.add(lblBalance);
        panelHeader.add(lblHello);

        panelNorth.add(panelHeader, BorderLayout.CENTER);
        panelNorth.add(panelInfo, BorderLayout.SOUTH);

        // BUTTON PANEL
        panelButton = new JPanel(new GridLayout(2, 2, 5, 5));
        panelButton.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        btnWithdraw = new JButton("Withdraw");
        btnTransfer = new JButton("Transfer");
        btnRecharge = new JButton("Recharge");
        btnChangePass = new JButton("Change Password");

        btnWithdraw.setFont(font2);
        btnTransfer.setFont(font2);
        btnRecharge.setFont(font2);
        btnChangePass.setFont(font2);

        panelButton.add(btnWithdraw);
        panelButton.add(btnTransfer);
        panelButton.add(btnRecharge);
        panelButton.add(btnChangePass);

        mainPanel.add(panelNorth);
        mainPanel.add(panelButton);

        return mainPanel;
    }

    // Lấy số dư từ server
    public void loadData() {
        try {
            lblBalance.setText("Balance: " + bank.getBalance(username));
        } catch (Exception e) {
            lblBalance.setText("Balance: ERROR");
        }
    }

    // Xử lý sự kiện
    public void initEvents() {
        btnWithdraw.addActionListener(e -> {
            String inp = JOptionPane.showInputDialog(this, "Enter amount:");
            if (inp != null) {
                try {
                    double money = Double.parseDouble(inp);
                    bank.withdraw(username, money);
                    loadData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Transaction failed!");
                }
            }
        });

        btnRecharge.addActionListener(e -> {
            String inp = JOptionPane.showInputDialog(this, "Enter amount:");
            if (inp != null) {
                try {
                    bank.deposit(username, Double.parseDouble(inp));
                    loadData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Transaction failed!");
                }
            }
        });

        btnTransfer.addActionListener(e -> {
            JTextField tfUser = new JTextField();
            JTextField tfMoney = new JTextField();
            Object[] msg = { "Receiver Username:", tfUser, "Amount:", tfMoney };
            int opt = JOptionPane.showConfirmDialog(this, msg, "Transfer", JOptionPane.OK_CANCEL_OPTION);
            if (opt == JOptionPane.OK_OPTION) {
                try {
                    boolean ok = bank.transfer(username,
                            tfUser.getText(), Double.parseDouble(tfMoney.getText()));

                    JOptionPane.showMessageDialog(this,
                            ok ? "Success!" : "Failed!");
                    loadData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error!");
                }
            }
        });

        btnChangePass.addActionListener(e -> {
            String newPass = JOptionPane.showInputDialog(this, "New password:");
            try {
                bank.changePassword(username, newPass);
                JOptionPane.showMessageDialog(this, "Password changed!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed!");
            }
        });
    }
}
