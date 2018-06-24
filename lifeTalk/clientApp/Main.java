package lifeTalk.clientApp;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lifeTalk.jsonRW.client.ClientStartupOperations;

/**
 * Starts the application and loads the GUI
 * 
 * @author Arthur H.
 *
 */
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
			primaryStage.setTitle(Info.APPNAME + " - " + Info.VERSION);
			primaryStage.show();
			//If user previously enabled auto connecting to the server then try to connect to the server automatically
			Platform.runLater(() -> {
				if (ClientStartupOperations.isAutoConnectActive()) {
					String srvAdr = ClientStartupOperations.getServerAddr();
					int port = ClientStartupOperations.getServerPort();
					loginController.setInfoText("Auto connecting to server");
					loginController.setMainStage(primaryStage);
					loginController.autoConnect(srvAdr, port);
				}
			});
		} catch (Exception e) {
			if (Boolean.parseBoolean(Info.getArgs()[0]))
				e.printStackTrace();
		}
	}

	/**
	 * start the GUI and save the command line arguments
	 * 
	 * @param args command line arguments </br>
	 * [0]: display exceptions or not (boolean); [1]: update interval in milliseconds
	 */
	public static void main(String[] args) {
		Info.setArgs(args);
		launch(args);
	}
}
