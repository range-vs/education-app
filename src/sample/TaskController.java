package sample;

import database.ConnectionPostgres;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import task.Task;
import task.TaskComparator;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class TaskController {

    @FXML
    private WebView webViewMain;
    @FXML
    private BorderPane startForm;
    @FXML
    private BorderPane centerForm;
    @FXML
    private FlowPane flowPaneButtons;
    @FXML
    private Button FurtherBtn;
    @FXML
    private ImageView imageViewThemes;

    private Stage _this;
    private WebEngine mainWebEngine;
    private List<Task> tasks;
    private int currentTask;
    private int isLoad;


    @FXML
    public void initialize(){
        mainWebEngine = webViewMain.getEngine();
        initImage();
        tasks = new ArrayList<>();
        currentTask = -1;
    }

    private void initImage(){
        imageViewThemes.setImage(new Image(getClass().getResource("static/logo.png").toExternalForm()));
    }

    public void closeWindow(ActionEvent actionEvent) {
        _this.close();
    }

    public void setThisStage(Stage st){
        _this = st;
    }

    public void initButtons(int idLanguage) throws SQLException {
        Statement st = ConnectionPostgres.getInstance().getConnection().createStatement();
        ResultSet rs = st.executeQuery("SELECT id, name FROM themes WHERE id_languages = " + idLanguage + ";");
        while (rs.next()) {
            Statement st_themes = ConnectionPostgres.getInstance().getConnection().createStatement();
            ResultSet rs_themes = st_themes.executeQuery("SELECT number, task, type FROM tasks WHERE id_theme = " + rs.getBigDecimal(1) + ";");
            Button btn = new Button(rs.getString(2));
            flowPaneButtons.getChildren().add(btn);
            btn.setMnemonicParsing(false);
            FlowPane.setMargin(btn, new Insets(20, 0, 0, 20));
            btn.setDisable(true);
            boolean isDisable = true;
            while(rs_themes.next()) {
                isDisable = false;
                StringBuffer content = new StringBuffer(rs_themes.getString(2));
                Statement st_img = ConnectionPostgres.getInstance().getConnection().createStatement();
                int id = rs_themes.getBigDecimal(1).intValue();
                ResultSet rs_img = st_img.executeQuery("SELECT img, img_path, img_name FROM images WHERE id_task = " + id + ";");
                int index = 1;
                while(rs_img.next()) {
                    String path = rs_img.getString(2);
                    String name = rs_img.getString(3);
                    File catalog = new File(path);
                    if(!catalog.exists()){
                        if(!catalog.mkdirs()){
                            System.out.println("Аварийная ошибка!");
                            return;
                        }
                    }
                    File file = new File(path + name);
                    System.out.println(file.getAbsolutePath()); // debug
                    if(!file.exists()){
                        Image img = new Image(rs_img.getBinaryStream(1));
                        String format = "PNG";
                        File f = new File(path + name);
                        System.out.println(f.getAbsolutePath()); // debug
                        try {
                            ImageIO.write(SwingFXUtils.fromFXImage(img, null), format, f);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        System.out.println(f.toURI()); // debug
                        file = f;
                    }
                    String findLine = "<img id=\"img" + index++ + "\" ";
                    int pos = content.indexOf(findLine);
                    content = content.insert(pos + findLine.length() + 5, file.toURI());
                }
                tasks.add(new Task(rs_themes.getBigDecimal(1).intValue(), content.toString(), rs_themes.getBigDecimal(3).intValue()));
                rs_img.close();
                st_img.close();
            }
            if(!isDisable){
                btn.setOnAction(actionEvent -> openTasks(actionEvent));
                btn.setDisable(false);
            }
            btn.setUserData(rs.getBigDecimal(1));
            rs_themes.close();
            st_themes.close();
        }
        tasks.sort(new TaskComparator());
        currentTask = 0;
        mainWebEngine.setJavaScriptEnabled(true);
        isLoad = -1;
        initListeners();
        rs.close();
        st.close();
    }

    private void initListeners(){
        webViewMain.addEventFilter(ScrollEvent.ANY, new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent scrollEvent) {
                checkScroll();
            }
        });
        webViewMain.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                checkScroll();
            }
        });
        webViewMain.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                checkScroll();
            }
        });
        mainWebEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
            @Override
            public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) {
                if (newValue == Worker.State.SUCCEEDED) {
                    System.out.println("Page is loaded");
                    isLoad = 1;
                    checkScroll();
                }
            }
        });
    }

    public void openTasks(ActionEvent actionEvent) {
        startForm.setVisible(false);
        centerForm.setVisible(true);
        replaceTask(currentTask);
    }

    public void clickBack(ActionEvent actionEvent) {
        if(currentTask - 1 >= 0) {
            currentTask--;
            replaceTask(currentTask);
        }
        else {
            startForm.setVisible(true);
            centerForm.setVisible(false);
        }
    }

    public void clickFurther(ActionEvent actionEvent) {
        if(FurtherBtn.getText().equals("Проверить")){
            if(!checkTask()){ // сначала надо верно ответить на вопрос(-ы)
                return;
            }
        }
        if(currentTask + 1 != tasks.size()) {
            currentTask++;
            replaceTask(currentTask);
        }
    }

    private void replaceTask(int index){
        if(tasks.get(index).getType() == 1){
            FurtherBtn.setText("Далее");
        }else if(tasks.get(index).getType() == 2){
            FurtherBtn.setText("Проверить");
        }
        mainWebEngine.loadContent(tasks.get(index).getTask());
        FurtherBtn.setDisable(true);
    }

    private void checkScroll(){
        /*Set<Node> scrollBars = webViewMain.lookupAll(".scroll-bar");
        for (Node node : scrollBars) {
            if (node instanceof ScrollBar) {
                ScrollBar sBar = (ScrollBar) node;
                if (sBar.getOrientation().equals(Orientation.VERTICAL)) {
                    if (sBar.getMax() - sBar.getValue() < 0.001 || sBar.getVisibleAmount() != 0) {
                        FurtherBtn.setDisable(false);
                    } else {
                        FurtherBtn.setDisable(true);
                    }
                }
            }
        }*/
        while(isLoad != 1){
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Boolean result = (Boolean) mainWebEngine.executeScript("getResult();");
        if(result){
            FurtherBtn.setDisable(false);
        } else {
            FurtherBtn.setDisable(true);
        }
    }

    private boolean checkTask(){
        while(isLoad != 1){
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Boolean result = (Boolean) mainWebEngine.executeScript("check();");
        Alert alert = null;
        if(result){
            alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Вы ввели верный ответ!");
            alert.setHeaderText("Успех");
            System.out.println("Верно");
        } else {
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Вы ввели не верный ответ!");
            alert.setHeaderText("Ошибка");
            System.out.println("Не верно");
        }
        alert.setTitle("Сообщение");
        alert.showAndWait();
        return result;
    }

}
