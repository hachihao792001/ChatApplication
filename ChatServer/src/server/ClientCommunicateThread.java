package server;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

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

				System.out.println("Header " + header);
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
							client.sender.write("new client");
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

				case "send to user": {
					String who = thisClient.receiver.readLine();
					String content = "";
					char c;
					do {
						c = (char) thisClient.receiver.read();
						if (c != '\0')
							content += c;
					} while (c != '\0');

					for (Client client : Main.socketController.connectedClient) {
						if (client.userName.equals(who)) {
							client.sender.write("send from user");
							client.sender.newLine();
							client.sender.write(thisClient.userName);
							client.sender.newLine();
							client.sender.write(content);
							client.sender.write('\0');
							client.sender.flush();
							break;
						}
					}
					break;
				}

				case "file to user": {
					String who = thisClient.receiver.readLine();
					String fileName = thisClient.receiver.readLine();
					int fileSize = Integer.parseInt(thisClient.receiver.readLine());

					for (Client client : Main.socketController.connectedClient) {
						if (client.userName.equals(who)) {
							client.sender.write("file from user");
							client.sender.newLine();
							client.sender.write(thisClient.userName);
							client.sender.newLine();
							client.sender.write(fileName);
							client.sender.newLine();
							client.sender.flush();

							break;
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

				case "download file": {
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
