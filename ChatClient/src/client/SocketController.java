package client;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SocketController {

	String userName;
	ServerData connectedServer;
	Socket s;
	BufferedReader receiver;
	BufferedWriter sender;

	Thread receiveAndProcessThread;

	public List<String> onlineUsers;
	public List<Room> allRooms;

	public SocketController(String name, ServerData connectedServer) {
		onlineUsers = new ArrayList<String>();
		allRooms = new ArrayList<Room>();
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
				for (int i = 0; i < serverOnlineAccountCount; i++)
					onlineUsers.add(receiver.readLine());

				Main.mainScreen.updateServerData();
				Main.mainScreen.updateOnlineUserJList();

				receiveAndProcessThread = new Thread(() -> {
					try {
						while (true) {
							String header = receiver.readLine();
							System.out.println("Header " + header);

							switch (header) {
							case "new user online": {
								String who = receiver.readLine();
								onlineUsers.add(who);
								Main.mainScreen.updateServerData();
								Main.mainScreen.updateOnlineUserJList();
								break;
							}
							case "user quit": {
								String whoQuit = receiver.readLine();
								onlineUsers.remove(whoQuit);
								Main.mainScreen.updateServerData();
								Main.mainScreen.updateOnlineUserJList();
								break;
							}
							case "new room": {
								int roomID = Integer.parseInt(receiver.readLine());
								String whoCreate = receiver.readLine();
								String name = receiver.readLine();
								int roomUserCount = Integer.parseInt(receiver.readLine());
								List<String> users = new ArrayList<String>();
								for (int i = 0; i < roomUserCount; i++)
									users.add(receiver.readLine());

								Room newRoom = new Room(roomID, name, users);
								Main.socketController.allRooms.add(newRoom);
								Main.mainScreen.newRoomTab(newRoom);
								break;
							}
							case "text from user to room": {
								String user = receiver.readLine();
								int roomID = Integer.parseInt(receiver.readLine());
								String content = "";
								char c;
								do {
									c = (char) receiver.read();
									if (c != '\0')
										content += c;
								} while (c != '\0');
								Main.mainScreen.addNewMessage(roomID, "text", user, content);
								break;
							}
							case "file from user to room": {
								String user = receiver.readLine();
								int roomID = Integer.parseInt(receiver.readLine());
								String fileName = receiver.readLine();
								System.out.println("Recevie file " + fileName + " from " + user + " to room " + roomID);
								Main.mainScreen.addNewMessage(roomID, "file", user, fileName);
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

	public void sendTextToRoom(int roomID, String content) {
		try {
			sender.write("text to room");
			sender.newLine();
			sender.write("" + roomID);
			sender.newLine();
			sender.write(content);
			sender.write('\0');
			sender.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendFileToRoom(int roomID, String fileName, String filePath) {
		try {
			System.out.println("Send file " + fileName + " to room " + roomID);

			File file = new File(filePath);

			sender.write("file to room");
			sender.newLine();
			sender.write("" + roomID);
			sender.newLine();
			sender.write(fileName);
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
			sender.write("request download file");
			sender.newLine();
			sender.write(fileName);
			sender.newLine();
			sender.flush();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void createPrivateRoom(String otherUser) {
		try {
			sender.write("request create room");
			sender.newLine();
			sender.write(otherUser); // room name
			sender.newLine();
			sender.write("2");
			sender.newLine();
			sender.write(userName);
			sender.newLine();
			sender.write(otherUser);
			sender.newLine();
			sender.flush();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void createGroup(String groupName, List<String> otherUsers) {
		try {
			sender.write("request create room");
			sender.newLine();
			sender.write(groupName);
			sender.newLine();
			sender.write("" + (otherUsers.size() + 1));
			sender.newLine();
			sender.write(userName);
			sender.newLine();
			for (String user : otherUsers) {
				sender.write(user);
				sender.newLine();
			}
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
