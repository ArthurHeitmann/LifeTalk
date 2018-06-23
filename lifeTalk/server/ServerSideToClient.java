package lifeTalk.server;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import lifeTalk.jsonRW.Message;
import lifeTalk.jsonRW.server.ServerOperations;

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
	private ObjectInputStream in;
	/** Output device for client serve communication */
	private ObjectOutputStream out;
	/** username of the current client */
	private String username;
	/** list of updates like new messages or similar */
	private ArrayList<String> updates = new ArrayList<>();
	private String selectedContact;

	/**
	 * Transfers connection devices from the previous object to this thread.
	 * 
	 * @param userSocket Socket with the client connection
	 * @param uName The users account name
	 * @param reader Input device
	 * @param outStream Output device
	 */
	public ServerSideToClient(Socket userSocket, String uName, ObjectInputStream reader, ObjectOutputStream outStream) {
		socket = userSocket;
		in = reader;
		out = outStream;
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
		//init
		initLoop: while (true) {
			try {
				//client text prompt
				String action = (String) in.readObject();
				if (action == null) {
					closeAllConnections();
					return;
				}
				switch (action) {
					case "GetUserInfo":
						write(gson.toJson(ServerOperations.getUserInfo(this.getClass().getResource("data/userInfo/").toExternalForm(), username)));
						serializeImg(ImageIO.read(new URL(Server.class.getResource("data/userInfo/" + username + ".png").toExternalForm())));
						break;
					case "GetChatContacts":
						sendContactList();
						break initLoop;
					default:
						write(null);
						closeAllConnections();
						return;
				}
			} catch (ClassNotFoundException | IOException e) {
				if (Boolean.parseBoolean(Info.getArgs()[0]))
					e.printStackTrace();
				return;
			}
		}
		System.out.println("init completed");
		//forever loop
		while (true) {
			try {
				String action = (String) in.readObject();
				if (action == null)
					throw new IOException();

				switch (action) {
					case "GETUPDATES":
						handleUpdates();
						break;
					case "msgPart":
						Object msgPart = in.readObject();
						if (msgPart.getClass().getName().equals("java.lang.String"))
							InterClientCommunication.sendMsg((String) msgPart, "msgPart");
						else
							InterClientCommunication.sendMsg(((Message) msgPart).receiver, "msgPart" + gson.toJson(msgPart));
						break;
					case "getMsg":
						System.out.println(1);
						String uName = (String) in.readObject();
						selectedContact = uName;
						int startNum = Integer.parseInt((String) in.readObject());
						JsonArray tmpJA = ServerOperations.getChat(uName, username, startNum);
						if (tmpJA == null)
							write("ERROR");
						write(tmpJA.toString());
						break;
					case "sendMsg":
						Message msg = (Message) in.readObject();
						InterClientCommunication.sendMsg(msg.receiver, "msgFrom" + gson.toJson(msg));
						ServerOperations.addMessageToChat(msg);
						break;
					case "contactRequest":
						JsonObject request = new JsonObject();
						String to = (String) in.readObject();
						String requestMsg = (String) in.readObject();
						request.addProperty("from", username);
						request.addProperty("to", to);
						request.addProperty("msg", requestMsg);
						if (ServerOperations.sendContactRequest(username, to, requestMsg))
							write("SENT");
						else {
							write("NO USERNAME");
							continue;
						}
						ServerOperations.addMessageToChat(new Message(//
								requestMsg.isEmpty() ? username + "want's to add you as a contact" : requestMsg, //
								System.currentTimeMillis(), username, to, true));
						InterClientCommunication.sendMsg(request.get("to").getAsString(), "newChat" + request.toString());
						break;
					case "getChatState":
						System.out.println(username);
						System.out.println(selectedContact);
						write(ServerOperations.getChatState(username, selectedContact));
						break;
					case "setChatState":
						int state = (Integer) in.readObject();
						ServerOperations.setChatState(state, username, selectedContact);
						InterClientCommunication.sendMsg(selectedContact, "chatStt" + ServerOperations.getChatState(username, selectedContact));
						break;
				}

			} catch (ClassNotFoundException | IOException | URISyntaxException e) {
				if (e.getClass().getName() != "java.net.SocketException")
					e.printStackTrace();
				closeAllConnections();
				break;
			}
		}
	}

	/**
	 * check whether any new updates (i. e. new message) are available and if so send them
	 * to the client
	 * 
	 * @throws IOException
	 */
	private void handleUpdates() throws IOException {
		write(selectedContact == null ? "UNKNOWN" : Boolean.toString(InterClientCommunication.userLoggedIn(selectedContact)));
		for (String task : updates) {
			switch (task.substring(0, 7)) {
				case "msgPart":
					if (task.length() > 7) {
						JsonObject msgPart = new JsonParser().parse(task.substring(7)).getAsJsonObject();
						if (msgPart.get("sender").getAsString().equals(selectedContact)) {
							write(task);
						}
					} else {
						write("msgPart");
					}

					break;
				case "newChat":
				case "chatStt": 					//chatState
				case "msgFrom":
					write(task);
					System.out.println("new Msg");
					break;
			}
		}
		write("finished");
		updates.clear();
	}

	private void serializeImg(BufferedImage img) {
		try {
			ByteArrayOutputStream bStream = new ByteArrayOutputStream();
			ImageIO.write(img, "png", bStream);
			byte[] imgBytes = bStream.toByteArray();
			bStream.close();
			write(imgBytes.length);
			write(imgBytes);
		} catch (IOException e) {
			if (Boolean.parseBoolean(Info.getArgs()[0]))
				e.printStackTrace();
			closeAllConnections();
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
			if (Boolean.parseBoolean(Info.getArgs()[0]))
				e.printStackTrace();
		}
		InterClientCommunication.removeClientComm(username);

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
			BufferedImage[] imgs = ServerOperations.getImagesFromId(ids, username);
			if (imgs == null)
				throw new IOException();
			//send all chats quick summary separately to the client
			int i = 0;
			for (int id : ids) {
				write(ServerOperations.getContactQuickInfo(Integer.toString(id), username));
				serializeImg(imgs[i]);
				i++;
			}
			write("FINISHED");
		} catch (IOException e) {
			closeAllConnections();
			if (Boolean.parseBoolean(Info.getArgs()[0]))
				e.printStackTrace();
		}
	}

	/**
	 * @param msg The text message to be sent to the client.
	 * @throws IOException
	 */
	private void write(Object obj) throws IOException {
		out.writeObject(obj);
		out.flush();
	}

	public void addToUpdateQueue(String task) {
		updates.add(task);
	}

}
