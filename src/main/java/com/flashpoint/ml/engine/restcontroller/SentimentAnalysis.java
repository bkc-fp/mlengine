package com.flashpoint.ml.engine.restcontroller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ai.djl.Application;
import ai.djl.Device;
import ai.djl.MalformedModelException;
import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author BumKi Cho
 */
@RestController
@RequestMapping(value = "/api/sent")
public class SentimentAnalysis {
    private static final Logger logger = LoggerFactory.getLogger(SentimentAnalysis.class);

    private SentimentAnalysis() {}

    @GetMapping
    @RequestMapping(value = "/predict")
    public static ResponseEntity<String> sentimentPredict(@RequestBody final String input) throws TranslateException, ModelNotFoundException, MalformedModelException, IOException {
        Classifications classifications = SentimentAnalysis.predict(input);
        logger.info(classifications.toString());
        return new ResponseEntity<>(classifications.toString(), HttpStatus.OK);
    }

    public static Classifications predict(String inputText)
            throws MalformedModelException, ModelNotFoundException, IOException,
            TranslateException {
        String input = inputText; //"I like DJL. DJL is the best DL framework!";
        logger.info("input Sentence: {}", input);

        Criteria<String, Classifications> criteria =
                Criteria.builder()
                        .optApplication(Application.NLP.SENTIMENT_ANALYSIS)
                        .setTypes(String.class, Classifications.class)
                        // This model was traced on CPU and can only run on CPU
                        .optDevice(Device.cpu())
                        .optProgress(new ProgressBar())
                        .build();

        try (ZooModel<String, Classifications> model = criteria.loadModel();
             Predictor<String, Classifications> predictor = model.newPredictor()) {
            return predictor.predict(input);
        }
    }
}
