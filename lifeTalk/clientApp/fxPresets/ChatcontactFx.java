package lifeTalk.clientApp.fxPresets;

import java.util.Date;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * A class to manage the visual representation of a contacts quick overview. Contains the
 * contacts image, the accounts name, its status info, what the last message was between
 * that person and the current client. The last message is grayed out if it was sent by
 * the current user.
 * 
 * @author Arthur H.
 */
public class ChatcontactFx {
	/** Parent node that holds all the child nodes. */
	private HBox primaryLayout;
	/** */
	private HBox bottomLayout;
	/** Parent node of all text related nodes, which are displayed vertically */
	private VBox secondaryLayout;
	/** Image of the other persons profile picture */
	private ImageView contactImage;
	/** Holds the name of the other persons username */
	private Label nameLabel;
	/** The last message that was sent in that chat */
	private Label lastLine;
	/** Status info of the other person */
	private Label statusInfo;
	private StackPane newMessageIndicator;
	private Text newMsgCount;
	/** Whether the last message sent was by the current user or not */
	private boolean lastMsgByMe;
	/** The time and date when the last message was sent */
	private Date lastMsgTime;
	private Timeline showNotification;
	private Timeline hideNotification;

	/**
	 * Creates and sets up all the necessary objects and nodes for this object
	 * 
	 * @param title Name of the other person
	 * @param firstLine The last message which was sent in that chat
	 * @param firstLineMe Whether the last message sent was by the current user or not
	 * @param statusInfo The status info of the other user
	 * @param img The profile picture of the other user
	 */
	public ChatcontactFx(String title, String firstLine, boolean firstLineMe, String statusInfo, Image img, Date lastMsgTime) {
		//Create the nodes for the GUI
		primaryLayout = new HBox(10);
		bottomLayout = new HBox();
		secondaryLayout = new VBox(7);
		nameLabel = new Label(title);
		lastLine = new Label(firstLine);
		this.statusInfo = new Label("„" + statusInfo + "“");
		contactImage = new ImageView(img);
		lastMsgByMe = firstLineMe;
		this.lastMsgTime = lastMsgTime;
		newMsgCount = new Text("0");
		newMessageIndicator = new StackPane(newMsgCount);
		Pane placeholder = new Pane();
		showNotification = new Timeline(new KeyFrame(Duration.millis(200), new KeyValue(newMessageIndicator.scaleXProperty(), 1), new KeyValue(newMessageIndicator.scaleYProperty(), 1)));
		hideNotification = new Timeline(new KeyFrame(Duration.millis(200), new KeyValue(newMessageIndicator.scaleXProperty(), 0), new KeyValue(newMessageIndicator.scaleYProperty(), 0)));

		//set the dimensions and text clipping style
		nameLabel.setMaxWidth(300);
		lastLine.setMaxWidth(300);
		newMessageIndicator.setScaleX(0);
		newMessageIndicator.setScaleY(0);
		HBox.setHgrow(placeholder, Priority.ALWAYS);
		this.statusInfo.setMaxWidth(300);
		nameLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
		lastLine.setTextOverrun(OverrunStyle.ELLIPSIS);
		this.statusInfo.setTextOverrun(OverrunStyle.ELLIPSIS);

		//setup the image size and position
		contactImage.setFitWidth(67);
		contactImage.setPreserveRatio(true);
		contactImage.setSmooth(true);
		primaryLayout.setPadding(new Insets(8));
		newMessageIndicator.setMinHeight(newMsgCount.getBoundsInLocal().getHeight() + 6);
		newMessageIndicator.setMinWidth(newMsgCount.getBoundsInLocal().getHeight() + 6);
		newMessageIndicator.setMaxHeight(newMsgCount.getBoundsInLocal().getHeight() + 6);
		newMessageIndicator.setMaxWidth(newMsgCount.getBoundsInLocal().getHeight() + 6);
		newMessageIndicator.setPadding(new Insets(3, 7, 3, 7));

		//CSS
		nameLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #f9f9f9");
		lastLine.setStyle("-fx-font-size: 15px; -fx-text-fill: #f9f9f9");
		this.statusInfo.setStyle("-fx-font-size: 15px; -fx-font-style: italic; -fx-text-fill: #f9f9f9");
		newMessageIndicator.setStyle("-fx-background-color: #28d62e; -fx-background-radius: 50%;");
		newMsgCount.setFill(Paint.valueOf("#ffffff"));

		//add the children to the parent layouts
		bottomLayout.getChildren().addAll(this.statusInfo, placeholder, newMessageIndicator);
		secondaryLayout.getChildren().addAll(nameLabel, lastLine, bottomLayout);
		primaryLayout.getChildren().addAll(contactImage, secondaryLayout);

		contactImage.setTranslateY((primaryLayout.getBoundsInLocal().getHeight() - contactImage.getFitWidth()) / 2 + 8);

		styleLastLine();
	}

	public void setSelected(boolean state) {
		if (state)
			//secondaryLayout.setEffect(new DropShadow(5, 0, 0, Color.BLACK));
			primaryLayout.setStyle("-fx-background-color: #404040;");
		else
			//secondaryLayout.setEffect(null);
			primaryLayout.setStyle("-fx-background-color: transparent;");
	}

	/**
	 * Gray out the last line if it was sent by the current user, otherwise make it look
	 * normal
	 */
	public void styleLastLine() {
		if (lastMsgByMe)
			lastLine.setDisable(true);
		else
			lastLine.setDisable(false);
	}

	/**
	 * Update the text and image in case something changed
	 * 
	 * @param firstLine Text of the last message sent in the chat
	 * @param firstLineMe Whether the last message was sent by the current user
	 * @param statusInfo Status info of the other user
	 * @param img Profile picture of the other user
	 */
	public void update(String firstLine, boolean firstLineMe, String statusInfo, Image img) {
		//check whether the last message changed
		if (!lastLine.getText().equals(firstLine) && lastMsgByMe != firstLineMe) {
			lastLine.setText(firstLine);
			lastMsgByMe = firstLineMe;
			styleLastLine();
		}
		//Whether the status info changed
		if (!this.statusInfo.getText().equals(statusInfo))
			this.statusInfo.setText(statusInfo);

		//whether the image changed
		if (!contactImage.getImage().equals(img))
			contactImage.setImage(img);
	}

	public void increaseNewMsgCounter() {
		showNotification.play();
		newMsgCount.setText(Integer.toString(Integer.parseInt(newMsgCount.getText()) + 1));
		if (newMsgCount.getBoundsInLocal().getWidth() > newMessageIndicator.getWidth() - 6 && newMsgCount.getScaleX() == 1) {
			newMsgCount.setScaleX(newMsgCount.getScaleX() * 0.6);
			newMsgCount.setScaleY(newMsgCount.getScaleY() * 0.6);
		}
	}

	public void clearNewMsgCounter() {
		hideNotification.play();
		newMsgCount.setText("0");
		newMsgCount.setScaleX(1);
		newMsgCount.setScaleY(1);
	}

	public String getTitle() {
		return nameLabel.getText();
	}

	/**
	 * @return The time when the last message was sent
	 */
	public Date getDate() {
		return lastMsgTime;
	}

	/**
	 * @return The parent node with a separator which holds all the child nodes.
	 */
	public HBox getLayout() {
		return primaryLayout;
	}
}
