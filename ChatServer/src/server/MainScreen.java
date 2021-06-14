package server;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class MainScreen extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;

	JLabel portLabel;
	static JTable clientTable;
	JButton openCloseButton;
	int port = 2190;
	boolean isSocketOpened = false;

	public MainScreen() {
		JPanel mainContent = new JPanel(new GridBagLayout());
		GBCBuilder gbc = new GBCBuilder(1, 1).setInsets(5);

		portLabel = new JLabel("Port: " + port);
		openCloseButton = new JButton("Mở server");
		openCloseButton.addActionListener(this);

		clientTable = new JTable(new Object[][] {}, new String[] { "Tên client", "Port client" });
		clientTable.setRowHeight(25);
		JScrollPane clientScrollPane = new JScrollPane(clientTable);
		clientScrollPane.setBorder(BorderFactory.createTitledBorder("Danh sách client đang kết nối"));

		mainContent.add(portLabel, gbc);
		mainContent.add(clientScrollPane, gbc.setGrid(1, 2).setFill(GridBagConstraints.BOTH).setWeight(1, 1));
		mainContent.add(openCloseButton, gbc.setGrid(1, 3).setWeight(1, 0));
		mainContent.setPreferredSize(new Dimension(250, 200));

		this.setTitle("Server chat");
		this.setContentPane(mainContent);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);

		Main.socketController = new SocketController();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (!isSocketOpened) {
			Main.socketController.OpenSocket(port);
			isSocketOpened = true;
			openCloseButton.setText("Đóng server");
		} else {
			Main.socketController.CloseSocket();
			isSocketOpened = false;
			openCloseButton.setText("Mở server");
		}
	}

	public void updateClientTable() {

		Object[][] tableContent = new Object[Main.socketController.connectedClient.size()][2];
		for (int i = 0; i < Main.socketController.connectedClient.size(); i++) {
			tableContent[i][0] = Main.socketController.connectedClient.get(i).account.getUserName();
			tableContent[i][1] = Main.socketController.connectedClient.get(i).port;
		}

		clientTable.setModel(new DefaultTableModel(tableContent, new String[] { "Tên client", "Port client" }));
	}
}
