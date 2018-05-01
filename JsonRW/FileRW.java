package JsonRW;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * Class with static methods to read and write from a specific file
 * 
 * @author Arthur H.
 *
 */
public class FileRW {

	/**
	 * Reads the contents of a given file and return the content in a String
	 * 
	 * @param location File location
	 * @return Content of the file
	 */
	public static String readFromFile(String location) {
		StringBuilder sb = null;
		try {
			//Reads from the file
			BufferedReader br = new BufferedReader(new FileReader(new File(location)));
			sb = new StringBuilder();
			String line = br.readLine();
			//read the file line by line and append the new line to the StringBuilder
			while (line != null) {
				sb.append(line + System.lineSeparator());
				line = br.readLine();
			}
			br.close();
			return sb.toString();
		} catch (IOException e) {
			System.err.println(e);
			return null;
		}
	}

	/**
	 * Method to write text to a file
	 * 
	 * @param fileLocation Location of the file
	 * @param content The text that should be written to the file
	 */
	public static void writeToFile(String fileLocation, String content) {
		try (Writer writer = new BufferedWriter(new FileWriter(new File(fileLocation)))) {
			writer.write(content);
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
