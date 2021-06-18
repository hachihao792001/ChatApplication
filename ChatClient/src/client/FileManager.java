package client;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileManager {

	public final static String SERVER_FILE = "config.txt";

	public static boolean exist(String fileName) {
		return (new File(fileName)).isFile();
	}

	public static List<ServerData> getServerList() {

		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(new File(SERVER_FILE)));
		} catch (FileNotFoundException e) {
			System.out.println("File " + SERVER_FILE + " does not exist");
			return null;
		}

		List<ServerData> serverList = new ArrayList<ServerData>();
		try {
			String line = reader.readLine();
			try {

				while (line != null && !line.isEmpty()) {
					String[] serverData = line.split(",");
					serverList.add(new ServerData(serverData[0], serverData[1], Integer.parseInt(serverData[2])));
					line = reader.readLine();
				}
			} catch (Exception num) {
				System.out.println("File's format is incorrect");
				reader.close();
				return null;
			}
			reader.close();
		} catch (IOException io) {
			System.out.println("Read error");
		}
		return serverList;
	}

	public static void setServerList(List<ServerData> serverList) {

		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(new File(SERVER_FILE)));

			for (ServerData serverData : serverList)
				writer.write(serverData.nickName + "," + serverData.ip + "," + serverData.port + "\n");

			writer.close();
		} catch (IOException io) {
			System.out.println("Write error");
		}
	}

	public static Object[][] getServerObjectMatrix(List<ServerData> serverList) {
		if (serverList == null)
			return new Object[][] {};
		Object[][] serverObjMatrix = new Object[serverList.size()][6];
		for (int i = 0; i < serverList.size(); i++) {
			serverObjMatrix[i][0] = serverList.get(i).nickName;
			serverObjMatrix[i][1] = serverList.get(i).realName;
			serverObjMatrix[i][2] = serverList.get(i).ip;
			serverObjMatrix[i][3] = serverList.get(i).port;
			serverObjMatrix[i][4] = serverList.get(i).isOpen ? "Hoạt động" : "Không hoạt động";
			serverObjMatrix[i][5] = serverList.get(i).connectAccountCount;
		}
		return serverObjMatrix;
	}
}
