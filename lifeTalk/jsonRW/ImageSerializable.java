package lifeTalk.jsonRW;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;

import javax.swing.ImageIcon;

public class ImageSerializable implements Serializable {
	private static final long serialVersionUID = 1L;
	//private byte[] imageBytes;
	private ImageIcon img;

	public ImageSerializable(BufferedImage img) throws IOException {
		this.img = new ImageIcon(img);
		/*ByteArrayOutputStream bStream = new ByteArrayOutputStream();
		ImageIO.write(img, "png", bStream);
		bStream.flush();
		imageBytes = bStream.toByteArray();*/
	}

	public BufferedImage getBufferedImage() throws IOException, ClassNotFoundException {
		/*ByteArrayInputStream bStream = new ByteArrayInputStream(imageBytes);
		ObjectInputStream oStream = new ObjectInputStream(bStream);
		return (BufferedImage) oStream.readObject();*/
		BufferedImage bImage = new BufferedImage(125, 128, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics2d = bImage.createGraphics();
		graphics2d.drawImage(img.getImage(), 0, 0, null);
		graphics2d.dispose();
		return bImage;
	}

}
