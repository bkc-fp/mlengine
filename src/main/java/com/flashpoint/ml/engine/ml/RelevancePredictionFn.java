package com.flashpoint.ml.engine.ml;


import ai.djl.Application;
import ai.djl.Device;
import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.Batchifier;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class RelevancePredictionFn  extends DoFn<String, String> {
    private static final Logger logger = LogManager.getLogger(RelevancePredictionFn.class);
    static Predictor<String, Classifications> predictor;

    static class RelevanceTranslator implements Translator<String, Classifications> {
        private static final List<String> CLASSIFICATIONS = Arrays.asList("Very High", "High", "Medium", "Low", "Very Low");

        @Override
        public Classifications processOutput(TranslatorContext ctx, NDList list) throws Exception {
            NDArray relevanceScores = list.singletonOrThrow();
            return new Classifications(CLASSIFICATIONS, relevanceScores);
        }

        @Override
        public NDList processInput(TranslatorContext ctx, String input) throws Exception {
            NDManager manager = ctx.getNDManager();
            NDArray ndArray = manager.create(input);

            return new NDList(ndArray);
        }

        @Override
        public Batchifier getBatchifier() {
            return Batchifier.STACK;
        }
    }

    @ProcessElement
    public void processElement(@Element String element, OutputReceiver<String> receiver) throws TranslateException, ModelException, IOException {
        if (predictor == null) {
            Criteria<String, Classifications> criteria =
                    Criteria.builder()
                            .setTypes(String.class, Classifications.class)
                            .optModelPath(Paths.get("src/main/resources/saved_model.pb.zip"))
                            .optTranslator(new RelevanceTranslator())
                            .optEngine("TensorFlow")
//                            .optModelName("model_artifacts_relevance_classifier_combined_v0_saved_model.pb")
                            // This model was traced on CPU and can only run on CPU
                            .optDevice(Device.cpu())
                            .optProgress(new ProgressBar())
                            .build();

            ZooModel model = criteria.loadModel();
            String engineName = model.getNDManager().getEngine().getEngineName();
            configureThreads(engineName);
            predictor = model.newPredictor();
        }
        receiver.output(predictor.predict(element).toString());
    }

    private void configureThreads(String engineName) {
        logger.info("Engine Name:"+engineName);
        if(engineName.equalsIgnoreCase("PyTorch")) {
            return;
        }
        return;
    }


}
