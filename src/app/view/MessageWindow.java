package app.view;

import javax.mail.Message;

import app.model.MailboxModel;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class MessageWindow extends Stage {
	private final WebView browser; // displays html
    private final WebEngine webEngine; // loads content
	private AnchorPane root;
	private Message message;
	private static final double TOP_OF_BROWSER = 100.0; // pixels from top of window 
	private static final double BOTTOM_OF_BROWSER = 50.0; // pixels from bottom of window
	
	public MessageWindow(Message message) throws Exception{
		setTitle(message.getSubject() + " from " + message.getFrom()[0].toString());
		this.message = message;
		root = new AnchorPane();
		browser = new WebView();
		webEngine = browser.getEngine();
		
		// parse html from message and load it into engine
		String content = MailboxModel.getHTMLFromMessage(message);
		webEngine.loadContent(content);
		
		// put the browser in an hbox
		HBox browserBox = new HBox();
		browserBox.getChildren().add(browser);
		
		// anchor the browser to preset top/bottom margins, 100% width
		AnchorPane.setTopAnchor(browserBox, TOP_OF_BROWSER);
		AnchorPane.setBottomAnchor(browserBox, BOTTOM_OF_BROWSER);
		AnchorPane.setRightAnchor(browserBox, 0.0); // pin to sides
		AnchorPane.setLeftAnchor(browserBox, 0.0);
		
		root.getChildren().add(browserBox);
		// set stage and show
		this.setScene(new Scene(root, 800, 500));
		this.show();
	}

}
