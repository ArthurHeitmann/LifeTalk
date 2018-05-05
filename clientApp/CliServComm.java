/**
 * 
 */
package clientApp;

import java.net.Socket;

/**
 * @author Arthur H.
 *
 */
public class CliServComm extends Thread {
	Socket socket;

	public CliServComm(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {

	}

}
