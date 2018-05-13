package clientApp.fxPresets;

import java.sql.Time;
import java.util.Date;

import javafx.beans.property.ReadOnlyDoubleProperty;
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

	public MessageFx(String contentText, boolean msgByMe, Date dateD, Time timeT, ReadOnlyDoubleProperty maxWidth) {
		primaryLayout = new StackPane();
		content = new Text(contentText);
		time = new Text(timeT.toString());

		content.setText(contentText);
		time.setText(timeT.toString());
		//date = date;

		content.setFill(Paint.valueOf("#f4f4f4"));
		time.setFill(Paint.valueOf("#f4f4f4"));

		Rectangle box = new Rectangle();
		box.setArcHeight(15);
		box.setArcWidth(15);
		box.setWidth(content.getBoundsInLocal().getWidth() + 26);

		if (content.getBoundsInLocal().getWidth() + 26 > maxWidth.doubleValue())
			content.setWrappingWidth(maxWidth.doubleValue());

		maxWidth.addListener((obsV, oldV, newV) -> {
			System.out.println("updated1");
			if (content.getBoundsInLocal().getWidth() + 26 > newV.doubleValue())
				content.setWrappingWidth(newV.doubleValue() - 26);
			else
				content.setWrappingWidth(0);
		});

		content.boundsInLocalProperty().addListener((obsV, oldV, newV) -> {
			System.out.println("updated2");
			if (newV.getWidth() != oldV.getWidth()) {
				box.setWidth(newV.getWidth() + 26);
			}
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
		System.out.println(box.getFill().toString());
		primaryLayout.getChildren().addAll(box, content, time);
	}

	public StackPane getPrimaryLayout() {
		return primaryLayout;
	}

}
