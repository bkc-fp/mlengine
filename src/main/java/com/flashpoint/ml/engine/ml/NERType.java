package com.flashpoint.ml.engine.ml;

import java.util.Arrays;
import java.util.List;

/**
 * @author BumKi Cho
 */

public enum NERType {

    LOCATION("Location"),
    CITY("City"),
    STATE_OR_PROVINCE("State_Or_Province"),
    COUNTRY("Country"),
    ALL("All");

    private String nerType;

    NERType(String nerType) {
        this.nerType = nerType;
    }

    public String getNerType() {
        return nerType;
    }

    public List<String> getAllGeoNerTypes() {
        return Arrays.asList("LOCATION", "CITY", "STATE_OR_PROVINCE", "COUNTRY");
    }

}