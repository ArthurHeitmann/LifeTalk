package clientApp;

import java.sql.Date;

import clientApp.fxPresets.ChatcontactFx;
import clientApp.fxPresets.MessageFx;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class ChatsController {
	@FXML
	private VBox chatList;
	@FXML
	private Label nameTitle;
	@FXML
	private ImageView chatPImg;
	@FXML
	private Label chatPName;
	@FXML
	private Label chatPInfo;
	@FXML
	public VBox chatView;
	@FXML
	public ScrollPane chatViewScrollPane;
	private MessageFx msg;
	private CliServComm serverCommunication;

	public void setNameTitle(String name) {

		nameTitle.setText(name);
	}

	public void addChatContact(String title, String firstLine, boolean firstLineMe, String statusInfo, Image img) {
		VBox chatElement = new ChatcontactFx(title, firstLine, firstLineMe, statusInfo, img).getLayout();
		chatList.getChildren().add(0, chatElement);
		chatElement.setOnMouseClicked(e -> {
			//TODO check whether already selected
			chatView.getChildren().clear();
			addMessages(serverCommunication.getMessages(title, 0));
		});
	}

	public void setActiveChatPerson(String name, String info, Image contactImg) {
		chatPImg = new ImageView(contactImg);
		chatPInfo.setText(info);
		chatPName.setText(name);
	}

	public void addMessages(MessageFx[] messages) {
		double tmpVV = chatViewScrollPane.getVvalue();
		msg = messages[0];
		for (MessageFx messageFx : messages) {
			chatView.getChildren().add(0, messageFx.getPrimaryLayout());
			chatViewScrollPane.widthProperty().addListener(messageFx.getListener());
		}
		PauseTransition wait = new PauseTransition(Duration.millis(10));
		wait.setOnFinished(EventHandler -> chatViewScrollPane.setVvalue(1));
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

	public void setComm(CliServComm cliServComm) {
		serverCommunication = cliServComm;
	}

	public double getScrollPaneWidth() {
		return chatViewScrollPane.getWidth();
	}

}
