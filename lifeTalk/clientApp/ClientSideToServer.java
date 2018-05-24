/**
 * 
 */
package lifeTalk.clientApp;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import lifeTalk.clientApp.fxPresets.MessageFx;

/**
 * This class communicates with the server to get/send messages and receive other
 * information which the server stores, since almost no user data is being stored locally.
 * 
 * @author Arthur H.
 *
 */
public class ClientSideToServer {
	/** Connection to the server */
	private Socket socket;
	/** The controller that allows the manipulation of the GUI */
	private ChatsController controller;
	/** Receives data from the server */
	private ObjectInputStream in;
	/** Sends data to the server */
	private ObjectOutputStream out;
	/**
	 * Holds basic information of the current user like name, status, profile pic, etc.
	 */
	private JsonObject userData;

	/**
	 * Gets the socket and input/output devices from the initial connection.
	 * 
	 * @param socket The socket user - server
	 * @param controller The controller that is connected to the fxml file
	 * @param outStream ObjectOutputStream: output device
	 * @param reader ObjectInputStream: input device
	 * @throws IOException
	 */
	public ClientSideToServer(Socket socket, ChatsController controller, ObjectOutputStream outStream, ObjectInputStream reader) throws IOException {
		this.socket = socket;
		this.controller = controller;
		in = reader;
		out = outStream;

	}

	/**
	 * Receive information about the user (name, about, chats, etc.)
	 */
	public void retrieveUserInfo() {
		Platform.runLater(() -> {
			//get basic user info and display the name
			userData = getUserData();
			controller.setProfilePic(SwingFXUtils.toFXImage(getImageFromBytes(), null));
			controller.setNameTitle(userData.get("name").getAsString());
			//get all chats and contacts from the server and add them to the GUI
			makeChatContactList();
			//give the controller this class
			controller.setComm(this);
		});
	}

	/**
	 * 
	 */
	public void update() {
		System.out.print("updated");
		//TODO check server for new info
	}

	/**
	 * gets up to 20 message from the server (to avoid unnecessary and big data
	 * transfers).
	 * 
	 * @param uName The name of the other chat partner
	 * @param msgStartNum At which number to start (0 -> start with latest message)
	 * @return Array of the last messages containing person, content, date/time
	 */
	public MessageFx[] getMessages(String uName, int msgStartNum) {
		JsonArray messages;
		ArrayList<MessageFx> messageFxs = new ArrayList<>();
		//prompt server to get messages
		write("getMSG");
		write(uName);
		write(Integer.toString(msgStartNum));
		try {
			//all messages are received in one JSON string
			String line = (String) in.readObject();
			if (line.equals("ERROR") || line == null)
				return null;
			messages = new JsonParser().parse(line).getAsJsonArray();
			//create the MessageFxs and add them to the array list
			double paneWidth = controller.getScrollPaneWidth();
			for (int i = 0; i < messages.size(); i++) {
				JsonObject tmpJO = (JsonObject) messages.get(i);
				messageFxs.add(new MessageFx(//
						tmpJO.get("textContent").getAsString(), //
						tmpJO.get("user").getAsString().equals(uName), //
						new SimpleDateFormat("d.M.yH:m").parse(tmpJO.get("date").getAsString() + //
								tmpJO.get("time").getAsString()),//
						paneWidth));
			}
			return messageFxs.toArray(new MessageFx[messageFxs.size()]);
		} catch (JsonSyntaxException | IOException | ParseException | ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Receive all contacts and chats associated with the current user and display them in
	 * the GUI
	 */
	private void makeChatContactList() {
		//contact server
		write("GetChatContacts");
		//receive chats/contacts until the server is finished or an error occurs
		while (true) {
			//One Json string of a chat/contact
			String line = null;
			try {
				line = (String) in.readObject();
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
			//check whether finished, an error occurred or and invalid string has been received
			if (line == null || line.equals("FINISHED") || line.equals("ERROR") || line.charAt(0) != '{') {
				System.out.println("chat list creation ended");
				return;
			}
			//Parse one contact/chat into a json object
			JsonObject listElement = new JsonParser().parse(line).getAsJsonObject();

			//add one chat/contact to the GUI
			controller.addChatContact(listElement.get("title").getAsString(), listElement.get("lastLine").getAsString(), listElement.get("firstLineMe").getAsBoolean(),
					listElement.get("statusInfo").getAsString(), SwingFXUtils.toFXImage(getImageFromBytes(), null));
		}
	}

	/**
	 * Get the basic user info from the server
	 * 
	 * @return Basic user info in a json string
	 */
	private JsonObject getUserData() {
		write("GetUserInfo");
		try {
			System.out.println(socket.isClosed());
			return new JsonParser().parse((String) in.readObject()).getAsJsonObject();
		} catch (IOException | JsonSyntaxException | ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Send an object, that can be serialized, to the server
	 * 
	 * @param msg The text message for the server
	 */
	private void write(Object obj) {
		try {
			out.writeObject(obj);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method receives and deserializes an image received from the server.
	 * 
	 * @return A deserialized (normal) (Buffered-)Image
	 */
	private BufferedImage getImageFromBytes() {
		BufferedImage buffImg = null;
		try {
			int size = (int) in.readObject();
			byte[] imgBytes = new byte[size];
			imgBytes = (byte[]) in.readObject();
			ByteArrayInputStream bStream = new ByteArrayInputStream(imgBytes);
			buffImg = ImageIO.read(bStream);
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return buffImg;
	}

}