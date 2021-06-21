package server;

import java.util.List;

public class Room {
	public static int currentRoomID = 1;

	public int id;
	public String name;
	public List<String> users;

	public Room(String name, List<String> users) {
		this.id = currentRoomID++;
		this.name = name;
		this.users = users;
	}

	public static Room findRoom(List<Room> roomList, int id) {
		for (Room room : roomList)
			if (room.id == id)
				return room;
		return null;
	}
}
