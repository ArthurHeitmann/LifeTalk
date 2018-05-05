package clientApp;

import JsonRW.ClientStartupOperations;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Login.fxml"));
			AnchorPane root = fxmlLoader.load();
			LoginController loginController = fxmlLoader.getController();
			Scene scene = new Scene(root, 400, 400);
			primaryStage.setWidth(1200);
			primaryStage.setHeight(800);
			primaryStage.setScene(scene);
			primaryStage.setTitle("NatTalk");
			primaryStage.show();
			//If user previously enabled auto connecting to the server than try to connect to the server automatically
			if (ClientStartupOperations.isAutoConnectActive()) {
				try {
					String srvAdr = ClientStartupOperations.getServerAddr();
					int port = ClientStartupOperations.getServerPort();
					loginController.setInfoText("Auto connecting to server");
					loginController.mainStage = primaryStage;
					loginController.autoConnect(srvAdr, port);
				} catch (Exception e) {
					loginController.infoText.setText("Couldn't automatically connect to server");
					ClientStartupOperations.setAutoConnect(false);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
