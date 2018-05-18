package jsonRW;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;

import javax.imageio.ImageIO;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import server.ClientChat;
import server.Server;

public class ServerOperations {
	private static HashMap<String, Integer> userIdsCache = new HashMap<>();
	private static JsonArray chatPartners;

	public static JsonObject getUserInfo(String location, String name) {
		return new JsonParser().parse(FileRW.readFromFile(location + name + "Info.json")).getAsJsonObject();
	}

	public static String getContactQuickInfo(String id, String curUsr) throws IOException {
		JsonObject contactElement = new JsonObject();
		JsonObject chatCont = new JsonParser().parse(//
				FileRW.readFromFile(ClientChat.class.getResource("data/chats/" + id + ".json").toExternalForm()))//
				.getAsJsonObject();

		String c1 = chatCont.get("index").getAsJsonObject().get("contact1").getAsString();
		String c2 = chatCont.get("index").getAsJsonObject().get("contact2").getAsString();
		int lastLineNum = chatCont.get("index").getAsJsonObject().get("count").getAsInt();
		String status = getUserInfo(ClientChat.class.getResource("data/userInfo/").toExternalForm(), c1.equals(curUsr) ? c2 : c1).get("status").getAsString();

		contactElement.addProperty("title", c1.equals(curUsr) ? c2 : c1);
		contactElement.addProperty("lastLine", chatCont.get(Integer.toString(lastLineNum)).getAsJsonObject().get("textContent").getAsString());
		contactElement.addProperty("firstLineMe", chatCont.get(Integer.toString(lastLineNum)).getAsJsonObject().get("user").getAsString().equals(curUsr));
		contactElement.addProperty("statusInfo", status);

		BufferedImage tmpImg = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
		//Graphics2D drawer = tmpImg.createGraphics();
		//drawer.drawImage(new Image, 0, 0, null);

		contactElement.addProperty("imgSerialized", serialize(new ImageSerializable(ImageIO.read(new URL(Server.class.getResource("data/userInfo/" + curUsr + ".png").toExternalForm())))));

		return new Gson().toJson(contactElement);
	}

	/** Write the object to a Base64 string. */
	private static String serialize(Serializable o) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(o);
		oos.close();
		return Base64.getEncoder().encodeToString(baos.toByteArray());
	}

	public static int getChatId(String uName) {
		if (userIdsCache.containsKey(uName))
			return userIdsCache.get(uName);
		if (chatPartners == null)
			chatPartners = new JsonParser().parse(//
					FileRW.readFromFile(Server.class.getResource("data/chats/index.json").toExternalForm()))//
					.getAsJsonObject()//
					.get("chatUsers")//
					.getAsJsonArray();

		for (int i = 0; i < chatPartners.size(); i++) {
			if (chatPartners.get(i).getAsJsonArray().get(0).getAsString().equals(uName) || chatPartners.get(i).getAsJsonArray().get(1).getAsString().equals(uName)) {
				userIdsCache.put(uName, i);
				return i;
			}
		}

		return -1;
	}

	public static void removeUserCache(String username) {
		if (userIdsCache.containsKey(username))
			userIdsCache.remove(username);
	}

	public static JsonArray getChat(String uName, int start) {
		JsonObject userChat = new JsonParser().parse(//
				FileRW.readFromFile(ClientChat.class.getResource("data/chats/" + getChatId(uName) + ".json").toExternalForm()))//
				.getAsJsonObject();
		int count = ((JsonObject) userChat.get("index")).get("count").getAsInt();
		if (count < 20 && start > 20)
			return null;
		JsonArray chat20msgs = new JsonArray();
		for (int i = 0; i <= Math.min(20, count); i++) {
			chat20msgs.add(userChat.get(Integer.toString(count - i)));
		}
		return chat20msgs;
	}
}
