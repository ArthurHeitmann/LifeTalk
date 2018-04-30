package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import JsonRW.FileRW;
import JsonRW.ServerOperations;

public class Server {
	private static JsonArray loginsData;
	private static String xmlLocation = "data/logins.json";
	private static JsonObject loginJson = new JsonParser().parse(FileRW.readFromFile(xmlLocation)).getAsJsonObject();

	public static void main(String[] args) throws IOException {
		ServerSocket server = new ServerSocket(2111);
		loginsData = loginJson.get("users").getAsJsonArray();
		boolean connected = false;
		System.out.println("Server running");
		while (true) {
			try {
				new CLientHandler(server.accept()).start();
				connected = true;
			} catch (Exception e) {
				System.err.println(e);
			} finally {
				if (!connected) {
					server.close();
					System.out.println("CLOSING SERVER");
					connected = false;
				}
			}
		}

	}

	private static class CLientHandler extends Thread {
		private boolean loggedIn;
		private Socket socket;
		private BufferedReader in;
		private PrintWriter out;

		public CLientHandler(Socket socket) throws IOException {
			this.socket = socket;
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream());
		}

		@Override
		public void run() {
			try {
				boolean userAllowed = false;
				System.out.println("Client running");
				int triesLeft = 4;
				ServerOperations.setFileLocation(xmlLocation);
				while (true) {
					String action = in.readLine();
					boolean stayLoggedin = Boolean.parseBoolean(in.readLine());
					if (action == null) {
						break;
					}
					String usrName = in.readLine();
					String pw = action.equals("AUTOLOGIN") ? in.readLine() : hashGenerator(in.readLine());
					if (usrName == null || pw == null) {
						break;
					}
					if (action.equals("LOGIN")) {
						if (ServerOperations.logUserIn(usrName, pw, xmlLocation)) {
							if (stayLoggedin) {
								write("SUCCESS LOGIN");
								String id = hashGenerator(Long.toString(System.nanoTime()));
								write(id);
								ServerOperations.createLoginID(usrName, id);
							} else {
								write("SUCCESS");
							}
							userAllowed = true;
							break;
						} else if (triesLeft > 0) {
							triesLeft--;
							write("INVALID INPUTS");
						} else {
							write("ACCESS DENIED");
							socket.close();
							break;
						}

					} else if (action.equals("AUTOLOGIN")) {
						if (ServerOperations.validLoginID(usrName, pw)) {
							userAllowed = true;
							write("SUCCESS");
							break;
						} else {
							write("DENIED");
						}
					} else if (action.equals("REGISTER")) {
						if (ServerOperations.registerUser(usrName, pw, xmlLocation)) {
							write("SUCCESS");
							userAllowed = true;
							break;
						} else {
							write("USERNAME TAKEN");
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					in.close();
					out.close();
					socket.close();

				} catch (IOException e1) {
					e1.printStackTrace();
				}
				System.out.println("Connection closed");
			}
		}

		private void write(String msg) {
			out.println(msg);
			out.flush();
			System.out.println(msg);
		}

		private String hashGenerator(String stringToHash) {
			stringToHash += "(`5#c&(\\zPU]'s`Y`6e@x\"h%MwE8=_z{";
			try {
				MessageDigest md = MessageDigest.getInstance("SHA-256");
				byte[] bytes = md.digest(stringToHash.getBytes());
				StringBuilder sb = new StringBuilder();
				for (byte b : bytes) {
					sb.append(Integer.toString((b & 0xff) + 0x100, 16));
				}
				return sb.toString();

			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
				return stringToHash;
			}
		}

		public class User {
			String name;
			String pw;

			public User(String name, String pw) {
				this.name = name;
				this.pw = pw;
			}
		}

	}

}
