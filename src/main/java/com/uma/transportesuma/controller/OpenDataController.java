package com.uma.transportesuma.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uma.transportesuma.vo.Place;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/opendata")
public class OpenDataController {

    private final String HERE_API_KEY = "MXq1nXbaUjtpL5d-EnteM0B3-EoiXFrZXgIqd5nry2g";
    private final String HERE_PREFFIX_API_DISCOVER = "https://discover.search.hereapi.com/v1/discover";


    @Autowired
    @Qualifier("restTemplate")
    private RestTemplate restTemplate;


    private String getUrlHereApiDiscover(Double lat, Double lng, String countryPrefix, String name, int limit){
        return this.HERE_PREFFIX_API_DISCOVER + "?at=" + lat + "," + lng + "&limit=" + limit + "&q=+" + name + "+&in=countryCode:" + countryPrefix + "&apiKey=" + this.HERE_API_KEY;
    }


    /**
     * Devuelve una lista de lugares (vease la clase Place) dados una latitud, longitud y un nombre.
     * Ejemplo de consulta: .../api/opendata/discover/01.23,04.56/facultad
     *
     * @param latAndLng Latitud y longitud separadas por una coma. Ejemplo: 01.23,04.56
     * @param name Nombre completo o parcial de un lugar. Ejemplos: "Facul", "Facultad"...
     */
    @GetMapping("/discover/{latlng}/{name}")
    public ResponseEntity<List<Place>> getPlacesByLatLngAndName(@PathVariable("latlng") String latAndLng,
                                                                @PathVariable("name") String name){

        try {
            Double lat = Double.parseDouble(latAndLng.split(",")[0]);
            Double lng = Double.parseDouble(latAndLng.split(",")[1]);

            String urlApiHere = this.getUrlHereApiDiscover(lat, lng, "ESP", name, 10);
            ResponseEntity<JsonNode> jsonResponse = restTemplate.exchange(urlApiHere, HttpMethod.GET, null, JsonNode.class);
            JsonNode map = jsonResponse.getBody().get("items");

            JsonNode arrNode = new ObjectMapper().readTree(String.valueOf(map));
            List<Place> places = new ArrayList<>();
            if(arrNode.isArray()){
                for(JsonNode obj : arrNode){
                    Place l = new Place();
                    l.setTitle(obj.get("title").asText());
                    l.setAddress(obj.get("address").get("label").asText());
                    l.setLat(obj.get("position").get("lat").asDouble());
                    l.setLng(obj.get("position").get("lng").asDouble());
                    l.setDistance(obj.get("distance").asInt());
                    places.add(l);
                }
            }

            return ResponseEntity.status(HttpStatus.OK).body(places);

        } catch (Exception ex){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

    }






}
