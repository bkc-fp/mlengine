package com.flashpoint.ml.engine;

import com.flashpoint.ml.engine.datapipeline.CityETLProcessor;
import com.flashpoint.ml.engine.datapipeline.GeoETLProcessor;
//import com.oracle.tools.packager.Log;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author BumKi Cho
 */

@Component
public class MLEngineEventListener {
    private static final Logger log = LogManager.getLogger(MLEngineRunner.class);

    @Autowired
    private CityETLProcessor cityETLProcessor;

    @Autowired
    private GeoETLProcessor geoETLProcessor;

    @EventListener
    public void onApplicationStart(ApplicationStartedEvent event) {
        log.info("Application started");
//        cityETLProcessor.processWorldCities();
    }
}
