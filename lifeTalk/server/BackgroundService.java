package lifeTalk.server;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import lifeTalk.jsonRW.FileRW;
import lifeTalk.jsonRW.server.ServerOperations;

public class BackgroundService implements Runnable {
	@Override
	public void run() {
		Gson jsonFormatter = new GsonBuilder().setPrettyPrinting().create();
		while (true) {
			System.out.println("Checking");
			HashMap<String, JsonObject> newMap = ServerOperations.getChatCache();
			if (newMap == null || newMap.isEmpty()) {
				System.out.println("completed");
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				continue;
			}

			for (String key : newMap.keySet()) {
				System.out.println(key);
				newMap.put(key, newMap.get(key));
				try {
					int chatID = ServerOperations.getChatId(newMap.get(key).get("index").getAsJsonObject().get("contact1").getAsString(), //
							newMap.get(key).get("index").getAsJsonObject().get("contact2").getAsString());
					FileRW.writeToFile(Server.class.getResource("data/chats/" + chatID + ".json").toExternalForm(), jsonFormatter.toJson(newMap.get(key)));
				} catch (JsonSyntaxException | IOException | URISyntaxException e) {
					e.printStackTrace();
				}

				System.out.println(key + " completed");
			}
			System.out.println("completed");
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
