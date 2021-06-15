package client;

public class MessageData {
	public String whoSend;
	public String type;
	public String fileName;
	public byte[] content;

	public MessageData(String whoSend, String type, byte[] content) {
		this.whoSend = whoSend;
		this.type = type;
		this.fileName = "";
		this.content = content;
	}

	public MessageData(String whoSend, String type, String fileName, byte[] content) {
		this.whoSend = whoSend;
		this.type = type;
		this.fileName = fileName;
		this.content = content;
	}
}