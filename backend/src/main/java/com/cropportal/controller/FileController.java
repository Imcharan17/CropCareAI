package com.cropportal.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.MalformedURLException;
import java.nio.file.Path;

@RestController
@RequestMapping("/files")
public class FileController {
    @Value("${app.upload-dir}")
    private String uploadDir;

    @GetMapping("/{fileName}")
    public ResponseEntity<Resource> file(@PathVariable String fileName) throws MalformedURLException {
        Path root = Path.of(uploadDir).toAbsolutePath().normalize();
        Path path = root.resolve(fileName).normalize();
        if (!path.startsWith(root)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file path");
        }
        return ResponseEntity.ok(new UrlResource(path.toUri()));
    }
}
