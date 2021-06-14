package client;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class SocketController {

	int portServer;
	Socket s;
	BufferedReader receiver;
	BufferedWriter sender;

	public SocketController(String name, String pass) {
		try {
			s = new Socket("localhost", 2190);
			InputStream is = s.getInputStream();
			receiver = new BufferedReader(new InputStreamReader(is));
			OutputStream os = s.getOutputStream();
			sender = new BufferedWriter(new OutputStreamWriter(os));

			sender.write(name);
			sender.newLine();
			sender.write(pass);
			sender.newLine();
			sender.flush();

			String loginResult = receiver.readLine();
			if (loginResult.equals("login success")) {
				Main.loginScreen.loginResultAction(true);

				int serverOnlineAccountCount = Integer.parseInt(receiver.readLine());
				for (int i = 0; i < serverOnlineAccountCount; i++) {
					Room newRoom = new Room();
					newRoom.otherUser = receiver.readLine();
					newRoom.messages = new ArrayList<Room.Message>();
					Main.mainScreen.rooms.add(newRoom);
				}
				if (Main.mainScreen.chattingToUser == null && Main.mainScreen.rooms.size() > 0)
					Main.mainScreen.chattingToUser = Main.mainScreen.rooms.get(0).otherUser;
				Main.mainScreen.updateRoomJList();

				new Thread(() -> {
					try {
						while (true) {
							String received = receiver.readLine();
							switch (received) {
							case "new client": {
								String who = receiver.readLine();
								Room newRoom = new Room();
								newRoom.otherUser = who;
								newRoom.messages = new ArrayList<Room.Message>();
								Main.mainScreen.rooms.add(newRoom);

								if (Main.mainScreen.chattingToUser == null)
									Main.mainScreen.chattingToUser = newRoom.otherUser;

								Main.mainScreen.updateRoomJList();
								break;
							}
							case "user quit": {
								String whoQuit = receiver.readLine();
								Main.mainScreen.rooms.remove(Main.mainScreen.findRoom(whoQuit));
								Main.mainScreen.updateRoomJList();
								break;
							}
							case "send from user": {
								String who = receiver.readLine();
								String content = receiver.readLine();
								Main.mainScreen.addToMessageJList(who, who, content);
								break;
							}
							default:
								break;
							}
						}
					} catch (IOException e) {

					}
				}).start();
			} else
				Main.loginScreen.loginResultAction(false);

		} catch (IOException e1) {
			System.out.println("Server closed");
		}
	}

	public void sendMessageToUser(String who, String content) {
		try {
			sender.write("send to user");
			sender.newLine();
			sender.write(who);
			sender.newLine();
			sender.write(content);
			sender.newLine();
			sender.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
