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
import java.util.Date;

import javax.imageio.ImageIO;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import lifeTalk.clientApp.fxPresets.MessageFx;
import lifeTalk.jsonRW.Message;

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
	private boolean communicationInProgress = false;
	private boolean updateRunning = false;

	/**
	 * Gets the socket and input/output devices from the initial connection.
	 * 
	 * @param socket The socket user - server
	 * @param controller The controller that is connected to the fxml file
	 * @param outStream ObjectOutputStream: output device
	 * @param reader ObjectInputStream: input device
	 * @throws IOException
	 */
	public ClientSideToServer(Socket socket, ChatsController controller, ObjectOutputStream outStream, ObjectInputStream reader) {
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
			try {
				userData = getUserData();
				if (userData == null)
					throw new IOException();
			} catch (IOException e) {
				controller.showInfoDialogue("A problem occured while starting: " + e.getMessage());
				if (Boolean.parseBoolean(Info.getArgs()[0]))
					e.printStackTrace();
			}
			controller.setProfilePic(SwingFXUtils.toFXImage(getImageFromBytes(), null));
			controller.setNameTitle(userData.get("name").getAsString());
			//get all chats and contacts from the server and add them to the GUI
			try {
				makeChatContactList();
			} catch (ClassNotFoundException | IOException | ParseException e) {
				controller.showInfoDialogue("A Problem occured while creating the chat list: " + e.getMessage());
				if (Boolean.parseBoolean(Info.getArgs()[0]))
					e.printStackTrace();
			}
			//give the controller this class
			controller.setComm(this);
			controller.basicSetup();
		});
	}

	/**
	 * 
	 */
	public void update() {
		if (updateRunning)
			return;
		updateRunning = true;
		waitForComm();
		communicationInProgress = true;
		try {
			write("GETUPDATES");
			while (true) {
				String line = (String) in.readObject();
				if (!line.equals("finished") && line != null)
					System.out.println(line);
				if (line != null && line.equals("finished"))
					break;
				else if (line.equals("newMsg")) {
					Message message = new Gson().fromJson((String) in.readObject(), Message.class);
					System.out.println(message);
					controller.displayMsg(message);
				}
			}
		} catch (IOException | ClassNotFoundException e) {

			e.printStackTrace();
		}
		communicationInProgress = false;
		updateRunning = false;
	}

	/**
	 * gets up to 20 message from the server (to avoid unnecessary and big data
	 * transfers).
	 * 
	 * @param uName The name of the other chat partner
	 * @param msgStartNum At which number to start (0 -> start with latest message)
	 * @return Array of the last messages containing person, content, date/time
	 * @throws IOException
	 */
	public MessageFx[] getMessages(String uName, int msgStartNum) throws IOException {
		waitForComm();
		communicationInProgress = true;
		JsonArray messages;
		ArrayList<MessageFx> messageFxs = new ArrayList<>();
		//prompt server to get messages
		write("getMsg");
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
						new SimpleDateFormat("d.M.y - H:m").parse(tmpJO.get("date").getAsString() + " - " + //
								tmpJO.get("time").getAsString()),//
						paneWidth));
			}
			communicationInProgress = false;
			return messageFxs.toArray(new MessageFx[messageFxs.size()]);
		} catch (IOException | ParseException | ClassNotFoundException e) {
			if (Boolean.parseBoolean(Info.getArgs()[0]))
				e.printStackTrace();
			communicationInProgress = false;
			return null;
		}
	}

	/**
	 * Receive all contacts and chats associated with the current user and display them in
	 * the GUI
	 * 
	 * @throws IOException When an error occurs while reading from the server
	 * @throws ClassNotFoundException When the line from the server is not a string
	 * @throws ParseException When an error occurs while parsing a date/time string
	 */
	private void makeChatContactList() throws ClassNotFoundException, IOException, ParseException {
		waitForComm();
		communicationInProgress = true;
		//contact server
		write("GetChatContacts");
		//receive chats/contacts until the server is finished or an error occurs
		while (true) {
			//One Json string of a chat/contact
			String line = null;
			line = (String) in.readObject();
			//check whether finished, an error occurred or and invalid string has been received
			if (line == null || line.equals("FINISHED") || line.equals("ERROR") || line.charAt(0) != '{') {
				System.out.println("chat list creation ended");
				communicationInProgress = false;
				return;
			}
			//Parse one contact/chat into a json object
			JsonObject listElement = new JsonParser().parse(line).getAsJsonObject();

			//add one chat/contact to the GUI
			if (listElement.has("lastLine")) {
				controller.addChatContact(listElement.get("title").getAsString(), //
						listElement.get("lastLine").getAsString(), //
						listElement.get("firstLineMe").getAsBoolean(), //
						listElement.get("statusInfo").getAsString(), //
						SwingFXUtils.toFXImage(getImageFromBytes(), null), //
						new SimpleDateFormat("d.M.y - H:m").parse(listElement.get("dateTime").getAsString()));
			} else {
				controller.addChatContact(listElement.get("title").getAsString(), //
						"", //
						false, //
						listElement.get("statusInfo").getAsString(), //
						SwingFXUtils.toFXImage(getImageFromBytes(), null), //
						new Date(0));
			}
		}
	}

	/**
	 * Get the basic user info from the server
	 * 
	 * @return Basic user info in a json string
	 * @throws IOException When an error occurs while sending a message to the server
	 */
	private JsonObject getUserData() throws IOException {
		waitForComm();
		communicationInProgress = true;
		write("GetUserInfo");
		try {
			communicationInProgress = false;
			return new JsonParser().parse((String) in.readObject()).getAsJsonObject();
		} catch (IOException | JsonSyntaxException | ClassNotFoundException e) {
			if (Boolean.parseBoolean(Info.getArgs()[0]))
				e.printStackTrace();
			communicationInProgress = false;
			return null;
		}
	}

	/**
	 * Let the current thread wait until the currently running communication with the
	 * server is finished.
	 */
	private void waitForComm() {
		while (communicationInProgress) {
			//wait until current communication is finished
		}
	}

	/**
	 * Send an object, that can be serialized, to the server
	 * 
	 * @param msg The text message for the server
	 * @throws IOException
	 */
	public synchronized void write(Object obj) throws IOException {
		out.writeObject(obj);
		out.flush();
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
			if (Boolean.parseBoolean(Info.getArgs()[0]))
				e.printStackTrace();
		}
		return buffImg;
	}

}
