package client;

import java.util.ArrayList;
import java.util.List;

public class Room {
	public int id;
	public String name;
	public String type;
	public List<String> users;
	public List<MessageData> messages;

	public Room(int id, String name, String type, List<String> users) {
		this.id = id;
		this.name = name;
		this.type = type;
		this.users = users;
		this.messages = new ArrayList<MessageData>();
	}

	public static Room findRoom(List<Room> roomList, int id) {
		for (Room room : roomList)
			if (room.id == id)
				return room;
		return null;
	}

	public static Room findPrivateRoom(List<Room> roomList, String otherUser) {
		for (Room room : roomList) {
			if (room.type.equals("private") && room.name.equals(otherUser))
				return room;
		}
		return null;
	}

	public static Room findGroup(List<Room> roomList, String groupName) {
		for (Room room : roomList) {
			if (room.type.equals("group") && room.name.equals(groupName))
				return room;
		}
		return null;
	}
}
