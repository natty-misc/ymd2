package cz.tefek.ymd2;

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URL;
import java.util.ResourceBundle;

import cz.tefek.ymd2.backend.WorkerBuilder;
import cz.tefek.ymd2.backend.WorkerManager;
import cz.tefek.ymd2.config.Config;
import cz.tefek.ymd2.config.ConfigManager;
import cz.tefek.ymd2.interconnect.progress.ProgressStatus;
import cz.tefek.ymd2.interconnect.progress.RetrieveProgressWatcher;
import cz.tefek.ymd2.util.InputValidator;

public class BaseAppController implements Initializable
{
    @FXML
    private Label versionBar;

    @FXML
    private TextField urlInput;

    @FXML
    private VBox jobList;

    @FXML
    private ComboBox<Config> configSelect;

    @FXML
    private Label queuedCountLabel;

    @FXML
    private Label progressCountLabel;

    @FXML
    private Label finishedCountLabel;

    @FXML
    private Label failedCountLabel;

    private ObservableList<ReadOnlyObjectProperty<ProgressStatus>> statuses;

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        this.statuses = FXCollections.observableArrayList(e -> new Observable[] { e });

        this.versionBar.setText("Version " + AppMain.getAppVersion());

        this.urlInput.textProperty().addListener(this::onEdit);

        var configs = this.configSelect.getItems();
        configs.addAll(ConfigManager.getConfigs());
        this.configSelect.setValue(this.configSelect.getItems().get(0));

        this.queuedCountLabel.textProperty().bind(Bindings.size(this.statuses.filtered(config -> config.get() == ProgressStatus.QUEUED)).asString());

        this.progressCountLabel.textProperty().bind(Bindings.size(this.statuses.filtered(config -> switch (config.get()) {
            case CONVERTING_AUDIO, CONVERTING_VIDEO, DOWNLOADING_AUDIO, DOWNLOADING_VIDEO, DELETING_TEMP_FILES, RETRIEVING_METADATA -> true;
            default -> false;
        })).asString());

        this.finishedCountLabel.textProperty().bind(Bindings.size(this.statuses.filtered(config -> config.get() == ProgressStatus.SUCCESS)).asString());

        this.failedCountLabel.textProperty().bind(Bindings.size(this.statuses.filtered(config -> config.get() == ProgressStatus.FAILED)).asString());
    }

    private void onEdit(ObservableValue<? extends String> prop, String oldValue, String newValue)
    {
        this.tryAddVideo(newValue);
    }

    private void tryAddVideo(String value)
    {
        var videoID = InputValidator.findVideoID(value);

        if (videoID != null)
        {
            this.urlInput.setText("");
            this.download(videoID);
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
    public void addMultipleDialog(ActionEvent event)
    {
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Add multiple videos...");
        window.setWidth(500);
        window.setHeight(300);
        window.initStyle(StageStyle.UTILITY);
        var label = new Label("Please enter video URLs, one per line.");
        label.setPadding(new Insets(5, 5, 5, 5));
        var textArea = new TextArea();
        var root = new BorderPane();
        root.setTop(label);
        root.setCenter(textArea);
        var hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_RIGHT);
        var cancel = new Button("Cancel");
        cancel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(cancel, Priority.ALWAYS);
        cancel.setOnAction(e -> window.close());
        var confirm = new Button("Confirm");
        confirm.setFont(Font.font("Open Sans", FontWeight.BOLD, 12));
        HBox.setHgrow(confirm, Priority.ALWAYS);
        confirm.setMaxWidth(Double.MAX_VALUE);
        confirm.setOnAction(e -> {
            var text = textArea.getText();
            text.lines().forEach(this::tryAddVideo);
            window.close();
        });
        var hBoxChildren = hBox.getChildren();
        hBoxChildren.add(cancel);
        hBoxChildren.add(confirm);
        root.setBottom(hBox);

        window.setScene(new Scene(root, 500, 300));
        window.showAndWait();
    }

    @FXML
    public void openSettings(ActionEvent event)
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

    private RetrieveProgressWatcher createTask(String videoID)
    {
        System.out.println("------------------");
        System.out.println("Received task: " + videoID);

        var watcher = new RetrieveProgressWatcher();

        var downloaderPane = AppMain.<AnchorPane, TaskPaneController>loadFXML("TaskPane", c -> {
            c.setWatcher(watcher);
            c.addStateListener(this);
        });
        this.jobList.getChildren().add(0, downloaderPane);

        return watcher;
    }

    public void addManagedTaskState(ReadOnlyObjectProperty<ProgressStatus> statusObservableValue)
    {
        this.statuses.add(statusObservableValue);
    }

    private void download(String videoID)
    {
        var workerBuilder = WorkerBuilder.fromConfig(this.configSelect.getValue());
        WorkerManager.newJob(videoID, this.createTask(videoID), workerBuilder);
    }
}
