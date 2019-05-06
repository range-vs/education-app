package sample;

import database.ConnectionPostgres;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        if(!ConnectionPostgres.getInstance().init("jdbc:postgresql://127.0.0.1:5432/web-tasks",
                "postgres",
                "lostworld")){
            Platform.exit();
            return;
        }
        FXMLLoader fxmlLoader = new FXMLLoader();
        Pane p = fxmlLoader.load(getClass().getResource("sample.fxml").openStream());
        //primaryStage.getIcons().add(new Image(getClass().getResource(pathIcon).toExternalForm()));
        primaryStage.setTitle("Education Pack");
        primaryStage.setScene(new Scene(p, 600, 275));
        Controller controller = fxmlLoader.getController();
        controller.setPrimaryStage(primaryStage);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
