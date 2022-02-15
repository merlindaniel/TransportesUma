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
        
        obtenerTokenBearer() {
            this.tokenConBearer = Vue.$cookies.get('TokenJWT');
        },
        logout() {
            Vue.$cookies.remove('TokenJWT');

            try {
                let auth2 = gapi.auth2.getAuthInstance();
                auth2.signOut().then(function () {
                    console.log('User signed out.');
                    window.location.href = './index.html';
                });
            } catch (error) {
                window.location.href = './index.html';
            }
        }
    },
    created: function () {
        this.obtenerTokenBearer();
    },

})