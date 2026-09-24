package com.expressservices.storage;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.core.checksums.ResponseChecksumValidation;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

@Service
public class PhotoStorageService {

    private static final Logger log = LoggerFactory.getLogger(PhotoStorageService.class);
    private static final String PREFIX = "uploads/profiles/";
    private static final Path LOCAL_DIR = Paths.get("uploads", "profiles");

    private final String accountId;
    private final String accessKeyId;
    private final String secretAccessKey;
    private final String bucket;

    private S3Client s3;

    public PhotoStorageService(
            @Value("${r2.account-id:}") String accountId,
            @Value("${r2.access-key-id:}") String accessKeyId,
            @Value("${r2.secret-access-key:}") String secretAccessKey,
            @Value("${r2.bucket:}") String bucket) {
        this.accountId = accountId;
        this.accessKeyId = accessKeyId;
        this.secretAccessKey = secretAccessKey;
        this.bucket = bucket;
    }

    @PostConstruct
    void init() {
        if (accountId.isBlank() || accessKeyId.isBlank() || secretAccessKey.isBlank() || bucket.isBlank()) {
            log.warn("R2 non configure : les photos de profil sont stockees sur le disque local (non persistant sur Render).");
            return;
        }
        s3 = S3Client.builder()
                .endpointOverride(URI.create("https://" + accountId + ".r2.cloudflarestorage.com"))
                .region(Region.of("auto"))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
                .httpClientBuilder(UrlConnectionHttpClient.builder())
                .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
                .responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED)
                .build();
        log.info("Stockage des photos de profil : Cloudflare R2 (bucket {}).", bucket);
    }

    @PreDestroy
    void close() {
        if (s3 != null) {
            s3.close();
        }
    }

    public void save(String filename, InputStream content, long size, String contentType) throws IOException {
        if (s3 != null) {
            s3.putObject(
                    PutObjectRequest.builder().bucket(bucket).key(PREFIX + filename).contentType(contentType).build(),
                    RequestBody.fromInputStream(content, size));
            return;
        }
        Files.createDirectories(LOCAL_DIR);
        Files.copy(content, LOCAL_DIR.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
    }

    public Optional<InputStream> open(String filename) throws IOException {
        if (s3 != null) {
            try {
                return Optional.of(s3.getObject(GetObjectRequest.builder().bucket(bucket).key(PREFIX + filename).build()));
            } catch (NoSuchKeyException e) {
                // Photo anterieure a la migration vers R2 : repli sur le disque local.
            }
        }
        Path local = LOCAL_DIR.resolve(filename);
        return Files.isRegularFile(local) ? Optional.of(Files.newInputStream(local)) : Optional.empty();
    }
}
