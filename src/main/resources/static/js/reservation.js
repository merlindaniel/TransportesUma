// javascript para la página de reserva (reservation.html)
const app = new Vue({

    el: '#app',
    data: {

        tokenConBearer: null,
        loggedUser: '',
        id: '',
        journey: '',
        organizer: '',
        nSpots: '',
        hour: '',
        date: '',
    },

    methods: {
        
        async getLoggedUser() {
            let response = await fetch('/api/users/current', {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization':this.tokenConBearer
                }});
            
            if(response.ok){
                this.loggedUser = await response.json();
            }

        },

        async getJourney(){
            const queryString = window.location.search;
            const urlParams = new URLSearchParams(queryString);
            const id = urlParams.get('journey');
            const nspots = urlParams.get('nSpots');

            if(id && nspots){
                
                this.id = id;
                this.nSpots = nspots;
                let response = await fetch('/api/journeys/'+ this.id, {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization':this.tokenConBearer
                }});

                if(response.ok){
                    this.journey = await response.json();
                    this.setArgs();
                }else{
                    //error
                    this.journey = 'TERRIBLE';
                }

            }else{
                //error
            }
        },

        async setArgs(){
            this.date = this.journey.startDate.split("T")[0];
            this.hour = this.hoursFunction(this.journey.startDate);
        },

        async book(){

            if(this.loggedUser){ // Comprobamos si está logueado para realizar la reserva
                let response = await fetch('/api/journeys/participants/' + this.journey.id + "/" + this.loggedUser.id,  {
                    method: 'GET',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization':this.tokenConBearer
                    }
                });

                if(response.ok){
                    window.location.href = frontendPaths.pathYourTravels;
                }else{
                    window.location.href = frontendPaths.pathReservation + "?journey=" + this.journey.id + "&nSpots=" + this.nSpots;
                    // frontendPaths.pathReservation + "?journey=" + this.journey.id + "&nSpots=" + this.nSpots;
                }
            }else{ // Si no está logueado 
                window.location.href = frontendPaths.pathLogin;
            }

        },

        getTokenBearer() {

            this.tokenConBearer = Vue.$cookies.get('TokenJWT');
            if (this.tokenConBearer === null) {
                window.location.href = frontendPaths.pathLogin;
            }else{
                this.getLoggedUser();
                this.getJourney();
            }

        },

        hoursFunction: function (string) {   
            return string.split("T")[1].split(":")[0] + ":" + string.split("T")[1].split(":")[1];
        }

    },

    created: function () {
        this.getTokenBearer();
    },

})