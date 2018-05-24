package lifeTalk.jsonRW.server;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import lifeTalk.jsonRW.FileRW;
import lifeTalk.server.Server;
import lifeTalk.server.ServerSideToClient;

/**
 * This class has static methods for getting user info from Json files
 * 
 * @author Arthur H.
 *
 */
public class ServerOperations {
	/** Cache of chat ids to avoid unnecessary file reading and other processes */
	private static HashMap<String, Integer> userIdsCache = new HashMap<>();
	/**
	 * Array of all users who are linked together (always two in a pair and alphabetically
	 * sorted)
	 */
	private static JsonArray chatPartners;
	/** Whether the cache is outdated and needs to be updated or not */
	public static boolean chatListUpdated = false;

	/**
	 * Returns basic information of the user like name, status, rights etc.
	 * 
	 * @param location Location of the file
	 * @param name Username
	 * @return The basic infos in a json object
	 */
	public static JsonObject getUserInfo(String location, String name) {
		return new JsonParser().parse(FileRW.readFromFile(location + name + "Info.json")).getAsJsonObject();
	}

	/**
	 * Get a quick summary of a chat between to users
	 * 
	 * @param id The chat ID
	 * @param curUsr The current user on the client side
	 * @return A JSON string that holds the the persons name, status info, profile pic and
	 * the last message
	 * @throws IOException
	 */
	public static String getContactQuickInfo(String id, String curUsr) throws IOException {
		JsonObject contactElement = new JsonObject();
		//read and parse the chat
		JsonObject chatCont = new JsonParser().parse(//
				FileRW.readFromFile(ServerSideToClient.class.getResource("data/chats/" + id + ".json").toExternalForm()))//
				.getAsJsonObject();

		//The two accounts in that chat
		String c1 = chatCont.get("index").getAsJsonObject().get("contact1").getAsString();
		String c2 = chatCont.get("index").getAsJsonObject().get("contact2").getAsString();
		//get the number of messages in that chat
		int lastLineNum = chatCont.get("index").getAsJsonObject().get("count").getAsInt();
		//get the other contacts status
		String status = getUserInfo(ServerSideToClient.class.getResource("data/userInfo/").toExternalForm(), //
				c1.equals(curUsr) ? c2 : c1)//
						.get("status").getAsString();

		//add all the info to the return json object
		contactElement.addProperty("title", c1.equals(curUsr) ? c2 : c1);
		contactElement.addProperty("lastLine", chatCont.get(Integer.toString(lastLineNum))//
				.getAsJsonObject()//
				.get("textContent")//
				.getAsString());
		contactElement.addProperty("firstLineMe", chatCont.get(Integer.toString(lastLineNum)).getAsJsonObject().get("user").getAsString().equals(curUsr));
		contactElement.addProperty("statusInfo", status);

		return new Gson().toJson(contactElement);
	}

	/**
	 * Get all the chat IDs of a person
	 * 
	 * @param uName The username
	 * @return all associated IDs
	 */
	public static int[] getChatId(String uName) {
		ArrayList<Integer> chatIds = new ArrayList<>();
		//check whether the cache has been initialized or is outdated, if so update it
		if (chatListUpdated || chatPartners == null)
			chatPartners = new JsonParser().parse(//
					FileRW.readFromFile(Server.class.getResource("data/chats/index.json").toExternalForm()))//
					.getAsJsonObject()//
					.get("chatUsers")//
					.getAsJsonArray();

		//search array for occurrences of the username and add them to to array list
		for (int i = 0; i < chatPartners.size(); i++) {
			if (chatPartners.get(i).getAsJsonArray().get(0).getAsString().equals(uName) || chatPartners.get(i).getAsJsonArray().get(1).getAsString().equals(uName)) {
				chatIds.add(i);
			}
		}

		//convert array list to array
		int[] out = new int[chatIds.size()];
		for (int i = 0; i < out.length; i++) {
			out[i] = chatIds.get(i);
		}

		return out;

	}

	/**
	 * Get the chat id of a chat between to specific users
	 * 
	 * @param uName1 Person 1
	 * @param uName2 Person 2
	 * @return Theri specific id
	 */
	public static int getChatId(String uName1, String uName2) {
		//make sure that the names are alphabetically sorted
		int i = 0;
		while (true) {
			if (uName1.charAt(i) < uName2.charAt(i))
				break;
			else if (uName1.charAt(i) > uName2.charAt(i)) {
				String tmpStr = uName1;
				uName1 = uName2;
				uName2 = tmpStr;
				break;
			}
			if (uName1.length() == i + 1 || uName2.length() == i + 1) {
				if (uName1.length() > uName2.length()) {
					String tmpStr = uName1;
					uName1 = uName2;
					uName2 = tmpStr;
				}
				break;
			}
			i++;
		}

		//check whether cache already contains id
		if (userIdsCache.containsKey(uName1 + ", " + uName2))
			return userIdsCache.get(uName1 + ", " + uName2);
		//check whether other cache has been updated or is outdated, if so update it
		if (chatListUpdated || chatPartners == null)
			chatPartners = new JsonParser().parse(//
					FileRW.readFromFile(Server.class.getResource("data/chats/index.json").toExternalForm()))//
					.getAsJsonObject()//
					.get("chatUsers")//
					.getAsJsonArray();

		//search array for both usernames
		for (int j = 0; j < chatPartners.size(); j++) {
			if (chatPartners.get(j).getAsJsonArray().get(0).getAsString().equals(uName1) && chatPartners.get(j).getAsJsonArray().get(1).getAsString().equals(uName2)) {
				userIdsCache.put(uName1 + ", " + uName2, j);
				return j;
			}
		}

		return -1;
	}

	/**
	 * Clear the cache of a specific user
	 * 
	 * @param username
	 */
	public static void removeUserCache(String username) {
		if (userIdsCache.containsKey(", " + username))
			userIdsCache.remove(", " + username);
	}

	/**
	 * Get the chat as a json between to users
	 * 
	 * @param uName1 User 1
	 * @param uName2 User 2
	 * @param start the beginning index of the first message
	 * @return
	 */
	public static JsonArray getChat(String uName1, String uName2, int start) {
		JsonArray chat20msgs = new JsonArray();
		//parse user chat from file
		JsonObject userChat = new JsonParser().parse(//
				FileRW.readFromFile(ServerSideToClient.class.getResource("data/chats/" + getChatId(uName1, uName2) + ".json").toExternalForm()))//
				.getAsJsonObject();
		//get number of messages
		int count = userChat.get("index").getAsJsonObject().get("count").getAsInt();
		if (count < 20 && start > 20)
			return null;
		//TODO beginning index
		//add messages to the return json array
		for (int i = 0; i < count; i++) {
			chat20msgs.add(userChat.get(Integer.toString(count - i)));
		}
		return chat20msgs;
	}

	public static BufferedImage[] getImagesFromId(int[] ids, String uName) {
		BufferedImage[] images = new BufferedImage[ids.length];
		int i = 0;
		for (int id : ids) {
			String p1 = chatPartners.get(id).getAsJsonArray().get(0).getAsString();
			String p2 = chatPartners.get(id).getAsJsonArray().get(1).getAsString();
			try {
				images[i] = ImageIO.read(new URL(Server.class.getResource("data/userInfo/" + (p1.equals(uName) ? p2 : p1 + ".png")).toExternalForm()));
			} catch (IOException e) {
				e.printStackTrace();
			}
			i++;
		}

		return images;
	}
}
