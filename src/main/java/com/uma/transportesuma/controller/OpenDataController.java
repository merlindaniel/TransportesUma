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
import java.util.stream.Collectors;

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



    @Autowired
    @Qualifier("restTemplate")
    private RestTemplate restTemplate;



    /**
     * Devuelve una lista de lugares (vease la clase Place) dados una latitud, longitud y un nombre.
     * Hace uso de la API de HERE
     *
     * Ejemplo de consulta: .../api/opendata/get/places/01.23/04.56/facultad
     *
     * @param lat Latitud
     * @param lng Longitud
     * @param name Nombre completo o parcial de un lugar. Ejemplos: "Facul", "Facultad"...
     */
    @GetMapping("/get/places/{lat}/{lng}/{name}")
    public ResponseEntity<List<Place>> getPlacesByLatLngAndName(@PathVariable("lat") Double lat,
                                                                @PathVariable("lng") Double lng,
                                                                @PathVariable("name") String name){

        try {

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
    @GetMapping("/get/route/{latSrc}/{lngSrc}/{latDst}/{lngDst}")
    public ResponseEntity<Route> getRouteByLatLng(
            @PathVariable("latSrc") Double latSrc,
            @PathVariable("lngSrc") Double lngSrc,
            @PathVariable("latDst") Double latDst,
            @PathVariable("lngDst") Double lngDst){

        try {

            JsonObject jsonObjectReponse = this.getJsonObjectByUrl(this.getUrlTomTomApiRouting(latSrc, lngSrc, latDst, lngDst));

            Gson gson = new Gson();
            double vv = this.getDistanceByLatLng(36.714608, -4.264197, 36.713706, -4.266476);

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
     * @return Devuelve una lista de gasolineras. Vease FuelStation.
     */
    @GetMapping("/get/fuelstation/")
    private ResponseEntity<List<FuelStation>> getFuelStationsEsp(){
        try {
            return ResponseEntity.status(HttpStatus.OK).body(this.getFuelStationList());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    private List<FuelStation> getFuelStationList() throws Exception{
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
        return fuelStationList;
    }


    /**
     * Devuelve los precios que posee cada casolinera que se encuentren dentro de un rango (en metros)
     * y su centro en latitud y longitud.
     * Nota: Es util si queremos obtener las gasolineras mas cercanas dado una posicion GPS.
     *
     * @param lat Latitud del usuario
     * @param lng Longitud del usuario
     * @param radius Radio en METROS
     * @return Devuelve una lista de gasolineras. Vease FuelStation.
     */

    @GetMapping("/get/fuelstation/lat/lng/radius/{lat}/{lng}/{radius}")
    private ResponseEntity<List<FuelStation>> getFuelStationsEspByLatLngAndRadius(
            @PathVariable("lat") Double lat,
            @PathVariable("lng") Double lng,
            @PathVariable("radius") Integer radius){

        try {
            List<FuelStation> fuelStationList = this.getFuelStationList();
            List<FuelStation> fuelStationListInsideRadius = fuelStationList
                    .stream()
                    .filter(elem -> (this.getDistanceByLatLng(elem.getLatLng().getLatitude(),elem.getLatLng().getLongitude(),lat,lng) <= radius.doubleValue()))
                    .collect(Collectors.toList());


            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(fuelStationListInsideRadius);

        } catch (Exception ex){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

    }




    //Se puede realizar con una API externa, sin embargo, es mejor así ya que mejora los tiempos
    private double getDistanceByLatLng(double lat1, double lng1, double lat2, double lng2) {
        double radioTierra = 6371;//en kilometros
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double va1 = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
        double va2 = 2 * Math.atan2(Math.sqrt(va1), Math.sqrt(1 - va1));
        return radioTierra * va2 * 1000;
    }


}
