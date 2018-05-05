package JsonRW;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import clientApp.ClientStartConnection;

/**
 * This class contains static methods to get and set informations the client application
 * needs at start up, i. e. auto connect and/or auto login
 * 
 * @author Arthur H.
 *
 */
public class ClientStartupOperations {
	/**
	 * Location of the JSON file that contains the informations for auto login/connect
	 */
	private static final String FILELOCATION = ClientStartConnection.class.getResource("data/startInfo.json").toExternalForm();
	/**
	 * This object reads the JSON file and allows to read/write specific properties
	 */
	private static JsonObject startInfo = new JsonParser().parse(FileRW.readFromFile(FILELOCATION)).getAsJsonObject();
	/**
	 * Can be used to convert a JSON String into a String that follows the formating rules
	 * for better reading.
	 */
	private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

	/**
	 * @return If the user previously chose to automatically connect to the server at
	 * startup. That information has been stored in the JSON file
	 */
	public static boolean isAutoConnectActive() {
		return startInfo.get("autoConnect").getAsBoolean();
	}

	/**
	 * @return Saved server address for auto connection
	 */
	public static String getServerAddr() {
		return startInfo.get("serverAddr").getAsString();
	}

	/**
	 * @return Saved server port for auto connection
	 */
	public static int getServerPort() {
		return startInfo.get("serverPort").getAsInt();
	}

	/**
	 * Set/write the auto connect properties to the file for future connections
	 * 
	 * @param address The server address
	 * @param port The server port
	 */
	public static void setServerInfo(String address, int port) {
		startInfo.addProperty("serverAddr", address);
		startInfo.addProperty("serverPort", port);
		startInfo.addProperty("autoConnect", true);
		FileRW.writeToFile(FILELOCATION, gson.toJson(startInfo));
	}

	/**
	 * Writes whether the user wants to automatically connect at startup to the file
	 * 
	 * @param state TRUE: auto connecting enabled | FALSE: auto connecting disabled
	 */
	public static void setAutoConnect(boolean state) {
		startInfo.addProperty("autoConnect", state);
		FileRW.writeToFile(FILELOCATION, gson.toJson(startInfo));
	}

	/**
	 * @return If auto login at the server is enabled return <b>true</b>
	 */
	public static boolean isAutoLoginEnabled() {
		return startInfo.get("autoLogin").getAsBoolean();
	}

	/**
	 * @return The user name to automatically login
	 */
	public static String getAutoLoginUsername() {
		return startInfo.get("loginName").getAsString();
	}

	/**
	 * @return The loginID of the user to be used instead of the password
	 */
	public static String getloginID() {
		return startInfo.get("loginID").getAsString();
	}

	/**
	 * Writes the auto login details to the JSON file.
	 * 
	 * @param state Whether auto login is enabled or not
	 * @param name The user name of the user with autologin
	 * @param id The users loginID
	 */
	public static void setAutoLogin(boolean state, String name, String id) {
		startInfo.addProperty("autoLogin", state);
		startInfo.addProperty("loginName", name);
		startInfo.addProperty("loginID", id);
		FileRW.writeToFile(FILELOCATION, gson.toJson(startInfo));
	}
}
