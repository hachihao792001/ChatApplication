package client;

public class MessageData {
	public String whoSend;
	public String type;
	public String content;

	public MessageData(String whoSend, String type, String content) {
		this.whoSend = whoSend;
		this.type = type;
		this.content = content;
	}
}