package clientApp;

import java.net.UnknownHostException;

import javax.security.auth.login.LoginException;

import JsonRW.ClientStartupOperations;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class LoginController {
	@FXML
	private TextField srvAdrInput;
	@FXML
	private TextField portInput;
	@FXML
	private TextField uNameInp;
	@FXML
	private TextField pwInp;
	@FXML
	private TextField uNameInpReg;
	@FXML
	private TextField pwInpReg;
	@FXML
	public Text infoText;
	@FXML
	private GridPane connectPane;
	@FXML
	private TabPane loginTabs;
	@FXML
	private GridPane loginGrid;
	@FXML
	private GridPane registerGrid;
	@FXML
	private TextField pwInpConf;
	@FXML
	private CheckBox connectedCheckBox;
	@FXML
	private CheckBox loggedinCheckBox;

	public void autoConnect(String adress, int port) {
		srvAdrInput.setText(adress);
		portInput.setText(Integer.toString(port));
		connectToServer(null);
	}

	public void connectToServer(ActionEvent event) {
		try {
			setInfoText("Connecting");
			String srvAdr = srvAdrInput.getText();
			int port = Integer.parseInt(portInput.getText());
			if (srvAdr == null || srvAdr.isEmpty() || port < 1) {
				setInfoText("Inputs must be not empty!");
				return;
			}
			if (ClientConnection.connectToServer(srvAdr, port)) {
				if (connectedCheckBox.isSelected()) {
					ClientStartupOperations.setServerInfo(srvAdr, port);
				}
				setInfoText("Connected");
				Timeline hideAnimation = new Timeline();
				hideAnimation.getKeyFrames().addAll(new KeyFrame(Duration.ZERO, new KeyValue(connectPane.translateXProperty(), 0)),
						new KeyFrame(Duration.millis(100), new KeyValue(connectPane.translateXProperty(), 15)),
						new KeyFrame(Duration.millis(450), new KeyValue(connectPane.translateXProperty(), -300)));
				hideAnimation.setOnFinished(e -> connectPane.setVisible(false));

				Timeline showAnimation = new Timeline();
				showAnimation.getKeyFrames().addAll((new KeyFrame(Duration.ZERO, new KeyValue(loginTabs.translateXProperty(), -335))),
						new KeyFrame(Duration.millis(400), new KeyValue(loginTabs.translateXProperty(), 15)), new KeyFrame(Duration.millis(475), new KeyValue(loginTabs.translateXProperty(), 0)));
				showAnimation.setDelay(Duration.millis(75));

				hideAnimation.play();
				showAnimation.play();
				setInfoText("");
				ClientConnection.autoLogin();
			} else {
				setInfoText("Couldn't connect to server");
			}
		} catch (IllegalArgumentException e) {
			setInfoText("Invalid port number");
			e.printStackTrace();
		} catch (UnknownHostException e) {
			setInfoText("Invalid Server adress");
			e.printStackTrace();
		} catch (Exception e) {
			setInfoText("An Error occured");
			e.printStackTrace();
		}
	}

	public void login(ActionEvent event) {
		try {
			String uName = uNameInp.getText();
			String pw = pwInp.getText();
			if (uName == null || uName.isEmpty() || pw == null || pw.isEmpty()) {
				setInfoText("Inputs must be not empty!");
				return;
			}
			if (ClientConnection.loginToServer(uNameInp.getText(), pwInp.getText(), "LOGIN", loggedinCheckBox.isSelected())) {
				if (loggedinCheckBox.isSelected()) {
					ClientStartupOperations.setAutoLogin(true, uName, ClientStartupOperations.getloginID());
				}
				//TODO switch to CHATS scene
			} else {
				setInfoText("Couldn't log in, please try again");
			}
		} catch (LoginException e) {
			setInfoText("Server closed the connection");
			try {
				Thread.sleep(500);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			System.exit(0);
		} catch (Exception e) {
			setInfoText("An error occured");
		}

	}

	public void register(ActionEvent event) {
		String uName = uNameInpReg.getText();
		String pw = pwInpReg.getText();
		String pwConf = pwInpConf.getText();
		if (uName == null || uName.isEmpty() || pw == null || pw.isEmpty()) {
			setInfoText("Inputs must be not empty!");
			return;
		} else if (!pw.equals(pwConf)) {
			setInfoText("Password must be the same");
			return;
		}
		try {
			if (ClientConnection.loginToServer(uNameInpReg.getText(), pwInpReg.getText(), "REGISTER", false)) {
				//TODO switch to CHATS scene
			} else {
				setInfoText("Couldn't register, please try again");
			}
		} catch (LoginException e) {
			//Exception can not happen in this method only when logging in
		}
	}

	public void setInfoText(String text) {
		infoText.setText(text);
	}
}
