package com.mylibrary.util;

import jakarta.servlet.ServletContext;
import java.nio.file.Paths;

/**
 * Resolves the upload directory from multiple configuration sources in priority order:
 *
 * <ol>
 *   <li>Environment variable {@code UPLOAD_DIR}</li>
 *   <li>JVM system property {@code -DuploadDir}</li>
 *   <li>Servlet context-param {@code uploadDir} (web.xml)</li>
 *   <li>Default: {@code <user.home>/mylibrary/uploads}</li>
 * </ol>
 *
 * Use {@link #resolve(ServletContext)} once at servlet init time and cache the result.
 */
public final class UploadConfig {

    /** Context-param name used in web.xml. */
    public static final String CONTEXT_PARAM = "uploadDir";

    private UploadConfig() {}

    /**
     * Resolve the upload directory path.
     *
     * @param ctx the active ServletContext (may be null for unit tests)
     * @return absolute path string, never null, never blank
     */
    public static String resolve(ServletContext ctx) {
        // 1. Environment variable
        String dir = System.getenv("UPLOAD_DIR");
        if (isSet(dir)) return dir.trim();

        // 2. JVM system property
        dir = System.getProperty("uploadDir");
        if (isSet(dir)) return dir.trim();

        // 3. Servlet context-param (web.xml)
        if (ctx != null) {
            dir = ctx.getInitParameter(CONTEXT_PARAM);
            if (isSet(dir)) return dir.trim();
        }

        // 4. Default: <user.home>/mylibrary/uploads
        return Paths.get(System.getProperty("user.home"), "mylibrary", "uploads")
                    .toAbsolutePath().toString();
    }

    private static boolean isSet(String s) {
        return s != null && !s.isBlank();
    }
}
