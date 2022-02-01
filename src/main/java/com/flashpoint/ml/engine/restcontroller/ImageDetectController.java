package com.flashpoint.ml.engine.restcontroller;

import ai.djl.MalformedModelException;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.translate.TranslateException;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.flashpoint.ml.engine.ml.ImageDetecter;

/**
 * @author BumKi Cho
 */

@RestController
@RequestMapping(value = "api/img")
public class ImageDetectController {

    @PostMapping(value = "/uploadimg", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity diagnose(@RequestParam("file") MultipartFile file) throws IOException, ModelNotFoundException, MalformedModelException, TranslateException {
        byte[] bytes = file.getBytes();
        Path imageFile = Paths.get(file.getOriginalFilename());
        Files.write(imageFile, bytes);
        return ImageDetecter.predeict(imageFile);
    }

    @GetMapping(value = "/getpng", produces = MediaType.IMAGE_PNG_VALUE)
    public @ResponseBody byte[] getImageWithMediaType() throws IOException {
        InputStream in = new ClassPathResource("detected.png").getInputStream();
        return IOUtils.toByteArray(in);
    }
}
