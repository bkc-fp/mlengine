package com.flashpoint.ml.engine.ml;

import ai.djl.Application;
import ai.djl.Device;
import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.ndarray.NDList;
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

public class SentimentAnalysisFn extends DoFn<String, String> {
    private static final Logger logger = LogManager.getLogger(SentimentAnalysisFn.class);
    static Predictor<String, Classifications> predictor;

    public SentimentAnalysisFn(){
        // possibly thread configuration place
    }

    static class MyTranslator implements Translator<String, Classifications> {

        @Override
        public Classifications processOutput(TranslatorContext ctx, NDList list) throws Exception {
            return null;
        }

        @Override
        public NDList processInput(TranslatorContext ctx, String input) throws Exception {
            return null;
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
                            .optApplication(Application.NLP.SENTIMENT_ANALYSIS)
                            .setTypes(String.class, Classifications.class)
                            // This model was traced on CPU and can only run on CPU
                            .optDevice(Device.cpu())
                            .optProgress(new ProgressBar())
                            .build();

            ZooModel<String, Classifications> model = criteria.loadModel();
            String engineName = model.getNDManager().getEngine().getEngineName();
            configureThreads(engineName);
            predictor = model.newPredictor();
        }
        receiver.output(element.toString() + predictor.predict(element).toString());
    }

    private void configureThreads(String engineName) {
        logger.info("Engine Name:"+engineName);
        if(engineName.equalsIgnoreCase("PyTorch")) {
            return;
        }
        return;
    }

}
