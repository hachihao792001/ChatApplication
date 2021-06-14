package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.net.Socket;

public class Client {
	public Account account;
	public int port;
	public Socket socket;
	public BufferedReader receiver;
	public BufferedWriter sender;

	public Client(Account account, int port, Socket socket, BufferedReader receiver, BufferedWriter sender) {
		this.account = account;
		this.port = port;
		this.socket = socket;
		this.receiver = receiver;
		this.sender = sender;
	}

	public Client() {
	}
}