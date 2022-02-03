package com.flashpoint.ml.engine.ml;

import ai.djl.Application;
import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author BumKi Cho
 */

@Component
public class ImageDetecter {

    private static final Logger log = LogManager.getLogger(ImageDetecter.class);

    public static ResponseEntity predeict(Path imageFile) throws ModelNotFoundException, MalformedModelException, IOException, TranslateException {

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

    private static ResponseEntity saveBoundingBoxImage(Image img, DetectedObjects detectedObjects) throws IOException {
        Path outputDir = Paths.get("src/main/resources");
        Files.createDirectories(outputDir);

        Image newImage = img.duplicate();
        newImage.drawBoundingBoxes(detectedObjects);

        Path imagePath = outputDir.resolve("detected.png");
        newImage.save(Files.newOutputStream(imagePath), "png");

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath().path("api/img/getpng").toUriString();
        return ResponseEntity.ok(fileDownloadUri);
    }

}
