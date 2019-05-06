package sample;

import database.ConnectionPostgres;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;

public class Controller {

    private Stage primaryStage;
    @FXML
    private VBox vBoxButtons;
    @FXML
    private ImageView imageViewLanguages;

    @FXML
    public void initialize(){
        try {
            createButtons();
            loadImage();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createButtons() throws SQLException {
        Statement st = ConnectionPostgres.getInstance().getConnection().createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM languages;");
        while (rs.next()) {
            Statement st_themes = ConnectionPostgres.getInstance().getConnection().createStatement();
            ResultSet rs_themes = st_themes.executeQuery("SELECT id FROM themes WHERE id_languages = " + rs.getBigDecimal(1) + ";");
            Button btn = new Button(rs.getString(2));
            vBoxButtons.getChildren().add(btn);
            btn.setMnemonicParsing(false);
            VBox.setMargin(btn, new Insets(20, 0, 0, 20));
            btn.setDisable(true);
            if(rs_themes.next()) {
                btn.setOnAction(actionEvent -> openStartTask(actionEvent));
                btn.setDisable(false);
            }
            btn.setStyle("-fx-text-fill: rgb(36, 166, 154)");
            btn.setUserData(rs.getBigDecimal(1));
            rs_themes.close();
            st_themes.close();
        }
        rs.close();
        st.close();
    }

    private void loadImage(){
        imageViewLanguages.setImage(new Image(getClass().getResource("static/logo.png").toExternalForm()));
    }

    public void openStartTask(ActionEvent actionEvent) {
        FXMLLoader fxmlLoaderSecond = new FXMLLoader(getClass().getResource("task-sample.fxml"));
        Parent root = null;
        try {
            root = (Parent) fxmlLoaderSecond.load();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        Stage newStage = new Stage();
        newStage.setScene(new Scene(root, Color.TRANSPARENT));
        newStage.initOwner(primaryStage);
        newStage.initModality(Modality.APPLICATION_MODAL);
        TaskController contr = fxmlLoaderSecond.getController();
        contr.setThisStage(newStage);
        int idLanguage = Integer.parseInt((((Button)actionEvent.getSource()).getUserData()).toString());
        try {
            contr.initButtons(idLanguage);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        newStage.showAndWait();
    }

    public void setPrimaryStage(Stage st){
        primaryStage = st;
    }

}
