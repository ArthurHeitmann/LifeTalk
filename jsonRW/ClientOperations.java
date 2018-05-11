package jsonRW;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Base64;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

public class ClientOperations {

	/** Read the object from Base64 string. */
	public static Image stringToImg(String s) throws IOException, ClassNotFoundException {
		byte[] data = Base64.getDecoder().decode(s);
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
		ImageSerializable tmpImg = (ImageSerializable) ois.readObject();
		ois.close();
		return SwingFXUtils.toFXImage(((ImageSerializable) tmpImg).getBufferedImage(), null);
	}
}
