package com.expressservices.storage;

import java.util.Locale;
import java.util.Map;

public final class PhotoTypes {

    private static final Map<String, String> TYPES = Map.of(
            ".jpg", "image/jpeg",
            ".jpeg", "image/jpeg",
            ".png", "image/png",
            ".webp", "image/webp",
            ".gif", "image/gif");

    private PhotoTypes() {
    }

    /** Extension autorisee (avec le point), ".jpg" par defaut. */
    public static String safeExtension(String originalFilename) {
        if (originalFilename != null && originalFilename.contains(".")) {
            String ext = originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase(Locale.ROOT);
            if (TYPES.containsKey(ext)) {
                return ext;
            }
        }
        return ".jpg";
    }

    public static String contentTypeFor(String filename) {
        return TYPES.getOrDefault(filename.substring(filename.lastIndexOf('.')).toLowerCase(Locale.ROOT), "image/jpeg");
    }
}
