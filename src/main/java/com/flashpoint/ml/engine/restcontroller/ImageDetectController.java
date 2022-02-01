package com.flashpoint.ml.engine.restcontroller;

import ai.djl.Application;
import ai.djl.ModelException;
import ai.djl.MalformedModelException;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
public class ImageDetectController {

    @PostMapping(value = "/uploadimg", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity diagnose(@RequestParam("file") MultipartFile file) throws IOException, ModelNotFoundException, MalformedModelException, TranslateException {
        byte[] bytes = file.getBytes();
        Path imageFile = Paths.get(file.getOriginalFilename());
        Files.write(imageFile, bytes);
        return predeict(imageFile);
    }

    public ResponseEntity predeict(Path imageFile) throws ModelNotFoundException, MalformedModelException, IOException, TranslateException {

        Image img = null;
        try {
            img = ImageFactory.getInstance().fromFile(imageFile);
        } catch (Exception e) {
            if(Files.exists(imageFile)){
                InputStream is = new FileInputStream(imageFile.toFile());
                //ImageInputStream inputStream = ImageIO.createImageInputStream(is);
                img = ImageFactory.getInstance().fromInputStream(is);
            }
        }

        Criteria criteria  = Criteria.builder()
                .optApplication(Application.CV.OBJECT_DETECTION)
                .setTypes(Image.class, DetectedObjects.class)
                .optFilter("backbone", "resnet50")
                .optProgress(new ProgressBar())
                .build();

        try (ZooModel model = ModelZoo.loadModel(criteria)) {
            try (Predictor predictor = model.newPredictor()) {
                DetectedObjects detectedObjects = (DetectedObjects) predictor.predict(img);
                return saveBoundingBoxImage(img, detectedObjects);
            }
        }
    }

    private ResponseEntity saveBoundingBoxImage(Image img, DetectedObjects detectedObjects) throws IOException {
        Path outputDir = Paths.get("src/main/resources");
        Files.createDirectories(outputDir);

        Image newImage = img.duplicate();
        newImage.drawBoundingBoxes(detectedObjects);

        Path imagePath = outputDir.resolve("detected.png");
        newImage.save(Files.newOutputStream(imagePath), "png");

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath().path("getpng").toUriString();
        return ResponseEntity.ok(fileDownloadUri);
    }

    @GetMapping(value = "/getpng", produces = MediaType.IMAGE_PNG_VALUE)
    public @ResponseBody byte[] getImageWithMediaType() throws IOException {
        InputStream in = new ClassPathResource("detected.png").getInputStream();
        return IOUtils.toByteArray(in);
    }
}
