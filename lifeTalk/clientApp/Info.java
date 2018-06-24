package lifeTalk.clientApp;

/**
 * Holds basic information of this build
 * 
 * @author Arthur H.
 *
 */
public class Info {
	/**
	 * command line arguments </br>
	 * [0]: display exceptions or not (boolean); [1]: update interval in milliseconds
	 */
	private static String[] args;
	/** The name of this application */
	public static final String APPNAME = "Life_Talk";
	/** Software version */
	public static final String VERSION = "beta 1.0";

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
