package clientApp.fxPresets;

import clientApp.CliServComm;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ChatcontactFx {
	private HBox primaryLayout;
	private VBox secondaryLayout;
	private ImageView contactImage;
	private Label nameLabel;
	private Label lastLine;
	private Label statusInfo;
	private boolean lastMsgByMe;
	private Separator divLine;

	public ChatcontactFx(String title, String firstLine, boolean firstLineMe, String statusInfo, Image img) {
		primaryLayout = new HBox(10);
		secondaryLayout = new VBox(7);
		nameLabel = new Label(title);
		lastLine = new Label(firstLine);
		this.statusInfo = new Label("„" + statusInfo + "“");
		contactImage = new ImageView(CliServComm.class.getResource("resources/user.png").toExternalForm());
		lastMsgByMe = firstLineMe;
		divLine = new Separator(Orientation.HORIZONTAL);

		nameLabel.setMaxWidth(300);
		lastLine.setMaxWidth(300);
		this.statusInfo.setMaxWidth(300);
		nameLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
		lastLine.setTextOverrun(OverrunStyle.ELLIPSIS);
		this.statusInfo.setTextOverrun(OverrunStyle.ELLIPSIS);

		nameLabel.setStyle("-fx-font-size: 20px");
		lastLine.setStyle("-fx-font-size: 15px");
		this.statusInfo.setStyle("-fx-font-size: 15px; -fx-font-style: italic;");
		secondaryLayout.getChildren().addAll(nameLabel, lastLine, this.statusInfo);
		primaryLayout.getChildren().addAll(contactImage, secondaryLayout);

		style();
	}

	public void style() {
		contactImage.setFitWidth(67);
		contactImage.setPreserveRatio(true);
		contactImage.setSmooth(true);
		contactImage.setTranslateY((primaryLayout.getBoundsInLocal().getHeight() - contactImage.getFitWidth() / 2) / 2);
		System.out.println(primaryLayout.getBoundsInLocal().getHeight());
		primaryLayout.setPadding(new Insets(8));
		if (lastMsgByMe)
			lastLine.setDisable(true);
		else
			lastLine.setDisable(false);
	}

	public void update(String firstLine, boolean firstLineMe, String statusInfo, Image img) {
		boolean changesDone = false;
		if (!lastLine.getText().equals(firstLine) && lastMsgByMe != firstLineMe) {
			lastLine.setText(firstLine);
			lastMsgByMe = firstLineMe;
			changesDone = true;
		}
		if (!this.statusInfo.getText().equals(statusInfo)) {
			this.statusInfo.setText(statusInfo);
			changesDone = true;
		}

		if (!contactImage.getImage().equals(img)) {
			contactImage.setImage(img);
			changesDone = true;
		}

		if (changesDone)
			style();
	}

	public VBox getLayout() {
		return new VBox(divLine, primaryLayout);
	}
}
