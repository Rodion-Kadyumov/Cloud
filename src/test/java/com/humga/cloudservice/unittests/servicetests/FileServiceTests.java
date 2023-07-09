package com.humga.cloudservice.unittests.servicetests;

import com.humga.cloudservice.model.entity.File;
import com.humga.cloudservice.repository.FileCrudRepository;
import com.humga.cloudservice.service.FileService;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FileServiceTests {
    @Autowired
    FileService fileService;

    @Autowired
    FileCrudRepository fileRepo;

   private final byte[] testFile = {1,2,3};

    @Test
    @Order(1)
    @Transactional
    void saveFileTest() {
        fileService.saveFile("testfile.txt", testFile, "rodion@email.com");

        File file = fileRepo.findFileByFilenameAndUser_Login("testfile.txt","rodion@email.com")
                .orElse(new File());
        assertNotNull(file);
        assertEquals(file.getFilename(),"testfile.txt");
        assertArrayEquals(file.getFile(), testFile);
    }

    @Test
    @Order(2)
    @Transactional
    void saveFileDuplicateTest() {
        fileService.saveFile("testfile.txt", testFile, "rodion@email.com");

        DataIntegrityViolationException e = assertThrows(DataIntegrityViolationException.class,
                () -> fileService.saveFile("testfile.txt", testFile, "rodion@email.com"));

        assertNotNull(Objects.requireNonNull(e.getMessage()));
        assertTrue(e.getMessage().contains("uk_files"));
    }

    @Test
    @Order(3)
    @Transactional
    void deleteFileTest (){
        fileService.saveFile("testfile.txt", testFile, "rodion@email.com");

        fileService.deleteFile("testfile.txt", "rodion@email.com");

        File file = fileRepo.findFileByFilenameAndUser_Login("testfile.txt","rodion@email.com")
                .orElse(new File());
        assertNotNull(file);
        assertNull(file.getFilename());
        assertArrayEquals(file.getFile(), null);
    }

    @Test
    @Order(4)
    @Transactional
    void deleteFileNotExistingTest (){
        NoSuchElementException e = assertThrows(NoSuchElementException.class,
                () -> fileService.deleteFile("notExisting.txt", "rodion@email.com"));

        assertEquals(e.getMessage(), "File not found.");
    }

    @Test
    @Order(5)
    @Transactional
    void getFileTest (){
        fileService.saveFile("testfile.txt", testFile, "rodion@email.com");

        byte[] result = fileService.getFile("testfile.txt", "rodion@email.com");

        assertNotNull(result);
        assertArrayEquals(result, testFile);
    }

    @Test
    @Order(6)
    @Transactional
    void renameFileTest (){
        fileService.saveFile("testfile.txt", testFile, "rodion@email.com");
        File file = fileRepo.findFileByFilenameAndUser_Login("testfile.txt","rodion@email.com")
                .orElseThrow(() -> new NoSuchElementException("File not found."));
        long id = file.getId();

        fileService.renameFile("testfile.txt", "renamedTestFile.txt", "rodion@email.com");
        file = fileRepo.findFileByFilenameAndUser_Login("renamedTestFile.txt","rodion@email.com")
                .orElseThrow(() -> new NoSuchElementException("File not found."));

        assertNotNull(file);
        assertEquals(file.getId(),id);
    }
    @Test
    @Order(7)
    @Transactional
    void getFilesList() {
        fileService.saveFile("00001", testFile, "rodion@email.com");
        fileService.saveFile("00002", testFile, "rodion@email.com");
        fileService.saveFile("00003", testFile, "rodion@email.com");

        String[] fileNames = {"00001", "00002", "00003"};

        List<File> list = fileService.getFilesList(3, "rodion@email.com");

        assertNotNull(list);
        assertArrayEquals(
                list.stream().map(File::getFilename).toArray(),
                fileNames);
    }

}
