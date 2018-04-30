package JsonRW;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;

import server.Server;

public class FileRW {

	public static String readFromFile(String location) {
		StringBuilder sb = null;
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(new URI(Server.class.getResource(".") + location))));
			sb = new StringBuilder();
			String line = br.readLine();
			while (line != null) {
				sb.append(line + System.lineSeparator());
				line = br.readLine();
			}
			br.close();
			return sb.toString();
		} catch (URISyntaxException | IOException e) {
			System.err.println(e);
			return null;
		}
	}

	public static void writeToFile(String fileLocation, String content) {
		try (Writer writer = new BufferedWriter(new FileWriter(new File(new URI(Server.class.getResource(".") + fileLocation))))) {
			writer.write(content);
			writer.flush();
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
