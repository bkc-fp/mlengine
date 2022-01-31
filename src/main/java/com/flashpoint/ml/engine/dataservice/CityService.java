package com.flashpoint.ml.engine.dataservice;

import com.flashpoint.ml.engine.datamodel.City;
import com.flashpoint.ml.engine.datamodel.MislabeledCity;
import com.flashpoint.ml.engine.datarepository.CityRepository;
import com.flashpoint.ml.engine.datarepository.MislabeledCityRepository;
import com.flashpoint.ml.engine.ml.NERType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * @author BumKi Cho
 */

@Service
public class CityService implements Serializable {

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private MislabeledCityRepository mislabeledCityRepository;

    public City saveCity(City city) {
        return cityRepository.save(city);
    }

    public String findCityByName(String cityName) {
        if(cityName==null || cityName.isEmpty()) {return "no value was passed";}

        String[] strArray = cityName.split(":");
        if(strArray.length<3) {return "problem with passed value >> "+cityName;}

        if(strArray[1].contains("CITY") || strArray[1].contains("STATE_OR_PROVINCE") ||
                strArray[1].contains("COUNTRY") || strArray[1].contains("LOCATION") ||
                strArray[1].contains("PERSON") || strArray[1].contains("ORGANIZATION")
        ) {

            String[] valuesToSearch = {strArray[0], strArray[2]};
            String searchResult = "";

            for (String value : valuesToSearch) {
                Optional<City> city = null;
//                city = cityRepository.findByCityName(value);

                List<City> cityList = cityRepository.findCitiesByCityName(value.toUpperCase());
                if(cityList.size() >= 1) {
                    //TODO: This should be a proper disambiguation not just population comparator
                    cityList.sort(new CityComparator().reversed());
                    city = Optional.ofNullable(cityList.get(0));
                } else {
                    continue;
                }

                if (city.isPresent()) {
                    if(!strArray[1].equalsIgnoreCase("CITY")) {
                        MislabeledCity mislabeledCity = new MislabeledCity();
                        mislabeledCity.setCityName(value);
                        mislabeledCity.setNer(strArray[1]);
                        mislabeledCity.setReason(value + " was labeled as " + strArray[1]);
                        mislabeledCityRepository.save(mislabeledCity);
                    }

                    City cityEntity = city.get();
                    searchResult = cityEntity.getCityName() + ", LAT:" + cityEntity.getLat() + ", LNG:" + cityEntity.getLng() + ", Population:" + cityEntity.getPopulation();
                    break;
                }
            }

            if(searchResult.isEmpty()) {
                if(strArray[1].equalsIgnoreCase("CITY")) {
                    MislabeledCity mislabeledCity = new MislabeledCity();
                    mislabeledCity.setCityName(valuesToSearch[0]);
                    mislabeledCity.setNer(strArray[1]);
                    mislabeledCity.setReason(valuesToSearch[0] + " was not found");
                    mislabeledCityRepository.save(mislabeledCity);
                    return valuesToSearch[0] + " was not found";
                } else {
                    searchResult = valuesToSearch[0] + " was labeled as " + strArray[1];
                }
            }
            return searchResult;
        }

        return cityName+":unannotated-as-is";
    }

    public String findCityByName(String cityName, NERType type) {
        String returnValue = findCityByName(cityName);

        if(type.getNerType().equalsIgnoreCase("CITY") && returnValue.endsWith(":unannotated-as-is")) {
            return "";
        }

        if(returnValue.endsWith(":unannotated-as-is")) {
            return returnValue.substring(0,returnValue.indexOf(":"));
        } else {
            return returnValue;
        }
    }

    public class CityComparator implements Comparator<City> {

        @Override
        public int compare(City o1, City o2) {
            return Integer.valueOf(o1.getPopulation()) - Integer.valueOf(o2.getPopulation());
        }
    }
}
