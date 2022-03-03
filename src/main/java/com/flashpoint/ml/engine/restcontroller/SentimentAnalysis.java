package com.flashpoint.ml.engine.restcontroller;

import ai.djl.Application;
import ai.djl.repository.Artifact;
import ai.djl.repository.zoo.ModelZoo;
import com.flashpoint.ml.engine.ml.SentimentAnalysisFn;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ai.djl.MalformedModelException;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.translate.TranslateException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

        PipelineOptions pipelineOptions = PipelineOptionsFactory.create();
        Pipeline pipeline = Pipeline.create(pipelineOptions);
        List<String> output = new ArrayList<String>();

        PCollection<String> strInput = pipeline.apply("Read", TextIO.read().from("src/main/resources/input.txt"));

        PCollection<String> saResult = strInput.apply("Inference", ParDo.of(
            new SentimentAnalysisFn()
        ));

        saResult.apply("Write", TextIO.write().to("src/main/resources/output.txt"));

        try {
            pipeline.run().waitUntilFinish();
        } catch (Exception e) {
            logger.info(e.getMessage());
        }

        Map<Application, List<Artifact>> allModels = ModelZoo.listModels();

        for (Map.Entry<Application, List<Artifact>> entry : allModels.entrySet()) {
            List<Artifact> entryValues = entry.getValue();
            for (Artifact artifact : entryValues) {
                System.out.println(entry.getKey() + ":" + artifact.getName());
            }
        }

        return new ResponseEntity<>("OK", HttpStatus.OK);
    }

}
