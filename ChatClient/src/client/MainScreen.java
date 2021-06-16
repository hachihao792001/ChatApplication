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
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class MainScreen extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;

	String chattingToUser;

	JList<String> connectedServerInfoJList;
	JList<String> roomJList;

	JScrollPane messagesScrollPane;
	JTable messagesTable;
	List<Room> rooms;

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

		messagesTable = new JTable();
		MessagePanel m = new MessagePanel();
		messagesTable.setDefaultRenderer(MessageData.class, m);
		messagesTable.setDefaultEditor(MessageData.class, m);
		messagesTable.setShowGrid(false);
		messagesTable.setIntercellSpacing(new Dimension(0, 0));
		messagesTable.getTableHeader().setVisible(false);
		messagesTable.setRowHeight(35);

		messagesScrollPane = new JScrollPane(messagesTable);
		messagesScrollPane.setMinimumSize(new Dimension(50, 100));
		messagesScrollPane.getViewport().setBackground(Color.white);

		chatPanel.setBackground(Color.white);
		chatPanel.add(messagesScrollPane, gbc.setGrid(1, 1).setFill(GridBagConstraints.BOTH).setWeight(1, 1));
		chatPanel.add(enterMessagePanel, gbc.setGrid(1, 2).setWeight(1, 0));

		JSplitPane mainSplitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, chatPanel);
		mainContent.add(mainSplitpane, gbc.setGrid(1, 1).setWeight(1, 1));
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				mainSplitpane.setDividerLocation(mainSplitpane.getSize().width / 4);
			}
		});

		this.setTitle("Ứng dụng chat đăng nhập với tên " + Main.socketController.userName);
		this.setContentPane(mainContent);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	// ************** ROOMS LIST ***************
	public void updateServerData() {
		Main.socketController.connectedServer.connectAccountCount = rooms.size();

		connectedServerInfoJList
				.setListData(new String[] { "Port server: " + Main.socketController.connectedServer.port,
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

		Object[][] messagesMatrix = new Object[room.messages.size()][1];
		for (int i = 0; i < messagesMatrix.length; i++) {
			messagesMatrix[i][0] = room.messages.get(i);
			System.out.println("sadfsadf " + room.messages.get(i).content);

		}
		messagesTable.setModel(new DefaultTableModel(messagesMatrix, new String[] { "" }) {
			private static final long serialVersionUID = 1L;

			public Class<?> getColumnClass(int columnIndex) {
				return MessageData.class;
			}
		});

		messagesTable.validate();
		messagesTable.repaint();
		messagesScrollPane.validate();
		messagesScrollPane.repaint();

		JScrollBar vertical = messagesScrollPane.getVerticalScrollBar();
		vertical.setValue(vertical.getMaximum());
	}

	// ************** ROOM MESSAGES ***************
	public void addNewMessage(String roomUser, String type, String whoSend, String content) {
		if (roomUser == null)
			return;
		MessageData messageData = new MessageData(whoSend, type, content);
		int roomIndex = findRoomIndex(roomUser);
		rooms.get(roomIndex).messages.add(messageData);

		if (roomUser.equals(chattingToUser)) {
			messagesPanelSwitchRoom(rooms.get(roomIndex));
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		switch (e.getActionCommand()) {
		case "send": {
			String content = messageArea.getText();
			if (content.isEmpty())
				break;
			Main.socketController.sendMessageToUser(chattingToUser, content);
			addNewMessage(chattingToUser, "text", Main.socketController.userName, content);
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
			emojiDialog.setLocationRelativeTo(null);
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

				Main.socketController.sendFileToUser(chattingToUser, fileName, filePath);
				addNewMessage(chattingToUser, "file", Main.socketController.userName, fileName);
			}
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

	/*
	 * public class MessagesTableCellRenderer implements TableCellRenderer { public
	 * Component getTableCellRendererComponent(JTable table, Object value, boolean
	 * isSelected, boolean hasFocus, int row, int column) {
	 * System.out.println("Value " + value); return new MessagePanel((MessageData)
	 * value); } }
	 * 
	 * public class MessagesTableCellEditor extends AbstractCellEditor implements
	 * TableCellEditor {
	 * 
	 * private static final long serialVersionUID = 1L;
	 * 
	 * public Component getTableCellEditorComponent(JTable table, Object value,
	 * boolean isSelected, int row, int column) { System.out.println("Value " +
	 * value); return new MessagePanel((MessageData) value); }
	 * 
	 * public Object getCellEditorValue() { return null; } }
	 */
}
