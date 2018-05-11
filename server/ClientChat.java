package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jsonRW.FileRW;
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

	protected void closeAllConnections() {
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
		JsonObject chatsIndex = new JsonParser().parse(FileRW.readFromFile(this.getClass().getResource("data/chats/index.json").toExternalForm())).getAsJsonObject();
		JsonArray chatPartners = chatsIndex.get("chatUsers").getAsJsonArray();

		for (int i = 0; i < chatPartners.size(); i++) {
			if (chatPartners.get(i).getAsJsonArray().get(0).getAsString().equals(username) || chatPartners.get(i).getAsJsonArray().get(1).getAsString().equals(username)) {
				try {
					write(ServerOperations.getContactQuickInfo(Integer.toString(i), username));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		write("FINISHED");
	}

	private void write(String msg) {
		out.println(msg);
		out.flush();
	}

}
