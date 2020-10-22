package cz.tefek.ymd2;

import java.util.ResourceBundle;

import java.net.URL;

import cz.tefek.ymd2.background.WorkerBuilder;
import cz.tefek.ymd2.background.WorkerManager;
import cz.tefek.ymd2.background.progress.RetrieveProgressWatcher;
import cz.tefek.ymd2.gui.DownloaderPane;
import cz.tefek.ymd2.input.InputValidator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;

public class BaseAppController implements Initializable
{
    @FXML
    private Label versionBar;

    @FXML
    private TextField urlInput;

    @FXML
    private ToggleButton autodownloadButton;

    @FXML
    private Button downloadMP3Button;

    @FXML
    private Button downloadMP4Button;

    @FXML
    private VBox jobList;

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        this.versionBar.setText("Version " + AppMain.getAppVersion());

        this.urlInput.textProperty().addListener((observable, oldValue, newValue) ->
        {
            if (!this.autodownloadButton.isSelected())
            {
                return;
            }

            var videoID = InputValidator.findVideoID(newValue);

            if (videoID != null)
            {
                this.urlInput.setText("");
                this.downloadMP3(videoID);
            }
        });

        this.autodownloadButton.selectedProperty().addListener(value ->
        {
            var videoID = InputValidator.findVideoID(this.urlInput.getText());

            if (videoID != null)
            {
                this.urlInput.setText("");
                this.downloadMP3(videoID);
            }
        });

        this.downloadMP3Button.disableProperty().bind(this.autodownloadButton.selectedProperty());
        this.downloadMP4Button.disableProperty().bind(this.autodownloadButton.selectedProperty());
    }

    @FXML
    public void mp3OnClick(ActionEvent event)
    {
        var videoID = InputValidator.findVideoID(this.urlInput.getText());

        if (videoID != null)
        {
            this.urlInput.setText("");
            this.downloadMP3(videoID);
        }
        else
        {
            this.showError("Error", "Could not find a video ID with that link. Please check again.");
        }
    }

    @FXML
    public void mp4OnClick(ActionEvent event)
    {
        var videoID = InputValidator.findVideoID(this.urlInput.getText());

        if (videoID != null)
        {
            this.urlInput.setText("");
            this.downloadMP4(videoID);
        }
        else
        {
            this.showError("Error", "Could not find a video ID with that link. Please check again.");
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
