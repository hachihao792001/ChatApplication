package client;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class LoginScreen extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;

	public static JOptionPane loading;

	private JLabel Title;
	private JLabel userNameLabel;
	private JTextField userNameText;
	private JButton registerButton;
	private JLabel passLabel;
	private JTextField passText;

	public LoginScreen() {
		GBCBuilder gbc;
		JPanel registerContent = new JPanel(new GridBagLayout());

		Title = new JLabel();
		userNameLabel = new JLabel();
		userNameText = new JTextField();
		passLabel = new JLabel();
		passText = new JTextField();
		registerButton = new JButton();

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		registerContent.setLayout(new GridBagLayout());

		Title.setFont(new Font("Tahoma", 0, 18)); // NOI18N
		Title.setHorizontalAlignment(SwingConstants.CENTER);
		Title.setText("Ứng dụng chat");
		Title.setAlignmentY(0.0F);
		Title.setHorizontalTextPosition(SwingConstants.CENTER);
		Title.setVerifyInputWhenFocusTarget(false);
		gbc = new GBCBuilder(1, 1);
		registerContent.add(Title, gbc.setSpan(2, 1).setInsets(20).setFill(GridBagConstraints.BOTH).setWeight(1, 0));

		userNameLabel.setText("Tài khoản");
		registerContent.add(userNameLabel, gbc.setGrid(1, 2).setSpan(1, 1).setInsets(0, 20, 10, 0).setWeight(0, 0));

		userNameText.setText("");
		registerContent.add(userNameText, gbc.setGrid(2, 2).setInsets(0, 20, 10, 20).setWeight(1, 0));

		passLabel.setText("Mật khẩu: ");
		registerContent.add(passLabel, gbc.setGrid(1, 3).setInsets(0, 20, 20, 0).setWeight(0, 0));

		passText.setText("");
		registerContent.add(passText, gbc.setGrid(2, 3).setInsets(0, 20, 20, 20).setWeight(1, 0));

		registerButton.setText("Đăng nhập");
		registerButton.setActionCommand("login");
		registerButton.addActionListener(this);
		getRootPane().setDefaultButton(registerButton);
		registerContent.add(registerButton, gbc.setGrid(1, 4).setSpan(2, 1).setInsets(0, 50, 20, 50).setWeight(0, 0));

		this.setTitle("Đăng ký chat");
		this.setContentPane(registerContent);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
	}

	
}
