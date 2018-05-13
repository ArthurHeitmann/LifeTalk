package clientApp;

import java.sql.Time;
import java.util.Date;

import clientApp.fxPresets.ChatcontactFx;
import clientApp.fxPresets.MessageFx;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

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

	public void setNameTitle(String name) {

		nameTitle.setText(name);
	}

	public void addChatContact(String title, String firstLine, boolean firstLineMe, String statusInfo, Image img) {
		VBox chatElement = new ChatcontactFx(title, firstLine, firstLineMe, statusInfo, img).getLayout();
		chatList.getChildren().add(0, chatElement);
		chatElement.setOnMouseClicked(e -> {
			//TODO
		});
	}

	public void setActiveChatPerson(String name, String info, Image contactImg) {
		chatPImg = new ImageView(contactImg);
		chatPInfo.setText(info);
		chatPName.setText(name);
	}

	public void addMessages(MessageFx[] messages) {
		for (MessageFx messageFx : messages) {
			chatView.getChildren().add(messageFx.getPrimaryLayout());
			chatView.getChildren().add(new Separator(Orientation.HORIZONTAL));
		}
	}

	public void buttonTest(ActionEvent event) {
		MessageFx[] a = new MessageFx[] {
				new MessageFx("HelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHello\nBye",
						true, new Date(0), new Time(0), chatView.widthProperty()),
				new MessageFx("Hi", false, new Date(0), new Time(0), chatView.widthProperty()) };
		addMessages(a);
	}
}
