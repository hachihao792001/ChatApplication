package client;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class MainScreen extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;

	public static int chattingRoom = -1;

	JList<String> connectedServerInfoJList;

	JList<String> onlineUserJList;
	JList<String> groupJList;

	JTabbedPane roomTabbedPane;
	List<RoomMessagesPanel> roomMessagesPanels;
	JList<String> roomUsersJList;

	JPanel enterMessagePanel;
	JTextArea messageArea;

	public MainScreen() {
		GBCBuilder gbc = new GBCBuilder(1, 1);
		JPanel mainContent = new JPanel(new GridBagLayout());

		connectedServerInfoJList = new JList<String>(
				new String[] { "Port server: " + Main.socketController.connectedServer.port,
						"Số user online: " + Main.socketController.connectedServer.connectAccountCount });

		connectedServerInfoJList.setBorder(BorderFactory
				.createTitledBorder(String.format("Server %s (%s)", Main.connectServerScreen.connectedServer.nickName,
						SocketController.serverName(Main.socketController.connectedServer.port))));

		onlineUserJList = new JList<String>();
		onlineUserJList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {

					String clickedUser = onlineUserJList.getSelectedValue();
					System.out.println("Double click " + clickedUser);
					Room foundRoom = Room.findPrivateRoom(Main.socketController.allRooms, clickedUser);
					if (foundRoom == null) {
						Main.socketController.createPrivateRoom(clickedUser);
					} else {
						int roomTabIndex = -1;
						for (int i = 0; i < roomTabbedPane.getTabCount(); i++) {
							JScrollPane currentScrollPane = (JScrollPane) roomTabbedPane.getComponentAt(i);
							RoomMessagesPanel currentRoomMessagePanel = (RoomMessagesPanel) currentScrollPane
									.getViewport().getView();
							if (currentRoomMessagePanel.room.id == foundRoom.id) {
								roomTabIndex = i;
								break;
							}
						}

						if (roomTabIndex == -1) { // room tồn tại nhưng tab bị chéo trước đó
							newRoomTab(foundRoom);
							roomTabbedPane.setSelectedIndex(roomTabbedPane.getTabCount() - 1);
						} else {
							roomTabbedPane.setSelectedIndex(roomTabIndex);
						}
					}
				}
			}
		});
		JScrollPane onlineUserScrollPane = new JScrollPane(onlineUserJList);
		onlineUserScrollPane.setBorder(BorderFactory.createTitledBorder("Danh sách user đang online"));

		groupJList = new JList<String>();
		groupJList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {

					String clickedGroup = groupJList.getSelectedValue();
					System.out.println("Double click " + clickedGroup);
					Room foundRoom = Room.findGroup(Main.socketController.allRooms, clickedGroup);

					int roomTabIndex = -1;
					for (int i = 0; i < roomTabbedPane.getTabCount(); i++) {
						JScrollPane currentScrollPane = (JScrollPane) roomTabbedPane.getComponentAt(i);
						RoomMessagesPanel currentRoomMessagePanel = (RoomMessagesPanel) currentScrollPane.getViewport()
								.getView();
						if (currentRoomMessagePanel.room.id == foundRoom.id) {
							roomTabIndex = i;
							break;
						}
					}

					if (roomTabIndex == -1) { // room tồn tại nhưng tab bị chéo trước đó
						newRoomTab(foundRoom);
						roomTabbedPane.setSelectedIndex(roomTabbedPane.getTabCount() - 1);
					} else {
						roomTabbedPane.setSelectedIndex(roomTabIndex);
					}
				}
			}
		});
		JScrollPane groupListScrollPane = new JScrollPane(groupJList);
		groupListScrollPane.setBorder(BorderFactory.createTitledBorder("Danh sách group"));

		JButton createGroupButton = new JButton("Tạo group");
		createGroupButton.setActionCommand("group");
		createGroupButton.addActionListener(this);

		JPanel groupPanel = new JPanel(new GridBagLayout());
		groupPanel.add(groupListScrollPane, gbc.setGrid(1, 1).setFill(GridBagConstraints.BOTH).setWeight(1, 1));
		groupPanel.add(createGroupButton, gbc.setGrid(1, 2).setWeight(1, 0));

		JSplitPane chatSubjectSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, onlineUserScrollPane, groupPanel);
		chatSubjectSplitPane.setDividerLocation(230);

		JPanel leftPanel = new JPanel(new GridBagLayout());
		leftPanel.add(connectedServerInfoJList, gbc.setGrid(1, 1).setWeight(1, 0).setFill(GridBagConstraints.BOTH));
		leftPanel.add(chatSubjectSplitPane, gbc.setGrid(1, 2).setWeight(1, 1));

		JPanel chatPanel = new JPanel(new GridBagLayout());
		enterMessagePanel = new JPanel(new GridBagLayout());
		enterMessagePanel.setBackground(Color.white);
		JButton sendButton, fileButton, emojiButton;

		sendButton = new JButton("Gửi");
		sendButton.setActionCommand("send");
		sendButton.addActionListener(this);

		emojiButton = new JButton(new String(Character.toChars(0x1F601)));
		emojiButton.setActionCommand("emoji");
		emojiButton.addActionListener(this);

		Image scaledImage = null;
		try {
			BufferedImage img = ImageIO.read(new File("fileIcon.png"));
			scaledImage = img.getScaledInstance(16, 16, Image.SCALE_SMOOTH);
		} catch (IOException e1) {
		}
		fileButton = new JButton(new ImageIcon(scaledImage));
		fileButton.setActionCommand("file");
		fileButton.addActionListener(this);

		messageArea = new JTextArea();
		messageArea.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1, true));
		messageArea.setMinimumSize(new Dimension(100, 20));
		InputMap input = messageArea.getInputMap();
		input.put(KeyStroke.getKeyStroke("shift ENTER"), "insert-break");
		input.put(KeyStroke.getKeyStroke("ENTER"), "text-submit");
		messageArea.getActionMap().put("text-submit", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				sendButton.doClick();
			}
		});

		enterMessagePanel.add(messageArea, gbc.setGrid(1, 1).setWeight(1, 1));
		enterMessagePanel.add(sendButton,
				gbc.setGrid(2, 1).setWeight(0, 0).setFill(GridBagConstraints.NONE).setAnchor(GridBagConstraints.NORTH));
		enterMessagePanel.add(emojiButton, gbc.setGrid(3, 1));
		enterMessagePanel.add(fileButton, gbc.setGrid(4, 1));

		roomTabbedPane = new JTabbedPane();
		roomTabbedPane.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				JScrollPane selectedTab = (JScrollPane) roomTabbedPane.getSelectedComponent();
				if (selectedTab != null) {
					RoomMessagesPanel selectedMessagePanel = (RoomMessagesPanel) selectedTab.getViewport().getView();
					chattingRoom = selectedMessagePanel.room.id;
					updateRoomUsersJList();
				}
			}
		});
		roomMessagesPanels = new ArrayList<RoomMessagesPanel>();
		roomUsersJList = new JList<String>();
		roomUsersJList.setBorder(BorderFactory.createTitledBorder("User trong room hiện tại"));

		chatPanel.setBackground(Color.white);
		chatPanel.add(roomTabbedPane, gbc.setGrid(1, 1).setFill(GridBagConstraints.BOTH).setWeight(1, 1));
		chatPanel.add(enterMessagePanel, gbc.setGrid(1, 2).setWeight(1, 0));

		JSplitPane roomSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chatPanel, roomUsersJList);
		roomSplitPane.setDividerLocation(420);
		JSplitPane mainSplitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, roomSplitPane);
		mainContent.add(mainSplitpane, gbc.setGrid(1, 1).setWeight(1, 1));
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				mainSplitpane.setDividerLocation(180);
			}
		});

		this.setPreferredSize(new Dimension(800, 500));
		this.setTitle("Ứng dụng chat đăng nhập với tên " + Main.socketController.userName);
		this.setContentPane(mainContent);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	public void updateServerData() {
		Main.socketController.connectedServer.connectAccountCount = Main.socketController.onlineUsers.size();

		connectedServerInfoJList
				.setListData(new String[] { "Port server: " + Main.socketController.connectedServer.port,
						"Số user online: " + Main.socketController.connectedServer.connectAccountCount });
	}

	public void newRoomTab(Room room) {
		RoomMessagesPanel roomMessagesPanel = new RoomMessagesPanel(room);
		roomMessagesPanels.add(roomMessagesPanel);

		for (MessageData messageData : room.messages)
			addNewMessageGUI(room.id, messageData);

		JScrollPane messagesScrollPane = new JScrollPane(roomMessagesPanel);
		messagesScrollPane.setMinimumSize(new Dimension(50, 100));
		messagesScrollPane.getViewport().setBackground(Color.white);

		roomTabbedPane.addTab(room.name, messagesScrollPane);
		roomTabbedPane.setTabComponentAt(roomTabbedPane.getTabCount() - 1,
				new TabComponent(room.name, new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						roomMessagesPanels.remove(roomMessagesPanel);
						roomTabbedPane.remove(messagesScrollPane);
					}
				}));
	}

	public void updateOnlineUserJList() {
		onlineUserJList.setListData(Main.socketController.onlineUsers.toArray(new String[0]));
	}

	public void updateRoomUsersJList() {
		System.out.println("updateRoomUsersJList");
		Room theChattingRoom = Room.findRoom(Main.socketController.allRooms, chattingRoom);
		if (theChattingRoom != null)
			roomUsersJList.setListData(theChattingRoom.users.toArray(new String[0]));
	}

	public void updateGroupJList() {
		List<String> groupList = new ArrayList<String>();
		for (Room room : Main.socketController.allRooms) {
			if (room.type.equals("group"))
				groupList.add(room.name);
		}
		groupJList.setListData(groupList.toArray(new String[0]));
	}

	// ************** ROOM MESSAGES ***************
	public void addNewMessage(int roomID, String type, String whoSend, String content) {
		MessageData messageData = new MessageData(whoSend, type, content);
		Room receiveMessageRoom = Room.findRoom(Main.socketController.allRooms, roomID);
		receiveMessageRoom.messages.add(messageData);

		addNewMessageGUI(roomID, messageData);
	}

	private void addNewMessageGUI(int roomID, MessageData messageData) {

		MessagePanel newMessagePanel = new MessagePanel(messageData);
		newMessagePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		RoomMessagesPanel receiveMessageRoomMessagesPanel = RoomMessagesPanel.findRoomMessagesPanel(roomMessagesPanels,
				roomID);
		receiveMessageRoomMessagesPanel.add(Box.createHorizontalGlue());
		receiveMessageRoomMessagesPanel.add(newMessagePanel);
		receiveMessageRoomMessagesPanel.validate();
		receiveMessageRoomMessagesPanel.repaint();
		roomTabbedPane.validate();
		roomTabbedPane.repaint();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		switch (e.getActionCommand()) {
		case "group": {
			JDialog chooseUserDialog = new JDialog();
			JPanel chooseUserContent = new JPanel(new GridBagLayout());
			GBCBuilder gbc = new GBCBuilder(1, 1);

			JList<String> onlineUserJList = new JList<String>(Main.socketController.onlineUsers.toArray(new String[0]));
			JScrollPane onlineUserScrollPanel = new JScrollPane(onlineUserJList);
			onlineUserScrollPanel.setBorder(BorderFactory.createTitledBorder("Chọn user để thêm vào nhóm"));

			JLabel groupNameLabel = new JLabel("Tên group: ");
			JTextField groupNameField = new JTextField();
			JButton createButton = new JButton("Tạo group");
			createButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String groupName = groupNameField.getText();
					if (groupName.isEmpty()) {
						JOptionPane.showMessageDialog(chooseUserDialog, "Tên group không được trống", "Lỗi tạo group",
								JOptionPane.WARNING_MESSAGE);
						return;
					}
					List<String> chosenUsers = onlineUserJList.getSelectedValuesList();
					if (chosenUsers.size() < 2) {
						JOptionPane.showMessageDialog(chooseUserDialog,
								"Group phải có từ 3 người trở lên (chọn 2 người trở lên)", "Lỗi tạo group",
								JOptionPane.WARNING_MESSAGE);
						return;
					}
					Main.socketController.createGroup(groupName, chosenUsers);
					chooseUserDialog.setVisible(false);
					chooseUserDialog.dispose();
				}
			});

			chooseUserContent.add(onlineUserScrollPanel,
					gbc.setSpan(2, 1).setFill(GridBagConstraints.BOTH).setWeight(1, 0));
			chooseUserContent.add(groupNameLabel, gbc.setGrid(1, 2).setSpan(1, 1).setWeight(0, 0));
			chooseUserContent.add(groupNameField, gbc.setGrid(2, 2).setWeight(1, 0));
			chooseUserContent.add(createButton,
					gbc.setGrid(1, 3).setSpan(2, 1).setWeight(0, 0).setFill(GridBagConstraints.NONE));

			chooseUserDialog.setMinimumSize(new Dimension(300, 150));
			chooseUserDialog.setContentPane(chooseUserContent);
			chooseUserDialog.setTitle("Tạo group mới");
			chooseUserDialog.setModalityType(JDialog.DEFAULT_MODALITY_TYPE);
			chooseUserDialog.pack();
			chooseUserDialog.getRootPane().setDefaultButton(createButton);
			chooseUserDialog.setLocationRelativeTo(null);
			chooseUserDialog.setVisible(true);
			break;
		}

		case "send": {
			String content = messageArea.getText();
			if (content.isEmpty())
				break;
			if (chattingRoom != -1)
				Main.socketController.sendTextToRoom(chattingRoom, content);
			messageArea.setText("");
			break;
		}

		case "emoji": {
			JDialog emojiDialog = new JDialog();
			Object[][] emojiMatrix = new Object[6][6];
			int emojiCode = 0x1F601;
			for (int i = 0; i < 6; i++) {
				for (int j = 0; j < 6; j++)
					emojiMatrix[i][j] = new String(Character.toChars(emojiCode++));
			}

			JTable emojiTable = new JTable();
			emojiTable.setModel(new DefaultTableModel(emojiMatrix, new String[] { "", "", "", "", "", "" }) {
				private static final long serialVersionUID = 1L;

				@Override
				public boolean isCellEditable(int row, int column) {
					return false;
				}
			});
			emojiTable.setFont(new Font("Dialog", Font.PLAIN, 20));
			emojiTable.setShowGrid(false);
			emojiTable.setIntercellSpacing(new Dimension(0, 0));
			emojiTable.setRowHeight(30);
			emojiTable.getTableHeader().setVisible(false);

			DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
			centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
			for (int i = 0; i < emojiTable.getColumnCount(); i++) {
				emojiTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
				emojiTable.getColumnModel().getColumn(i).setMaxWidth(30);
			}
			emojiTable.setCellSelectionEnabled(true);
			emojiTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			emojiTable.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					messageArea.setText(messageArea.getText() + emojiTable
							.getValueAt(emojiTable.rowAtPoint(e.getPoint()), emojiTable.columnAtPoint(e.getPoint())));
				}
			});

			emojiDialog.setContentPane(emojiTable);
			emojiDialog.setTitle("Chọn emoji");
			emojiDialog.setModalityType(JDialog.DEFAULT_MODALITY_TYPE);
			emojiDialog.pack();
			emojiDialog.setLocationRelativeTo(this);
			emojiDialog.setVisible(true);
			break;
		}

		case "file": {
			JFileChooser jfc = new JFileChooser();
			jfc.setDialogTitle("Chọn file để gửi");
			int result = jfc.showDialog(null, "Chọn file");
			jfc.setVisible(true);

			if (result == JFileChooser.APPROVE_OPTION) {
				String fileName = jfc.getSelectedFile().getName();
				String filePath = jfc.getSelectedFile().getAbsolutePath();

				Main.socketController.sendFileToRoom(chattingRoom, fileName, filePath);
			}
		}
		}
	}

	public static class RoomMessagesPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		public Room room;

		public RoomMessagesPanel(Room room) {
			this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			this.setBackground(Color.white);
			this.room = room;
		}

		public static RoomMessagesPanel findRoomMessagesPanel(List<RoomMessagesPanel> roomMessagesPanelList, int id) {
			for (RoomMessagesPanel roomMessagesPanel : roomMessagesPanelList) {
				if (roomMessagesPanel.room.id == id)
					return roomMessagesPanel;
			}
			return null;
		}
	}

	public static class TabComponent extends JPanel {

		private static final long serialVersionUID = 1L;

		public TabComponent(String tabTitle, ActionListener closeButtonListener) {
			JLabel titleLabel = new JLabel(tabTitle);
			JButton closeButton = new JButton(UIManager.getIcon("InternalFrame.closeIcon"));
			closeButton.addActionListener(closeButtonListener);
			closeButton.setPreferredSize(new Dimension(16, 16));

			this.setLayout(new FlowLayout());
			this.add(titleLabel);
			this.add(closeButton);
			this.setOpaque(false);
		}

	}
}
