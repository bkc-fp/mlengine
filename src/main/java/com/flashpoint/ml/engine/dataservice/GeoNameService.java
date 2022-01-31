package com.flashpoint.ml.engine.dataservice;

import com.flashpoint.ml.engine.datamodel.GeoName;
import com.flashpoint.ml.engine.datarepository.GeoNameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author BumKi Cho
 */

@Service
public class GeoNameService {

    @Autowired
    private GeoNameRepository geoNameRepository;

    public GeoName saveGeoName(GeoName geoName) {
        return geoNameRepository.save(geoName);
    }
}
