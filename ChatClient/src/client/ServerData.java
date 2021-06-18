package client;

public class ServerData {
	public String nickName;
	public String realName;
	public String ip;
	public int port;
	public boolean isOpen;
	public int connectAccountCount;

	public ServerData(String name, String ip, int port) {
		this.nickName = name;
		this.realName = "";
		this.ip = ip;
		this.port = port;
		this.isOpen = false;
		this.connectAccountCount = 0;
	}

	public ServerData(String nickName, String realName, String ip, int port, boolean isOpen, int connectAccountCount) {
		this.nickName = nickName;
		this.realName = realName;
		this.ip = ip;
		this.port = port;
		this.isOpen = isOpen;
		this.connectAccountCount = connectAccountCount;
	}

}