package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class Controller {

    private Stage primaryStage;

    public void openStartTask(ActionEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("task-sample.fxml"));
        Parent root1 = (Parent) fxmlLoader.load();
        Stage newStage = new Stage();
        newStage.setScene(new Scene(root1, Color.TRANSPARENT));
        newStage.initOwner(primaryStage);
        newStage.initModality(Modality.APPLICATION_MODAL);
        TaskController contr = fxmlLoader.getController();
        contr.setThisStage(newStage);
        newStage.showAndWait();
    }

    public void setPrimaryStage(Stage st){
        primaryStage = st;
    }
}
