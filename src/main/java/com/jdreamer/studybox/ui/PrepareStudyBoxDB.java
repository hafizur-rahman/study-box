package com.jdreamer.studybox.ui;

import com.jdreamer.studybox.dao.StudyItemRepository;
import com.jdreamer.studybox.dao.StudyItemRepositoryImpl;
import com.jdreamer.studybox.model.StudyItem;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.apache.commons.io.IOUtils;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class PrepareStudyBoxDB {
    public static void main(String[] args) {
        // Create our entity manager
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("StudyBox");
        EntityManager em = emf.createEntityManager();

        PrepareStudyBoxDB app = new PrepareStudyBoxDB();

        List<StudyItem> studyItems = app.loadStudyItems();

        em.getTransaction().begin();
        for (StudyItem item: studyItems) {
            em.persist(item);
        }
        em.getTransaction().commit();

        StudyItemRepository repo = new StudyItemRepositoryImpl(em);
        List<StudyItem> allItems = repo.findAll();

        // Close the entity manager and associated factory
        em.close();
        emf.close();
    }

    private List<StudyItem> loadStudyItems() {
        List<StudyItem> list = new ArrayList<StudyItem>();

        String studyItemsFile = System.getProperty("study.items.file", "classpath:study-items.csv");
        System.out.println(studyItemsFile);

        Reader filereader = null;
        try {
            if (studyItemsFile.startsWith("classpath:")) {
                studyItemsFile = studyItemsFile.replace("classpath:", "");

                filereader = new InputStreamReader(
                        getClass().getClassLoader().getResourceAsStream(studyItemsFile));
            } else {
                filereader = new InputStreamReader(new FileInputStream(studyItemsFile));
            }

            CSVParser parser = new CSVParserBuilder().withSeparator(',').build();
            CSVReader csvReader = new CSVReaderBuilder(filereader)
                    .withCSVParser(parser)
                    .build();

            List<String[]> allData = csvReader.readAll();

            for (String[] row : allData) {
                StudyItem item = new StudyItem(row[0], row[1], row[2]);
                if (row.length > 3) {
                    item.setLocalMediaLocation(row[3]);
                }
                list.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                IOUtils.close(filereader);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return list;
    }
}
