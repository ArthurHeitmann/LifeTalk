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
	/** TODO */
	private ArrayList<String> updates = new ArrayList<>();

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
		while (true) {
			try {
				//client text prompt
				String line = (String) in.readObject();
				if (line == null) {
					closeAllConnections();
					return;
				} else if (line.equals("GetUserInfo")) {
					write(gson.toJson(ServerOperations.getUserInfo(this.getClass().getResource("data/userInfo/").toExternalForm(), username)));
					serializeImg(ImageIO.read(new URL(Server.class.getResource("data/userInfo/" + username + ".png").toExternalForm())));
				} else if (line.equals("GetChatContacts")) {
					sendContactList();
					break;
				} else {
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
				String line = (String) in.readObject();
				if (line == null)
					throw new IOException();

				switch (line) {
					case "GETUPDATES":
						handleUpdates();
						break;
					case "getMsg":
						String uName = (String) in.readObject();
						int startNum = Integer.parseInt((String) in.readObject());
						JsonArray tmpJA = ServerOperations.getChat(uName, username, startNum);
						if (tmpJA == null)
							write("ERROR");
						write(tmpJA.toString());
						break;
					case "sendMsg":
						Message msg = (Message) in.readObject();
						InterClientCommunication.sendMsg(new String[] { msg.receiver }, "msgFrom" + gson.toJson(msg));
						break;
					default:
						break;
				}

			} catch (ClassNotFoundException | IOException | URISyntaxException e) {
				// TODO Auto-generated catch block
				if (e.getClass().getName() != "java.net.SocketException")
					e.printStackTrace();
				closeAllConnections();
				break;
			}
		}
	}

	/**
	 * TODO
	 * 
	 * @throws IOException
	 */
	private void handleUpdates() throws IOException {
		for (String task : updates) {
			System.out.println(task);
			switch (task.substring(0, 7)) {
				case "msgFrom":
					JsonObject msg = new JsonParser().parse(task.substring(7)).getAsJsonObject();
					try {
						write("newMsg");
						write(new Gson().toJson(msg));
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;

				default:
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
		for (String update : updates) {
			System.out.println(update);
		}
	}

}
