package com.flashpoint.ml.engine.datamodel;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author BumKi Cho
 */

@Data
@Entity
@Table(name="GEONAME",
        indexes = {
                @Index(name="IDX_GEO_ID", columnList="GEO_ID", unique = true),
                @Index(name="IDX_GEO_NAME", columnList = "GEO_NAME", unique = false),
                @Index(name="IDX_GEO_ALT_NAME", columnList = "ALT_NAME", unique = false)
        })
public class GeoName implements Serializable {
    @Id
    @Column(name="GEO_ID", length=30)
    private String geoId;

    @Column(name="GEO_NAME", length=300)
    private String geoName;

    @Column(name="ALT_NAME", length=8000)
    private String altName;

    @Column(name="LAT", length=20)
    private String lat;

    @Column(name="LNG", length=20)
    private String lng;

    @Column(name="COUNTRY_ID", length=100)
    private String countryId;

    @Column(name="POPULATION", length=20)
    private String population;

}

