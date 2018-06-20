package lifeTalk.server;

import java.util.HashMap;

public class InterClientCommunication {
	private static HashMap<String, ServerSideToClient> commMap = new HashMap<>();

	public static void sendMsg(String target, String msg) {
		if (commMap.containsKey(target))
			commMap.get(target).addToUpdateQueue(msg);
	}

	public synchronized static void putClientComm(String uName, ServerSideToClient comm) {
		commMap.put(uName, comm);
	}

	public static void removeClientComm(String uName) {
		if (commMap.containsKey(uName))
			commMap.remove(uName);
	}

	public static boolean userLoggedIn(String uName) {
		return commMap.containsKey(uName);
	}

}
