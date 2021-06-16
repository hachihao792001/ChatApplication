package client;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class SocketController {

	String userName;
	ServerData connectedServer;
	Socket s;
	BufferedReader receiver;
	BufferedWriter sender;

	Thread receiveAndProcessThread;

	public SocketController(String name, ServerData connectedServer) {
		try {
			this.userName = name;
			this.connectedServer = connectedServer;
			s = new Socket("localhost", connectedServer.port);
			InputStream is = s.getInputStream();
			receiver = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
			OutputStream os = s.getOutputStream();
			sender = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));

		} catch (IOException e1) {
			Main.connectServerScreen.loginResultAction("closed");
		}
	}

	public void Login() {
		try {
			sender.write("new login");
			sender.newLine();
			sender.write(userName);
			sender.newLine();
			sender.flush();

			String loginResult = receiver.readLine();
			if (loginResult.equals("login success")) {
				Main.connectServerScreen.loginResultAction("success");

				int serverOnlineAccountCount = Integer.parseInt(receiver.readLine());
				for (int i = 0; i < serverOnlineAccountCount; i++) {
					Room newRoom = new Room();
					newRoom.otherUser = receiver.readLine();
					newRoom.messages = new ArrayList<MessageData>();
					Main.mainScreen.rooms.add(newRoom);
				}
				if (Main.mainScreen.chattingToUser == null && Main.mainScreen.rooms.size() > 0)
					Main.mainScreen.chattingToUser = Main.mainScreen.rooms.get(0).otherUser;
				Main.mainScreen.updateRoomJList();
				Main.mainScreen.updateServerData();

				receiveAndProcessThread = new Thread(() -> {
					try {
						while (true) {
							String header = receiver.readLine();
							System.out.println("Header " + header);

							switch (header) {
							case "new client": {
								String who = receiver.readLine();
								Room newRoom = new Room();
								newRoom.otherUser = who;
								newRoom.messages = new ArrayList<MessageData>();
								Main.mainScreen.rooms.add(newRoom);

								if (Main.mainScreen.chattingToUser == null)
									Main.mainScreen.chattingToUser = newRoom.otherUser;

								Main.mainScreen.updateRoomJList();
								Main.mainScreen.updateServerData();
								break;
							}
							case "user quit": {
								String whoQuit = receiver.readLine();
								Main.mainScreen.rooms.remove(Main.mainScreen.findRoom(whoQuit));
								Main.mainScreen.updateRoomJList();
								Main.mainScreen.updateServerData();
								break;
							}
							case "send from user": {
								String who = receiver.readLine();
								String content = "";
								char c;
								do {
									c = (char) receiver.read();
									if (c != '\0')
										content += c;
								} while (c != '\0');
								Main.mainScreen.addNewMessage(who, "text", who, content);
								break;
							}
							case "file from user": {
								String who = receiver.readLine();
								String fileName = receiver.readLine();
								System.out.println("Recevie file " + fileName + " from " + who);
								Main.mainScreen.addNewMessage(who, "file", who, fileName);
								break;
							}
							case "response download file": {
								int fileSize = Integer.parseInt(receiver.readLine());
								File file = new File(downloadToPath);
								byte[] buffer = new byte[512];
								InputStream in = s.getInputStream();
								OutputStream out = new FileOutputStream(file);

								int count;
								int receivedFileSize = 0;
								while ((count = in.read(buffer)) > 0) {
									out.write(buffer, 0, count);
									receivedFileSize += count;
									if (receivedFileSize >= fileSize)
										break;
								}

								out.close();
								break;
							}
							default:
								break;
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
				receiveAndProcessThread.start();
			} else
				Main.connectServerScreen.loginResultAction("existed");

		} catch (IOException e1) {

		}
	}

	public void sendMessageToUser(String who, String content) {
		if (who == null)
			return;
		try {
			sender.write("send to user");
			sender.newLine();
			sender.write(who);
			sender.newLine();
			sender.write(content);
			sender.write('\0');
			sender.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendFileToUser(String who, String content, String filePath) {
		if (who == null)
			return;
		try {
			System.out.println("Send file " + content + " to " + who);

			File file = new File(filePath);

			sender.write("file to user");
			sender.newLine();
			sender.write(who);
			sender.newLine();
			sender.write(content);
			sender.newLine();
			sender.write("" + file.length());
			sender.newLine();
			sender.flush();

			byte[] buffer = new byte[512];
			InputStream in = new FileInputStream(file);
			OutputStream out = s.getOutputStream();

			int count;
			while ((count = in.read(buffer)) > 0) {
				out.write(buffer, 0, count);
			}

			in.close();
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String downloadToPath;

	public void downloadFile(String fileName, String downloadToPath) {
		this.downloadToPath = downloadToPath;
		try {
			sender.write("download file");
			sender.newLine();
			sender.write(fileName);
			sender.newLine();
			sender.flush();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public static boolean serverOnline(int port) {
		try {
			Socket s = new Socket();
			s.connect(new InetSocketAddress("localhost", port), 300);
			s.close();
			return true;
		} catch (IOException ex) {
			return false;
		}
	}

	public static String serverName(int port) {

		try {
			Socket s = new Socket("localhost", port);
			InputStream is = s.getInputStream();
			BufferedReader receiver = new BufferedReader(new InputStreamReader(is));
			OutputStream os = s.getOutputStream();
			BufferedWriter sender = new BufferedWriter(new OutputStreamWriter(os));

			sender.write("get name");
			sender.newLine();
			sender.flush();

			String name = receiver.readLine();

			s.close();
			return name;
		} catch (IOException ex) {
			return "";
		}
	}

	public static int serverConnectedAccountCount(int port) {
		try {
			Socket s = new Socket("localhost", port);
			InputStream is = s.getInputStream();
			BufferedReader receiver = new BufferedReader(new InputStreamReader(is));
			OutputStream os = s.getOutputStream();
			BufferedWriter sender = new BufferedWriter(new OutputStreamWriter(os));

			sender.write("get connected count");
			sender.newLine();
			sender.flush();

			int count = Integer.parseInt(receiver.readLine());

			s.close();
			return count;
		} catch (IOException ex) {
			return 0;
		}
	}
}
