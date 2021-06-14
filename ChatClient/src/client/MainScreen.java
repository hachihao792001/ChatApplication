package client;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class MainScreen extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;

	List<Room> rooms;

	JList<String> messagesJList;
	JList<String> roomJList;
	JTextField messageField;
	String chattingToUser;

	public MainScreen() {
		GBCBuilder gbc = new GBCBuilder(1, 1);

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
				updateMessageJList(room);
			}
		});
		JScrollPane roomListScrollPane = new JScrollPane(roomJList);

		JPanel chatPanel = new JPanel(new GridBagLayout());
		messageField = new JTextField();
		JButton sendButton = new JButton("Gửi");
		sendButton.addActionListener(this);
		sendButton.setActionCommand("send");

		messagesJList = new JList<String>();
		JScrollPane messagesScrollPane = new JScrollPane(messagesJList);
		messagesScrollPane.setMinimumSize(new Dimension(50, 100));
		chatPanel.add(messagesScrollPane, gbc.setSpan(2, 1).setFill(GridBagConstraints.BOTH).setWeight(1, 1));
		chatPanel.add(messageField, gbc.setGrid(1, 2).setSpan(1, 1).setWeight(1, 0));
		chatPanel.add(sendButton, gbc.setGrid(2, 2).setWeight(0, 0));

		JSplitPane mainSplitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, roomListScrollPane, chatPanel);

		this.setTitle("Ứng dụng chat đăng nhập với " + Main.loginScreen.loggedInUser);
		this.setContentPane(mainSplitpane);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	public void addToMessageJList(String roomUser, String whoSend, String newMessage) {
		Room room = findRoom(roomUser);
		room.messages.add(new Room.Message(whoSend, newMessage));
		
		if(roomUser.equals(chattingToUser)) {
			updateMessageJList(room);
		}
	}

	public void updateMessageJList(Room room) {
		String[] messages = new String[room.messages.size()];
		for (int i = 0; i < room.messages.size(); i++)
			messages[i] = room.messages.get(i).whoSend + ": " + room.messages.get(i).content;
		messagesJList.setListData(messages);
	}

	public void updateRoomJList() {
		String[] onlineAccounts = new String[rooms.size()];
		for (int i = 0; i < rooms.size(); i++)
			onlineAccounts[i] = rooms.get(i).otherUser;
		roomJList.setListData(onlineAccounts);

		if (roomJList.getModel().getSize() > 0 && roomJList.getSelectedIndex() == -1)
			roomJList.getSelectionModel().setSelectionInterval(0, 0);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		switch (e.getActionCommand()) {
		case "send": {
			String sendToUser = roomJList.getSelectedValue();
			String content = messageField.getText();
			Main.socketController.sendMessageToUser(sendToUser, content);
			Main.mainScreen.addToMessageJList(sendToUser, Main.loginScreen.loggedInUser, content);
			break;
		}
		}
	}
	
	public Room findRoom(String user) {
		Room room = null;
		for (int i = 0; i < rooms.size(); i++) {
			System.out.println("Room " + rooms.get(i).otherUser);
			if (rooms.get(i).otherUser.equals(user)) {
				room = rooms.get(i);
				break;
			}
		}
		return room;
	}
}
