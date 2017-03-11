package app.view;

import javax.mail.Message;

import app.model.MailboxModel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class MailboxWindow extends Stage implements EventHandler<ActionEvent>{
	
	AnchorPane root;
	MailboxModel mailbox; // model that supplies methods to get info for window
	HBox buttonWindow;
	Button composeBtn;
	VBox mainWindow;
	ScrollPane vScroll;
	
	private double SPACING = 10, PADDING = 10;
	private double TOP_HEIGHT = 180;
	
	public MailboxWindow(MailboxModel mailbox){
		
		this.mailbox = mailbox; // store model
		
		//initialize variables
		root = new AnchorPane();
		buttonWindow = new HBox();
		buttonWindow.setAlignment(Pos.BOTTOM_LEFT);
		composeBtn = new Button ("Compose New Message");
		composeBtn.setPrefSize(180.0, 40.0);
		composeBtn.setOnAction(new EventHandler<ActionEvent>() {
		    public void handle(ActionEvent t) {
		    	new ComposeMailWindow();
		    }
		});
		mainWindow = new VBox();
		mainWindow.setId("mainWindow");
		vScroll = new ScrollPane();
		vScroll.setId("vScroll");
		vScroll.setContent(mainWindow);
		vScroll.setVbarPolicy(ScrollBarPolicy.ALWAYS);
		
		//create menu
		final Menu fileMenu = new Menu("File");
		MenuBar menuBar = new MenuBar();
		menuBar.getMenus().add(fileMenu);
		MenuItem exit = new MenuItem("Exit");
		exit.setOnAction(new EventHandler<ActionEvent>() {
		    public void handle(ActionEvent t) {
		        System.exit(0);
		    }
		});
		fileMenu.getItems().add(exit);
		
		//sizing
		buttonWindow.setPrefHeight(160);
		buttonWindow.setPrefWidth(1180);
		mainWindow.setPrefHeight(580);
		mainWindow.setPrefWidth(1140);
		buttonWindow.setPadding(new Insets(PADDING));
		vScroll.setPrefWidth(1180);
		vScroll.setPrefHeight(580);
		vScroll.setPadding(new Insets(PADDING));
		
		//anchoring
		AnchorPane.setLeftAnchor(buttonWindow, 10.0);
		AnchorPane.setTopAnchor(buttonWindow, 30.0);
		AnchorPane.setRightAnchor(buttonWindow, 10.0);
		
		AnchorPane.setTopAnchor(vScroll, TOP_HEIGHT + 10.0);
		AnchorPane.setBottomAnchor(vScroll, 10.0);
		AnchorPane.setRightAnchor(vScroll, 10.0);
		AnchorPane.setLeftAnchor(vScroll, 10.0);
		
		
		
		for (int i=0; i<30; i++)
		{
			Text sender = new Text();
			sender.setText("sender");
		
			Text message = new Text();
			message.setText("Message will be displayed here");
		
			Text date = new Text();
			date.setText("March 1");
			
			HBox messageLine = new HBox();
			messageLine.setId("messageLine");
			messageLine.setPadding(new Insets(5));
			messageLine.setPrefWidth(1150);
			
			GridPane mgp = new GridPane();
			mgp.setPrefWidth(1140);
			mgp.setHgap(50.00);
			GridPane.setHalignment(date, HPos.RIGHT);
			GridPane.setHalignment(sender, HPos.LEFT);
			GridPane.setHalignment(message, HPos.CENTER);
			
			mgp.add(sender, 0, 0);
			mgp.add(message, 1, 0);
			mgp.add(date, 2, 0);
			
			messageLine.getChildren().add(mgp);
			mainWindow.getChildren().add(messageLine);
		}
		
		//add nodes to scenes
		buttonWindow.getChildren().add(composeBtn);
		root.getChildren().addAll(menuBar, buttonWindow, vScroll);
		root.getStylesheets().add("app/view/MailboxWindowStyles.css");
		
		// set stage and show
		this.setScene(new Scene(root, 1200, 800));
		this.show();
	}

	@Override
	public void handle(ActionEvent event) {
		// TODO Auto-generated method stub
		
	}

}
