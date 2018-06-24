package lifeTalk.server;

import java.util.HashMap;
import java.util.TimerTask;

import lifeTalk.clientApp.Info;

/**
 * This class is being used to send messages to users that are currently online.
 * 
 * @author Arthur H.
 *
 */
public class InterClientCommunication {
	private static HashMap<String, ServerSideToClient> commMap = new HashMap<>();

	/**
	 * Adds a string message to a users update queue
	 * 
	 * @param target The target username
	 * @param msg The string message
	 */
	public static void sendMsg(String target, String msg) {
		if (commMap.containsKey(target))
			commMap.get(target).addToUpdateQueue(msg);
	}

	/**
	 * adds a user to the hashmap
	 * 
	 * @param uName The username
	 * @param comm The server side communication object
	 */
	public synchronized static void putClientComm(String uName, ServerSideToClient comm) {
		commMap.put(uName, comm);
	}

	/**
	 * Removes a user after 5 second to allow the background service to save the cached
	 * chats
	 * 
	 * @param uName Username
	 */
	public static void removeClientComm(String uName) {
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					if (Boolean.parseBoolean(Info.getArgs()[0]))
						e.printStackTrace();
				}
				if (commMap.containsKey(uName))
					commMap.remove(uName);
			}
		};
		task.run();
	}

	/**
	 * @param uName Username
	 * @return true if a user is logged in otherwise false
	 */
	public static boolean userLoggedIn(String uName) {
		return commMap.containsKey(uName);
	}

}
