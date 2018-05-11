package clientApp;

import clientApp.fxPresets.ChatcontactFx;
import clientApp.fxPresets.MessageFx;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
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
	private ImageView list1LoadingImg;
	@FXML
	private ImageView chatPImg;
	@FXML
	private Label chatPName;
	@FXML
	private Label chatPInfo;
	@FXML
	private VBox chatView;

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
		}
	}

	public void hideLoadingImg1() {
		ScaleTransition transition = new ScaleTransition(Duration.millis(150), list1LoadingImg);
		transition.setFromY(1);
		transition.setToY(0);
		transition.play();
		transition.setOnFinished(e -> list1LoadingImg.setVisible(false));

	}

	public void buttonTest(ActionEvent event) {
		addChatContact("Title1", "last msg", false, "Info", null);
		addChatContact("Title2", "last msg", true, "Info", null);
	}
}
