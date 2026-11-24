package com.mylibrary.servlet;

import com.mylibrary.util.UploadConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Serves cover images stored outside the webroot.
 *
 * <p>URL pattern: {@code /covers/*}</p>
 *
 * <p>The servlet resolves {@code <uploadDir>/covers/<filename>} and streams the file
 * back to the client. Access control is enforced by {@code AuthFilter} in web.xml
 * (remove that filter-mapping if cover images should be publicly accessible).</p>
 *
 * <p>Security measures:</p>
 * <ul>
 *   <li>Path traversal rejected: "..", URL-encoded "%2e%2e", backslashes, absolute paths.</li>
 *   <li>Only jpg, jpeg, png, webp extensions permitted.</li>
 *   <li>Files outside {@code <uploadDir>/covers} are never accessible.</li>
 * </ul>
 */
public class CoverServlet extends HttpServlet {

    /** Buffer size for streaming (8 KB). */
    private static final int BUFFER_SIZE = 8192;

    /** 24-hour browser cache for public cover images. */
    private static final long CACHE_MAX_AGE_SECONDS = 86_400L;

    /** Allowed file extensions → MIME type. */
    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            "jpg",  "image/jpeg",
            "jpeg", "image/jpeg",
            "png",  "image/png",
            "webp", "image/webp"
    );

    private Path coversRoot;

    @Override
    public void init() throws ServletException {
        String uploadDir = UploadConfig.resolve(getServletContext());
        coversRoot = Paths.get(uploadDir, "covers").toAbsolutePath().normalize();
        log("[CoverServlet] Serving covers from: " + coversRoot);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        // 1. Extract the filename from /covers/<name>
        String pathInfo = req.getPathInfo(); // e.g. "/abc123.jpg"
        if (pathInfo == null || pathInfo.equals("/")) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Strip the leading slash; must be a simple filename with no path separators
        String rawName = pathInfo.substring(1); // e.g. "abc123.jpg"

        // 2. Security: reject any traversal characters before resolving the path
        if (isMalicious(rawName)) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // 3. Resolve and normalise, then verify it stays inside coversRoot
        Path resolved = coversRoot.resolve(rawName).normalize();
        if (!resolved.startsWith(coversRoot)) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // 4. Validate extension
        String ext = extension(rawName);
        String mimeType = ALLOWED_TYPES.get(ext);
        if (mimeType == null) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // 5. File must exist
        if (!Files.isRegularFile(resolved)) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // 6. Conditional GET — honor If-Modified-Since
        long lastModifiedMs = Files.getLastModifiedTime(resolved).toMillis();
        long lastModifiedSec = (lastModifiedMs / 1000L) * 1000L; // truncate to seconds
        long ifModifiedSince = req.getDateHeader("If-Modified-Since");
        if (ifModifiedSince != -1 && lastModifiedSec <= ifModifiedSince) {
            res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }

        // 7. Send headers
        long fileSize = Files.size(resolved);
        res.setContentType(mimeType);
        res.setContentLengthLong(fileSize);
        res.setHeader("Cache-Control", "public, max-age=" + CACHE_MAX_AGE_SECONDS);
        res.setDateHeader("Last-Modified", lastModifiedMs);
        res.setHeader("X-Content-Type-Options", "nosniff");

        // 8. Stream file — no full in-memory load
        byte[] buffer = new byte[BUFFER_SIZE];
        try (InputStream in = Files.newInputStream(resolved);
             OutputStream out = res.getOutputStream()) {
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------

    /**
     * Returns true if the raw filename contains any traversal or injection patterns.
     * This is a belt-and-suspenders check on top of {@link Path#normalize()} + prefix check.
     */
    private static boolean isMalicious(String name) {
        if (name == null || name.isBlank()) return true;

        // Contains a path separator (forward slash, backslash) → not a simple filename
        if (name.contains("/") || name.contains("\\")) return true;

        // Dot-dot traversal (plain or URL-encoded)
        String lower = name.toLowerCase();
        if (lower.contains("..")) return true;
        if (lower.contains("%2e")) return true;  // URL-encoded dot: %2e%2e = ".."
        if (lower.contains("%2f")) return true;  // URL-encoded /
        if (lower.contains("%5c")) return true;  // URL-encoded \

        // Absolute-path prefix on non-Windows (starts with /)
        // already prevented by stripping leading slash above, but guard anyway
        if (name.startsWith("/") || name.startsWith("\\")) return true;

        return false;
    }

    /**
     * Returns the lower-case extension of a filename, or empty string if none.
     * E.g. "abc123.JPG" → "jpg"
     */
    private static String extension(String name) {
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) return "";
        return name.substring(dot + 1).toLowerCase();
    }
}
