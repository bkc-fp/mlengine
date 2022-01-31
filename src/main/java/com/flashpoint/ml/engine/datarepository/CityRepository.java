package com.flashpoint.ml.engine.datarepository;

import com.flashpoint.ml.engine.datamodel.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author BumKi Cho
 */

@Repository
public interface CityRepository extends JpaRepository<City, String> {

    Optional<City> findByCityName(String cityName);

    @Query(
        "select a from City a " +
        "where upper(cityName) = :cityName"
    )
    List<City> findCitiesByCityName(@Param("cityName") String cityName);

}
