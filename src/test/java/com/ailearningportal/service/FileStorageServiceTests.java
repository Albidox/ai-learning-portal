package com.ailearningportal.service;

import com.ailearningportal.dto.StoredFile;
import com.ailearningportal.service.impl.FileStorageServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileStorageServiceTests {

    @TempDir
    private Path tempDir;

    @Test
    void storeSubmissionFileShouldAcceptAllowedFileType() {
        FileStorageService service = new FileStorageServiceImpl(tempDir.toString(), "pdf,txt,java");
        MockMultipartFile file = new MockMultipartFile("file", "Answer.java", "text/plain", "class Answer {}".getBytes());

        StoredFile storedFile = service.storeSubmissionFile(file, 10L);

        assertTrue(storedFile.getFilePath().endsWith(".java"));
        assertTrue(Files.exists(Path.of(storedFile.getFilePath())));
    }

    @Test
    void storeSubmissionFileShouldBlockDangerousFileType() {
        FileStorageService service = new FileStorageServiceImpl(tempDir.toString(), "pdf,txt,java");
        MockMultipartFile file = new MockMultipartFile("file", "run.exe", "application/octet-stream", "bad".getBytes());

        assertThrows(IllegalArgumentException.class, () -> service.storeSubmissionFile(file, 10L));
    }
}
