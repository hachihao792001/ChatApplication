package server;

import java.io.*;
import java.net.Socket;
import java.util.List;

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

			String clientUsername = thisClient.receiver.readLine();
			String clientPass = thisClient.receiver.readLine();
			Account foundAccount = FileManager.findAccount(clientUsername, clientPass);
			if (foundAccount != null) {
				Account clientAccount = new Account(clientUsername, clientPass);
				thisClient.account = clientAccount;
				Main.socketController.connectedClient.add(thisClient);
				Main.mainScreen.updateClientTable();

				thisClient.sender.write("login success");
				thisClient.sender.newLine();
				thisClient.sender.flush();

				thisClient.sender.write("" + (Main.socketController.connectedClient.size() - 1));
				thisClient.sender.newLine();
				thisClient.sender.flush();
				for (Client client : Main.socketController.connectedClient) {
					if (client.account.getUserName().equals(thisClient.account.getUserName()))
						continue;
					thisClient.sender.write(client.account.getUserName());
					thisClient.sender.newLine();
					thisClient.sender.flush();
				}

				for (Client client : Main.socketController.connectedClient) {
					if (client.account.getUserName().equals(thisClient.account.getUserName()))
						continue;
					System.out.println("Sending new client " + thisClient.account.getUserName() + " to "
							+ client.account.getUserName());
					client.sender.write("new client");
					client.sender.newLine();
					client.sender.write(thisClient.account.getUserName());
					client.sender.newLine();
					client.sender.flush();
				}

				while (true) {
					String received = thisClient.receiver.readLine();
					switch (received) {
					case "send to user":
						String who = thisClient.receiver.readLine();
						String content = thisClient.receiver.readLine();

						for (Client client : Main.socketController.connectedClient) {
							if (client.account.getUserName().equals(who)) {
								client.sender.write("send from user");
								client.sender.newLine();
								client.sender.write(thisClient.account.getUserName());
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

			} else {
				thisClient.sender.write("login failed");
				thisClient.sender.newLine();
				thisClient.sender.flush();
			}

		} catch (IOException e) {
			System.out.println("Client " + thisClient.account.getUserName() + " quit");
			try {
				for (Client client : Main.socketController.connectedClient) {
					if (!client.account.getUserName().equals(thisClient.account.getUserName())) {

						client.sender.write("user quit");
						client.sender.newLine();
						client.sender.write(thisClient.account.getUserName());
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
