package client;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class MessagePanel extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {

	private static final long serialVersionUID = 1L;
	public MessageData data;

	JPanel panel;
	JLabel whoSendLabel;
	JPanel contentPanel;

	JTextArea textContent;
	JPanel filePanel;
	JLabel fileIcon;
	JLabel fileNameLabel;

	public MessagePanel() {
		whoSendLabel = new JLabel();
		contentPanel = new JPanel();
		contentPanel.setLayout(new CardLayout());
		FlowLayout flowLayout = new FlowLayout();
		flowLayout.setAlignment(FlowLayout.LEADING);
		panel = new JPanel(new GridBagLayout());
		panel.add(whoSendLabel,
				new GBCBuilder(1, 1).setAnchor(GridBagConstraints.FIRST_LINE_START).setFill(GridBagConstraints.NONE));
		panel.add(contentPanel, new GBCBuilder(2, 1).setWeight(1, 0).setFill(GridBagConstraints.BOTH));
		panel.setBackground(null);

		textContent = new JTextArea();
		textContent.setEditable(false);

		filePanel = new JPanel(new GridBagLayout());
		filePanel.setBackground(null);
		fileIcon = new JLabel();
		fileNameLabel = new JLabel();
		filePanel.add(fileIcon, new GBCBuilder(1, 1).setWeight(0, 0).setAnchor(GridBagConstraints.LINE_START)
				.setFill(GridBagConstraints.NONE));
		filePanel.add(fileNameLabel, new GBCBuilder(2, 1).setWeight(1, 0).setAnchor(GridBagConstraints.LINE_START));
		filePanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		filePanel.addMouseListener(new MouseAdapter() {
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

		contentPanel.add(textContent, "text");
		contentPanel.add(filePanel, "file");
	}

	public void updateGUI() {

		whoSendLabel.setText((data.whoSend.equals(Main.socketController.userName) ? "Bạn" : data.whoSend) + ": ");
		if (data.type.equals("text")) {

			textContent.setText(data.content);
			int lineCount = data.content.split("\r\n|\r|\n").length;
			//textContent.setMinimumSize(new Dimension(100, 35 * lineCount));

			((CardLayout) contentPanel.getLayout()).show(contentPanel, "text");

		} else if (data.type.equals("file")) {

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
			fileNameLabel.setText("<HTML><U>" + data.content + "</U></HTML>");

			((CardLayout) contentPanel.getLayout()).show(contentPanel, "file");

		}

		panel.validate();
		panel.repaint();
	}

	@Override
	public Object getCellEditorValue() {
		return null;
	}

	@Override
	public Component getTableCellEditorComponent(JTable arg0, Object value, boolean arg2, int row, int column) {
		System.out.println("Row " + row);
		this.data = Main.mainScreen.findRoom(Main.mainScreen.chattingToUser).messages.get(row);
		updateGUI();
		return panel;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		System.out.println("Row " + row);
		this.data = Main.mainScreen.findRoom(Main.mainScreen.chattingToUser).messages.get(row);
		updateGUI();
		return panel;
	}
}
