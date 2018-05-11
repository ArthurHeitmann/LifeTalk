package jsonRW;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.Base64;

import javax.imageio.ImageIO;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import server.ClientChat;
import server.Server;

public class ServerOperations {

	public static JsonObject getUserInfo(String location, String name) {
		return new JsonParser().parse(FileRW.readFromFile(location + name + "Info.json")).getAsJsonObject();
	}

	public static String getContactQuickInfo(String id, String curUsr) throws IOException {
		JsonObject contactElement = new JsonObject();
		String fileContent = FileRW.readFromFile(ClientChat.class.getResource("data/chats/" + id + ".json").toExternalForm());
		JsonObject chaCont = new JsonParser().parse(fileContent).getAsJsonObject();

		String c1 = chaCont.get("index").getAsJsonObject().get("contact1").getAsString();
		String c2 = chaCont.get("index").getAsJsonObject().get("contact2").getAsString();
		int lastLineNum = chaCont.get("index").getAsJsonObject().get("count").getAsInt();
		String status = getUserInfo(ClientChat.class.getResource("data/userInfo/").toExternalForm(), c1.equals(curUsr) ? c2 : c1).get("status").getAsString();

		contactElement.addProperty("title", c1.equals(curUsr) ? c2 : c1);
		contactElement.addProperty("lastLine", chaCont.get(Integer.toString(lastLineNum)).getAsJsonObject().get("textContent").getAsString());
		contactElement.addProperty("firstLineMe", chaCont.get(Integer.toString(lastLineNum)).getAsJsonObject().get("user").getAsString().equals(curUsr));
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

}
