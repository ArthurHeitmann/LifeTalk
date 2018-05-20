package clientApp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.security.auth.login.LoginException;

import javafx.event.ActionEvent;
import javafx.stage.Stage;
import jsonRW.ClientStartupOperations;

/**
 * This class takes care of all the communication with the server. Most important Methods:
 * connect to server, login and register. Allows to automatically connect and/or login to
 * the server.
 * 
 * @author Arthur H.
 *
 */
public class ClientStartConnection {
	/**
	 * Interface to communicate with the server
	 */
	private Socket socket;
	/**
	 * Get information that the server send to the client (in.readLine())
	 */
	private BufferedReader in;
	/**
	 * sends messages to the server (out.println(MESSAGE); out.flush())
	 */
	private PrintWriter out;
	/**
	 * stores the loginID of the user if he chose the option to stay logged in for future
	 * uses
	 */
	private String loginID;

	/**
	 * Establish a connection with the server (using the socket) and setup input & output
	 * devices
	 * 
	 * @param srvAdress Server address like 192.168.178.15 or 79.56.45.34 or
	 * www.server.com or localhost
	 * @param port Port at the Server
	 * @return whether the connection attempt was successful or not
	 * @throws UnknownHostException Invalid server address
	 */
	public boolean connectToServer(String srvAdress, int port) throws UnknownHostException {
		try {
			//setup the communication tools
			socket = new Socket(srvAdress, port);
			out = new PrintWriter(socket.getOutputStream());
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			return true;
		} catch (IOException e) {
			//e.printStackTrace();
			return false;
		}

	}

	/**
	 * Get's called after the a connection was established with the server. If the user
	 * previously prompted to stay logged in, login with the username, loginID for the
	 * password and AUTOLOGIN as action command. If unsuccessful the program proceeds to
	 * the normal login screen for the user to enter the login credentials.
	 * 
	 * @param controller The controller of the FXML file
	 * @param stage The main Stage of the current application
	 */
	public void autoLogin(LoginController controller, Stage stage) {
		if (ClientStartupOperations.isAutoLoginEnabled()) {
			controller.uNameInp.setText(ClientStartupOperations.getAutoLoginUsername());
			controller.pwInp.setText(ClientStartupOperations.getloginID());
			controller.autoLogin = true;
			controller.login(new ActionEvent(stage.getScene(), null));
		}
	}

	/**
	 * Try to login to the server.
	 * 
	 * @param uName Username
	 * @param pw User password
	 * @param action following action command are possible: </br>
	 * LOGIN: simply login to the server </br>
	 * AUTOLOGIN: automatically login to the server with a loginID for the password
	 * REGISTER: when registering a new user account
	 * @param stayLoggedin tell the server whether to generate a loginID to automatically
	 * login the next time
	 * @return whether the login attempt was successful or not
	 * @throws LoginException if the connection to the server gets lost, i. e. when
	 * entering wrong credentials 4 times
	 */
	public boolean loginToServer(String uName, String pw, String action, boolean stayLoggedin) throws LoginException {
		//send parameter to the server
		write(action);
		write(Boolean.toString(stayLoggedin));
		write(uName);
		write(pw);
		String line;
		try {
			//read result from the server
			line = in.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		//connection to server get'S interrupted
		if (line == null) {
			System.out.println("EXIT");
			throw new LoginException();
		}
		//Login or Register was successful
		else if (line.equals("SUCCESS")) {
			System.out.println("logged in");
			return true;
		}
		//Login with the option to stay logged in was successful | now get the loginID
		else if (line.equals("SUCCESS LOGIN")) {
			try {
				//read loginID
				line = in.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			//if connection get's interrupted
			if (line == null) {
				throw new LoginException();
			}
			loginID = line;
			return true;

		}
		//User entered wrong credentials 4 times
		else if (line.equals("ACCESS DENIED")) {
			throw new LoginException();
		}
		return false;
	}

	/**
	 * Send a message to the server,
	 * 
	 * @param msg The message for the server
	 */
	private void write(String msg) {
		//print message to the OutputStream
		out.println(msg);
		//send the message
		out.flush();
	}

	/**
	 * Get the loginID. Is being used to automatically login the next time
	 * 
	 * @return the loginID
	 */
	public String getLoginID() {
		return loginID;
	}

	/**
	 * @return PrintWriter: The output device.
	 */
	public PrintWriter getOut() {
		return out;
	}

	/**
	 * @return BufferedReader: The input device.
	 */
	public BufferedReader getIn() {
		return in;
	}

	/**
	 * @return Socket: allows connecting to the server
	 */
	public Socket getSocket() {
		return socket;
	}
}
