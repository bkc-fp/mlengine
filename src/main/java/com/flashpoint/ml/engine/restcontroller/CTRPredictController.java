package com.flashpoint.ml.engine.restcontroller;

import com.flashpoint.ml.engine.ml.CTRPredictor;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.Pipeline;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * @author BumKi Cho
 */

@RestController
@RequestMapping(value = "/api/ctr")
public class CTRPredictController {

    @PostMapping
    @RequestMapping(value = "/predict")
    public ResponseEntity<String> predictCTR(@RequestParam("csv-file") MultipartFile file, @RequestParam("output") String output) {

        String args = "--inputFile="+file.getOriginalFilename();

        CTRPredictor.CtrOptions options =
                PipelineOptionsFactory.fromArgs(args).withValidation().as(CTRPredictor.CtrOptions.class);

        Pipeline p = Pipeline.create(options);
        PCollection<String> records = p.apply("ReadData", TextIO.read().from(options.getInputFile()));

        // add unique id to each record
        PCollection<String> addIds = records.apply("AddUUID", ParDo.of(new DoFn<String, String>() {
            @ProcessElement
            public void processElement(ProcessContext c) {
                String uniqueID = UUID.randomUUID().toString();
                c.output(uniqueID + "\t" + c.element());
            }
        }));

        // convert categorical features to integer according to feature map used in training
        PCollection<String> preprocess = addIds.apply("Preprocess", ParDo.of(new CTRPredictor.FeatureMap()));

        // run inference using Deep Java Library
        PCollection<String> ctr = preprocess.apply("Inference", ParDo.of(new CTRPredictor.Inference()));

        ctr.apply(TextIO.write().to(options.getOutput()));
        p.run().waitUntilFinish();

        return new ResponseEntity<>("completed", HttpStatus.OK);
    }
}
