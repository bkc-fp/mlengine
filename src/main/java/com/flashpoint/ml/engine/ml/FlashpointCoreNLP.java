package com.flashpoint.ml.engine.ml;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.springframework.stereotype.Service;

import java.util.Properties;

/**
 * @author BumKi Cho
 */

public class FlashpointCoreNLP extends StanfordCoreNLP {
    public FlashpointCoreNLP(Properties properties) {
        super(properties);
    }
}
