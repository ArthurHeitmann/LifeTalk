package lifeTalk.clientApp;

import java.sql.Date;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import javafx.util.Duration;
import lifeTalk.clientApp.fxPresets.ChatcontactFx;
import lifeTalk.clientApp.fxPresets.MessageFx;

/**
 * The controller class of the chat screen. Holds nodes from the fxml file, so that they
 * can be accessed and changed.
 * 
 * 
 * @author Arthur H.
 *
 */
public class ChatsController {
	/**
	 * Holds a list of the contacts and chats the user has (element ->
	 * fxPresets.ChatcontactFx)
	 */
	@FXML
	private VBox chatList;
	/** Displays the username of the current user */
	@FXML
	private Label nameTitle;
	/**
	 * Profile pic of the user the current user just selected from the list (VBox
	 * chatList)
	 */
	@FXML
	private ImageView chatPImg;
	/** Name of the user the current user just selected from the list (VBox chatList) */
	@FXML
	private Label chatPName;
	/**
	 * Status info of the user the current user just selected from the list (VBox
	 * chatList)
	 */
	@FXML
	private Label chatPInfo;
	/** Holds all message from the current chat (messages -> fxPresets.MessageFx) */
	@FXML
	private VBox chatView;
	/** ScrollPane of the current chat which holds chatView */
	@FXML
	private ScrollPane chatViewScrollPane;
	@FXML
	private ImageView loadingImg;
	@FXML
	private HBox chatHeader;
	/** The class that communicates with the server */
	private ClientSideToServer serverCommunication;
	private String selectedChat;
	private ChatcontactFx selectedContact;

	/**
	 * Display the name of the current user
	 * 
	 * @param name The name of the current user
	 */
	public void setNameTitle(String name) {

		nameTitle.setText(name);
	}

	/**
	 * Adds a contact/chat link, to the left side list, with which the user is associated
	 * 
	 * @param title Name of the other user
	 * @param firstLine The last message from that chat
	 * @param firstLineMe Whether the last message sent is from the current user
	 * @param statusInfo The status info of the other user
	 * @param img The profile pic of the other user
	 */
	public void addChatContact(String title, String firstLine, boolean firstLineMe, String statusInfo, Image img) {
		ChatcontactFx contactFx = new ChatcontactFx(title, firstLine, firstLineMe, statusInfo, img);
		HBox chatElement = contactFx.getLayout();
		chatList.getChildren().add(0, chatElement);
		//load the chat with the other user when clicked
		chatElement.setOnMouseClicked(e -> {
			//check whether the element is already selected or not
			if (selectedChat != null && selectedChat.equals(title))
				return;
			//highlight the selection
			if (selectedContact != null)
				selectedContact.setSelected(false);
			contactFx.setSelected(true);
			selectedContact = contactFx;
			selectedChat = title;
			chatPName.setText(title);
			chatPInfo.setText(statusInfo);
			chatPImg.setImage(img);
			swipeChat(Pos.TOP_CENTER, title);
		});
	}

	private void swipeChat(Pos pos, String uName) {
		if (pos == Pos.TOP_CENTER) {
			Scale scaleCurrent = new Scale(1, 1, 0, 50);
			Timeline yScaleAnim = new Timeline();
			yScaleAnim.setCycleCount(1);
			yScaleAnim.getKeyFrames().addAll(new KeyFrame(Duration.millis(200), //
					new KeyValue(scaleCurrent.xProperty(), 2), //
					new KeyValue(scaleCurrent.yProperty(), 2), new KeyValue(selectedContact.getLayout().opacityProperty(), 0))//
					, new KeyFrame(Duration.millis(100), //
							new KeyValue(chatViewScrollPane.translateYProperty(), -15), //
							new KeyValue(chatViewScrollPane.opacityProperty(), 0)));
			yScaleAnim.setOnFinished(e -> {
				chatView.getChildren().clear();
				scaleCurrent.setX(1);
				scaleCurrent.setY(1);
				selectedContact.getLayout().setOpacity(1);
				Timeline showMsgs = new Timeline(new KeyFrame(Duration.millis(100), //
						new KeyValue(chatViewScrollPane.translateYProperty(), 0),//
						new KeyValue(chatViewScrollPane.opacityProperty(), 1)));
				addMessages(serverCommunication.getMessages(uName, 0));
				chatViewScrollPane.setTranslateY(15);
				showMsgs.play();
			});
			selectedContact.getLayout().getTransforms().add(scaleCurrent);
			yScaleAnim.play();
		}

	}

	/**
	 * Setup the header of the current chat
	 * 
	 * @param name Name of the other user
	 * @param info status info of the other user
	 * @param contactImg profile pic of the other user
	 */
	public void setActiveChatPerson(String name, String info, Image contactImg) {
		chatPImg = new ImageView(contactImg);
		chatPInfo.setText(info);
		chatPName.setText(name);
	}

	/**
	 * Adds messages from an array to the current chat
	 * 
	 * @param messages
	 */
	public void addMessages(MessageFx[] messages) {
		double tmpVV = chatViewScrollPane.getVvalue();
		for (MessageFx messageFx : messages) {
			chatView.getChildren().add(0, messageFx.getPrimaryLayout());
			chatViewScrollPane.widthProperty().addListener(messageFx.getListener());
		}
		//wait a moment for the view to update than scroll to last message
		PauseTransition wait = new PauseTransition(Duration.millis(5));
		wait.setOnFinished(e -> chatViewScrollPane.setVvalue(1));
		wait.play();
		//chatViewScrollPane.setVvalue(tmpVV);

	}

	public void test1(ActionEvent event) {

	}

	public void buttonTest(ActionEvent event) {
		MessageFx[] a = new MessageFx[] { new MessageFx(
				"Lorem ipsum dolor sit amet, consetetur sadipscing elitr, \nsed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.",
				true, new Date(0), chatView.getWidth()),
				new MessageFx("loremipsumdollorsitametloremipsumdollorsitametloremipsumdollorsitametloremipsumdollorsitametloremipsumdollorsitametloremipsumdollorsitametloremipsumdollorsitamet",
						false, new Date(0), chatView.getWidth()) };
		addMessages(a);

	}

	/**
	 * @param cliServComm The class that handles the server communication
	 */
	public void setComm(ClientSideToServer cliServComm) {
		serverCommunication = cliServComm;
	}

	/**
	 * @return The width of the current scroll pane
	 */
	public double getScrollPaneWidth() {
		return chatViewScrollPane.getWidth();
	}

	/**
	 * @return VBox that holds all the messages of the current chat
	 */
	public VBox getChatView() {
		return chatView;
	}

	/**
	 * @return ScrollPane that holds all messages from the current chat
	 */
	public ScrollPane getChatViewScrollPane() {
		return chatViewScrollPane;
	}

}
