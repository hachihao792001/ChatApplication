package client;

import java.awt.Image;

import javax.swing.ImageIcon;

public class Main {

	public static ConnectServerScreen connectServerScreen;
	public static MainScreen mainScreen;
	public static SocketController socketController;

	public static void main(String arg[]) {
		connectServerScreen = new ConnectServerScreen();
	}

	public static ImageIcon getScaledImage(String path, int width, int height) {
		Image img = new ImageIcon(Main.class.getResource(path)).getImage();
		Image scaledImage = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
		return new ImageIcon(scaledImage);
	}
}
