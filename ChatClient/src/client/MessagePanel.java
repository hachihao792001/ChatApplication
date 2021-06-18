package client;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

public class MessagePanel extends JPanel {

	private static final long serialVersionUID = 1L;
	public MessageData data;

	public MessagePanel(MessageData data) {
		this.data = data;
		Dimension thisMaxSize = this.getMaximumSize();

		JLabel whoSendLabel = new JLabel(
				(data.whoSend.equals(Main.socketController.userName) ? "Bạn" : data.whoSend) + ": ");
		whoSendLabel.setFont(new Font("Dialog", Font.BOLD, 15));

		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.X_AXIS));
		contentPanel.setBackground(Color.white);

		if (data.type.equals("notify")) {

			JTextArea textContent = new JTextArea(data.content);
			textContent.setFont(new Font("Dialog", Font.ITALIC, 15));
			textContent.setForeground(Color.red);
			textContent.setEditable(false);

			contentPanel.add(textContent);
			this.setMaximumSize(new Dimension(thisMaxSize.width, 25));

		} else if (data.type.equals("text")) {

			JTextArea textContent = new JTextArea(data.content);
			textContent.setFont(new Font("Dialog", Font.PLAIN, 15));
			textContent.setEditable(false);
			// textContent.setBackground(new Color(52, 149, 235));
			// textContent.setAlignmentY(SwingConstants.NORTH);

			contentPanel.add(textContent);

			int lineCount = data.content.split("\r\n|\r|\n").length;
			if (lineCount > 1) {
				this.setMaximumSize(new Dimension(thisMaxSize.width, 19 * lineCount));
			} else {
				this.setMaximumSize(new Dimension(thisMaxSize.width, 25));
			}

		} else if (data.type.equals("file")) {

			contentPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			contentPanel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					String fileName = data.content;
					String extension = fileName.split("\\.")[1];

					JFileChooser jfc = new JFileChooser();
					jfc.setDialogTitle("Chọn đường dẫn download");
					jfc.setFileFilter(new FileNameExtensionFilter(extension.toUpperCase() + " files", extension));
					jfc.setSelectedFile(new File(fileName));
					int result = jfc.showSaveDialog(contentPanel);
					jfc.setVisible(true);

					if (result == JFileChooser.APPROVE_OPTION) {
						String filePath = jfc.getSelectedFile().getAbsolutePath();
						if (!filePath.endsWith("." + extension))
							filePath += "." + extension;

						Main.socketController.downloadFile(fileName, filePath);
					}
				}
			});

			JLabel fileIcon = new JLabel();
			try {
				String extension = data.content.split("\\.")[1];
				Random r = new Random();
				File tempFile = new File("temp" + r.nextInt() + r.nextInt() + r.nextInt() + "." + extension);
				tempFile.createNewFile();
				fileIcon.setIcon(FileSystemView.getFileSystemView().getSystemIcon(tempFile));
				tempFile.delete();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			JLabel fileNameLabel = new JLabel("<HTML><U>" + data.content + "</U></HTML>");
			fileNameLabel.setFont(new Font("Dialog", Font.PLAIN, 15));

			contentPanel.add(fileIcon, new GBCBuilder(1, 1).setWeight(0, 0).setAnchor(GridBagConstraints.LINE_START)
					.setFill(GridBagConstraints.NONE).setInsets(0, 0, 0, 5));
			contentPanel.add(fileNameLabel,
					new GBCBuilder(2, 1).setWeight(1, 0).setAnchor(GridBagConstraints.LINE_START));

			this.setMaximumSize(new Dimension(thisMaxSize.width, 30));
		}

		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		whoSendLabel.setAlignmentY(Component.TOP_ALIGNMENT);
		this.add(whoSendLabel);
		contentPanel.setAlignmentY(Component.TOP_ALIGNMENT);
		this.add(contentPanel);
		this.setBackground(null);

	}

}
