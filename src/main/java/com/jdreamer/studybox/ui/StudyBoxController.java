package com.jdreamer.studybox.ui;

import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.downloader.request.RequestVideoInfo;
import com.github.kiulian.downloader.downloader.response.Response;
import com.github.kiulian.downloader.model.videos.VideoInfo;
import com.github.kiulian.downloader.model.videos.formats.VideoFormat;
import com.github.kiulian.downloader.model.videos.quality.VideoQuality;
import com.jdreamer.studybox.dao.StudyItemRepository;
import com.jdreamer.studybox.dao.StudyItemRepositoryImpl;
import com.jdreamer.studybox.model.StudyItem;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.apache.commons.io.IOUtils;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;

import static java.util.stream.Collectors.groupingBy;

public class StudyBoxController {
    private static final String YOUTUBE_VIDEO_URL_PREFIX = "https://www.youtube.com/watch?v=";

    @FXML
    private TreeView studyItemsTree;

    @FXML
    private TextField studyItemCategory;

    @FXML
    private TextField studyItemTitle;

    @FXML
    private CheckBox toggleIsViewed;

    @FXML
    private MediaView mediaView;

    @FXML
    private Slider timeSlider;

    @FXML
    private Slider volumeSlider;

    @FXML
    private Label playTime;

    @FXML
    private Button playButton;

    @FXML
    private Button updateStudyItem;

    private MediaPlayer mediaPlayer;

    private List<StudyItem> studyItems;

    private Duration duration;

    private boolean stopRequested = false;
    private boolean atEndOfMedia = false;

    private StudyItem currentStudyItem;

    public StudyBoxController() {
        this.studyItems = loadStudyItems();
    }

