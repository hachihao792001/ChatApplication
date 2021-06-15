package server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class SocketController {

	public String serverName;
	public int serverPort;
	ServerSocket s;
	public List<Client> connectedClient;

	public void OpenSocket(int port) {
		try {
			s = new ServerSocket(port);
			connectedClient = new ArrayList<Client>();

			new Thread(() -> {
				try {
					do {
						System.out.println("Waiting for client");

						Socket clientSocket = s.accept();

						ClientCommunicateThread clientCommunicator = new ClientCommunicateThread(clientSocket);
						clientCommunicator.start();

					} while (s != null && !s.isClosed());
				} catch (IOException e) {
					System.out.println("Server or client socket closed");
				}
			}).start();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void CloseSocket() {
		try {
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
