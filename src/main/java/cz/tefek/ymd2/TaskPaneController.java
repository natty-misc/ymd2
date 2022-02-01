package cz.tefek.ymd2;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.awt.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import cz.tefek.pluto.chrono.MiniTime;
import cz.tefek.ymd2.interconnect.progress.ProgressStatus;
import cz.tefek.ymd2.interconnect.progress.RetrieveProgressWatcher;
import cz.tefek.ymd2.util.BinaryUnitUtil;
import cz.tefek.ymd2.util.DesktopUtils;

public class TaskPaneController implements Initializable
{
    private RetrieveProgressWatcher watcher;

    @FXML
    private Label title;

    @FXML
    private Label subtitle;

    @FXML
    private Label stateLabel;

    @FXML
    private Label progressLabel;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Button cancelButton;

    @FXML
    private Button retryButton;

    @FXML
    private Button openFolderButton;

    private SimpleObjectProperty<ProgressStatus> state;

    private SimpleStringProperty errorText;

    private SimpleObjectProperty<List<Path>> outputFiles;

    private SimpleDoubleProperty progress;

    public void setWatcher(RetrieveProgressWatcher watcher)
    {
        this.watcher = watcher;
        this.watcher.addListener(this::update);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        this.state = new SimpleObjectProperty<>();
        this.state.set(ProgressStatus.QUEUED);
        this.title.textFillProperty().bind(Bindings.createObjectBinding(() -> this.state.get().getColor(), this.state));

        this.progress = new SimpleDoubleProperty();

        this.progressBar.visibleProperty().bind(Bindings.createBooleanBinding(this::getProgressBarVisibility, this.state));
        this.progressBar.progressProperty().bind(Bindings.createDoubleBinding(this::getProgress, this.state, this.progress));
        this.progressBar.managedProperty().bind(this.progressBar.visibleProperty());

        this.cancelButton.visibleProperty().bind(Bindings.createBooleanBinding(this::getCancelButtonVisibility, this.state));
        this.cancelButton.managedProperty().bind(this.cancelButton.visibleProperty());

        this.retryButton.visibleProperty().bind(Bindings.equal(this.state, ProgressStatus.FAILED));
        this.retryButton.managedProperty().bind(this.retryButton.visibleProperty());

        this.openFolderButton.visibleProperty().bind(Bindings.equal(this.state, ProgressStatus.SUCCESS).and(new ReadOnlyBooleanWrapper(Desktop.isDesktopSupported())));

        this.openFolderButton.managedProperty().bind(this.openFolderButton.visibleProperty());

        this.errorText = new SimpleStringProperty();

        this.outputFiles = new SimpleObjectProperty<>();

        this.stateLabel.textProperty().bind(Bindings.createStringBinding(this::getStateText, this.state, this.errorText));

        this.progressLabel.visibleProperty().bind(this.progressBar.visibleProperty().and(this.progressBar.indeterminateProperty().not()));
        this.progressLabel.managedProperty().bind(this.progressLabel.visibleProperty());
    }

    @FXML
    public void showInExplorer()
    {
        var files = this.outputFiles.get();

        if (files.isEmpty())
            return;

        var file = files.get(0);

        if (file == null)
            return;

        if (!Files.isRegularFile(file))
        {
            DesktopUtils.showError("Could not show the file", "The file was moved or deleted.");
            return;
        }

        DesktopUtils.showInExplorer(file);
    }

    private boolean getCancelButtonVisibility()
    {
        return switch (this.state.get())
        {
            case CONVERTING_AUDIO, CONVERTING_VIDEO, DOWNLOADING_AUDIO, DOWNLOADING_VIDEO, RETRIEVING_METADATA -> true;
            default -> false;
        };
    }

    private boolean getProgressBarVisibility()
    {
        return switch (this.state.get())
        {
            case CONVERTING_AUDIO, CONVERTING_VIDEO, DOWNLOADING_AUDIO, DOWNLOADING_VIDEO, DELETING_TEMP_FILES, RETRIEVING_METADATA -> true;
            default -> false;
        };
    }