    @FXML
    public void initialize() {
        studyItemsTree.setRoot(createNodes(this.studyItems));
        studyItemsTree.addEventHandler(MouseEvent.MOUSE_CLICKED, this::handleMouseClicked);
        studyItemsTree.setShowRoot(false);

        updateStudyItem.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                currentStudyItem.setTitle(studyItemTitle.getText());
                currentStudyItem.setViewed(toggleIsViewed.isSelected());

                saveUpdatedStudyItem(currentStudyItem);
            }
        });

        HBox.setHgrow(timeSlider, Priority.ALWAYS);

        playButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                if (mediaPlayer == null) {
                    return;
                }

                MediaPlayer.Status status = mediaPlayer.getStatus();
                if (status == MediaPlayer.Status.UNKNOWN || status == MediaPlayer.Status.HALTED) {
                    // don't do anything in these states
                    return;
                }

                if (status == MediaPlayer.Status.PAUSED
                        || status == MediaPlayer.Status.READY
                        || status == MediaPlayer.Status.STOPPED) {
                    // rewind the movie if we're sitting at the end
                    if (atEndOfMedia) {
                        mediaPlayer.seek(mediaPlayer.getStartTime());
                        atEndOfMedia = false;
                    }
                    mediaPlayer.play();
                } else {
                    mediaPlayer.pause();
                }
            }
        });

        timeSlider.valueProperty().addListener(new InvalidationListener() {
            public void invalidated(Observable ov) {
                if (timeSlider.isValueChanging() && mediaPlayer != null) {
                    // multiply duration by percentage calculated by slider position
                    mediaPlayer.seek(duration.multiply(timeSlider.getValue() / 100.0));
                }
            }
        });

        volumeSlider.valueProperty().addListener(new InvalidationListener() {
            public void invalidated(Observable ov) {
                if (volumeSlider.isValueChanging() && mediaPlayer != null) {
                    mediaPlayer.setVolume(volumeSlider.getValue() / 100.0);
                }
            }
        });
    }

    private void handleMouseClicked(MouseEvent event) {
        Node node = event.getPickResult().getIntersectedNode();

        // Accept clicks only on node cells, and not on empty spaces of the TreeView
        if (node instanceof Text || (node instanceof TreeCell && ((TreeCell) node).getText() != null)) {
            TreeItem selectedItem = (TreeItem) studyItemsTree.getSelectionModel().getSelectedItem();

            if (selectedItem != null && selectedItem.getValue() instanceof StudyItem) {
                StudyItem studyItem = (StudyItem) selectedItem.getValue();

                if (studyItem.getMediaLocation() != null) {
                    if (currentStudyItem == null || !studyItem.getMediaLocation().equals(currentStudyItem.getMediaLocation())) {
                        currentStudyItem = studyItem;
                    }

                    studyItemCategory.setText(currentStudyItem.getCategory());
                    studyItemTitle.setText(currentStudyItem.getTitle());
                    toggleIsViewed.setSelected(currentStudyItem.isViewed());

                    String videoId = studyItem.getMediaLocation().replace(YOUTUBE_VIDEO_URL_PREFIX, "");

                    // Parse the streaming url
                    YoutubeDownloader downloader = new YoutubeDownloader();

                    // async parsing
                    RequestVideoInfo request = new RequestVideoInfo(videoId).async();
                    Response<VideoInfo> response = downloader.getVideoInfo(request);
                    VideoInfo video = response.data(); // will block thread

                    Optional<VideoFormat> videoFormat = null;
                    if (video.bestVideoFormat().width() >= 720) {
                        videoFormat = video.videoFormats().stream()
                                .filter(format -> format.videoQuality() == VideoQuality.hd720).findFirst();
                    } else {
                        videoFormat = Optional.of(video.bestVideoWithAudioFormat());
                    }
                    if (videoFormat.isPresent()) {
                        String url = videoFormat.get().url();

                        if (mediaPlayer != null) {
                            mediaPlayer.dispose();
                        }

                        mediaPlayer = prepareMediaPlayer(url);
                    }
                }
            }
        }
    }

    private MediaPlayer prepareMediaPlayer(String url) {
        MediaPlayer mediaPlayer = new MediaPlayer(new Media(url));
        mediaView.setMediaPlayer(mediaPlayer);
        mediaPlayer.play();

        mediaPlayer.currentTimeProperty().addListener(new InvalidationListener() {
            public void invalidated(Observable ov) {
                updateValues();
            }
        });
        mediaPlayer.setOnReady(new Runnable() {
            public void run() {
                duration = mediaPlayer.getMedia().getDuration();
                updateValues();
            }
        });

        mediaPlayer.setOnPlaying(new Runnable() {
            public void run() {
                if (stopRequested) {
                    mediaPlayer.pause();
                    stopRequested = false;
                } else {
                    playButton.setText("||");
                }
            }
        });

        mediaPlayer.setOnPaused(new Runnable() {
            public void run() {
                //System.out.println("onPaused");
                playButton.setText(">");
            }
        });

        mediaPlayer.setOnReady(new Runnable() {
            public void run() {
                duration = mediaPlayer.getMedia().getDuration();
                updateValues();
            }
        });

        mediaPlayer.setOnEndOfMedia(new Runnable() {
            public void run() {
                playButton.setText(">");
                stopRequested = true;
            }
        });


        return mediaPlayer;
    }

    protected void updateValues() {
        if (mediaPlayer != null && playTime != null && timeSlider != null && volumeSlider != null) {
            Platform.runLater(new Runnable() {
                public void run() {
                    Duration currentTime = mediaPlayer.getCurrentTime();
                    playTime.setText(formatTime(currentTime, duration));

                    timeSlider.setDisable(duration.isUnknown());
                    if (!timeSlider.isDisabled()
                            && duration.greaterThan(Duration.ZERO)
                            && !timeSlider.isValueChanging()) {
                        timeSlider.setValue(currentTime.divide(duration).toMillis()
                                * 100.0);
                    }

                    if (!volumeSlider.isValueChanging()) {
                        volumeSlider.setValue((int) Math.round(mediaPlayer.getVolume()
                                * 100));
                    }
                }
            });
        }
    }

    private TreeItem<StudyItem> createNodes(List<StudyItem> studyItemList) {
        final Image checkedIcon = new Image(getClass().getClassLoader().getResourceAsStream("checked.png"));
        final Image uncheckedIcon = new Image(getClass().getClassLoader().getResourceAsStream("unchecked.png"));

        TreeItem<StudyItem> top = new TreeItem<StudyItem>(new StudyItem("My Study Items"));

        Map<String, List<StudyItem>> groupedStudyItem = studyItemList.stream().collect(groupingBy(StudyItem::getCategory));
        //System.out.println(groupedStudyItem.keySet());

        ArrayList<String> categories = new ArrayList<String>(groupedStudyItem.keySet());
        Collections.sort(categories);

        for (String key : categories) {
            TreeItem<StudyItem> category = new TreeItem<StudyItem>(new StudyItem(key));
            top.getChildren().add(category);

            for (StudyItem item : groupedStudyItem.get(key)) {
                category.getChildren().add(new TreeItem<StudyItem>(item,
                        new ImageView(item.isViewed() ? checkedIcon : uncheckedIcon)));
            }
        }

        return top;
    }

    private List<StudyItem> loadStudyItems() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("StudyBox");
        EntityManager em = emf.createEntityManager();

        StudyItemRepository repo = new StudyItemRepositoryImpl(em);
        List<StudyItem> studyItems = repo.findAll();

        em.close();
        emf.close();

        return studyItems;
    }

    private StudyItem saveUpdatedStudyItem(StudyItem item) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("StudyBox");
        EntityManager em = emf.createEntityManager();

        StudyItemRepository repo = new StudyItemRepositoryImpl(em);

        em.getTransaction().begin();
        StudyItem result = repo.merge(item);
        em.getTransaction().commit();

        em.close();
        emf.close();

        return result;
    }

    private static String formatTime(Duration elapsed, Duration duration) {
        int intElapsed = (int) Math.floor(elapsed.toSeconds());
        int elapsedHours = intElapsed / (60 * 60);
        if (elapsedHours > 0) {
            intElapsed -= elapsedHours * 60 * 60;
        }
        int elapsedMinutes = intElapsed / 60;
        int elapsedSeconds = intElapsed - elapsedHours * 60 * 60
                - elapsedMinutes * 60;

        if (duration.greaterThan(Duration.ZERO)) {
            int intDuration = (int) Math.floor(duration.toSeconds());
            int durationHours = intDuration / (60 * 60);
            if (durationHours > 0) {
                intDuration -= durationHours * 60 * 60;
            }
            int durationMinutes = intDuration / 60;
            int durationSeconds = intDuration - durationHours * 60 * 60
                    - durationMinutes * 60;
            if (durationHours > 0) {
                return String.format("%d:%02d:%02d/%d:%02d:%02d",
                        elapsedHours, elapsedMinutes, elapsedSeconds,
                        durationHours, durationMinutes, durationSeconds);
            } else {
                return String.format("%02d:%02d/%02d:%02d",
                        elapsedMinutes, elapsedSeconds, durationMinutes,
                        durationSeconds);
            }
        } else {
            if (elapsedHours > 0) {
                return String.format("%d:%02d:%02d", elapsedHours,
                        elapsedMinutes, elapsedSeconds);
            } else {
                return String.format("%02d:%02d", elapsedMinutes,
                        elapsedSeconds);
            }
        }
    }
}