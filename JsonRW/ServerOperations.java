package JsonRW;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ServerOperations {
	private static final String SALT = "(`5#c&(\\zPU]'s`Y`6e@x\"h%MwE8=_z{";
	private static String fileLocation;
	private static JsonObject loginsJson;
	private static JsonArray loginsData;

	public static void setFileLocation(String fileLocation) {
		ServerOperations.fileLocation = fileLocation;
		loginsJson = new JsonParser().parse(FileRW.readFromFile(fileLocation)).getAsJsonObject();
		loginsData = loginsJson.get("users").getAsJsonArray();
	}

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

	public static boolean registerUser(String usrName, String pw, String xmlLocation) {
		System.out.println("Registering " + usrName);
		for (int i = 0; i < loginsData.size(); i++) {
			if (((JsonObject) loginsData.get(i)).get("name").getAsString().equals(usrName)) {
				return false;
			}
		}
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		loginsData.add(new JsonParser().parse(gson.toJson(new User(usrName, pw))));
		loginsJson.remove("users");
		loginsJson.add("users", loginsData);
		FileRW.writeToFile(xmlLocation, gson.toJson(loginsJson));
		return true;

	}

	static class User {
		String name;
		String pw;

		public User(String name, String pw) {
			this.name = name;
			this.pw = pw;
		}
	}

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

	public static void createLoginID(String uName, String id) {
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
