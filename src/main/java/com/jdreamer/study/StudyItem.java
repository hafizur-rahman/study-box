package com.jdreamer.study;

import java.util.Objects;

public class StudyItem {
    private String category;
    private String title;
    private String mediaLocation;
    private String localMediaLocation;

    public StudyItem(String category, String title, String mediaLocation) {
        this.category = category;
        this.title = title;
        this.mediaLocation = mediaLocation;
    }

    public String getCategory() {
        return category;
    }

    public String getTitle() {
        return title;
    }

    public String getMediaLocation() {
        return mediaLocation;
    }

    public String getLocalMediaLocation() {
        return localMediaLocation;
    }

    public void setLocalMediaLocation(String localMediaLocation) {
        this.localMediaLocation = localMediaLocation;
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
