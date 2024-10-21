package com.example.demo.upload;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/files")
public class FileUploadController {

    private final IFileUploadService iFileUploadService;

    @PostMapping("/upload-files")
    public ResponseEntity<FileResponseMessage> uploadFiles(@RequestParam("file")MultipartFile[] files){
        String message = null;

        try {
            List<String> fileNames = new ArrayList<>();
            Arrays.stream(files).forEach(file -> {
                iFileUploadService.save(file);
                fileNames.add(file.getOriginalFilename());
            });
            message = "Danh sách file được tải lên thành công " + fileNames;
            return ResponseEntity.ok(new FileResponseMessage(message));
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new FileResponseMessage(e.getMessage()));
        }
    }

    @GetMapping("/file/{fileName}")
    public ResponseEntity<Resource> getFileByName(@PathVariable String fileName){
        Resource resource = iFileUploadService.getFileByName(fileName);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; fileName=\"" + resource.getFilename() + "\"").body(resource);
    }

    @GetMapping("/all-files")
    public ResponseEntity<List<FileResponse>> loadAllFiles(){
        List<FileResponse> files = iFileUploadService.loadAllFiles()
                .map(path -> {
                    String fileName = path.getFileName().toString();
                    String url = MvcUriComponentsBuilder
                            .fromMethodName(FileUploadController.class,
                                    "getFileByName",
                                    path.getFileName().toString()).build().toString();
                    return new FileResponse(fileName,url);
                }).toList();
        return ResponseEntity.ok(files);
    }
}
