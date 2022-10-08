package com.jdreamer.studybox.dao;

import com.jdreamer.studybox.model.StudyItem;

import javax.persistence.EntityManager;
import java.util.List;


public class StudyItemRepositoryImpl implements StudyItemRepository {
    private final EntityManager entityManager;

    public StudyItemRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<StudyItem> findAll() {
        return entityManager.createNamedQuery("findAllStudyItems").getResultList();
    }

    @Override
    public boolean persist(StudyItem item) {
        try {
            entityManager.persist(item);
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    @Override
    public StudyItem merge(StudyItem item) {
        return entityManager.merge(item);
    }
}
