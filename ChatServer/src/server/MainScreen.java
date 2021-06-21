package server;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class MainScreen extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;

	JLabel portLabel;
	JTextField portText;
	JLabel serverNameLabel;
	JTextField serverNameText;

	static JTable clientTable;
	JButton openCloseButton;
	boolean isSocketOpened = false;

	public MainScreen() {
		JPanel mainContent = new JPanel(new GridBagLayout());
		GBCBuilder gbc = new GBCBuilder(1, 1).setInsets(5);

		JLabel ipLabel = new JLabel("IP: " + SocketController.getThisIP());

		portLabel = new JLabel("Port: ");
		portText = new JTextField();
		serverNameLabel = new JLabel("Tên server: ");
		serverNameText = new JTextField();
		openCloseButton = new JButton("Mở server");
		openCloseButton.addActionListener(this);

		clientTable = new JTable(new Object[][] {}, new String[] { "Tên client", "Port client" });
		clientTable.setRowHeight(25);
		JScrollPane clientScrollPane = new JScrollPane(clientTable);
		clientScrollPane.setBorder(BorderFactory.createTitledBorder("Danh sách client đang kết nối"));

		mainContent.add(ipLabel, gbc.setFill(GridBagConstraints.BOTH).setWeight(0, 0).setSpan(1, 1));
		mainContent.add(portLabel, gbc.setGrid(2, 1).setWeight(0, 0).setSpan(1, 1));
		mainContent.add(portText, gbc.setGrid(3, 1).setWeight(1, 0));
		mainContent.add(serverNameLabel, gbc.setGrid(1, 2).setWeight(0, 0).setSpan(1, 1));
		mainContent.add(serverNameText, gbc.setGrid(2, 2).setWeight(1, 0).setSpan(2, 1));
		mainContent.add(clientScrollPane,
				gbc.setGrid(1, 3).setFill(GridBagConstraints.BOTH).setWeight(1, 1).setSpan(4, 1));
		mainContent.add(openCloseButton, gbc.setGrid(1, 4).setWeight(1, 0).setSpan(4, 1));
		mainContent.setPreferredSize(new Dimension(250, 300));

		this.setTitle("Server chat");
		this.setContentPane(mainContent);
		this.getRootPane().setDefaultButton(openCloseButton);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);

		Main.socketController = new SocketController();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (!isSocketOpened) {
			try {
				if (serverNameText.getText().isEmpty())
					JOptionPane.showMessageDialog(this, "Tên server không được trống", "Lỗi",
							JOptionPane.WARNING_MESSAGE);
				else if (portText.getText().isEmpty())
					JOptionPane.showMessageDialog(this, "Port không được trống", "Lỗi", JOptionPane.WARNING_MESSAGE);
				else {

					Main.socketController.serverName = serverNameText.getText();
					Main.socketController.serverPort = Integer.parseInt(portText.getText());

					Main.socketController.OpenSocket(Main.socketController.serverPort);
					isSocketOpened = true;
					openCloseButton.setText("Đóng server");
				}

			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(this, "Port phải là 1 số nguyên dương", "Lỗi",
						JOptionPane.WARNING_MESSAGE);
			}
		} else {
			Main.socketController.CloseSocket();
			isSocketOpened = false;
			openCloseButton.setText("Mở server");
		}
	}

	public void updateClientTable() {

		Object[][] tableContent = new Object[Main.socketController.connectedClient.size()][2];
		for (int i = 0; i < Main.socketController.connectedClient.size(); i++) {
			tableContent[i][0] = Main.socketController.connectedClient.get(i).userName;
			tableContent[i][1] = Main.socketController.connectedClient.get(i).port;
		}

		clientTable.setModel(new DefaultTableModel(tableContent, new String[] { "Tên client", "Port client" }));
	}
}