    private double getProgress()
    {
        return switch (this.state.get())
        {
            case DOWNLOADING_AUDIO, DOWNLOADING_VIDEO, CONVERTING_AUDIO, CONVERTING_VIDEO -> this.progress.get();
            default -> ProgressBar.INDETERMINATE_PROGRESS;
        };
    }

    private String getStateText()
    {
        return switch (this.state.get())
        {
            case RETRIEVING_METADATA -> "Loading...";
            case QUEUED -> "Queued";
            case DOWNLOADING_VIDEO -> "Downloading video...";
            case DOWNLOADING_AUDIO -> "Downloading audio...";
            case CONVERTING_AUDIO -> "Converting audio...";
            case CONVERTING_VIDEO -> "Converting video...";
            case DELETING_TEMP_FILES -> "Deleting temporary files...";
            case FAILED -> this.errorText.get();
            case SUCCESS -> ("Done, final file name(s): \n" + this.outputFiles.get().stream().map(Path::toAbsolutePath).map(Path::toString).collect(Collectors.joining("\n")));
        };
    }

    private void update(Observable observable)
    {
        Platform.runLater(() ->
        {
            if (observable instanceof RetrieveProgressWatcher data)
            {
                this.updateComponentData(data);
            }
        });
    }

    private void updateComponentData(RetrieveProgressWatcher data)
    {
        var status = data.getStatus();

        var videoData = data.getVideoData();
        var metadata = videoData == null ? null : videoData.getMetadata();

        var metadataStr = "";

        if (metadata != null)
        {
            var length = metadata.length();

            var lengthStr = MiniTime.formatDiff(length * 1000);

            metadataStr = String.format(Locale.ENGLISH, "by %s • %d views • Video ID: %s • %s", metadata.author(), metadata.views(), data.getVideoID(), lengthStr);

            this.title.setText(metadata.title());
        }
        else
        {
            metadataStr = String.format(Locale.ENGLISH, "Video ID: %s", data.getVideoID());
        }

        this.subtitle.setText(metadataStr);

        this.outputFiles.set(data.getOutputFiles());

        this.state.set(status);

        switch (status)
        {
            case DOWNLOADING_AUDIO, DOWNLOADING_VIDEO -> {
                var downloaded = data.getBytesTransfered();
                var downloadedStr = BinaryUnitUtil.formatAsBinaryBytes(downloaded);
                var fileSize = data.getFileSize();
                var fileSizeStr = BinaryUnitUtil.formatAsBinaryBytes(fileSize);
                var progress = downloaded / (double) fileSize;
                var progressPercentage = progress * 100.0d;
                var downloadingStr = String.format(Locale.ENGLISH, "%s/%s (%.2f%%)", downloadedStr, fileSizeStr, progressPercentage);
                this.progressLabel.setText(downloadingStr);
                this.progress.set(progress);
            }

            case CONVERTING_AUDIO, CONVERTING_VIDEO -> {
                var converted = data.getSecondsConverted();
                var convertedStr = MiniTime.formatDiff(converted * 1000);
                var totalLength = data.getSecondsTotal();
                var totalLengthStr = MiniTime.formatDiff(totalLength * 1000);
                var progress = converted / (double) totalLength;
                var progressPercentage = progress * 100.0d;
                var conversionStr = String.format(Locale.ENGLISH, "%s/%s (%.2f%%)", convertedStr, totalLengthStr, progressPercentage);
                this.progressLabel.setText(conversionStr);
                this.progress.set(progress);
            }

            case FAILED -> {
                this.errorText.set(data.getErrorText());
                this.retryButton.setOnAction(event -> data.restart());
            }
        }
    }

    public void addStateListener(BaseAppController controller)
    {
        controller.addManagedTaskState(this.state);
    }
}
