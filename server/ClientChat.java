package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import jsonRW.ServerOperations;

public class ClientChat implements Runnable {
	private Socket socket;
	private JsonObject userInfo;
	private BufferedReader in;
	private PrintWriter out;
	private String username;

	public ClientChat(Socket userSocket, String uName, BufferedReader reader, PrintWriter writer) {
		socket = userSocket;
		in = reader;
		out = writer;
		username = uName;
	}

	@Override
	public void run() {
		System.out.println("User connected to new server socket");
		Gson gson = new Gson();
		while (true) {
			try {
				String line = in.readLine();
				if (line == null) {
					closeAllConnections();
				} else if (line.equals("GetUserInfo")) {
					write(gson.toJson(ServerOperations.getUserInfo(this.getClass().getResource("data/userInfo/").toExternalForm(), username)));
				} else if (line.equals("GetChatContacts")) {
					sendContactList();
				} else if (line.equals("getMSG")) {
					String uName = in.readLine();
					int startNum = Integer.parseInt(in.readLine());
					JsonArray tmpJA = ServerOperations.getChat(uName, startNum);
					if (tmpJA == null)
						write("ERROR");
					write(tmpJA.toString());
				} else {
					write(null);
				}
			} catch (SocketException e) {
				closeAllConnections();
			} catch (IOException e) {
				closeAllConnections();
				return;
			}
		}
	}

	private void closeAllConnections() {
		try {
			System.out.println("Client connection closed");
			in.close();
			out.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void sendContactList() {
		try {
			int id = ServerOperations.getChatId(username);
			if (id == -1)
				throw new IOException();
			write(ServerOperations.getContactQuickInfo(Integer.toString(id), username));
			write("FINISHED");
		} catch (IOException e) {
			write("ERROR");
			e.printStackTrace();
		}
	}

	private void write(String msg) {
		out.println(msg);
		out.flush();
	}

}
