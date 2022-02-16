package com.flashpoint.ml.engine.ml;

import org.apache.commons.io.FileUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.Properties;

/**
 * @author BumKi Cho
 */

@Service
public class FlashpointNLPProvider {
    private static Properties properties;
    private static String propertiesName = "tokenize, ssplit, pos, lemma, ner, entitymentions";

    private static FlashpointCoreNLP flashpointCoreNLP;

    private FlashpointNLPProvider() {
    }

    //    static initialization block for the class. it runs when the class is first loaded. only once
    static {
        properties = new Properties();
        properties.setProperty("annotators", propertiesName);
        String modelFilePath = System.getProperty("user.home")+"/Documents/ML/NER/Model/City/ner-city-model.ser.gz";
        if(FileUtils.getFile(modelFilePath).exists()) {
            properties.setProperty("ner.model","edu/stanford/nlp/models/ner/english.all.3class.distsim.crf.ser.gz,edu/stanford/nlp/models/ner/english.muc.7class.distsim.crf.ser.gz,edu/stanford/nlp/models/ner/english.conll.4class.distsim.crf.ser.gz,"+modelFilePath);
        } else {
            properties.setProperty("ner.model","edu/stanford/nlp/models/ner/english.all.3class.distsim.crf.ser.gz,edu/stanford/nlp/models/ner/english.muc.7class.distsim.crf.ser.gz,edu/stanford/nlp/models/ner/english.conll.4class.distsim.crf.ser.gz");
        }

        properties.setProperty("entitymentions.acronyms", "true");
        properties.setProperty("ner.combinationMode", "HIGH_RECALL");
    }

    @Bean(name = "flashpointCoreNLP")
    public static FlashpointCoreNLP getInstance() {
        if(flashpointCoreNLP == null) {
            flashpointCoreNLP = new FlashpointCoreNLP(properties);
        }
        return flashpointCoreNLP;
    }

}
