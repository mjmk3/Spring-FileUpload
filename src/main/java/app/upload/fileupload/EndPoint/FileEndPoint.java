package app.upload.fileupload.EndPoint;

import app.upload.fileupload.Helper.Payload.UploadResponse;
import app.upload.fileupload.Helper.Service.FileStorageService;
import app.upload.fileupload.Model.File;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * @author MJ Makki
 * @version 1.0
 * @license SkyLimits, LLC (<a href="https://www.skylimits.tech">SkyLimits, LLC</a>)
 * @email m.makki@skylimits.tech
 * @since long time ago
 */

@RestController
@RequestMapping("/file")
@CrossOrigin(origins = "*")
public class FileEndPoint {

    private final FileStorageService fileStorageService;

    @Autowired
    public FileEndPoint(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/upload")
    public UploadResponse uploadFile(MultipartFile file, String modelType, Long modelId) {

        File dbFile = fileStorageService.storeFile(file);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/files/")
                .path(modelType)
                .path("/")
                .path(String.valueOf(modelId))
                .path("/")
                .path(dbFile.getId())
                .toUriString();

        return new UploadResponse(dbFile.getFileName(), fileDownloadUri,
                file.getContentType(), file.getSize());
    }

    @PostMapping("/uploads")
    public List<UploadResponse> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files,
                                                    @RequestParam("modelType") String modelType,
                                                    @RequestParam("modelId") Long modelId) {
        List<UploadResponse> uploadResponses = new ArrayList<>();

        for (MultipartFile file : files) {
            UploadResponse uploadResponse = uploadFile(file, modelType, modelId);
            uploadResponses.add(uploadResponse);
        }

        return uploadResponses;
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileId) {
        // Load file from database
        File dbFile = fileStorageService.getFile(fileId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(dbFile.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + dbFile.getFileName() + "\"")
                .body(new ByteArrayResource(dbFile.getFileData()));
    }
}
