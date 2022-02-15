// javascript para la página de las búsquedas de trayecto (search.html)
const app = new Vue({

    el: '#app',
    data: {

        tokenConBearer: null,
        origin: '',
        destination: '',
        nSpots: '',
        date: '',
        listOrigin: [], 
        listDestination:  [],
        listJourney: []
    },
    
    methods: {
        
        async getListOrigin(){

            let response = await fetch('/api/opendata/get/places/40.416729/-3.703339/'+ this.origin, {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization':this.tokenConBearer
                }});
    
            if(response.ok){
                // Devuelve la lista con posibles origenes
                this.listOrigin = await response.json();
            } else {
                // Muestra un error
    
            }
    
        },

        async getListDestination(){

            let response = await fetch('/api/opendata/get/places/40.416729/-3.703339/'+ this.destination, {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization':this.tokenConBearer
                }});
    
            if(response.ok){
                // Devuelve la lista con posibles destinos
                this.listDestination = await response.json();
            } else {
                // Muestra un error
    
            }
    
        },
        
        async searchJourney(){
       
            window.location.href = frontendPaths.pathJourneys + "?origin=" + this.origin + "&destination=" 
            + this.destination + "&nSpots=" + this.nSpots + "&date=" + this.date;

        },

        getTokenBearer() {

            this.tokenConBearer = Vue.$cookies.get('TokenJWT');
            //if (this.tokenConBearer === null) {
            //    window.location.href = frontendPaths.pathIndex;
            //}

        }
    },
    
    created: function () {
        this.getTokenBearer();
    }

})