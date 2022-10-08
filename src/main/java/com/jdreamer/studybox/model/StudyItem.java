package com.jdreamer.studybox.model;

import javafx.beans.property.SimpleStringProperty;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "STUDY_ITEM")
@NamedQuery(
        name = "findAllStudyItems",
        query = "SELECT s FROM StudyItem s"
)
public class StudyItem implements Serializable {
    private Long id;

    private SimpleStringProperty category = new SimpleStringProperty();

    private SimpleStringProperty title = new SimpleStringProperty();

    private SimpleStringProperty mediaLocation = new SimpleStringProperty();

    private SimpleStringProperty localMediaLocation = new SimpleStringProperty();

    private boolean isViewed;

    public StudyItem() {
    }

    public StudyItem(String title) {
        this.title.set(title);
    }

    public StudyItem(String category, String title, String mediaLocation) {
        this.category.set(category);
        this.title.set(title);
        this.mediaLocation.set(mediaLocation);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "CATEGORY", nullable = false)
    public String getCategory() {
        return category.get();
    }

    public void setCategory(String category) {
        this.category.set(category);
    }

    @Column(name = "TITLE", nullable = false)
    public String getTitle() {
        return title.get();
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    @Column(name = "MEDIA_LOCATION", nullable = false)
    public String getMediaLocation() {
        return mediaLocation.get();
    }

    public void setMediaLocation(String mediaLocation) {
        this.mediaLocation.set(mediaLocation);
    }


    @Column(name = "LOCAL_MEDIA_LOCATION", nullable = true)
    public String getLocalMediaLocation() {
        return localMediaLocation.get();
    }

    public void setLocalMediaLocation(String localMediaLocation) {
        this.localMediaLocation.set(localMediaLocation);
    }

    @Column(name = "IS_VIEWED", nullable = false)
    public boolean isViewed() {
        return isViewed;
    }

    public void setViewed(boolean viewed) {
        isViewed = viewed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudyItem studyItem = (StudyItem) o;
        return Objects.equals(category, studyItem.category) &&
                Objects.equals(title, studyItem.title) &&
                Objects.equals(mediaLocation, studyItem.mediaLocation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(category, title, mediaLocation);
    }

    @Override
    public String toString() {
        return title.get();
    }
}
