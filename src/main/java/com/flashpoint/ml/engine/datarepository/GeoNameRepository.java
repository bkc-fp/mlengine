package com.flashpoint.ml.engine.datarepository;

import com.flashpoint.ml.engine.datamodel.GeoName;
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
public interface GeoNameRepository extends JpaRepository<GeoName, String> {

    Optional<GeoName> findByGeoName(String geoName);

    @Query("select a from GeoName a where geoName = :geoName")
    List<GeoName> findGeoNamesByGeoName(@Param("geoName") String geoName);
}
