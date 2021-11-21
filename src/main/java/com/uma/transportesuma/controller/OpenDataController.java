package com.uma.transportesuma.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.uma.transportesuma.dto.FuelStation;
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

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
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

    //API G
    private final String GOB_ESP_API_URL_FUEL_PRICE = "https://sedeaplicaciones.minetur.gob.es/ServiciosRESTCarburantes/PreciosCarburantes/EstacionesTerrestres/";

    //HTTP Client Java 11
    private HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();



    @Autowired
    @Qualifier("restTemplate")
    private RestTemplate restTemplate;


    /**
     * Devuelve una lista de lugares (vease la clase Place) dados una latitud, longitud y un nombre.
     * Hace uso de la API de HERE
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

    private String getUrlHereApiDiscover(Double lat, Double lng, String countryPrefix, String name, int limit){
        return this.HERE_PREFFIX_API_DISCOVER + "?at=" + lat + "," + lng + "&limit=" + limit + "&q=+" + name + "+&in=countryCode:" + countryPrefix + "&apiKey=" + this.HERE_API_KEY;
    }


    /**
     * Devuelve una ruta (vease la clase Route) con el camino mas corto y mas rapido entre un origen
     * y un destino. Hace uso de la API de TomTom
     * @param latSrc latitud origen
     * @param lngSrc longitud origen
     * @param latDst latitud destino
     * @param lngDst longitud destino
     * @return Devuelve un objeto de clase Route. Poseera la lista de puntos (points) por
     */
    @GetMapping("/route/{latSrc}/{lngSrc}/{latDst}/{lngDst}")
    public ResponseEntity<Route> getRouteByLatLng(
            @PathVariable("latSrc") Double latSrc,
            @PathVariable("lngSrc") Double lngSrc,
            @PathVariable("latDst") Double latDst,
            @PathVariable("lngDst") Double lngDst){

        try {

            JsonObject jsonObjectReponse = this.getJsonObjectByUrl(this.getUrlTomTomApiRouting(latSrc, lngSrc, latDst, lngDst));

            Gson gson = new Gson();


            JsonObject jsonSummaryAndPoints = jsonObjectReponse
                    .get("routes").getAsJsonArray()
                    .get(0).getAsJsonObject()
                    .get("legs").getAsJsonArray()
                    .get(0).getAsJsonObject();

            RouteSummary rs = gson.fromJson(jsonSummaryAndPoints.get("summary"), RouteSummary.class);

            List<LatLng> latLngs = new ArrayList<>();
            JsonArray points = jsonSummaryAndPoints.get("points").getAsJsonArray();
            points.forEach(elem -> latLngs.add(gson.fromJson(elem, LatLng.class)));


            return ResponseEntity.status(HttpStatus.OK).body(new Route(rs, latLngs));

        } catch (Exception ex){
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


    /**
     * Devuelve los precios que posee cada gasolinera en españa.
     * Nota: La llamada a este url es algo tena debido a la gran cantidad de datos.
     *
     * @return Devuelve una lista de gasolineras. Vease FuelStation
     */
    @GetMapping("/fuelstation")
    private ResponseEntity<List<FuelStation>> getFuelStationsEsp(){
        try {
            Gson gson = new Gson();
            JsonObject jsonObjectResponse = this.getJsonObjectByUrl(this.GOB_ESP_API_URL_FUEL_PRICE);

            JsonArray jsonFuelStationList = jsonObjectResponse.get("ListaEESSPrecio").getAsJsonArray();

            List<FuelStation> fuelStationList = new ArrayList<>();

            jsonFuelStationList.forEach(elem -> {
                FuelStation fs = gson.fromJson(elem.getAsJsonObject(), FuelStation.class);
                Double lat, lng;
                try {
                    String slat = elem.getAsJsonObject().get("Latitud").getAsString().replace(",", ".");
                    String slng = elem.getAsJsonObject().get("Longitud (WGS84)").getAsString().replace(",", ".");
                    lat = Double.parseDouble(slat);
                    lng = Double.parseDouble(slng);
                } catch (NumberFormatException ex){
                    lat = null;
                    lng = null;
                }
                fs.setLatLng(new LatLng(lat, lng));
                fuelStationList.add(fs);
            });


            return ResponseEntity.status(HttpStatus.OK).body(fuelStationList);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }


    private JsonObject getJsonObjectByUrl(String url) throws Exception{

        /*HttpClient httpClient = HttpClient.newBuilder()
                .sslContext(SSLContext.getDefault())
                .sslParameters(new SSLParameters())
                .version(HttpClient.Version.HTTP_2)
                .build();*/

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .setHeader("Accept", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return JsonParser.parseString(response.body()).getAsJsonObject();
    }



}
