package com.flashpoint.ml.engine.ml;

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

}