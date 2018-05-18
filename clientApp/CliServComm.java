/**
 * 
 */
package clientApp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import clientApp.fxPresets.MessageFx;
import javafx.scene.image.Image;
import jsonRW.ClientOperations;

/**
 * @author Arthur H.
 *
 */
public class CliServComm {
	private Socket socket;
	private ChatsController controller;
	private BufferedReader in;
	private PrintWriter out;
	private JsonObject userData;
	private Gson gson;
	private String usrName;

	public CliServComm(Socket socket, ChatsController controller, PrintWriter writer, BufferedReader reader) throws IOException {
		this.socket = socket;
		this.controller = controller;
		in = reader;
		out = writer;

		gson = new GsonBuilder().setPrettyPrinting().create();

		start();

	}

	public void start() {
		userData = getUserData();
		usrName = userData.get("name").getAsString();
		controller.setNameTitle(usrName);
		makeChatContactList();
		controller.setComm(this);
		MessageFx[] a = new MessageFx[] { new MessageFx("Hello", true, new Date(0), controller.chatView.getWidth()), new MessageFx("Hi", false, new Date(0), controller.chatView.getWidth()) };
		controller.addMessages(a);
	}

	public MessageFx[] getMessages(String uName, int msgNumber) {
		JsonArray messages;
		ArrayList<MessageFx> messageFxs = new ArrayList<>();
		write("getMSG");
		write(uName);
		write(Integer.toString(msgNumber));
		try {
			String line = in.readLine();
			if (line.equals("ERROR") || line == null)
				return null;
			messages = new JsonParser().parse(line).getAsJsonArray();
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
		} catch (JsonSyntaxException | IOException | ParseException e) {
			e.printStackTrace();
			return null;
		}

	}

	private void makeChatContactList() {
		write("GetChatContacts");
		while (true) {
			String line = null;
			try {
				line = in.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (line == null || line.equals("FINISHED") || line.equals("ERROR") || line.charAt(0) != '{') {
				System.out.println("chat list creation ended");
				return;
			}
			JsonObject listElement = new JsonParser().parse(line).getAsJsonObject();
			Image tmpImg = null;
			try {
				tmpImg = ClientOperations.stringToImg(listElement.get("imgSerialized").getAsString());
				System.out.println("Image recieved");
			} catch (IOException | ClassNotFoundException e) {
				tmpImg = new Image(CliServComm.class.getResource("resources/user.png").toExternalForm());
				e.printStackTrace();
			}
			controller.addChatContact(listElement.get("title").getAsString(), listElement.get("lastLine").getAsString(), listElement.get("firstLineMe").getAsBoolean(),
					listElement.get("statusInfo").getAsString(), tmpImg);
		}
	}

	private JsonObject getUserData() {
		write("GetUserInfo");
		try {
			return new JsonParser().parse(in.readLine()).getAsJsonObject();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private void write(String msg) {
		out.println(msg);

		out.flush();
	}

}
