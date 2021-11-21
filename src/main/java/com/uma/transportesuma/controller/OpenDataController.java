package com.uma.transportesuma.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.uma.transportesuma.dto.Place;
import com.uma.transportesuma.dto.Route;
import com.uma.transportesuma.vo.LatLng;
import com.uma.transportesuma.vo.RouteSummary;
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

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/opendata")
public class OpenDataController {

    //API HERE
    private final String HERE_API_KEY = "MXq1nXbaUjtpL5d-EnteM0B3-EoiXFrZXgIqd5nry2g";
    private final String HERE_PREFFIX_API_DISCOVER = "https://discover.search.hereapi.com/v1/discover";

    //API TOMTOM
    private final String TOMTOM_API_KEY ="wSGBFXYcE42mlcntsL9LXVKSKGs9ikUy";
    private final String TOMTOM_PREFFIX_API_ROUTING="https://api.tomtom.com/routing/1/calculateRoute/";


    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();


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



    @GetMapping("/route/{latSrc}/{lngSrc}/{latDst}/{lngDst}")
    public ResponseEntity<Route> getRouteByLatLng(
            @PathVariable("latSrc") Double latSrc,
            @PathVariable("lngSrc") Double lngSrc,
            @PathVariable("latDst") Double latDst,
            @PathVariable("lngDst") Double lngDst){

        try{

            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(this.getUrlTomTomApiRouting(latSrc, lngSrc, latDst, lngDst)))
                    .setHeader("Accept", "application/json")
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            Gson gson = new Gson();

            JsonObject jsonObjectAll = JsonParser.parseString(response.body()).getAsJsonObject();

            JsonObject jsonSummaryAndPoints = jsonObjectAll
                    .get("routes").getAsJsonArray()
                    .get(0).getAsJsonObject()
                    .get("legs").getAsJsonArray()
                    .get(0).getAsJsonObject();

            RouteSummary rs = gson.fromJson(jsonSummaryAndPoints.get("summary"), RouteSummary.class);

            List<LatLng> latLngs = new ArrayList<>();
            JsonArray points = jsonSummaryAndPoints.get("points").getAsJsonArray();
            points.forEach(elem -> latLngs.add(gson.fromJson(elem, LatLng.class)));



            return ResponseEntity.status(HttpStatus.OK).body(new Route(rs, latLngs));

        }catch (Exception ex){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }


    }

    private String getUrlTomTomApiRouting(Double latSrc, Double lngSrc, Double latDst, Double lngDst){
        return this.TOMTOM_PREFFIX_API_ROUTING + latSrc + "," + lngSrc + ":" + latDst + "," + lngDst +
                "/json?" +
                "instructionsType=coded" +
                "&computeBestOrder=true" +
                "&routeRepresentation=polyline" +
                "&computeTravelTimeFor=none" +
                "&report=effectiveSettings" +
                "&routeType=fastest" +
                "&traffic=true" +
                "&avoid=unpavedRoads" +
                "&travelMode=car" +
                "&vehicleCommercial=false" +
                "&vehicleEngineType=combustion" +
                "&key=" + this.TOMTOM_API_KEY;
    }




}
