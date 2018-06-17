package lifeTalk.clientApp.fxPresets;

import java.sql.Time;
import java.util.Date;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;

/**
 * This class represents one message which can be displayed. It contains the messages
 * content, the date and time. It can either be green or gray depending who is the
 * receiver/sender.
 * 
 * @author Arthur H.
 *
 */
public class MessageFx {
	private boolean writingInProgress = false;
	/** The date when the message was send */
	private Date date;
	/** contains all the nodes of the visual message */
	private HBox primaryLayout;
	private Text textContent;
	/**
	 * Listener that adjusts the messages dimensions responsive when the parent pane is
	 * being scaled
	 */
	private ChangeListener<Number> listener;
	private final int REFRESHRATE = 300;
	private final String CHARLIST = "qwertzuiopasdfghjklyxcvbnmQWERTZUIOPASDFGHJKLYXCVBNM1234567890!?=.,-_~{}][°^<>|:;#'+*()/&%$";
	private int lastSpaceAt = -1;
	private boolean killIt = false;
	private boolean writingAnimRunning = false;

	/**
	 * Creates and sets up all the nodes necessary for the message
	 * 
	 * @param contentText The main content of the message
	 * @param msgByMe Whether message for send by the current client or not. TRUE:
	 * alignment to the right, gray color; FALSE: alignment: left, green color
	 * @param dateD Date when the message was sent
	 * @param timeT Time when the message was sent (will be displayed below for the user)
	 * @param currentWidth The current width of the parent Panel/ScrolPane
	 */
	public MessageFx(String contentText, boolean msgByMe, Date dateTime, double currentWidth) {
		//create all necessary nodes
		primaryLayout = new HBox();
		StackPane msgContainer = new StackPane();
		VBox textContainer = new VBox(7);
		HBox timePositioner = new HBox();
		Pane timerPositionerPlaceholder = new Pane();
		textContent = new Text(contentText);
		Text time = new Text(new Time(dateTime.getTime()).toString().substring(0, 5));
		this.date = new Date(dateTime.getTime());

		//add the nodes to the parent nodes
		timePositioner.getChildren().addAll(timerPositionerPlaceholder, time);
		textContainer.getChildren().addAll(textContent, timePositioner);

		//setup basic visuals
		HBox.setHgrow(timerPositionerPlaceholder, Priority.ALWAYS);
		textContainer.setPadding(new Insets(13));
		textContent.setFill(Paint.valueOf("#f4f4f4"));
		time.setFill(Paint.valueOf("#f4f4f4"));
		//setup type specific visuals
		if (msgByMe) {
			Pane placeholder = new Pane();
			HBox.setHgrow(placeholder, Priority.ALWAYS);
			placeholder.setMinWidth(50);
			primaryLayout.getChildren().addAll(placeholder, textContainer);
			textContainer.setStyle("-fx-background-color: #727272; -fx-background-radius: 22px;");
		} else {
			primaryLayout.getChildren().addAll(textContainer);
			textContainer.setStyle("-fx-background-color: #20ad25; -fx-background-radius: 22px;");
		}

		//listener for the ScrollPane width property
		listener = (obsV, oldV, newV) -> {
			boolean resetTranslateX = true;
			//If the parent pane is less then 350px wide then stop word wrapping at 170px
			if (newV.doubleValue() < 350) {
				textContent.setWrappingWidth(170);
				return;
			}
			primaryLayout.setPrefWidth(newV.doubleValue());
			//If the text is short enough to be displayed in one line
			if (new Text(textContent.getText()).getBoundsInLocal().getWidth() + 50 + 26 < newV.doubleValue())
				textContent.setWrappingWidth(0);
			//If the message box is bigger than the ScrollPane
			else if (textContainer.getWidth() + 50 > newV.doubleValue()) {
				textContent.setWrappingWidth(textContainer.getWidth() - 50 - 26);
				textContainer.setTranslateX(-25);
				resetTranslateX = false;
			}
			//if the ScrollPane width gets bigger
			else if (oldV != null && newV.doubleValue() > oldV.doubleValue()) {
				textContent.setWrappingWidth(newV.doubleValue() - 50 - 26);
				textContainer.setTranslateX(-25);
				resetTranslateX = false;
			}
			//initial setup after first creation
			else if (oldV == null)
				textContent.setWrappingWidth(newV.doubleValue() - 50 - 26);
			if (resetTranslateX)
				textContainer.setTranslateX(0);
		};

		listener.changed(null, null, currentWidth);
	}

