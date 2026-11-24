package com.mylibrary.util;

import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

/**
 * Handles multipart file uploads for cover images and book files.
 * Files are stored OUTSIDE the webapp webroot (uploadDir context param).
 */
public class FileUploadUtil {

    public static final long MAX_COVER_BYTES = 2L * 1024 * 1024;   // 2 MB
    public static final long MAX_BOOK_BYTES  = 20L * 1024 * 1024;  // 20 MB

    private static final Set<String> COVER_MIME = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp");
    private static final Set<String> BOOK_MIME = Set.of(
            "application/pdf", "text/plain");

    private FileUploadUtil() {}

    /**
     * Save a cover image part and return the relative path stored in DB.
     * Returns null if no file was submitted or the part is empty.
     * Throws IllegalArgumentException on validation failure.
     */
    public static String saveCover(Part part, String uploadDir) throws IOException {
        if (part == null || part.getSize() == 0) return null;
        validateMime(part, COVER_MIME, "Cover image must be JPEG, PNG, or WebP.");
        validateSize(part, MAX_COVER_BYTES, "Cover image must not exceed 2 MB.");
        return saveFile(part, uploadDir + File.separator + "covers");
    }

    /**
     * Save a book file (PDF/TXT) and return the relative path stored in DB.
     * Returns null if no file was submitted or the part is empty.
     */
    public static String saveBookFile(Part part, String uploadDir) throws IOException {
        if (part == null || part.getSize() == 0) return null;
        validateMime(part, BOOK_MIME, "Book file must be PDF or TXT.");
        validateSize(part, MAX_BOOK_BYTES, "Book file must not exceed 20 MB.");
        return saveFile(part, uploadDir + File.separator + "books");
    }

    /**
     * Delete a file given its relative path from uploadDir.
     */
    public static void delete(String relativePath, String uploadDir) {
        if (relativePath == null || relativePath.isBlank()) return;
        try {
            Path path = Paths.get(uploadDir, relativePath.replace('/', File.separatorChar));
            Files.deleteIfExists(path);
        } catch (IOException e) {
            System.err.println("[FileUploadUtil] Could not delete " + relativePath + ": " + e.getMessage());
        }
    }

    /** Extract the submitted filename from a Part's Content-Disposition header. */
    public static String getSubmittedFileName(Part part) {
        if (part == null) return null;
        for (String cd : part.getHeader("Content-Disposition").split(";")) {
            if (cd.trim().startsWith("filename")) {
                String name = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
                // IE sends full path — keep only the filename
                int lastSlash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
                return lastSlash >= 0 ? name.substring(lastSlash + 1) : name;
            }
        }
        return null;
    }

    // ----------------------------------------------------------------
    // Private helpers
    // ----------------------------------------------------------------

    private static String saveFile(Part part, String targetDir) throws IOException {
        Files.createDirectories(Paths.get(targetDir));
        String originalName = getSubmittedFileName(part);
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf('.'));
        }
        String storedName = UUID.randomUUID() + extension;
        Path destination = Paths.get(targetDir, storedName);
        try (InputStream in = part.getInputStream()) {
            Files.copy(in, destination, StandardCopyOption.REPLACE_EXISTING);
        }
        // Return relative path from uploadDir root (e.g. "covers/uuid.jpg")
        String subdir = Paths.get(targetDir).getFileName().toString();
        return subdir + "/" + storedName;
    }

    private static void validateMime(Part part, Set<String> allowed, String message) {
        String mime = part.getContentType();
        if (mime == null || !allowed.contains(mime.toLowerCase().split(";")[0].trim())) {
            throw new IllegalArgumentException(message);
        }
    }

    private static void validateSize(Part part, long maxBytes, String message) {
        if (part.getSize() > maxBytes) {
            throw new IllegalArgumentException(message);
        }
    }
}
