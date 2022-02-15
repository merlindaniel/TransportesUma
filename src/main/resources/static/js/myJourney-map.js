let app = new Vue({
    el: '#app',
    data:{
        //Leaflet
        map: null,
        mapLine: [],

        //Token JWT
        tokenConBearer: null,

        //Journey
        journeyId: null,
        journey: null,
        journeyName: "",

        //Direct routes
        routeDirect: null,
        loadingRouteDirect: false,

        //Routes with stop fuel station
        routeWithFuelstation: [],
        radius: 1000,
        loadingRouteWithFuelstation: false,

        lastRouteSummary : [],

        //Toast
        toastElem : null
    },
    methods: {
        getJourneyId() {
            let queryString = window.location.search;
            let urlParams = new URLSearchParams(queryString);
            let journey = urlParams.get('journey');

            if (journey) {
                this.journeyId = journey;
            } else {
                console.log("--No existe journey");
                window.location.href = frontendPaths.pathIndex;
            }
        },
        async getJourney() {

            let response = await fetch('/api/journeys/my/journey/' + this.journeyId, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization':this.tokenConBearer
                }
            });
            
            if(response.status !== 200){
                console.log("--respuesta distinta de 200");
                window.location.href = frontendPaths.pathIndex;
            } else {
                this.journey = await response.json();
                this.journeyName = this.journey.name;
                console.log(this.journey);
                this.setOrgDestMarkers();
                //this.getDirectRoute();
                
            }

        },
        getTokenBearer() {
            this.tokenConBearer = Vue.$cookies.get('TokenJWT');
            if (this.tokenConBearer===null)
                window.location.href = frontendPaths.pathIndex;
        },
        setOrgDestMarkers() {
            L.marker([this.journey.origin.lat, this.journey.origin.lng]).addTo(this.map);
            L.marker([this.journey.destination.lat, this.journey.destination.lng]).addTo(this.map);
            this.map.panTo(new L.LatLng(this.journey.origin.lat, this.journey.origin.lng));
                
        },
        async getDirectRoute(){
            this.loadingRouteDirect = true;
            let ok = false;

            //Evitamos sobrecarga de la api al pulsar varias veces el boton
            if(this.routeDirect === null){
                let response = await fetch(
                    '/api/opendata/get/route/direct/' 
                    + this.journey.origin.lat + '/'
                    + this.journey.origin.lng + '/'
                    + this.journey.destination.lat + '/'
                    + this.journey.destination.lng, {
                    method: 'GET',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization':this.tokenConBearer
                    }
                });
            
                if(response.status !== 200){
                    console.log("----Error al obtener una ruta");
                    this.loadingRouteDirect = false;
                } else {
                    ok = true;
                    this.routeDirect = await response.json();
                }
            } else {
                ok = true;
            }
            
            if(ok){

                this.cleanRoutes();
                
                let points = this.routeDirect.points;
    
                for(let i=0; i<points.length-1; i++){
                    let line = L.polygon([
                        [points[i].latitude, points[i].longitude],
                        [points[i+1].latitude, points[i+1].longitude]
                    ]).addTo(this.map);
                    this.mapLine.push(line);
                }
                this.lastRouteSummary = [];
                this.lastRouteSummary.push(this.routeDirect.routeSummary);
                this.runToast();
                this.loadingRouteDirect = false;
            }
            
        },
        async getRouteWithFuelstationStop(){
            this.loadingRouteWithFuelstation = true;
            let ok = false;

            //Evitamos sobrecarga de la api al pulsar varias veces el boton
            if(this.routeWithFuelstation.length === 0){
                let response = await fetch(
                    '/api/opendata/get/route/stopping/closer/fuelstation/' 
                    + this.journey.origin.lat + '/'
                    + this.journey.origin.lng + '/'
                    + this.journey.destination.lat + '/'
                    + this.journey.destination.lng + '/'
                    + this.radius, {
                    method: 'GET',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization':this.tokenConBearer
                    }
                });
            
                if(response.status !== 200){
                    console.log("----Error al obtener una ruta parando por gasolinera");
                    this.loadingRouteWithFuelstation = false;
                } else {
                    ok = true;
                    this.routeWithFuelstation = await response.json();
                }
            } else {
                ok = true;
            }
            
            if(ok){

                this.cleanRoutes();

                let route1 = this.routeWithFuelstation[0];
                let points1 = route1.points;

                for(let i=0;i < points1.length -1 ; i++){
                    let line = L.polygon([
                        [points1[i].latitude, points1[i].longitude],
                        [points1[i+1].latitude, points1[i+1].longitude]
                    ]).addTo(this.map);
                    this.mapLine.push(line);
                }

                let route2 = this.routeWithFuelstation[1];
                let points2 = route2.points;
                for(let i=0; i < points2.length -1; i++){
                    let line = L.polygon([
                        [points2[i].latitude, points2[i].longitude],
                        [points2[i+1].latitude, points2[i+1].longitude]
                    ]).addTo(this.map);
                    this.mapLine.push(line);
                }
                this.lastRouteSummary = [];
                this.lastRouteSummary.push(route1.routeSummary);
                this.lastRouteSummary.push(route2.routeSummary);
                this.runToast();
                this.loadingRouteWithFuelstation = false;
            }
        },
        cleanRoutes(){
            this.lastRouteSummary = [];
            this.mapLine.forEach(elem => {
                elem.removeFrom(this.map);
            });
            this.mapLine = [];
        },
        runToast(){
            let toast = new bootstrap.Toast(this.toastElem);
            toast.show();
        }
    },
    created(){
        this.getTokenBearer();
    },
    mounted() {
        //Toast
        this.toastElem = document.getElementById('liveToast');

        //Map
        const tilesOSM = 'https://tile.openstreetmap.org/{z}/{x}/{y}.png';

        this.map = L.map('map', {
            center : [36.7213028, -4.4216366],
            zoom : 20,
        });

        L.tileLayer(tilesOSM, {
            attribution: 'Map data &copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
            maxZoom: 18,
        }).addTo(this.map);
        
        this.getJourneyId();
        this.getJourney();
    },
})
