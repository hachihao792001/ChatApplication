package client;

public class ServerData {
	public String nickName;
	public String realName;
	public int port;
	public boolean isOpen;
	public int connectAccountCount;

	public ServerData(String name, int port) {
		this.nickName = name;
		this.realName = "";
		this.port = port;
		this.isOpen = false;
		this.connectAccountCount = 0;
	}

	public ServerData(String nickName, String realName, int port, boolean isOpen, int connectAccountCount) {
		this.nickName = nickName;
		this.realName = realName;
		this.port = port;
		this.isOpen = isOpen;
		this.connectAccountCount = connectAccountCount;
	}

}