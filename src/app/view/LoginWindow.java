package app.view;

import app.model.MailboxModel;
import app.model.SMTPModel;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class LoginWindow extends Stage implements EventHandler<ActionEvent> {
	AnchorPane root;
	VBox mainPart;
	HBox row1, row2;
	Button loginBtn;
	Text titleText, promptText;
	Label userLabel, passwordLabel, atLabel;
	TextField userField;
	PasswordField passwordField;
	ComboBox<String> hostBox;
	
	String host;
	private double SPACING = 10, PADDING = 10;
	
	public LoginWindow(){
		setTitle("Sign-in");
		root = new AnchorPane();
		
		mainPart = new VBox();
		// initialize variables
		Image logo = new Image("img/logo.png");
		ImageView logoView = new ImageView();
		logoView.setImage(logo);
		logoView.setFitWidth(100);
		logoView.setPreserveRatio(true);
		logoView.setSmooth(true);
		logoView.setCache(true);
		Font titleFont = new Font("Verdana", 90);
		titleText = new Text("MailJar");
		titleText.setFont(titleFont);
		userLabel = new Label("Email: ");
		atLabel = new Label("@");
		passwordLabel = new Label("Password: ");
		userField = new TextField("mailjar.mgmt");
		userField.setPrefWidth(200.0);
		passwordField = new PasswordField();
		passwordField.setText("agileasFUCK");
		passwordField.setPrefWidth(200.0);
		promptText = new Text();
		promptText.setWrappingWidth(390.00);
		promptText.setId("prompt");
		loginBtn = new Button("Sign-in");
		loginBtn.setPrefSize(80.0, 40.0);
		loginBtn.getStyleClass().add("buttonClass");
		hostBox = new ComboBox<String>();
		hostBox.getItems().addAll("gmail.com", "hotmail.com");

		// add action handlers	       
		loginBtn.setOnAction(this); // set to LoginWindow's action handler
		hostBox.valueProperty().addListener(new ChangeListener<String>(){
			@Override
			public void changed(ObservableValue<? extends String> comboBox, String lastSelection, String currentSelection) {
				host = currentSelection;
			}	
		});
		hostBox.getSelectionModel().selectFirst(); // set to first host by default


		// add nodes to scene
		// title
		row1 = new HBox();
		row1.setAlignment(Pos.CENTER);
		row1.getChildren().addAll(logoView, titleText);

		// Username/password entry
		row2 = new HBox(); 
		GridPane gp = new GridPane();
		gp.setPadding(new Insets(PADDING));
		gp.setPrefSize(500, 150);
		gp.setVgap(SPACING);
		gp.setHgap(SPACING);
		// first line
		gp.add(userLabel, 0, 0);
		GridPane.setHalignment(userLabel, HPos.RIGHT);
		gp.add(userField, 1, 0);
		gp.add(atLabel, 2, 0);
		gp.add(hostBox, 3, 0);
		// second line
		gp.add(passwordLabel, 0, 1);
		GridPane.setHalignment(passwordLabel, HPos.RIGHT);
		gp.add(passwordField, 1, 1);	       
		row2.getChildren().add(gp);

		// sizing	       
		row1.setPrefHeight(250);
		row2.setPrefHeight(55);
		mainPart.setPadding(new Insets(PADDING));
		mainPart.getChildren().addAll(row1, row2);

		// anchoring
		AnchorPane.setLeftAnchor(mainPart, 0.0);
		AnchorPane.setTopAnchor(mainPart, 0.0);
		AnchorPane.setRightAnchor(loginBtn, 20.0);
		AnchorPane.setBottomAnchor(loginBtn, 20.0);
		AnchorPane.setBottomAnchor(promptText, 10.0);
		AnchorPane.setLeftAnchor(promptText, 10.0);	

		// add nodes to root scene
		root.getChildren().addAll(mainPart, loginBtn, promptText);
		// import css
		root.getStylesheets().add("app/view/common.css");
		root.getStylesheets().add("app/view/LoginWindowStyles.css");
		this.setScene(new Scene(root, 500, 400));
		this.setResizable(false);
		this.show();
}

	// handle login
	@Override
	public void handle(ActionEvent arg0) {
		String email = userField.getText() + '@' + host;
		String password = passwordField.getText();
		// try to login
		try {
			SMTPModel smtp = new SMTPModel(email, password);
			MailboxModel mailbox = new MailboxModel(email, password);
			// if successful, create main window and close this one
			new MailboxWindow(mailbox, smtp);
			this.close();
		} catch (Exception e) {
			// show user error message so they can correct
			e.printStackTrace();
			promptText.setText(e.getMessage());
		} 
	}
}

