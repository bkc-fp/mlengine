package com.flashpoint.ml.engine.ml;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.Properties;

/**
 * @author BumKi Cho
 */

@Service
public class CoreNLPProvider {
    private static Properties properties;
    private static String propertiesName = "tokenize, ssplit, pos, lemma, ner, entitymentions";

    private static StanfordCoreNLP stanfordCoreNLP;

    private CoreNLPProvider() {
    }

    //    static initialization block for the class. it runs when the class is first loaded. only once
    static {
        properties = new Properties();
        properties.setProperty("annotators", propertiesName);
    }

    @Bean(name = "stanfordCoreNLP")
    public static StanfordCoreNLP getInstance() {
        if(stanfordCoreNLP == null) {
            stanfordCoreNLP = new StanfordCoreNLP(properties);
        }
        return stanfordCoreNLP;
    }

}

