package com.mylibrary.listener;

import com.mylibrary.util.UploadConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Runs once at application startup.
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Resolve and log the upload directory via {@link UploadConfig}.</li>
 *   <li>Create {@code <uploadDir>/covers} and {@code <uploadDir>/books} if they do not exist.</li>
 *   <li>Store the resolved path as a ServletContext attribute ({@code "resolvedUploadDir"})
 *       so servlets can retrieve it without re-resolving each request.</li>
 * </ul>
 */
public class AppStartupListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        String uploadDir = UploadConfig.resolve(ctx);

        // Make the resolved path available as a context attribute
        ctx.setAttribute("resolvedUploadDir", uploadDir);

        ctx.log("[AppStartupListener] Upload directory resolved to: " + uploadDir);

        // Create required subdirectories
        ensureDir(ctx, Paths.get(uploadDir, "covers"));
        ensureDir(ctx, Paths.get(uploadDir, "books"));
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Nothing to tear down
    }

    // ----------------------------------------------------------------
    // Private helpers
    // ----------------------------------------------------------------

    private void ensureDir(ServletContext ctx, Path dir) {
        try {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
                ctx.log("[AppStartupListener] Created directory: " + dir);
            }
        } catch (IOException e) {
            ctx.log("[AppStartupListener] WARNING: could not create " + dir + " — " + e.getMessage());
        }
    }
}
