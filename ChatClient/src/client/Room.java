package client;

import java.util.List;

public class Room {

	public static class Message {
		public String whoSend;
		public String content;

		public Message(String whoSend, String content) {
			this.whoSend = whoSend;
			this.content = content;
		}

	}

	public String otherUser;
	public List<Message> messages;
}
