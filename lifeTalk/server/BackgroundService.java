package lifeTalk.server;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import lifeTalk.clientApp.Info;
import lifeTalk.jsonRW.FileRW;
import lifeTalk.jsonRW.server.ServerOperations;

/**
 * This is a background thread that in specific time intervals gets the cached chats and
 * saves them to a file.
 * 
 * @author Arthur H.
 *
 */
public class BackgroundService implements Runnable {
	@Override
	public void run() {
		Gson jsonFormatter = new GsonBuilder().setPrettyPrinting().create();
		while (true) {
			HashMap<String, JsonObject> newMap = ServerOperations.getChatCache();
			if (newMap == null || newMap.isEmpty()) {
				try {
					Thread.sleep(Integer.parseInt(Info.getArgs()[2]));
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				continue;
			}

			for (String key : newMap.keySet()) {
				//newMap.put(key, newMap.get(key));
				try {
					int chatID = ServerOperations.getChatId(newMap.get(key).get("index").getAsJsonObject().get("contact1").getAsString(), //
							newMap.get(key).get("index").getAsJsonObject().get("contact2").getAsString());
					FileRW.writeToFile(Server.class.getResource("data/chats/" + chatID + ".json").toExternalForm(), jsonFormatter.toJson(newMap.get(key)));
				} catch (JsonSyntaxException | IOException | URISyntaxException e) {
					if (Boolean.parseBoolean(Info.getArgs()[0]))
						e.printStackTrace();
				}
			}
			try {
				Thread.sleep(Integer.parseInt(Info.getArgs()[2]));
			} catch (InterruptedException e) {
				if (Boolean.parseBoolean(Info.getArgs()[0]))
					e.printStackTrace();
			}
		}
	}

}
