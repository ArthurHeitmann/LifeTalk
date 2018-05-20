package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

import jsonRW.ServerOperations;

/**
 * This class communicates with one client to send and receive text info. In most of the
 * cases JSON is being used for data storage.
 * 
 * @author Arthur H.
 *
 */
public class ServerSideToClient implements Runnable {
	/** Allows connecting to a client */
	private Socket socket;
	/** Input device for client serve communication */
	private BufferedReader in;
	/** Output device for client serve communication */
	private PrintWriter out;
	/** username of the current client */
	private String username;

	/**
	 * Transfers connection devices from the previous object to this thread.
	 * 
	 * @param userSocket Socket with the client connection
	 * @param uName The users account name
	 * @param reader Input device
	 * @param writer Output device
	 */
	public ServerSideToClient(Socket userSocket, String uName, BufferedReader reader, PrintWriter writer) {
		socket = userSocket;
		in = reader;
		out = writer;
		username = uName;
	}

	/**
	 * Main infinite method of this thread. Receives text from the user and than sends
	 * info back.
	 */
	@Override
	public void run() {
		System.out.println("User connected to new server socket");
		Gson gson = new Gson();
		while (true) {
			try {
				//client text prompt
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
					JsonArray tmpJA = ServerOperations.getChat(uName, username, startNum);
					if (tmpJA == null)
						write("ERROR");
					write(tmpJA.toString());
				} else {
					write(null);
				}
			} catch (SocketException e) {
				closeAllConnections();
				return;
			} catch (IOException e) {
				closeAllConnections();
				return;
			}
		}
	}

	/**
	 * If something goes wrong or the connection gets interrupted, close all streams and
	 * the socket
	 */
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

	/**
	 * Send all chats and contacts associated with the current user
	 */
	private void sendContactList() {
		try {
			//all user combinations in which the user occurs and their id
			int[] ids = ServerOperations.getChatId(username);
			if (ids.length == 0) {
				write("No Chats");
				return;
			}
			//send all chats quick summary separately to the client
			for (int id : ids) {
				write(ServerOperations.getContactQuickInfo(Integer.toString(id), username));
			}
			write("FINISHED");
		} catch (IOException e) {
			write("ERROR");
			e.printStackTrace();
		}
	}

	/**
	 * @param msg The text message to be sent to the client.
	 */
	private void write(String msg) {
		out.println(msg);
		out.flush();
	}

}
