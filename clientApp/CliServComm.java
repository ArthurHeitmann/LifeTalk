/**
 * 
 */
package clientApp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
		controller.setNameTitle(userData.get("name").getAsString());
		makeChatContactList();

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
			if (line == null || line.equals("FINISHED") || line.charAt(0) != '{') {
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
			controller.hideLoadingImg1();
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
