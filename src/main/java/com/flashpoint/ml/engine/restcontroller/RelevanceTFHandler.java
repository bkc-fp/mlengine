package com.flashpoint.ml.engine.restcontroller;

import com.flashpoint.ml.engine.ml.RelevancePredictionFn;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author BumKi Cho
 */
@RestController
@RequestMapping(value = "/api/rel")
public class RelevanceTFHandler {
    private static final Logger logger = LoggerFactory.getLogger(RelevanceTFHandler.class);

    public RelevanceTFHandler() {
    }

    @GetMapping
    @RequestMapping(value = "/predict")
    public static ResponseEntity<String> predictRelevance() {

        PipelineOptions pipelineOptions = PipelineOptionsFactory.create();
        Pipeline pipeline = Pipeline.create(pipelineOptions);
        List<String> output = new ArrayList<String>();

        PCollection<String> strInput = pipeline.apply("Read", TextIO.read().from("src/main/resources/input_rel.txt"));

        PCollection<String> saResult = strInput.apply("Inference", ParDo.of(
                new RelevancePredictionFn()
        ));

        saResult.apply("Write", TextIO.write().to("src/main/resources/output_rel.txt"));

        try {
            pipeline.run().waitUntilFinish();
        } catch (Exception e) {
            logger.info(e.getMessage());
        }

        return new ResponseEntity<>("OK", HttpStatus.OK);
    }
}
