package lifeTalk.clientApp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import com.google.gson.Gson;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.util.Duration;
import lifeTalk.clientApp.fxPresets.ChatDateInoFx;
import lifeTalk.clientApp.fxPresets.ChatcontactFx;
import lifeTalk.clientApp.fxPresets.MessageFx;
import lifeTalk.jsonRW.Message;

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
	/** An info dialogue that appears i. e. when an error occurs to notify the user */
	@FXML
	private VBox infoDialogue;
	@FXML
	private ImageView onlineStatusImg;
	/** The class that communicates with the server */
	private ClientSideToServer serverCommunication;
	/** Name of the person the user chats with */
	private String selectedChatContact;
	/** Object of the currently selected chat */
	private ChatcontactFx selectedContact;
	/** TRUE: if the user just clicked on a chat and an animation is still playing */
	private boolean switchingBlocked = false;
	/** List of all contacts/chats */
	private ArrayList<ChatcontactFx> contacts = new ArrayList<>();
	/** Tells which index a chat has in the chats list associated with a username */
	private HashMap<String, Integer> contactsIndex = new HashMap<>();
	private MessageFx previousMsg;
	private MessageFx writingMsg;
	private ChangeListener<String> textInpListener;
	/** the current window */
	private Stage window;
	private Image statusUnknown;
	private Image online;
	private Image offline;

	/**
	 * Call every 0.5 seconds the server communication class and let it check whether
	 * there are any update (like new messages or updated profile infos).
	 */
	public void basicSetup() {
		//updating
		window = (Stage) chatView.getScene().getWindow();
		Task<Void> updater = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				while (true) {
					serverCommunication.update();
					Thread.sleep(Integer.parseInt(Info.getArgs()[1]));
				}
			}
		};
		Thread thread = new Thread(updater);
		thread.start();
		window.setOnCloseRequest(e -> {
			System.out.println("exiting application");
			updater.cancel();
			removeLiveMessage();
		});
		textInpListener = (obsV, oldV, newV) -> {
			try {
				if (!newV.isEmpty() && !oldV.equals(newV)) {
					serverCommunication.setBlocking(true);
					serverCommunication.write("msgPart");
					serverCommunication.write(new Message(msgInp.getText(), System.currentTimeMillis(), nameTitle.getText(), selectedChatContact, false));
					serverCommunication.setBlocking(false);
				} else if (newV.length() == 0 && oldV.length() > 0) {
					serverCommunication.setBlocking(true);
					serverCommunication.write("msgPart");
					serverCommunication.write(selectedChatContact);
					serverCommunication.setBlocking(false);
				}
			} catch (IOException e) {
				e.printStackTrace();
				serverCommunication.setBlocking(false);
			}
		};
		msgInp.textProperty().addListener(textInpListener);

		online = new Image(this.getClass().getResource("resources/online.png").toExternalForm());
		offline = new Image(this.getClass().getResource("resources/offline.png").toExternalForm());
		statusUnknown = onlineStatusImg.getImage();

		((Text) infoDialogue.getChildren().get(0)).wrappingWidthProperty().bind(infoDialogue.widthProperty().subtract(50));
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
				contacts.add(0, contactFx);
			}
		}
		for (ChatcontactFx chatcontactFx : contacts) {
			contactsIndex.put(chatcontactFx.getTitle(), contacts.indexOf(chatcontactFx));
		}
		//load the chat with the other user when clicked
		chatElement.setOnMouseClicked(e -> {
			//check whether the element is already selected or not OR 
			//currently another clicking animation (duration = 250 ms) is currently in progress
			if (switchingBlocked || (selectedChatContact != null && selectedChatContact.equals(title)))
				return;
			switchingBlocked = true;
			msgInp.setText("");
			//highlight the selection
			if (selectedContact != null)
				selectedContact.setSelected(false);
			contactFx.setSelected(true);
			selectedContact = contactFx;
			selectedChatContact = title;
			//setup visuals and play switching animations
			chatPName.setText(title);
			chatPInfo.setText(statusInfo);
			chatPImg.setImage(img);
			onlineStatusImg.setImage(statusUnknown);
			swipeChat(title);
			msgInp.setEditable(true);
			contactFx.clearNewMsgCounter();
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
				addMessagesAtTop(serverCommunication.getMessages(uName, 0));
			} catch (IOException | NullPointerException e1) {
				showInfoDialogue("Error occured while loading messages: " + e1.getMessage());
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
		previousMsg = null;
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
	 * Adds messages from an array to the current chat </br>
	 * Adds new messages at the top. So to add messages the newest has to be the first and
	 * the oldest the last.
	 * 
	 * @param messages
	 */
	public void addMessagesAtTop(MessageFx[] messages) {
		addMessages(0, messages);
	}

	public void addMessageAtBottom(MessageFx message) {
		addMessages(chatView.getChildren().size(), new MessageFx[] { message });
	}

	private void addMessages(int pos, MessageFx[] messages) {
		if (messages.length > 0 && !messages[0].isNormal()) {
			chatView.getChildren().add(messages[0].getPrimaryLayout());
			chatViewScrollPane.widthProperty().addListener(messages[0].getListener());
		} else {
			for (MessageFx msg : messages) {
				if (previousMsg == null) {
					chatView.getChildren().add(pos, msg.getPrimaryLayout());
					previousMsg = msg;
					continue;
				}
				if (olderThan1Day(previousMsg, msg))
					chatView.getChildren().add(pos, new ChatDateInoFx(msg.getDate()).getLayout());
				chatView.getChildren().add(pos, msg.getPrimaryLayout());
				chatViewScrollPane.widthProperty().addListener(msg.getListener());

				previousMsg = msg;
			}

			if (previousMsg != null && chatView.getChildren().size() == 0)
				chatView.getChildren().add(pos, new ChatDateInoFx(previousMsg.getDate()).getLayout());
		}
		//wait a moment for the view to update than scroll to last message
		PauseTransition wait = new PauseTransition(Duration.millis(5));
		wait.setOnFinished(e -> chatViewScrollPane.setVvalue(1));
		wait.play();
	}

	private boolean olderThan1Day(MessageFx prevMsg, MessageFx newMsg) {
		Calendar prevMsgCal = Calendar.getInstance();
		Calendar newMsgCal = Calendar.getInstance();
		prevMsgCal.setTime(prevMsg.getDate());
		newMsgCal.setTime(newMsg.getDate());
		prevMsgCal.get(Calendar.YEAR);
		newMsgCal.get(Calendar.YEAR);
		prevMsgCal.get(Calendar.DAY_OF_YEAR);
		newMsgCal.get(Calendar.DAY_OF_YEAR);
		if (prevMsg.getDate().toString().substring(0, 10).equals(newMsg.getDate().toString().substring(0, 10)))
			return false;
		else if (prevMsgCal.get(Calendar.YEAR) > newMsgCal.get(Calendar.YEAR))
			return true;
		else if (prevMsgCal.get(Calendar.DAY_OF_YEAR) > newMsgCal.get(Calendar.DAY_OF_YEAR))
			return true;

		return false;
	}

	public void msgPart(String contentJson) {
		Message tmpMsgPart = new Gson().fromJson(contentJson, Message.class);
		if (tmpMsgPart.sender.equals(selectedChatContact)) {
			if (writingMsg == null) {
				writingMsg = new MessageFx(chatViewScrollPane.getWidth());
				Platform.runLater(() -> addMessageAtBottom(writingMsg));
			}
			writingMsg.updateWriting(tmpMsgPart.content);
		}
	}

	/**
	 * 
	 * 
	 * @param event
	 */
	public void sendMessage(MouseEvent event) {
		String text = msgInp.getText();
		if (text.isEmpty())
			return;
		serverCommunication.setBlocking(true);
		try {
			serverCommunication.write("sendMsg");
			serverCommunication.write(new Message(text, System.currentTimeMillis(), nameTitle.getText(), selectedChatContact, true));
		} catch (IOException e) {
			showInfoDialogue(e.getMessage());
			e.printStackTrace();
		}
		addMessageAtBottom(new MessageFx(msgInp.getText(), true, new Date(), chatViewScrollPane.getWidth()));
		if (writingMsg != null) {
			chatView.getChildren().remove(writingMsg.getPrimaryLayout());
			chatView.getChildren().add(writingMsg.getPrimaryLayout());
		}
		msgInp.textProperty().removeListener(textInpListener);
		msgInp.clear();
		msgInp.textProperty().addListener(textInpListener);
		serverCommunication.setBlocking(false);
	}

	public void displayMsg(Message msg) {
		Platform.runLater(() -> {
			removeLiveMessage();
			if (msg.sender.equals(selectedChatContact)) {
				addMessageAtBottom(new MessageFx(//
						msg.content, //
						msg.sender.equals(nameTitle.getText()), //
						new Date(msg.date), //
						chatViewScrollPane.getWidth()));
			} else {
				contacts.get(contactsIndex.get(msg.sender)).increaseNewMsgCounter();
			}
		});
	}

	public void setOnlineStatus(String state) {
		onlineStatusImg.setVisible(true);
		switch (state) {
			case "false":
				onlineStatusImg.setImage(offline);
				break;
			case "true":
				onlineStatusImg.setImage(online);
				break;
			default:
				onlineStatusImg.setImage(statusUnknown);
				break;
		}
	}

	public void closeInfoDialogue(ActionEvent event) {
		TranslateTransition hide = new TranslateTransition(Duration.millis(200), infoDialogue);
		hide.setFromY(0);
		hide.setToY(infoDialogue.getHeight());
		hide.play();
	}

	public void showInfoDialogue(String msg) {
		((Text) infoDialogue.getChildren().get(0)).setText(msg);
		TranslateTransition show = new TranslateTransition(Duration.millis(200), infoDialogue);
		show.setFromY(infoDialogue.getHeight());
		show.setToY(0);
		show.play();
	}

	public void test1(ActionEvent event) {
		System.out.println(2);
	}

	public void buttonTest(MouseEvent event) {
		MessageFx mFx = new MessageFx(chatViewScrollPane.getWidth());
		addMessageAtBottom(mFx);
		mFx.updateWriting("byee");
		writingMsg = mFx;
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

	public void removeLiveMessage() {
		if (writingMsg != null) {
			writingMsg.stopAnim();
			Platform.runLater(() -> {
				chatView.getChildren().remove(writingMsg.getPrimaryLayout());
				writingMsg = null;
			});
		}
	}

}
