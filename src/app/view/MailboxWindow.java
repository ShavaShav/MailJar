package app.view;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;

import app.model.MailboxModel;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class MailboxWindow extends Stage {
	
	AnchorPane root;
	MailboxModel mailbox; // model that supplies methods to get info for window
	HBox buttonWindow;
	Button composeBtn;
	VBox mainWindow;
	ScrollPane vScroll;
	
	private double SPACING = 10, PADDING = 10;
	private double TOP_HEIGHT = 180;
	
	public MailboxWindow(MailboxModel mailbox) throws MessagingException, IOException{
		setTitle("Inbox");
		// set up mailbox model
		this.mailbox = mailbox;
		try {
			mailbox.openInbox();
		} catch (Exception e) {
			// TODO Display a message if unable to open inbox
			e.printStackTrace();
		}
		
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
				try {
					mailbox.close();
				} catch (MessagingException e) {
					System.out.println("Unable to close store");
				} finally {
					Platform.exit();
					System.exit(0);	// exit regardless				
				}
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
		
		// display messages
		for (Message message : mailbox.getMessages())
		{
			
			Address[] sender = message.getFrom();
			String stringSender = sender[0].toString();
			Text textSender = new Text();
			textSender.setText(stringSender);
			
			String subject = message.getSubject();
			System.out.println(subject);
			String stringSubject = subject.toString();
			Text textSubject = new Text();
			textSubject.setText(stringSubject);
		
			Date date = message.getSentDate();
			SimpleDateFormat format = new SimpleDateFormat("MMM d");
			String formatDate = format.format(date);
			Text textDate = new Text();
			textDate.setText(formatDate);
			
			HBox messageLine = new HBox();
			messageLine.getStyleClass().add("messageLine"); // css #messageLine class
			messageLine.setOnMouseClicked(new EventHandler<MouseEvent>() {
			    @Override
			    public void handle(MouseEvent mouseEvent) {
			        if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
			            if(mouseEvent.getClickCount() == 2){
			            	try {
								new MessageWindow(message);
							} catch (Exception e) {
								// TODO Print a message if unable to open!
								e.printStackTrace();
							}
			            }
			        }
			    }
			});
			messageLine.setId("messageLine");
			messageLine.setPadding(new Insets(5));
			messageLine.setPrefWidth(1150);
			
			GridPane mgp = new GridPane();
			mgp.setPrefWidth(1140);
			mgp.setHgap(50.00);
			
			mgp.add(textSender, 0, 0);
			mgp.add(textSubject, 1, 0);
			mgp.add(textDate, 2, 0);
			
			
			messageLine.getChildren().add(mgp);
			mainWindow.getChildren().add(messageLine);
		}
		
		//add nodes to scenes
		buttonWindow.getChildren().add(composeBtn);
		root.getChildren().addAll(menuBar, buttonWindow, vScroll);
		root.getStylesheets().add("app/view/MailboxWindowStyles.css");
		
		// if this window is closed, then whole application will close
		this.setOnCloseRequest(new EventHandler<WindowEvent>() {
			public void handle(WindowEvent we) {
				try {
					mailbox.close();
				} catch (MessagingException e) {
					System.out.println("Unable to close store");
				} finally {
					Platform.exit();
					System.exit(0);	// exit regardless				
				}
			  }
	    });       
		
		// set stage and show
		this.setScene(new Scene(root, 1200, 800));
		this.show();
	}

}