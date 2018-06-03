package lifeTalk.server;

import java.util.HashMap;

public class InterClientCommunication {
	private static HashMap<String, ServerSideToClient> commMap = new HashMap<>();

	public synchronized static void sendMsg(String[] targets, String msg) {
		for (String uName : targets) {
			if (commMap.containsKey(uName)) {
				commMap.get(uName).addToUpdateQueue(msg);
			}
		}
	}

	public synchronized static void putClientComm(String uName, ServerSideToClient comm) {
		commMap.put(uName, comm);
	}
}
