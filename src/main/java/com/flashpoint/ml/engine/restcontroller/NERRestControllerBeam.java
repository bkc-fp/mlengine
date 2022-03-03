package com.flashpoint.ml.engine.restcontroller;

import com.flashpoint.ml.engine.dataservice.CityService;
import com.flashpoint.ml.engine.ml.FlashpointCoreNLP;
import com.flashpoint.ml.engine.ml.NERTrainer;
import com.flashpoint.ml.engine.ml.NERType;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static edu.stanford.nlp.ling.CoreLabel.OutputFormat.VALUE_TAG_NER;

/**
 * @author BumKi Cho
 */

@RestController
@RequestMapping(value = "/api/nerbeam")
public class NERRestControllerBeam {

    private static final Logger log = LogManager.getLogger(NERRestControllerBeam.class);

    @Autowired
    private StanfordCoreNLP stanfordCoreNLP;

    @PostMapping
    @RequestMapping(value = "/coreNer")
    public ResponseEntity<String> coreNer(@RequestBody final String input, @RequestParam final NERType type) {

        List<CoreLabel> outputList = new ArrayList<CoreLabel>();

        PipelineOptions pipelineOptions = PipelineOptionsFactory.create();
        Pipeline pipeline = Pipeline.create(pipelineOptions);

        PCollection<String> records = pipeline.apply("ReadData", TextIO.read().from("src/main/resources/input.txt"));

        PCollection<List<CoreLabel>> coreLabels = records
            .apply("annotate",
                ParDo.of(new annotateElement(stanfordCoreNLP))
            );

        PCollection<List<String>> collected = coreLabels
            .apply("collect",
                ParDo.of(new collectElement(type))
            );

        collected
            .apply(Flatten.iterables())
            .apply("Write", TextIO.write().to("src/main/resources/output.txt"));

        try {
            pipeline.run().waitUntilFinish();
        } catch (Exception e) {
            log.info(e.getMessage());
        }

        return new ResponseEntity<>("OK", HttpStatus.OK);
    }

    public static class annotateElement extends DoFn<String, List<CoreLabel>> {
        static StanfordCoreNLP nlp;

        public annotateElement(StanfordCoreNLP stanfordCoreNLP) {
            nlp = stanfordCoreNLP;
        }

        @ProcessElement
        public void processElement(@Element String element, OutputReceiver<List<CoreLabel>> out) {
            Annotation annotationSentence = new Annotation(element);
            if(nlp == null) {
                nlp = new StanfordCoreNLP();
            }
            nlp.annotate(annotationSentence);
            out.output(annotationSentence.get(CoreAnnotations.TokensAnnotation.class).subList(0, annotationSentence.get(CoreAnnotations.TokensAnnotation.class).size()));
        }
    }

    static class collectElement extends DoFn<List<CoreLabel>, List<String>> {

        NERType nerType;
        public collectElement(NERType type) {
            nerType = type;
        }

        @ProcessElement
        public void process(@Element List<CoreLabel> listCoreLabel, OutputReceiver<List<String>> out, ProcessContext processContext) {
            String test = processContext.element().toString();
            out.output(collectListCore(listCoreLabel, nerType));
        }
    }

    static List<String> collectListCore(List<CoreLabel> coreLabels, final NERType type) {
        Stream<CoreLabel> stream = coreLabels.stream();
        if (!type.getNerType().equalsIgnoreCase("ALL")) {
            stream = stream.filter(coreLabel ->
                    type.getAllGeoNerTypes().contains(coreLabel.get(CoreAnnotations.NamedEntityTagAnnotation.class))
            );
        }
        return stream
                .map(CoreLabel -> CoreLabel.toString(VALUE_TAG_NER))
                .collect(Collectors.toList());
    }

}
