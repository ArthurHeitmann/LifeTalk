package clientApp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.security.auth.login.LoginException;

import JsonRW.ClientStartupOperations;

public class ClientConnection {
	private static Socket socket;
	private static BufferedReader in;
	private static PrintWriter out;
	private static String loginID;

	public static boolean connectToServer(String srvAdress, int port) throws UnknownHostException, ConnectException {
		try {
			socket = new Socket(srvAdress, port);
			out = new PrintWriter(socket.getOutputStream());
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			return true;
		} catch (IOException e) {
			//e.printStackTrace();
			return false;
		}

	}

	public static void autoLogin() {
		if (ClientStartupOperations.isAutoLoginEnabled()) {
			try {
				loginToServer(ClientStartupOperations.getAutoLoginUsername(), ClientStartupOperations.getloginID(), "AUTOLOGIN", true);
			} catch (LoginException e) {
				e.printStackTrace();
			}
		}
	}

	public static boolean loginToServer(String uName, String pw, String action, boolean stayLoggedin) throws LoginException {
		write(action);
		write(Boolean.toString(stayLoggedin));
		write(uName);
		write(pw);
		String line;
		try {
			line = in.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		if (line == null) {
			System.out.println("EXIT");
			throw new LoginException();
		} else if (line.equals("SUCCESS")) {
			System.out.println("logged in");
			return true;
		} else if (line.equals("SUCCESS LOGIN")) {
			try {
				line = in.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (line == null) {
				throw new LoginException();
			}
			loginID = line;
			return true;

		} else if (line.equals("ACCESS DENIED")) {
			throw new LoginException();
		}
		return false;
	}

	private static void write(String msg) {
		out.println(msg);
		out.flush();
	}

	public static String getLoginID() {
		return loginID;
	}

}
