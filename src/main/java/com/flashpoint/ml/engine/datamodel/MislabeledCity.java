package com.flashpoint.ml.engine.datamodel;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author BumKi Cho
 */

@Data
@Entity
@Table (name="MISLABELED_CITY",
        indexes = {
            @Index(name="IDX_MC_NAME", columnList="CITY_NAME", unique = false),
            @Index(name="IDX_MC_NER", columnList = "NER", unique = false)
        }
)
public class MislabeledCity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="CITY_NAME", length=200)
    private String cityName;

    @Column(name="NER", length=20)
    private String ner;

    @Column(name="REASON", length=100)
    private String reason;

}
