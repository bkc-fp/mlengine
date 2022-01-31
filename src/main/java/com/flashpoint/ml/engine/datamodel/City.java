package com.flashpoint.ml.engine.datamodel;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author BumKi Cho
 */

@Data
@Entity
@Table(name="CITY",
    indexes = {
        @Index(name="IDX_CITY_ID", columnList="CITY_ID", unique = true),
        @Index(name="IDX_CITY_NAME", columnList = "CITY_NAME", unique = false),
        @Index(name="IDX_COUNTRY_ID", columnList = "COUNTRY_ID", unique = false)
    })
public class City implements Serializable {
    @Id
    @Column(name="CITY_ID", length=30)
    private String cityId;

    @Column(name="CITY_NAME", length=200)
    private String cityName;

    @Column(name="LAT", length=20)
    private String lat;

    @Column(name="LNG", length=20)
    private String lng;

    @Column(name="COUNTRY_ID", length=100)
    private String countryId;

    @Column(name="POPULATION", length=20)
    private String population;

}
