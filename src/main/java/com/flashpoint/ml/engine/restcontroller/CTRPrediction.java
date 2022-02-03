package com.flashpoint.ml.engine.restcontroller;

import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.Batchifier;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;
import ai.djl.util.JsonUtils;
import com.google.gson.reflect.TypeToken;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping (value = "/api/ctr")
public class CTRPrediction {

    @PostMapping
    @RequestMapping(value = "/predict", params = {"csv-file"})
    public static ResponseEntity<String> predictCTR(@RequestParam("csv-file") String filepath) {

        String args = "--inputFile="+filepath;
        CtrOptions options =
                PipelineOptionsFactory.fromArgs(args).withValidation().as(CtrOptions.class);
        options.setOutput("src/main/resources/output.txt");

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
        PCollection<String> preprocess = addIds.apply("Preprocess", ParDo.of(new FeatureMap()));

        // run inference using Deep Java Library
        PCollection<String> ctr = preprocess.apply("Inference", ParDo.of(new Inference()));

        ctr.apply(TextIO.write().to(options.getOutput()));
        try {
            p.run().waitUntilFinish();
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>("completed", HttpStatus.OK);
    }

    public static Map<String, Map<String, String>> readFeatureMap() {
        Map<String, Map<String, String>> featureMap = new ConcurrentHashMap<>();
        String modelPath = new ClassPathResource("src/main/resources/feature_map.json").getPath();

        try (Reader reader = Files.newBufferedReader(Paths.get(modelPath))) {
            Type mapType = new TypeToken<Map<String, Map<String, String>>>() {
            }.getType();
            featureMap = JsonUtils.GSON.fromJson(reader, mapType);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return featureMap;
    }

    public interface CtrOptions extends PipelineOptions {

        /**
         * By default, this example reads from a public dataset containing the Ads data record
         */
        @Description("Path of the file to read from")
        @Default.String("./ctr/test.csv")
        String getInputFile();

        void setInputFile(String value);

        /**
         * Set this required option to specify where to write the output.
         */
        @Description("Path of the file to write to")
        @Default.String("ctr")
        String getOutput();

        void setOutput(String value);
    }

    static class FeatureMap extends DoFn<String, String> {

        private final Map<String, Map<String, String>> featureMap = readFeatureMap();
        private final int numFeatures = featureMap.size();

        @ProcessElement
        public void processElement(@Element String element, OutputReceiver<String> receiver) {
            String[] fields = element.trim().split("\t");
            List<String> features = new ArrayList<>();
            // first column uuid, second column label (ignored)
            if (fields.length == numFeatures + 2) {
                features.add(fields[0]);
                for (int i = 1; i < numFeatures + 1; i++) {
                    features.add(featureMap.get(String.valueOf(i)).getOrDefault(fields[i + 1].trim(), "-1"));
                }
                receiver.output(String.join("\t", features));
            }
        }
    }

    static class Inference extends DoFn<String, String> {
        static Predictor<String, String> predictor;

        static Predictor<String, String> getOrCreatePredictor() throws ModelException, IOException {
            if (predictor == null) {
                Criteria<String, String> criteria = Criteria.builder()
                        .setTypes(String.class, String.class)
                        .optTranslator(new CtrTranslator())
                        .optEngine("MXNet")
                        .build();
                ZooModel<String, String> model = criteria.loadModel();
                predictor = model.newPredictor();
            }
            return predictor;
        }

        @ProcessElement
        public void processElement(@Element String element, OutputReceiver<String> receiver)
                throws TranslateException, ModelException, IOException {
            receiver.output(element.split("\t")[0] + "\t" + getOrCreatePredictor().predict(element));
        }
    }

    static class CtrTranslator implements Translator<String, String> {
        @Override
        public NDList processInput(TranslatorContext ctx, String input) {
            String[] fields = input.trim().split("\t");
            NDManager manager = ctx.getNDManager();
            float[] features = new float[fields.length - 1];
            // first column uuid
            for (int i = 1; i < fields.length; i++) {
                features[i - 1] = Float.parseFloat(fields[i]);
            }
            return new NDList(manager.create(features));
        }

        @Override
        public String processOutput(TranslatorContext ctx, NDList list) {
            NDArray ctr = list.singletonOrThrow();
            return String.valueOf(ctr.getFloat());
        }

        @Override
        public Batchifier getBatchifier() {
            return Batchifier.STACK;
        }
    }

}
