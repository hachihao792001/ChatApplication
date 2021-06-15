package server;

import java.io.*;
import java.net.Socket;

public class ClientCommunicateThread extends Thread {

	Client thisClient;

	public ClientCommunicateThread(Socket clientSocket) {
		try {
			thisClient = new Client();
			thisClient.socket = clientSocket;
			OutputStream os = clientSocket.getOutputStream();
			thisClient.sender = new BufferedWriter(new OutputStreamWriter(os));
			InputStream is = clientSocket.getInputStream();
			thisClient.receiver = new BufferedReader(new InputStreamReader(is));
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

				case "send to user":
					String who = thisClient.receiver.readLine();
					String content = thisClient.receiver.readLine();

					for (Client client : Main.socketController.connectedClient) {
						if (client.userName.equals(who)) {
							client.sender.write("send from user");
							client.sender.newLine();
							client.sender.write(thisClient.userName);
							client.sender.newLine();
							client.sender.write(content);
							client.sender.newLine();
							client.sender.flush();
							break;
						}
					}
					break;
				}
			}

		} catch (IOException e) {
			System.out.println("Client " + thisClient.userName + " quit");
			if (thisClient.userName != null) {

				try {
					for (Client client : Main.socketController.connectedClient) {
						if (!client.userName.equals(thisClient.userName)) {

							client.sender.write("user quit");
							client.sender.newLine();
							client.sender.write(thisClient.userName);
							client.sender.newLine();
							client.sender.flush();

							break;
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
