package clientApp.fxPresets;

import java.sql.Time;
import java.util.Date;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;

public class MessageFx {
	//private Date date;
	private HBox primaryLayout;
	private ChangeListener<Number> listener;
	private final double DISTANCE1 = 13;

	public MessageFx(String contentText, boolean msgByMe, Date dateD, Time timeT, double currentWidth) {
		//create all necessary nodes
		primaryLayout = new HBox();
		StackPane msgContainer = new StackPane();
		VBox textContainer = new VBox(7);
		Pane placeholder = new Pane();
		HBox timePositioner = new HBox();
		Pane timerPositionerPlaceholder = new Pane();
		//Rectangle box = new Rectangle();
		Text content = new Text(contentText.replace("\\", "\\\\"));
		Text time = new Text(timeT.toString().substring(0, 5));
		//date = date;

		timePositioner.getChildren().addAll(timerPositionerPlaceholder, time);
		textContainer.getChildren().addAll(content, timePositioner);

		//setup basic visuals
		HBox.setHgrow(placeholder, Priority.ALWAYS);
		HBox.setHgrow(timerPositionerPlaceholder, Priority.ALWAYS);
		textContainer.setPadding(new Insets(DISTANCE1));
		content.setFill(Paint.valueOf("#f4f4f4"));
		time.setFill(Paint.valueOf("#f4f4f4"));
		placeholder.setMinWidth(50);
		/*box.setArcHeight(DISTANCE1*2);
		box.setArcWidth(DISTANCE1*2);*/
		//setup type specific visuals
		if (msgByMe) {
			primaryLayout.getChildren().addAll(placeholder, textContainer);
			textContainer.setStyle("-fx-background-color: #727272; -fx-background-radius: 26px;");
		} else {
			primaryLayout.getChildren().addAll(textContainer, placeholder);
			textContainer.setStyle("-fx-background-color: #20ad25; -fx-background-radius: 26px;");
		}

		//listener for the ScrollPane width property
		listener = (obsV, oldV, newV) -> {
			if (newV.doubleValue() < 350) {
				content.setWrappingWidth(170);
				return;
			}
			primaryLayout.setPrefWidth(newV.doubleValue());
			if (new Text(content.getText()).getBoundsInLocal().getWidth() + 50 + DISTANCE1 * 2 < newV.doubleValue())
				content.setWrappingWidth(0);
			else if (textContainer.getWidth() + 50 > newV.doubleValue())
				content.setWrappingWidth(textContainer.getWidth() - 50 - DISTANCE1 * 2);
			else if (oldV != null && newV.doubleValue() > oldV.doubleValue())
				content.setWrappingWidth(newV.doubleValue() - 50 - DISTANCE1 * 2);
			else if (oldV == null)
				content.setWrappingWidth(newV.doubleValue() - 50 - DISTANCE1 * 2);
		};

		listener.changed(null, null, currentWidth);

	}

	public void fire(double v) {
		listener.changed(null, null, v);
	}

	public ChangeListener<Number> getListener() {
		return listener;
	}

	public HBox getPrimaryLayout() {
		return primaryLayout;
	}

}
