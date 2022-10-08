package com.jdreamer.studybox.dao;

import com.jdreamer.studybox.model.StudyItem;

import java.util.List;

public interface StudyItemRepository {
    List<StudyItem> findAll();

    boolean persist(StudyItem item);

    StudyItem merge(StudyItem item);
}
