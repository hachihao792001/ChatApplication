package server;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileManager {

	public final static String ACCOUNT_FILE = "accounts.txt";

	public static boolean exist(String fileName) {
		return (new File(fileName)).isFile();
	}

	/*
	 * public static List<Account> getAccountList() {
	 * 
	 * BufferedReader reader; try { reader = new BufferedReader(new FileReader(new
	 * File(ACCOUNT_FILE))); } catch (FileNotFoundException e) {
	 * System.out.println("File " + ACCOUNT_FILE + " does not exist"); return null;
	 * }
	 * 
	 * List<Account> accountList = new ArrayList<Account>(); try { String line =
	 * reader.readLine(); try {
	 * 
	 * while (line != null && !line.isEmpty()) { String[] accountData =
	 * line.split(","); accountList.add(new Account(accountData[0],
	 * accountData[1])); line = reader.readLine(); } } catch (Exception num) {
	 * System.out.println("File's format is incorrect"); reader.close(); return
	 * null; } reader.close(); } catch (IOException io) {
	 * System.out.println("Read error"); } return accountList; }
	 * 
	 * public static Account findAccount(String userName, String pass) {
	 * List<Account> accountList = getAccountList();
	 * 
	 * for (Account account : accountList) { if
	 * (userName.equals(account.getUserName())) { if
	 * (pass.equals(account.getPassword())) { return new Account(userName, pass); }
	 * else { return null; } } } return null; }
	 */
}
