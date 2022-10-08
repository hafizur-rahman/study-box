package com.jdreamer.studybox.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name="STUDY_ITEM")
@NamedQuery(
        name="findAllStudyItems",
        query="SELECT s FROM StudyItem s"
)
public class StudyItem implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "CATEGORY", nullable = false)
    private String category;

    @Column(name = "TITLE", nullable = false)
    private String title;

    @Column(name = "MEDIA_LOCATION", nullable = false)
    private String mediaLocation;

    @Column(name = "LOCAL_MEDIA_LOCATION", nullable = true)
    private String localMediaLocation;

    @Column(name = "IS_VIEWED", nullable = false)
    private boolean isViewed;

    public StudyItem() {
    }

    public StudyItem(String title) {
        this.title = title;
    }

    public StudyItem(String category, String title, String mediaLocation) {
        this.category = category;
        this.title = title;
        this.mediaLocation = mediaLocation;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMediaLocation() {
        return mediaLocation;
    }

    public void setMediaLocation(String mediaLocation) {
        this.mediaLocation = mediaLocation;
    }

    public String getLocalMediaLocation() {
        return localMediaLocation;
    }

    public void setLocalMediaLocation(String localMediaLocation) {
        this.localMediaLocation = localMediaLocation;
    }

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
        return title;
    }
}
