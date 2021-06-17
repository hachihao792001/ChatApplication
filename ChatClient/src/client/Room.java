package client;

import java.util.ArrayList;
import java.util.List;

public class Room {
	public int id;
	public String name;
	public List<String> users;
	public List<MessageData> messages;

	public Room(int id, String name, List<String> users) {
		this.id = id;
		this.name = name;
		this.users = users;
		this.messages = new ArrayList<MessageData>();
	}

	public String getType() {
		return users.size() > 2 ? "group" : "private";
	}

	public static Room findRoom(List<Room> roomList, int id) {
		for (Room room : roomList)
			if (room.id == id)
				return room;
		return null;
	}

	public static Room findPrivateRoom(List<Room> roomList, String otherUser) {
		for (Room room : roomList) {
			if (room.users.size() == 2 && (room.users.get(0).equals(otherUser) || room.users.get(1).equals(otherUser)))
				return room;
		}
		return null;
	}
}
