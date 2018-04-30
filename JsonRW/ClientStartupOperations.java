package JsonRW;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ClientStartupOperations {
	private static final String FILELOCATION = "../clientApp/data/startInfo.json";
	private static JsonObject startInfo = new JsonParser().parse(FileRW.readFromFile(FILELOCATION)).getAsJsonObject();
	private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public static boolean isAutoConnectActive() {
		return startInfo.get("autoConnect").getAsBoolean();
	}

	public static String getServerAdr() {
		return startInfo.get("serverAdr").getAsString();
	}

	public static int getServerPort() {
		return startInfo.get("serverPort").getAsInt();
	}

	public static void setServerInfo(String adress, int port) {
		startInfo.addProperty("serverAdr", adress);
		startInfo.addProperty("serverPort", port);
		startInfo.addProperty("autoConnect", true);
		FileRW.writeToFile(FILELOCATION, gson.toJson(startInfo));
	}

	public static void setAutoConnect(boolean state) {
		startInfo.addProperty("autoConnect", state);
		FileRW.writeToFile(FILELOCATION, gson.toJson(startInfo));
	}

	public static boolean isAutoLoginEnabled() {
		return startInfo.get("autoLogin").getAsBoolean();
	}

	public static String getAutoLoginUsername() {
		return startInfo.get("loginName").getAsString();
	}

	public static String getloginID() {
		return startInfo.get("loginID").getAsString();
	}

	public static void setAutoLogin(boolean state, String name, String id) {
		startInfo.addProperty("autoLogin", state);
		startInfo.addProperty("loginName", name);
		startInfo.addProperty("loginID", id);
		FileRW.writeToFile(FILELOCATION, gson.toJson(startInfo));
	}
}
