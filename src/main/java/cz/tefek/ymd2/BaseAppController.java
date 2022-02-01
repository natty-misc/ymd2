package cz.tefek.ymd2;

import java.util.ResourceBundle;

import java.net.URL;

import cz.tefek.ymd2.backend.WorkerBuilder;
import cz.tefek.ymd2.backend.WorkerManager;
import cz.tefek.ymd2.interconnect.progress.RetrieveProgressWatcher;
import cz.tefek.ymd2.gui.DownloaderPane;
import cz.tefek.ymd2.util.InputValidator;

import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class BaseAppController implements Initializable
{
    @FXML
    private Label versionBar;

    @FXML
    private TextField urlInput;

    @FXML
    private VBox jobList;

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        this.versionBar.setText("Version " + AppMain.getAppVersion());

        this.urlInput.textProperty().addListener(this::onEdit);
    }

    private void onEdit(ObservableValue<? extends String> prop, String oldValue, String newValue)
    {
        var videoID = InputValidator.findVideoID(newValue);

        if (videoID != null)
        {
            this.urlInput.setText("");
            this.downloadMP3(videoID);
        }
    }

    @FXML
    public void reportABugOnClick(ActionEvent event)
    {
        var text = "Please send the latest .log file from the `logs/` directory of YMD2 to `Tefek#0493` on Discord.\n\nThanks!";
        var alert = new Alert(AlertType.INFORMATION, text, ButtonType.OK);
        alert.setTitle("Information");
        alert.setWidth(500);
        alert.setHeight(250);
        alert.setHeaderText("Report a bug");
        alert.show();
    }

    @FXML
    public void openSettings(ActionEvent event)
    {
        try
        {
            var stage = new Stage();
            var baseAppPane = AppMain.<AnchorPane>loadFXML("ConfigWindow");
            Scene scene = new Scene(baseAppPane, 800, 700);
            stage.setMinHeight(700);
            stage.setMinWidth(800);
            stage.setResizable(false);
            stage.setTitle("YMD2 settings");
            stage.setScene(scene);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(this.versionBar.getScene().getWindow());
            stage.showAndWait();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void showError(String header, String text)
    {
        var alert = new Alert(AlertType.ERROR, text, ButtonType.OK);
        alert.setTitle(header);
        alert.showAndWait();
    }

    private RetrieveProgressWatcher createTask(String videoID)
    {
        System.out.println("------------------");
        System.out.println("Received task: " + videoID);

        var watcher = new RetrieveProgressWatcher();
        var downloaderPane = new DownloaderPane(watcher);

        this.jobList.getChildren().add(0, downloaderPane);

        return watcher;
    }

    private void downloadMP3(String videoID)
    {
        var workerBuilder = WorkerBuilder.basicMP3Worker();
        WorkerManager.newJob(videoID, this.createTask(videoID), workerBuilder);
    }

    private void downloadMP4(String videoID)
    {
        var workerBuilder = WorkerBuilder.basicMP4Worker();
        WorkerManager.newJob(videoID, this.createTask(videoID), workerBuilder);
    }
}