	public MessageFx(double currentWidth) {
		writingInProgress = true;
		//create all necessary nodes
		primaryLayout = new HBox();
		StackPane msgContainer = new StackPane();
		VBox textContainer = new VBox(7);
		HBox timePositioner = new HBox();
		Pane timePositionerPlaceholder = new Pane();
		textContent = new Text();
		Text time = new Text("Currently writing");

		//add the nodes to the parent nodes
		timePositioner.getChildren().addAll(timePositionerPlaceholder, time);
		textContainer.getChildren().addAll(textContent, timePositioner);

		//setup basic visuals
		HBox.setHgrow(timePositionerPlaceholder, Priority.ALWAYS);
		textContainer.setPadding(new Insets(13));
		textContent.setFill(Paint.valueOf("#f4f4f4"));
		time.setFill(Paint.valueOf("#f4f4f4"));

		//setup type specific visuals
		primaryLayout.getChildren().addAll(textContainer);
		textContainer.setStyle("-fx-background-color: #51c8ff; -fx-background-radius: 22px;");

		//listener for the ScrollPane width property
		listener = (obsV, oldV, newV) -> {
			boolean resetTranslateX = true;
			//If the parent pane is less then 350px wide then stop word wrapping at 170px
			if (newV.doubleValue() < 350) {
				textContent.setWrappingWidth(170);
				return;
			}
			primaryLayout.setPrefWidth(newV.doubleValue());
			//If the text is short enough to be displayed in one line
			if (new Text(textContent.getText()).getBoundsInLocal().getWidth() + 50 + 26 < newV.doubleValue())
				textContent.setWrappingWidth(0);
			//If the message box is bigger than the ScrollPane
			else if (textContainer.getWidth() + 50 > newV.doubleValue()) {
				textContent.setWrappingWidth(textContainer.getWidth() - 50 - 26);
				textContainer.setTranslateX(-25);
				resetTranslateX = false;
			}
			//if the ScrollPane width gets bigger
			else if (oldV != null && newV.doubleValue() > oldV.doubleValue()) {
				textContent.setWrappingWidth(newV.doubleValue() - 50 - 26);
				textContainer.setTranslateX(-25);
				resetTranslateX = false;
			}
			//initial setup after first creation
			else if (oldV == null)
				textContent.setWrappingWidth(newV.doubleValue() - 50 - 26);
			if (resetTranslateX)
				textContainer.setTranslateX(0);
		};

		listener.changed(null, null, currentWidth);

	}

	public void updateWriting(String content) {
		stopAnim();
		if (!writingInProgress)
			return;
		new Thread(() -> {
			lastSpaceAt = -1;
			for (int i = content.length() - 1; i > -1; i--) {
				if (content.charAt(i) == ' ') {
					lastSpaceAt = i;
					break;
				}
			}
			killIt = false;
			int i = 0;
			while (writingAnimRunning) {
			}
			while (!killIt) {
				String text;
				try {
					text = lastSpaceAt == -1 ? //
					genRandomString(content.length()) : //
					content.substring(0, lastSpaceAt) + " " + genRandomString(content.length() - lastSpaceAt - 1);
				} catch (StringIndexOutOfBoundsException e1) {
					System.out.println(content + lastSpaceAt);
					e1.printStackTrace();
					break;
				}

				Platform.runLater(() -> {
					writingAnimRunning = true;
					textContent.setText(text);
					writingAnimRunning = false;
				});
				try {
					Thread.sleep(REFRESHRATE);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	private String genRandomString(int length) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			sb.append(CHARLIST.charAt((int) (Math.random() * (CHARLIST.length() - 1))));
		}
		return sb.toString();
	}

	public void stopAnim() {
		killIt = true;
		try {
			Thread.sleep(REFRESHRATE);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public boolean isNormal() {
		return !writingInProgress;
	}

	/**
	 * 
	 * @return The listener that adjusts the message responsive to the parent ScrollPane
	 */
	public ChangeListener<Number> getListener() {
		return listener;
	}

	public Date getDate() {
		return date;
	}

	/**
	 * 
	 * @return The main Node that contains all the other nodes
	 */
	public HBox getPrimaryLayout() {
		return primaryLayout;
	}

	@Override
	public String toString() {
		return "Date: " + date;
	}

}
