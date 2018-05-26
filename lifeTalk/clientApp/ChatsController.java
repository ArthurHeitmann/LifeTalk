package lifeTalk.clientApp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
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
	/** Profile picture of the current user */
	@FXML
	private ImageView userProfilePic;
	/** Text field where the user can enter text and send it to the chat */
	@FXML
	private TextField msgInp;
	/** */
	@FXML
	private VBox infoDialogue;
	/** The class that communicates with the server */
	private ClientSideToServer serverCommunication;
	/** Name of the person the user chats with */
	private String selectedChat;
	/** Object of the currently selected chat */
	private ChatcontactFx selectedContact;
	/** TRUE: if the user just clicked on a chat and an animation is still playing */
	private boolean switchingBlocked = false;
	/** */
	private LinkedList<ChatcontactFx> contacts = new LinkedList<>();
	/** */
	private Stage window;

	/**
	 * Call every 0.5 seconds the server communication class and let it check whether
	 * there are any update (like new messages or updated profile infos).
	 */
	public void setUpdateCycle() {
		window = (Stage) chatView.getScene().getWindow();
		Task<Void> updater = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				while (true) {
					serverCommunication.update();
					Thread.sleep(500);
				}
			}
		};
		Thread thread = new Thread(updater);
		thread.start();
		window.setOnCloseRequest(e -> updater.cancel());
	}

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
	public void addChatContact(String title, String firstLine, boolean firstLineMe, String statusInfo, Image img, Date dateTime) {
		ChatcontactFx contactFx = new ChatcontactFx(title, firstLine, firstLineMe, statusInfo, img, dateTime);
		HBox chatElement = contactFx.getLayout();
		if (contacts.size() == 0) {
			chatList.getChildren().add(chatElement);
			contacts.add(contactFx);
		} else {
			boolean added = false;
			//create temporary list to avoid a ConcurrentModificationException
			ArrayList<ChatcontactFx> tmpList = new ArrayList<>(contacts);
			for (ChatcontactFx contact : tmpList) {
				if (!added && contactFx.getDate().after(contact.getDate())) {
					chatList.getChildren().add(contacts.indexOf(contact), chatElement);
					contacts.add(chatList.getChildren().indexOf(chatElement), contactFx);
					added = true;
				}
			}
			if (!added) {
				chatList.getChildren().add(chatElement);
				contacts.addFirst(contactFx);
			}
		}

		//load the chat with the other user when clicked
		chatElement.setOnMouseClicked(e -> {
			//check whether the element is already selected or not OR 
			//currently another clicking animation (duration = 250 ms) is currently in progress
			if (switchingBlocked || (selectedChat != null && selectedChat.equals(title)))
				return;
			switchingBlocked = true;
			msgInp.setText("");
			//highlight the selection
			if (selectedContact != null)
				selectedContact.setSelected(false);
			contactFx.setSelected(true);
			selectedContact = contactFx;
			selectedChat = title;
			//setup visuals and play switching animations
			chatPName.setText(title);
			chatPInfo.setText(statusInfo);
			chatPImg.setImage(img);
			swipeChat(title);
			msgInp.setEditable(true);
		});
	}

	/**
	 * Animates the chat from the list and the previous chat. <br>
	 * chat from the list: enlarge and fade <br>
	 * current chat: swipe up, load new messages, show new messages
	 * 
	 * @param uName
	 */
	private void swipeChat(String uName) {
		Scale scaleCurrent = new Scale(1, 1, 0, 50);
		Timeline scaleAnim = new Timeline();
		//animate chat from list (x and y scale and opacity) and current chat (translateY and opacity)
		scaleAnim.getKeyFrames().addAll(new KeyFrame(Duration.millis(250), //
				new KeyValue(scaleCurrent.xProperty(), 1.5), //
				new KeyValue(scaleCurrent.yProperty(), 1.5), new KeyValue(selectedContact.getLayout().opacityProperty(), 0))//
				, new KeyFrame(Duration.millis(70), //
						new KeyValue(chatViewScrollPane.translateYProperty(), -15), //
						new KeyValue(chatViewScrollPane.opacityProperty(), 0)));
		//once main/hiding animation is finished reset list element to normal and show new messages with animation
		scaleAnim.setOnFinished(e -> {
			chatView.getChildren().clear();
			scaleCurrent.setX(1);
			scaleCurrent.setY(1);
			selectedContact.getLayout().setOpacity(1);
			Timeline showMsgs = new Timeline(new KeyFrame(Duration.millis(70), //
					new KeyValue(chatViewScrollPane.translateYProperty(), 0),//
					new KeyValue(chatViewScrollPane.opacityProperty(), 1)));
			try {
				addMessages(serverCommunication.getMessages(uName, 0));
			} catch (IOException e1) {
				showInfoDialogue("Error occured while loading messages");
				e1.printStackTrace();
			}
			chatViewScrollPane.setTranslateY(15);
			showMsgs.play();
			//allow clicking on new chat elements again
			switchingBlocked = false;
		});
		//apply scale transform to chat panel
		selectedContact.getLayout().getTransforms().add(scaleCurrent);
		//play all the animations
		scaleAnim.play();
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

	/**
	 * 
	 * 
	 * @param event
	 */
	public void sendMessage(ActionEvent event) {

	}

	public void closeInfoDialogue(ActionEvent event) {
		TranslateTransition hide = new TranslateTransition(Duration.millis(200), ((Node) event.getSource()).getParent());
		hide.setFromY(0);
		hide.setToY(130);
		hide.play();
	}

	public void showInfoDialogue(String msg) {
		((Text) infoDialogue.getChildren().get(0)).setText(msg);
		TranslateTransition show = new TranslateTransition(Duration.millis(200), infoDialogue);
		show.setFromY(130);
		show.setToY(0);
	}

	public void test1(ActionEvent event) {
		System.out.println(2);
	}

	public void buttonTest(ActionEvent event) {
		System.out.println("1");

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

	/**
	 * Display to profile image of the current user
	 * 
	 * @param img
	 */
	public void setProfilePic(Image img) {
		userProfilePic.setImage(img);
	}

}
