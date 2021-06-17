package server;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ClientCommunicateThread extends Thread {

	Client thisClient;

	public ClientCommunicateThread(Socket clientSocket) {
		try {
			thisClient = new Client();
			thisClient.socket = clientSocket;
			OutputStream os = clientSocket.getOutputStream();
			thisClient.sender = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
			InputStream is = clientSocket.getInputStream();
			thisClient.receiver = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
			thisClient.port = clientSocket.getPort();
		} catch (IOException e) {

		}
	}

	@Override
	public void run() {
		try {
			while (true) {
				String header = thisClient.receiver.readLine();
				if (header == null)
					throw new IOException();

				System.out.println("Header: " + header);
				switch (header) {

				case "new login": {

					String clientUsername = thisClient.receiver.readLine();

					boolean userNameExisted = false;
					for (Client connectedClient : Main.socketController.connectedClient) {
						if (connectedClient.userName.equals(clientUsername)) {
							userNameExisted = true;
							break;
						}
					}

					if (!userNameExisted) {
						thisClient.userName = clientUsername;
						Main.socketController.connectedClient.add(thisClient);
						Main.mainScreen.updateClientTable();

						thisClient.sender.write("login success");
						thisClient.sender.newLine();
						thisClient.sender.flush();

						thisClient.sender.write("" + (Main.socketController.connectedClient.size() - 1));
						thisClient.sender.newLine();
						thisClient.sender.flush();
						for (Client client : Main.socketController.connectedClient) {
							if (client.userName.equals(thisClient.userName))
								continue;
							thisClient.sender.write(client.userName);
							thisClient.sender.newLine();
							thisClient.sender.flush();
						}

						for (Client client : Main.socketController.connectedClient) {
							if (client.userName.equals(thisClient.userName))
								continue;
							client.sender.write("new user online");
							client.sender.newLine();
							client.sender.write(thisClient.userName);
							client.sender.newLine();
							client.sender.flush();
						}
					} else {
						thisClient.sender.write("login failed");
						thisClient.sender.newLine();
						thisClient.sender.flush();
					}
					break;
				}

				case "get name": {
					thisClient.sender.write(Main.socketController.serverName);
					thisClient.sender.newLine();
					thisClient.sender.flush();
					break;
				}

				case "get connected count": {
					thisClient.sender.write("" + Main.socketController.connectedClient.size());
					thisClient.sender.newLine();
					thisClient.sender.flush();
					break;
				}

				case "request create room": {
					String roomName = thisClient.receiver.readLine();
					int userCount = Integer.parseInt(thisClient.receiver.readLine());
					List<String> users = new ArrayList<String>();
					for (int i = 0; i < userCount; i++)
						users.add(thisClient.receiver.readLine());

					Room newRoom = new Room(roomName, users);
					Main.socketController.allRooms.add(newRoom);

					for (int i = 0; i < userCount; i++) {
						BufferedWriter currentClientSender = Client.findClient(Main.socketController.connectedClient,
								users.get(i)).sender;
						currentClientSender.write("new room");
						currentClientSender.newLine();
						currentClientSender.write("" + newRoom.id);
						currentClientSender.newLine();
						currentClientSender.write(thisClient.userName);
						currentClientSender.newLine();
						if (users.size() == 2) {
							// private chat thì tên room của mỗi người sẽ là tên của người kia
							currentClientSender.write(users.get(1 - i)); // user 0 thì gửi 1, user 1 thì gửi 0
							currentClientSender.newLine();
						} else {
							currentClientSender.write(roomName);
							currentClientSender.newLine();
						}
						currentClientSender.write("" + users.size());
						currentClientSender.newLine();
						for (String userr : users) {
							currentClientSender.write(userr);
							currentClientSender.newLine();
						}
						currentClientSender.flush();
					}
					break;
				}

				case "text to room": {
					int roomID = Integer.parseInt(thisClient.receiver.readLine());
					String content = "";
					char c;
					do {
						c = (char) thisClient.receiver.read();
						if (c != '\0')
							content += c;
					} while (c != '\0');

					Room room = Room.findRoom(Main.socketController.allRooms, roomID);
					for (String user : room.users) {
						System.out.println("Send text from " + thisClient.userName + " to " + user);
						Client currentClient = Client.findClient(Main.socketController.connectedClient, user);
						if (currentClient != null) {
							currentClient.sender.write("text from user to room");
							currentClient.sender.newLine();
							currentClient.sender.write(thisClient.userName);
							currentClient.sender.newLine();
							currentClient.sender.write("" + roomID);
							currentClient.sender.newLine();
							currentClient.sender.write(content);
							currentClient.sender.write('\0');
							currentClient.sender.flush();
						}
					}
					break;
				}

				case "file to room": {
					int roomID = Integer.parseInt(thisClient.receiver.readLine());
					String fileName = thisClient.receiver.readLine();
					int fileSize = Integer.parseInt(thisClient.receiver.readLine());

					Room room = Room.findRoom(Main.socketController.allRooms, roomID);
					for (String user : room.users) {
						Client currentClient = Client.findClient(Main.socketController.connectedClient, user);
						if (currentClient != null) {
							currentClient.sender.write("file from user to room");
							currentClient.sender.newLine();
							currentClient.sender.write(thisClient.userName);
							currentClient.sender.newLine();
							currentClient.sender.write("" + roomID);
							currentClient.sender.newLine();
							currentClient.sender.write(fileName);
							currentClient.sender.newLine();
							currentClient.sender.flush();
						}
					}

					File file = new File(fileName);
					byte[] buffer = new byte[512];
					InputStream in = thisClient.socket.getInputStream();
					OutputStream out = new FileOutputStream(file);

					int receivedSize = 0;
					int count;
					while ((count = in.read(buffer)) > 0) {
						out.write(buffer, 0, count);
						receivedSize += count;
						if (receivedSize >= fileSize)
							break;
					}

					out.close();

					break;
				}

				case "request download file": {
					try {
						String fileName = thisClient.receiver.readLine();
						File file = new File(fileName);

						thisClient.sender.write("response download file");
						thisClient.sender.newLine();
						thisClient.sender.write("" + file.length());
						thisClient.sender.newLine();
						thisClient.sender.flush();

						byte[] buffer = new byte[512];
						InputStream in = new FileInputStream(file);
						OutputStream out = thisClient.socket.getOutputStream();

						int count;
						while ((count = in.read(buffer)) > 0) {
							out.write(buffer, 0, count);
						}

						in.close();
						out.flush();
					} catch (IOException ex) {
						ex.printStackTrace();
					}
					break;
				}
				}
			}

		} catch (IOException e) {
			if (thisClient.userName != null) {

				try {
					for (Client client : Main.socketController.connectedClient) {
						if (!client.userName.equals(thisClient.userName)) {
							client.sender.write("user quit");
							client.sender.newLine();
							client.sender.write(thisClient.userName);
							client.sender.newLine();
							client.sender.flush();
						}
					}

					thisClient.socket.close();

				} catch (IOException e1) {
					e1.printStackTrace();
				}
				Main.socketController.connectedClient.remove(thisClient);
				Main.mainScreen.updateClientTable();
			}
		}
	}
}
