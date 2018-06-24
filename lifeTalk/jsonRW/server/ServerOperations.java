package lifeTalk.jsonRW.server;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.imageio.ImageIO;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import lifeTalk.jsonRW.FileRW;
import lifeTalk.jsonRW.Message;
import lifeTalk.server.Info;
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
	 * Cache of all last used chats; keys are the two chat persons of that chat in
	 * alphabetical order
	 */
	private static HashMap<String, JsonObject> chatsCache = new HashMap<>();
	/**
	 * Array of all users who are linked together (always two in a pair and alphabetically
	 * sorted)
	 */
	private static JsonArray chatPartners;

	/**
	 * Sends a contact/friend request to another user. A new chat (+ file) will be
	 * created. Process will be terminated if the target does not exist, the target and
	 * sender are the same user or they already have a chat.
	 * 
	 * @param from The person who sent the request
	 * @param to The person who receives the request
	 * @param msg An additianal message that will be added to the chat
	 * 
	 * @return False if 1. the target does not exist OR 2. from and to are equal OR 3.
	 * they already have a chat OTHERWISE True
	 * 
	 * @throws JsonSyntaxException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static boolean sendContactRequest(String from, String to, String msg) throws JsonSyntaxException, IOException, URISyntaxException {
		if (!ServerStartupOperations.userExists(to) || from.equals(to) || getChatId(from, to) != -1)
			return false;
		String chatFolderLocation = Server.class.getResource("data/chats").toExternalForm() + "/";
		String secondName = to;
		String names[] = sortNamesAlphabetically(to, from);
		from = names[0];
		to = names[1];
		//add contact to the chats index list/array
		JsonObject chatIndex = new JsonObject();
		JsonArray chatIndexArr = new JsonParser().parse(//
				FileRW.readFromFile(chatFolderLocation + "index.json"))//
				.getAsJsonObject().get("chatUsers").getAsJsonArray();
		JsonArray newContactPair = new JsonArray();
		chatPartners.add(newContactPair);
		newContactPair.add(from);
		newContactPair.add(to);
		chatIndexArr.add(newContactPair);
		chatIndex.add("chatUsers", chatIndexArr);
		FileRW.writeToFile(chatFolderLocation + "index.json", //
				new GsonBuilder().setPrettyPrinting().create().toJson(chatIndex));

		//copy template chat file and replace place holders with usernames
		FileRW.copyFile(chatFolderLocation + "template.json", //
				chatFolderLocation + (chatIndexArr.size() - 1) + ".json");
		String fileCont = FileRW.readFromFile(chatFolderLocation + "template.json");
		fileCont = fileCont.replace("USER1", from);
		fileCont = fileCont.replace("USER2", to);
		fileCont = fileCont.replace("VARUSER", secondName);
		FileRW.writeToFile(chatFolderLocation + (chatIndexArr.size() - 1) + ".json", fileCont);

		return true;
	}

	/**
	 * Change the chat state (-2 -> blocked, -1 -> declined, 0 -> undecided, 1 ->
	 * accepted)
	 * 
	 * @param state The target state
	 * @param uName1 The person who wants to do the changes
	 * @param uName2 the target chat partner
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static void setChatState(int state, String uName1, String uName2) throws IOException, URISyntaxException {
		String firstUName = uName1;
		String[] names = sortNamesAlphabetically(uName1, uName2);
		uName1 = names[0];
		uName2 = names[1];
		String key = uName1 + ", " + uName2;
		if (!chatsCache.containsKey(key))
			getChat(uName1, uName2);

		int currentState = chatsCache.get(key).get("index").getAsJsonObject().get("state").getAsInt();
		//the person who can change the state (i. e. the person who sent the friend request cannot accept it for some one else)
		String canBeEditedBy = chatsCache.get(key).get("index").getAsJsonObject().get("canbeEditedBy").getAsString();
		//if state is 1 both have the option to block the other user
		if (currentState == 1 && state == -2) {
			chatsCache.get(key).get("index").getAsJsonObject().addProperty("state", -2);
			chatsCache.get(key).get("index").getAsJsonObject().addProperty("canbeEditedBy", firstUName);
		} else {
			//check whether the user is allowed to change the state
			if (canBeEditedBy.equals(firstUName)) {
				switch (currentState) {
					//if UNDECIDED the user can allow or decline the request
					case 0:
						if (state == 1 || state == -1) {
							chatsCache.get(key).get("index").getAsJsonObject().addProperty("state", state);
							chatsCache.get(key).get("index").getAsJsonObject().addProperty("canbeEditedBy", firstUName);
						}
						break;
					//if DECLINED the user can either accept the user or block him entirely
					case -1:
						if (state == 1 || state == -2) {
							chatsCache.get(key).get("index").getAsJsonObject().addProperty("state", state);
							chatsCache.get(key).get("index").getAsJsonObject().addProperty("canbeEditedBy", firstUName);
						}
						break;
					//if BLOCKED the user can ublock the chat again
					case -2:
						if (state == 1) {
							chatsCache.get(key).get("index").getAsJsonObject().addProperty("state", 1);
							chatsCache.get(key).get("index").getAsJsonObject().addProperty("canbeEditedBy", firstUName);
						}
						break;
				}
			}
		}

		chatsCache.get(key).get("index").getAsJsonObject().addProperty("state", state);
	}

	/**
	 * Returns a combination of the chat state and the person who is allowed to edit it
	 * (state + " " + thePerson).
	 * 
	 * @param uName1 Username 1
	 * @param uName2 Username 1
	 * @return Combination of the state (int) and the person allow to edit it (int + " " +
	 * string)
	 */
	public static String getChatState(String uName1, String uName2) {
		String[] names = sortNamesAlphabetically(uName1, uName2);
		uName1 = names[0];
		uName2 = names[1];
		return chatsCache.get(uName1 + ", " + uName2).get("index").getAsJsonObject().get("state").getAsInt() + " " + //
				chatsCache.get(uName1 + ", " + uName2).get("index").getAsJsonObject().get("canbeEditedBy").getAsString();
	}

	/**
	 * Returns basic information of the user like name, status, rights etc.
	 * 
	 * @param location Location of the file
	 * @param name Username
	 * @return The basic infos in a json object
	 * @throws IOException
	 * @throws JsonSyntaxException
	 */
	public static JsonObject getUserInfo(String location, String name) throws JsonSyntaxException, IOException {
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
		//get the other contacts status
		String status = getUserInfo(ServerSideToClient.class.getResource("data/userInfo/").toExternalForm(), //
				c1.equals(curUsr) ? c2 : c1)//
						.get("status").getAsString();
		//get the number of messages in that chat
		int lastLineNum = chatCont.get("index").getAsJsonObject().get("count").getAsInt();
		JsonObject lastMsg;
		if (chatCont.has(Integer.toString(lastLineNum))) {
			lastMsg = chatCont.get(Integer.toString(lastLineNum)).getAsJsonObject();
			contactElement.addProperty("lastLine", lastMsg//
					.get("textContent")//
					.getAsString());
			contactElement.addProperty("firstLineMe", lastMsg.get("user").getAsString().equals(curUsr));
			contactElement.addProperty("dateTime", lastMsg.get("date").getAsString() + " - " + lastMsg.get("time").getAsString());
		}

		//add all the info to the return json object
		contactElement.addProperty("title", c1.equals(curUsr) ? c2 : c1);
		contactElement.addProperty("statusInfo", status);

		return new Gson().toJson(contactElement);
	}

	/**
	 * Get all the chat IDs of a person
	 * 
	 * @param uName The username
	 * @return all associated IDs
	 * @throws IOException
	 * @throws JsonSyntaxException
	 */
	public static int[] getChatId(String uName) throws JsonSyntaxException, IOException {
		ArrayList<Integer> chatIds = new ArrayList<>();
		//check whether the cache has been initialized or is outdated, if so update it
		if (chatPartners == null)
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
	 * @throws IOException
	 * @throws JsonSyntaxException
	 */
	public static int getChatId(String uName1, String uName2) throws JsonSyntaxException, IOException {
		//make sure that the names are alphabetically sorted
		String[] tmpNames = sortNamesAlphabetically(uName1, uName2);
		uName1 = tmpNames[0];
		uName2 = tmpNames[1];
		//check whether cache already contains id
		String key = uName1 + ", " + uName2;
		if (userIdsCache.containsKey(key))
			return userIdsCache.get(key);
		//check whether other cache has been updated or is outdated, if so update it
		if (chatPartners == null)
			chatPartners = new JsonParser().parse(//
					FileRW.readFromFile(Server.class.getResource("data/chats/index.json").toExternalForm()))//
					.getAsJsonObject()//
					.get("chatUsers")//
					.getAsJsonArray();

		//search array for both usernames
		for (int j = 0; j < chatPartners.size(); j++) {
			if (chatPartners.get(j).getAsJsonArray().get(0).getAsString().equals(uName1) && chatPartners.get(j).getAsJsonArray().get(1).getAsString().equals(uName2)) {
				userIdsCache.put(key, j);
				return j;
			}
		}

		return -1;
	}

	/**
	 * Get the chat as a json object
	 * 
	 * @param uName1 User 1
	 * @param uName2 User 2
	 * 
	 * @return JsonArray that contains all message from that chat
	 * 
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws JsonSyntaxException
	 */
	public static JsonArray getChat(String uName1, String uName2) throws JsonSyntaxException, IOException, URISyntaxException {
		JsonArray chatMsgs = new JsonArray();
		String[] tmpNames = sortNamesAlphabetically(uName1, uName2);
		uName1 = tmpNames[0];
		uName2 = tmpNames[1];
		String key = uName1 + ", " + uName2;
		JsonObject userChat;
		chatsCache.containsKey(key);
		if (chatsCache.containsKey(key))
			userChat = chatsCache.get(key);
		else {
			//parse user chat from file
			String path = ServerSideToClient.class.getResource("data/chats/").toExternalForm() + getChatId(uName1, uName2) + ".json";
			File file = new File(new URI(path));
			if (!file.getAbsoluteFile().exists()) {
				long timeoutStart = System.currentTimeMillis();
				while (true) {
					System.out.print("waiting for file ");
					if (file.getAbsoluteFile().exists() || System.currentTimeMillis() - timeoutStart > 1000)
						break;
					try {
						Thread.sleep(75);
					} catch (InterruptedException e) {
						if (Boolean.parseBoolean(Info.getArgs()[0]))
							e.printStackTrace();
					}
				}
			}
			userChat = new JsonParser().parse(//
					FileRW.readFromFile(path))//
					.getAsJsonObject();
			chatsCache.put(key, userChat);
		}
		//get number of messages
		int count = userChat.get("index").getAsJsonObject().get("count").getAsInt();
		//add messages to the return json array
		for (int i = 0; i < count; i++) {
			chatMsgs.add(userChat.get(Integer.toString(count - i)));
		}
		return chatMsgs;
	}

	/**
	 * Adds a message to the chat associated with it.
	 * 
	 * @param msg The message
	 */
	public static void addMessageToChat(Message msg) {
		String[] names = sortNamesAlphabetically(msg.sender, msg.receiver);
		String key = names[0] + ", " + names[1];
		JsonObject jsonMsg = new JsonObject();
		jsonMsg.addProperty("user", msg.receiver);
		jsonMsg.addProperty("textContent", msg.content);
		jsonMsg.addProperty("date", new SimpleDateFormat("d.M.y").format(new Date(msg.date)));
		jsonMsg.addProperty("time", new SimpleDateFormat("H:m").format(new Date(msg.date)));
		if (!chatsCache.containsKey(key)) {
			try {
				getChat(msg.receiver, msg.sender);
			} catch (JsonSyntaxException | IOException | URISyntaxException e) {
				if (Boolean.parseBoolean(Info.getArgs()[0]))
					e.printStackTrace();
			}
		}
		int count = chatsCache.get(key).get("index").getAsJsonObject().get("count").getAsInt() + 1;
		chatsCache.get(key).add(Integer.toString(count), //
				jsonMsg);
		chatsCache.get(key).get("index").getAsJsonObject().addProperty("count", count);
	}

	/**
	 * Takes two strings as an input in returns them in an array sorted alphabetically.
	 * 
	 * @param uName1 Username 1
	 * @param uName2 Username 2
	 * @return alphabetically sorted string array of the two usernames
	 */
	private static String[] sortNamesAlphabetically(String uName1, String uName2) {
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
		return new String[] { uName1, uName2 };
	}

	/**
	 * Get all the profile images from the usernames associated with that id
	 * 
	 * @param ids Array of all chat IDs
	 * @param uName The user that wants to receive the images
	 * @return Array of images
	 */
	public static BufferedImage[] getImagesFromId(int[] ids, String uName) {
		BufferedImage[] images = new BufferedImage[ids.length];
		int i = 0;
		for (int id : ids) {
			//get both users that are in that chat
			String p1 = chatPartners.get(id).getAsJsonArray().get(0).getAsString();
			String p2 = chatPartners.get(id).getAsJsonArray().get(1).getAsString();
			try {
				//get the profile image of the user that is not the initial user (uName)
				images[i] = ImageIO.read(new URL(Server.class.getResource("data/userInfo/" + (p1.equals(uName) ? p2 : p1) + ".png").toExternalForm()));
			} catch (IOException e) {
				if (Boolean.parseBoolean(Info.getArgs()[0]))
					e.printStackTrace();
				return null;
			}
			i++;
		}

		return images;
	}

	/**
	 * Returns all cached chats. Used to save the changes to a file from a different
	 * thread
	 * 
	 * @return the cached chat
	 */
	public static HashMap<String, JsonObject> getChatCache() {
		return chatsCache;
	}
}
