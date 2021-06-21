package client;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class ConnectServerScreen extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	public ServerData connectedServer;
	JTable serverTable;
	List<ServerData> serverList;

	public ConnectServerScreen() {
		GBCBuilder gbc = new GBCBuilder(1, 1);
		JPanel connectServerContent = new JPanel(new GridBagLayout());

		JButton refreshButton = new JButton("Làm mới");
		refreshButton.setActionCommand("refresh");
		refreshButton.addActionListener(this);

		serverTable = new JTable();
		serverTable.setRowHeight(25);
		serverTable.setDefaultRenderer(String.class, new DefaultTableCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				if (column == 4) {
					c.setForeground(value.toString().equals("Hoạt động") ? Color.green : Color.red);
					c.setFont(new Font("Dialog", Font.BOLD, 13));
				} else
					c.setForeground(Color.black);

				return c;
			}
		});
		serverList = FileManager.getServerList();

		updateServerTable();

		JScrollPane serverScrollPane = new JScrollPane(serverTable);
		serverScrollPane.setBorder(BorderFactory.createTitledBorder("Danh sách server để kết nối"));

		JButton joinButton = new JButton("Tham gia server");
		joinButton.addActionListener(this);
		joinButton.setActionCommand("join");
		serverTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					joinButton.doClick();
				}
			}
		});

		JButton addButton = new JButton("Thêm");
		addButton.addActionListener(this);
		addButton.setActionCommand("add");

		JButton deleteButton = new JButton("Xoá");
		deleteButton.addActionListener(this);
		deleteButton.setActionCommand("delete");

		JButton editButton = new JButton("Sửa");
		editButton.addActionListener(this);
		editButton.setActionCommand("edit");

		connectServerContent.add(serverScrollPane,
				gbc.setSpan(3, 1).setGrid(1, 1).setWeight(1, 1).setFill(GridBagConstraints.BOTH).setInsets(5));
		JPanel joinRefreshPanel = new JPanel(new FlowLayout());

		joinRefreshPanel.add(joinButton);
		joinRefreshPanel.add(refreshButton);
		connectServerContent.add(joinRefreshPanel,
				gbc.setGrid(1, 2).setSpan(3, 1).setWeight(1, 0).setFill(GridBagConstraints.NONE));

		connectServerContent.add(addButton, gbc.setSpan(1, 1).setGrid(1, 3).setFill(GridBagConstraints.BOTH));
		connectServerContent.add(deleteButton, gbc.setGrid(2, 3));
		connectServerContent.add(editButton, gbc.setGrid(3, 3));

		this.setTitle("Ứng dụng chat");
		this.setContentPane(connectServerContent);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	JTextField nameText;

	@Override
	public void actionPerformed(ActionEvent e) {
		switch (e.getActionCommand()) {
		case "join": {
			if (serverTable.getSelectedRow() == -1)
				break;
			String serverState = serverTable.getValueAt(serverTable.getSelectedRow(), 4).toString();
			if (serverState.equals("Không hoạt động")) {
				JOptionPane.showMessageDialog(this, "Server không hoạt động", "Thông báo",
						JOptionPane.INFORMATION_MESSAGE);
				break;
			}

			JDialog askNameDialog = new JDialog();

			nameText = new JTextField();
			nameText.setPreferredSize(new Dimension(250, 30));
			JButton joinServerButton = new JButton("Vào");
			joinServerButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					if (nameText.getText().isEmpty())
						JOptionPane.showMessageDialog(askNameDialog, "Tên không được trống", "Thông báo",
								JOptionPane.INFORMATION_MESSAGE);
					else {
						String selectedIP = serverTable.getValueAt(serverTable.getSelectedRow(), 2).toString();
						int selectedPort = Integer
								.parseInt(serverTable.getValueAt(serverTable.getSelectedRow(), 3).toString());
						ServerData selectedServer = serverList.stream()
								.filter(x -> x.ip.equals(selectedIP) && x.port == selectedPort).findAny().orElse(null);

						Main.socketController = new SocketController(nameText.getText(), selectedServer);
						Main.socketController.Login();
						// kết quả join ở loginResultAction
						askNameDialog.setVisible(false);
						askNameDialog.dispose();
					}
				}
			});

			JPanel askNameContent = new JPanel(new GridBagLayout());
			askNameContent.add(nameText, new GBCBuilder(1, 1).setFill(GridBagConstraints.BOTH).setWeight(1, 1));
			askNameContent.add(joinServerButton, new GBCBuilder(2, 1).setFill(GridBagConstraints.BOTH));

			askNameDialog.setContentPane(askNameContent);
			askNameDialog.setTitle("Nhập tên của bạn để vào server "
					+ serverTable.getValueAt(serverTable.getSelectedRow(), 0).toString());
			askNameDialog.setModalityType(JDialog.DEFAULT_MODALITY_TYPE);
			askNameDialog.pack();
			askNameDialog.getRootPane().setDefaultButton(joinServerButton);
			askNameDialog.setLocationRelativeTo(null);
			askNameDialog.setVisible(true);
			break;
		}
		case "add": {
			JDialog addServerDialog = new JDialog();

			JLabel nickNameLabel = new JLabel("Biệt danh server");
			JLabel ipLabel = new JLabel("IP");
			JLabel portLabel = new JLabel("Port");
			JTextField nickNameText = new JTextField();
			nickNameText.setPreferredSize(new Dimension(150, 20));
			JTextField ipText = new JTextField();
			JTextField portText = new JTextField();
			JButton addServerButton = new JButton("Thêm");
			addServerButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						int port = Integer.parseInt(portText.getText());
						String nickName = nickNameText.getText();
						String ip = ipText.getText();
						String serverName = SocketController.serverName(ip, port);

						if (serverList == null)
							serverList = new ArrayList<ServerData>();
						serverList.add(new ServerData(nickName, serverName, ip, port, false, 0));

						FileManager.setServerList(serverList);
						updateServerTable();

						addServerDialog.setVisible(false);
						addServerDialog.dispose();
					} catch (NumberFormatException ex) {
						JOptionPane.showMessageDialog(addServerDialog, "Port phải là 1 số nguyên dương", "Thông báo",
								JOptionPane.INFORMATION_MESSAGE);
					}
				}
			});

			GBCBuilder gbc = new GBCBuilder(1, 1).setFill(GridBagConstraints.BOTH).setWeight(1, 0).setInsets(5);
			JPanel addServerContent = new JPanel(new GridBagLayout());
			// askPortContent.setPreferredSize(new Dimension(200, 50));
			addServerContent.add(nickNameLabel, gbc.setWeight(0, 0));
			addServerContent.add(nickNameText, gbc.setGrid(2, 1).setWeight(1, 0));
			addServerContent.add(ipLabel, gbc.setGrid(1, 2).setWeight(0, 0));
			addServerContent.add(ipText, gbc.setGrid(2, 2).setWeight(1, 0));
			addServerContent.add(portLabel, gbc.setGrid(1, 3).setWeight(0, 0));
			addServerContent.add(portText, gbc.setGrid(2, 3).setWeight(1, 0));
			addServerContent.add(addServerButton,
					gbc.setGrid(1, 4).setSpan(2, 1).setWeight(0, 0).setFill(GridBagConstraints.NONE));

			addServerDialog.setContentPane(addServerContent);
			addServerDialog.setTitle("Nhập port của server");
			addServerDialog.getRootPane().setDefaultButton(addServerButton);
			addServerDialog.setModalityType(JDialog.DEFAULT_MODALITY_TYPE);
			addServerDialog.pack();
			addServerDialog.setLocationRelativeTo(null);
			addServerDialog.setVisible(true);

			break;
		}
		case "delete": {
			if (serverTable.getSelectedRow() == -1)
				break;

			String selectedIP = serverTable.getValueAt(serverTable.getSelectedRow(), 2).toString();
			int selectedPort = Integer.parseInt(serverTable.getValueAt(serverTable.getSelectedRow(), 3).toString());
			for (int i = 0; i < serverList.size(); i++) {
				if (serverList.get(i).ip.equals(selectedIP) && serverList.get(i).port == selectedPort) {
					serverList.remove(i);
					break;
				}
			}
			FileManager.setServerList(serverList);
			updateServerTable();
			break;
		}
		case "edit": {
			if (serverTable.getSelectedRow() == -1)
				break;

			String selectedIP = serverTable.getValueAt(serverTable.getSelectedRow(), 2).toString();
			int selectedPort = Integer.parseInt(serverTable.getValueAt(serverTable.getSelectedRow(), 3).toString());
			ServerData edittingServer = serverList.stream()
					.filter(x -> x.ip.equals(selectedIP) && x.port == selectedPort).findAny().orElse(null);

			JDialog editDialog = new JDialog();

			JLabel serverNameLabel = new JLabel("Biệt danh server");
			JTextField nickNameText = new JTextField(edittingServer.nickName);
			nickNameText.setPreferredSize(new Dimension(150, 20));
			JLabel ipLabel = new JLabel("IP");
			JTextField ipText = new JTextField(edittingServer.ip);
			JLabel portLabel = new JLabel("Port");
			JTextField portText = new JTextField("" + edittingServer.port);
			portText.setPreferredSize(new Dimension(150, 20));
			JButton editButton = new JButton("Sửa");
			editButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						String newServerName = nickNameText.getText();
						int newPort = Integer.parseInt(portText.getText());
						String newIP = ipText.getText();

						edittingServer.nickName = newServerName;
						edittingServer.port = newPort;
						edittingServer.ip = newIP;

						FileManager.setServerList(serverList);

						updateServerTable();

						editDialog.setVisible(false);
						editDialog.dispose();

					} catch (NumberFormatException ex) {
						JOptionPane.showMessageDialog(editDialog, "Port phải là 1 số nguyên dương", "Thông báo",
								JOptionPane.INFORMATION_MESSAGE);
					}
				}
			});

			JPanel editContent = new JPanel(new GridBagLayout());
			GBCBuilder gbc = new GBCBuilder(1, 1).setFill(GridBagConstraints.BOTH).setWeight(1, 0).setInsets(5);
			editContent.add(serverNameLabel, gbc);
			editContent.add(nickNameText, gbc.setGrid(2, 1));
			editContent.add(portLabel, gbc.setGrid(1, 2));
			editContent.add(portText, gbc.setGrid(2, 2));
			editContent.add(ipLabel, gbc.setGrid(1, 3));
			editContent.add(ipText, gbc.setGrid(2, 3));
			editContent.add(editButton, gbc.setGrid(1, 4).setSpan(2, 1));

			editDialog.setTitle("Chỉnh sửa thông tin server");
			editDialog.setContentPane(editContent);
			editDialog.getRootPane().setDefaultButton(editButton);
			editDialog.setModalityType(JDialog.DEFAULT_MODALITY_TYPE);
			editDialog.setLocationRelativeTo(null);
			editDialog.pack();
			editDialog.setVisible(true);
			break;
		}

		case "refresh": {
			updateServerTable();
			break;
		}
		}
	}

	public void loginResultAction(String result) {
		if (result.equals("success")) {
			String selectedIP = serverTable.getValueAt(serverTable.getSelectedRow(), 2).toString();
			int selectedPort = Integer.parseInt(serverTable.getValueAt(serverTable.getSelectedRow(), 3).toString());
			connectedServer = serverList.stream().filter(x -> x.ip.equals(selectedIP) && x.port == selectedPort)
					.findAny().orElse(null);

			this.setVisible(false);
			this.dispose();
			Main.mainScreen = new MainScreen();

		} else if (result.equals("existed"))
			JOptionPane.showMessageDialog(this, "Username đã tồn tại", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
		else if (result.equals("closed"))
			JOptionPane.showMessageDialog(this, "Server đã đóng", "Thông báo", JOptionPane.INFORMATION_MESSAGE);

	}

	public void updateServerTable() {
		if (serverList == null)
			return;
		for (ServerData serverData : serverList) {
			serverData.isOpen = SocketController.serverOnline(serverData.ip, serverData.port);
			if (serverData.isOpen) {
				serverData.realName = SocketController.serverName(serverData.ip, serverData.port);
				serverData.connectAccountCount = SocketController.serverConnectedAccountCount(serverData.ip,
						serverData.port);
			}
		}

		serverTable.setModel(new DefaultTableModel(FileManager.getServerObjectMatrix(serverList), new String[] {
				"Biệt danh server", "Tên gốc server", "IP server", "Port server", "Trạng thái", "Số user online" }) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int arg0, int arg1) {
				return false;
			}

			@Override
			public Class<?> getColumnClass(int columnIndex) {
				return String.class;
			}

		});
	}
}
