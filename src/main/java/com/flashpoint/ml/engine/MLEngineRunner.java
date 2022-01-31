package com.flashpoint.ml.engine;

import com.flashpoint.ml.engine.datapipeline.CityETLProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author BumKi Cho
 */

public class MLEngineRunner {
    private static final Logger log = LogManager.getLogger(MLEngineRunner.class);

    private String command;

    @Autowired
    private CityETLProcessor cityETLProcessor;

    public void set(String[] args) {
        if (args.length == 0) {
            return;
        }
        this.command = args[0];
    }

    public void run(String a) {
        if (this.command.equalsIgnoreCase("init")) {
            try {
                cityETLProcessor.processWorldCities();
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }
}
