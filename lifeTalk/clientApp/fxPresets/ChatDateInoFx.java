package lifeTalk.clientApp.fxPresets;

import java.text.SimpleDateFormat;
import java.util.Date;

import javafx.geometry.Insets;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class ChatDateInoFx {
	/** Holds the text node */
	private VBox textlayout;

	public ChatDateInoFx(Date date) {
		//create text node with a readable date
		Text text = new Text(new SimpleDateFormat("d.M.y").format(date).toString());
		textlayout = new VBox();
		//styling
		textlayout.setStyle("-fx-background-radius: 5px; -fx-background-color: #9bff9e;");
		textlayout.setPadding(new Insets(4, 12, 4, 12));
		textlayout.getChildren().add(text);
	}

	/**
	 * @return The main container
	 */
	public VBox getLayout() {
		return textlayout;
	}
}
