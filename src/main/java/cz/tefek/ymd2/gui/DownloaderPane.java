package cz.tefek.ymd2.gui;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import java.awt.Desktop;
import java.util.stream.Collectors;

import cz.tefek.pluto.chrono.MiniTime;
import cz.tefek.ymd2.interconnect.progress.RetrieveProgressWatcher;
import cz.tefek.ymd2.util.BinaryUnitUtil;
import cz.tefek.ymd2.util.DesktopUtils;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class DownloaderPane extends AnchorPane
{
    private final Label title;
    private final Label subtitle;
    private final Label progressLabel;

    private final ProgressBar progressBar;

    private final Button retryButton;

    private final Button openFolderButton;

    public DownloaderPane(RetrieveProgressWatcher progressWatcher)
    {
        var innerVBox = new VBox(5);
        innerVBox.setPadding(new Insets(10, 10, 10, 15));

        this.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(8), new Insets(5, 5, 0, 5))));
        this.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.GRAY, 3, 0, 0, 1));

        this.title = new Label();
        this.title.setFont(new Font("System", 14));
        this.title.setTextFill(Color.BLUEVIOLET);

        this.subtitle = new Label();
        this.subtitle.setFont(new Font("System", 12));
        this.subtitle.setTextFill(Color.GRAY);

        this.progressLabel = new Label();
        this.progressLabel.setFont(new Font("System", 14));
        this.progressLabel.setTextFill(Color.BLACK);

        this.progressBar = new ProgressBar();
        this.progressBar.setPrefWidth(400);



        this.minHeightProperty().bind(this.prefHeightProperty());

        var vBoxChildren = innerVBox.getChildren();

        vBoxChildren.add(this.title);
        vBoxChildren.add(this.subtitle);
        vBoxChildren.add(this.progressLabel);
        vBoxChildren.add(this.progressBar);

        var toolButtonBar = new HBox(5);

        this.retryButton = new Button("Retry");
        this.openFolderButton = new Button("Show in explorer");

        var toolButtons = toolButtonBar.getChildren();
        toolButtons.add(this.retryButton);
        toolButtons.add(this.openFolderButton);

        vBoxChildren.add(toolButtonBar);

        this.getChildren().add(innerVBox);

        progressWatcher.addListener(this::update);
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

        this.title.setTextFill(status.getColor());

        this.retryButton.setVisible(false);
        this.retryButton.setManaged(false);

        this.progressBar.setVisible(false);
        this.progressBar.setManaged(false);

        this.openFolderButton.setVisible(false);
        this.openFolderButton.setManaged(false);

        this.progressLabel.setText("");

        switch (status)
        {
            case RETRIEVING_METADATA -> {
                this.title.setText("Retrieving metadata...");

                this.progressBar.setVisible(true);
                this.progressBar.setManaged(true);
                this.progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
            }

            case QUEUED -> this.progressLabel.setText("Queued...");

            case DOWNLOADING_AUDIO -> {
                var downloaded = data.getBytesTransfered();
                var downloadedStr = BinaryUnitUtil.formatAsBinaryBytes(downloaded);
                var fileSize = data.getFileSize();
                var fileSizeStr = BinaryUnitUtil.formatAsBinaryBytes(fileSize);
                var progress = downloaded / (double) fileSize;
                var progressPercentage = progress * 100.0d;
                var downloadingStr = String.format(Locale.ENGLISH, "Downloading audio... %s/%s (%.2f%%)", downloadedStr, fileSizeStr, progressPercentage);
                this.progressLabel.setText(downloadingStr);
                this.progressBar.setVisible(true);
                this.progressBar.setManaged(true);
                this.progressBar.setProgress(progress);
            }

            case DOWNLOADING_VIDEO -> {
                var downloaded = data.getBytesTransfered();
                var downloadedStr = BinaryUnitUtil.formatAsBinaryBytes(downloaded);
                var fileSize = data.getFileSize();
                var fileSizeStr = BinaryUnitUtil.formatAsBinaryBytes(fileSize);
                var progress = downloaded / (double) fileSize;
                var progressPercentage = progress * 100.0d;
                var downloadingStr = String.format(Locale.ENGLISH, "Downloading video... %s/%s (%.2f%%)", downloadedStr, fileSizeStr, progressPercentage);
                this.progressLabel.setText(downloadingStr);
                this.progressBar.setVisible(true);
                this.progressBar.setManaged(true);
                this.progressBar.setProgress(progress);
            }

            case CONVERTING_AUDIO -> {
                var converted = data.getSecondsConverted();
                var convertedStr = MiniTime.formatDiff(converted * 1000);
                var totalLength = data.getSecondsTotal();
                var totalLengthStr = MiniTime.formatDiff(totalLength * 1000);
                var progress = converted / (double) totalLength;
                var progressPercentage = progress * 100.0d;
                var conversionStr = String.format(Locale.ENGLISH, "Converting audio... %s/%s (%.2f%%)", convertedStr, totalLengthStr, progressPercentage);
                this.progressLabel.setText(conversionStr);
                this.progressBar.setVisible(true);
                this.progressBar.setManaged(true);
                this.progressBar.setProgress(progress);
            }

            case CONVERTING_VIDEO -> {
                var converted = data.getSecondsConverted();
                var convertedStr = MiniTime.formatDiff(converted * 1000);
                var totalLength = data.getSecondsTotal();
                var totalLengthStr = MiniTime.formatDiff(totalLength * 1000);
                var progress = converted / (double) totalLength;
                var progressPercentage = progress * 100.0d;
                var conversionStr = String.format(Locale.ENGLISH, "Converting video... %s/%s (%.2f%%)", convertedStr, totalLengthStr, progressPercentage);
                this.progressLabel.setText(conversionStr);
                this.progressBar.setVisible(true);
                this.progressBar.setManaged(true);
                this.progressBar.setProgress(progress);
            }

            case DELETING_TEMP_FILES -> {
                this.progressLabel.setText("Deleting temporary files...");
                this.progressBar.setVisible(true);
                this.progressBar.setManaged(true);
                this.progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
            }

            case SUCCESS -> {
                this.progressLabel.setText("Done, final file name(s): \n" + data.getOutputFiles().stream().map(Path::toAbsolutePath).map(Path::toString).collect(Collectors.joining()));

                if (Desktop.isDesktopSupported())
                {
                    this.openFolderButton.setVisible(true);
                    this.openFolderButton.setManaged(true);
                    this.openFolderButton.setOnAction(event ->
                    {
                        var files = data.getOutputFiles();

                        if (files.size() == 0)
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
                    });
                }
            }
            case FAILED -> {
                this.progressLabel.setText(data.getErrorText());
                this.retryButton.setOnAction(event -> data.restart());
                this.retryButton.setVisible(true);
                this.retryButton.setManaged(true);
            }
        }
    }
}
