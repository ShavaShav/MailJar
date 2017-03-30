package app;

import java.util.ArrayList;

import app.view.LoginWindow;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MainApp extends Application {

	public static ArrayList<Image> ICONS;

    @Override
    public void start(Stage primaryStage) {
    	ICONS = new ArrayList<Image>();	
    	ICONS.add(
	    	new Image(
					MainApp.class.getResourceAsStream("/img/logoIcon128.png")));
    	ICONS.add(
	    	new Image(
					MainApp.class.getResourceAsStream("/img/logoIcon64.png")));
    	ICONS.add(
	    	new Image(
					MainApp.class.getResourceAsStream("/img/logoIcon32.png")));
    	ICONS.add(
	    	new Image(
					MainApp.class.getResourceAsStream("/img/logoIcon16.png")));
		
    	new LoginWindow();
    
    }

    public static void main(String[] args) {
        launch(args);
    }
}
