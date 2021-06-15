package client;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class MainScreen extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;

	String chattingToUser;

	JList<String> connectedServerInfoJList;
	JList<String> roomJList;

	JScrollPane messagesScrollPane;
	JList<JPanel> messagesJList;
	List<Room> rooms;

	JTextField messageField;

	public MainScreen() {
		GBCBuilder gbc = new GBCBuilder(1, 1);
		JPanel mainContent = new JPanel(new GridBagLayout());

		connectedServerInfoJList = new JList<String>(
				new String[] { "Port server: " + Main.socketController.connectedServer.port,
						"Số user online: " + Main.socketController.connectedServer.connectAccountCount });

		connectedServerInfoJList.setBorder(BorderFactory
				.createTitledBorder(String.format("Server %s (%s)", Main.connectServerScreen.connectedServer.nickName,
						SocketController.serverName(Main.socketController.connectedServer.port))));

		rooms = new ArrayList<Room>();
		roomJList = new JList<String>();
		roomJList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (roomJList.getSelectedIndex() == -1)
					return;

				String selectedRoom = roomJList.getSelectedValue();

				Room room = null;
				for (int i = 0; i < rooms.size(); i++) {
					if (rooms.get(i).otherUser.equals(selectedRoom)) {
						room = rooms.get(i);
						break;
					}
				}

				chattingToUser = selectedRoom;
				messagesPanelSwitchRoom(room);
			}
		});
		JScrollPane roomListScrollPane = new JScrollPane(roomJList);
		roomListScrollPane.setBorder(BorderFactory.createTitledBorder("Danh sách user đang online"));

		JPanel leftPanel = new JPanel(new GridBagLayout());
		leftPanel.add(connectedServerInfoJList, gbc.setGrid(1, 1).setWeight(1, 0).setFill(GridBagConstraints.BOTH));
		leftPanel.add(roomListScrollPane, gbc.setGrid(1, 2).setWeight(1, 1));

		JPanel chatPanel = new JPanel(new GridBagLayout());
		messageField = new JTextField();
		JButton sendButton = new JButton("Gửi");
		sendButton.addActionListener(this);
		sendButton.setActionCommand("send");

		messagesJList = new JList<JPanel>();
		messagesJList.setCellRenderer(new ListCellRenderer<JPanel>() {
			@Override
			public Component getListCellRendererComponent(JList<? extends JPanel> list, JPanel value, int index,
					boolean isSelected, boolean cellHasFocus) {
				return (JPanel) value;
			}
		});
		messagesScrollPane = new JScrollPane(messagesJList);
		messagesScrollPane.setMinimumSize(new Dimension(50, 100));

		chatPanel.add(messagesScrollPane,
				gbc.setGrid(1, 1).setSpan(2, 1).setFill(GridBagConstraints.BOTH).setWeight(1, 1));
		chatPanel.add(messageField, gbc.setGrid(1, 2).setSpan(1, 1).setWeight(1, 0));
		chatPanel.add(sendButton, gbc.setGrid(2, 2).setWeight(0, 0));

		JSplitPane mainSplitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, chatPanel);
		mainContent.add(mainSplitpane, gbc.setGrid(1, 1).setWeight(1, 1));
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				mainSplitpane.setDividerLocation(mainSplitpane.getSize().width / 4);
			}
		});

		this.setTitle("Ứng dụng chat đăng nhập với " + Main.socketController.userName);
		this.setContentPane(mainContent);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	// ************** ROOMS LIST ***************
	public void updateServerData() {
		Main.socketController.connectedServer.connectAccountCount = rooms.size();

		connectedServerInfoJList = new JList<String>(
				new String[] { "Port server: " + Main.socketController.connectedServer.port,
						"Số user online: " + Main.socketController.connectedServer.connectAccountCount });
	}

	public void updateRoomJList() {
		String[] onlineAccounts = new String[rooms.size()];
		for (int i = 0; i < rooms.size(); i++)
			onlineAccounts[i] = rooms.get(i).otherUser;
		roomJList.setListData(onlineAccounts);

		if (roomJList.getModel().getSize() > 0 && roomJList.getSelectedIndex() == -1)
			roomJList.getSelectionModel().setSelectionInterval(0, 0);
	}

	public void messagesPanelSwitchRoom(Room room) {

		JPanel[] messagePanels = new JPanel[room.messages.size()];
		for (int i = 0; i < room.messages.size(); i++)
			messagePanels[i] = new MessagePanel(room.messages.get(i));

		messagesJList.setListData(messagePanels);
	}

	// ************** ROOM MESSAGES ***************
	public void addNewMessage(String roomUser, String whoSend, String newMessage) {
		if (roomUser == null)
			return;
		MessageData messageData = new MessageData(whoSend, "text", newMessage.getBytes());

		int roomIndex = findRoomIndex(roomUser);
		rooms.get(roomIndex).messages.add(messageData);

		if (roomUser.equals(chattingToUser)) {
			JPanel[] messagePanels = new JPanel[rooms.get(roomIndex).messages.size()];
			for (int i = 0; i < rooms.get(roomIndex).messages.size(); i++)
				messagePanels[i] = new MessagePanel(rooms.get(roomIndex).messages.get(i));

			messagesJList.setListData(messagePanels);
		}
		messagesScrollPane.validate();
		messagesScrollPane.repaint();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		switch (e.getActionCommand()) {
		case "send": {
			String sendToUser = roomJList.getSelectedValue();
			String content = messageField.getText();
			Main.socketController.sendMessageToUser(sendToUser, content);
			Main.mainScreen.addNewMessage(sendToUser, Main.socketController.userName, content);
			break;
		}
		}
	}

	public Room findRoom(String user) {
		Room room = null;
		for (int i = 0; i < rooms.size(); i++) {
			if (rooms.get(i).otherUser.equals(user)) {
				room = rooms.get(i);
				break;
			}
		}
		return room;
	}

	public int findRoomIndex(String user) {
		int room = -1;
		for (int i = 0; i < rooms.size(); i++) {
			if (rooms.get(i).otherUser.equals(user)) {
				room = i;
				break;
			}
		}
		return room;
	}
}
