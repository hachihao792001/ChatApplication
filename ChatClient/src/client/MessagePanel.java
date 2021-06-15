package client;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.charset.StandardCharsets;

import javax.swing.*;

public class MessagePanel extends JPanel {

	private static final long serialVersionUID = 1L;
	public MessageData data;

	public MessagePanel(MessageData data) {
		this.data = data;
		initComponents();
	}

	public MessagePanel(String whoSend, String type, byte[] content) {
		this.data = new MessageData(whoSend, type, content);
		initComponents();
	}

	public MessagePanel(String whoSend, String type, String fileName, byte[] content) {
		this.data = new MessageData(whoSend, type, fileName, content);
		initComponents();
	}

	public void initComponents() {
		this.setLayout(new GridBagLayout());
		this.setBackground(null);
		JLabel whoSendLabel = new JLabel(
				(data.whoSend.equals(Main.socketController.userName) ? "Bạn" : data.whoSend) + ": ");
		JPanel contentPanel = new JPanel();

		if (data.type.equals("text")) {

			JLabel textContent = new JLabel(new String(data.content, StandardCharsets.UTF_8));
			contentPanel.add(textContent);

		} else if (data.type.equals("file")) {

			JLabel fileNameLabel = new JLabel("<HTML><U>" + data.fileName + "</U></HTML>");
			contentPanel.add(fileNameLabel);
			contentPanel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					Main.socketController.sendFileToUser(Main.mainScreen.chattingToUser, data.fileName, data.content);
				}
			});

		}

		FlowLayout flowLayout = new FlowLayout();
		flowLayout.setAlignment(FlowLayout.LEADING);
		JPanel panel = new JPanel(flowLayout);
		panel.add(whoSendLabel);
		panel.add(contentPanel);
		panel.setBackground(null);
		this.add(panel, new GBCBuilder(1, 1).setFill(GridBagConstraints.HORIZONTAL).setWeight(1, 0)
				.setAnchor(GridBagConstraints.LINE_START));
	}
}
