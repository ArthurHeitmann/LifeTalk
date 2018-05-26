package lifeTalk.jsonRW.server;

import java.io.IOException;
import java.net.URISyntaxException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import lifeTalk.jsonRW.FileRW;
import lifeTalk.server.Server;

/**
 * A class that contains static methods to take care of the login and registration process
 * using a JSON file.
 * 
 * @author Arthur H.
 *
 */
public class ServerStartupOperations {
	//location of the JSON file with the login data of the users
	private static String fileLocation;
	//Java representation of the JSON file
	private static JsonObject loginsJson;
	//array with the users
	private static JsonArray loginsData;

	/**
	 * Initialization | set the file location and the JSON objects
	 * 
	 * @param fileLocation location of the JSON file with the details of the users
	 * @throws IOException
	 * @throws JsonSyntaxException
	 */
	public static void setFileLocation(String fileLocation) throws JsonSyntaxException, IOException {
		ServerStartupOperations.fileLocation = fileLocation;
		loginsJson = new JsonParser().parse(FileRW.readFromFile(fileLocation)).getAsJsonObject();
		loginsData = loginsJson.get("users").getAsJsonArray();
	}

	/**
	 * Try to log a user in. Search the array for the user name and than check if the
	 * given password matches the one in the file
	 * 
	 * @param uName Username of the user
	 * @param pw Password of the user
	 * @param fileLocation location of the JSON file
	 * @return Whether the login attempt was successful or not
	 */
	public static boolean logUserIn(String uName, String pw, String fileLocation) {
		for (int i = 0; i < loginsData.size(); i++) {
			String uNameAt_i = ((JsonObject) loginsData.get(i)).get("name").getAsString();
			if (uName.startsWith(uNameAt_i)) {
				String passw = ((JsonObject) loginsData.get(i)).get("pw").getAsString();
				if (pw.startsWith(passw)) {
					return true;
				} else {
					return false;
				}
			}
		}
		return false;
	}

	/**
	 * First check whether the user name already exists or not and than create a new user
	 * account.
	 * 
	 * @param usrName Username
	 * @param pw Password
	 * @param JsonLocation
	 * @return whether the username already exists or not
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public static boolean registerUser(String usrName, String pw, String JsonLocation) throws IOException, URISyntaxException {
		System.out.println("Registering " + usrName);
		for (int i = 0; i < loginsData.size(); i++) {
			if (((JsonObject) loginsData.get(i)).get("name").getAsString().equals(usrName)) {
				return false;
			}
		}
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		loginsData.add(new JsonParser().parse(gson.toJson(new User(usrName, pw))));
		System.out.println(gson.toJson(loginsData));
		loginsJson.add("users", loginsData);
		FileRW.writeToFile(JsonLocation, gson.toJson(loginsJson));
		JsonObject userInfo = new JsonObject();
		userInfo.addProperty("name", usrName);
		userInfo.addProperty("status", "");
		userInfo.addProperty("rights", "default");
		FileRW.writeToFile(Server.class.getResource("data/userInfo/").toExternalForm() + usrName + "Info.json", gson.toJson(userInfo));
		try {
			FileRW.copyFile(Server.class.getResource("data/userInfo/^template.png").toExternalForm(), Server.class.getResource("data/userInfo/").toExternalForm() + usrName + ".png");
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	//template for the de-serialization of the one user
	static class User {
		String name;
		String pw;
		boolean autoLogin = false;
		String loginID = "";

		public User(String name, String pw) {
			this.name = name;
			this.pw = pw;
		}
	}

	/**
	 * Check whether the loginID from the user equals the one on the server
	 * 
	 * @param uName Username associated with the loginID
	 * @param id the corresponding loginID
	 * @return TRUE: the loginID is valid
	 */
	public static boolean validLoginID(String uName, String id) {
		for (int i = 0; i < loginsData.size(); i++) {
			if (((JsonObject) loginsData.get(i)).get("name").getAsString().equals(uName) &&		//
					((JsonObject) loginsData.get(i)).get("autoLogin").getAsBoolean() && 			//
					((JsonObject) loginsData.get(i)).get("loginID").getAsString().equals(id)  		//
			) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Add a new loginID with a user to the JSON file
	 * 
	 * @param uName Username
	 * @param id loginID
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public static void createLoginID(String uName, String id) throws IOException, URISyntaxException {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		for (int i = 0; i < loginsData.size(); i++) {
			if (((JsonObject) loginsData.get(i)).get("name").getAsString().equals(uName)) {
				((JsonObject) loginsData.get(i)).addProperty("autoLogin", true);
				((JsonObject) loginsData.get(i)).addProperty("loginID", id);
			}
		}
		FileRW.writeToFile(fileLocation, gson.toJson(loginsJson));
	}

}
