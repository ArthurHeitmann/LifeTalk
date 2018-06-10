package lifeTalk.jsonRW;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;

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
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static String readFromFile(String location) throws IOException {
		StringBuilder sb = null;
		//Reads from the file
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(new File(new URI(location))));
		} catch (URISyntaxException e) {
			//pray that it will not occur
			e.printStackTrace();
		}
		sb = new StringBuilder();
		String line = br.readLine();
		//read the file line by line and append the new line to the StringBuilder
		while (line != null) {
			sb.append(line + System.lineSeparator());
			line = br.readLine();
		}
		br.close();
		return sb.toString();
	}

	public static String readFromFile(String location, int lastLine) throws URISyntaxException, IOException {
		StringBuilder sb = null;
		//Reads from the file
		BufferedReader br = new BufferedReader(new FileReader(new File(new URI(location))));
		sb = new StringBuilder();
		String line = br.readLine();
		//read the file line by line and append the new line to the StringBuilder
		int linesRead = 1;
		while (line != null && linesRead <= lastLine) {
			sb.append(line + System.lineSeparator());
			line = br.readLine();
		}
		br.close();
		return sb.toString();

	}

	/**
	 * Method to write text to a file
	 * 
	 * @param fileLocation Location of the file
	 * @param content The text that should be written to the file
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static void writeToFile(String fileLocation, String content) throws IOException, URISyntaxException {
		Writer writer = null;
		try {
			File file = new File(new URI(fileLocation));
			file.createNewFile();
			writer = new BufferedWriter(new FileWriter(file));
			writer.write(content);
			writer.flush();
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void copyFile(String srcLocation, String destinationLocation) throws IOException, URISyntaxException {
		InputStream is = null;
		OutputStream os = null;
		try {
			is = new FileInputStream(new File(new URI(srcLocation)));
			os = new FileOutputStream(new File(new URI(destinationLocation)));
			byte[] buffer = new byte[1024];
			int length;
			while ((length = is.read(buffer)) > 0) {
				os.write(buffer, 0, length);
			}
		} finally {
			is.close();
			os.close();
		}
	}
}
