package com.expressservices.storage;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Optional;

@RestController
public class UploadsController {

    private final PhotoStorageService storage;

    public UploadsController(PhotoStorageService storage) {
        this.storage = storage;
    }

    @GetMapping("/uploads/profiles/{filename:[A-Za-z0-9_-]+\\.(?i:jpg|jpeg|png|webp|gif)}")
    public ResponseEntity<InputStreamResource> profilePhoto(@PathVariable String filename) throws IOException {
        Optional<InputStream> content = storage.open(filename);
        if (content.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(PhotoTypes.contentTypeFor(filename)))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic())
                .header("X-Content-Type-Options", "nosniff")
                .body(new InputStreamResource(content.get()));
    }
}
