package com.flashpoint.ml.engine.datarepository;

import com.flashpoint.ml.engine.datamodel.MislabeledCity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author BumKi Cho
 */

public interface MislabeledCityRepository extends JpaRepository<MislabeledCity, String> {
}
