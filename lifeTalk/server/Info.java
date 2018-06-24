package lifeTalk.server;

/**
 * Holds basic information of this build
 * 
 * @author Arthur H.
 *
 */
public class Info {
	/**
	 * command line arguments [0]: </br>
	 * display exceptions or not (boolean); [1]: save messages or not [2]: the intervals
	 * when to save the cached chats
	 */
	private static String[] args;
	/** The name of this application */
	public static final String appName = "Life_Talk - Server";
	/** Software version */
	public static final String version = "beta 1.0";

	/**
	 * Initialize the command line arguments. Only allowed once
	 * 
	 * @param args commandl ine arguments
	 */
	public static void setArgs(String[] args) {
		if (args != null)
			Info.args = args;

	}

	/**
	 * @return command line arguments
	 */
	public static String[] getArgs() {
		return args;
	}

}
