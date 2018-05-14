package clientApp.fxPresets;

import java.sql.Time;
import java.util.Date;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class MessageFx {
	private Text content;
	private Text time;
	//private Date date;
	private StackPane primaryLayout;
	private ChangeListener<Number> listener;

	public MessageFx(String contentText, boolean msgByMe, Date dateD, Time timeT, double currentWidth) {
		primaryLayout = new StackPane();
		content = new Text(contentText);
		time = new Text(timeT.toString());

		content.setText(contentText);
		time.setText(timeT.toString());
		//date = date;

		content.setFill(Paint.valueOf("#f4f4f4"));
		time.setFill(Paint.valueOf("#f4f4f4"));

		primaryLayout.setMaxWidth(currentWidth);
		if (content.getBoundsInLocal().getWidth() + 26 > currentWidth)
			content.setWrappingWidth(currentWidth - 26);

		Rectangle box = new Rectangle();
		box.setArcHeight(25);
		box.setArcWidth(25);
		box.setWidth(content.getBoundsInLocal().getWidth() + 26);

		listener = (obsV, oldV, newV) -> {
			if (content.getWrappingWidth() != 0 && box.getWidth() < newV.doubleValue())
				content.setWrappingWidth(0);

			if (content.getBoundsInLocal().getWidth() + 26 > newV.doubleValue())
				content.setWrappingWidth(newV.doubleValue() - 26);
			primaryLayout.maxWidth(newV.doubleValue());
		};

		content.boundsInLocalProperty().addListener((obsV, oldV, newV) -> {
			box.setWidth(newV.getWidth() + 26);

		});
		time.translateXProperty().bind(primaryLayout.widthProperty().subtract(box.widthProperty()).subtract(13).multiply(-1));
		box.setHeight(content.getBoundsInLocal().getHeight() + 52 + 16 + 10);
		StackPane.setAlignment(time, Pos.BOTTOM_RIGHT);
		time.setTranslateY(-13);
		content.setTranslateY(25);
		if (msgByMe) {
			primaryLayout.setAlignment(Pos.TOP_RIGHT);
			content.setTranslateX(-13);
			box.setFill(Paint.valueOf("#727272"));
		} else {
			primaryLayout.setAlignment(Pos.TOP_LEFT);
			content.setTranslateX(13);
			box.setFill(Paint.valueOf("#20ad25"));
		}
		primaryLayout.getChildren().addAll(box, content, time);
	}

	public ChangeListener<Number> getListener() {
		return listener;
	}

	public StackPane getPrimaryLayout() {
		return primaryLayout;
	}

}
