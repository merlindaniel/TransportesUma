const app = new Vue({

    el: '#app',
    data: {

        tokenConBearer: null,
        cabecera: 'Lista de tus Viajes',
        loggedUser: {},
        isDriver: true,
        listaViajesParticipados: [],
        listaViajesOrganizados: []

    },
    methods: {
        async getLoggedUser() {
            let response = await fetch('/api/users/current', {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization':this.tokenConBearer
                }});
            this.loggedUser = await response.json();
            

        },
        async obtenerViajes() {

            if (this.isDriver) {
                
                let response = await fetch('/api/journeys/user/organizing', {
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': this.tokenConBearer
                    }
                });

                if (response.ok) {
                    this.listaViajesOrganizados = await response.json();
                    console.log(this.listaViajesOrganizados);
                } else {
                }

            }


            let response = await fetch('/api/journeys/user/participating', {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': this.tokenConBearer
                }
            });

            if (response.ok) {
                this.listaViajesParticipados = await response.json();
            } else {

            }

        },

        obtenerFecha(fecha){
            var dateJourney = new Date(Date.parse(fecha));
            var day = dateJourney.getDate();
            if (day < 10){
                day = "0" + day;
            }
            var month = dateJourney.getMonth() + 1;
            if(month < 10){
                month = "0" + month;
            }
            return day + "/" + month + "/" + dateJourney.getFullYear();
        },

        obtenerHora(fecha){
            var dateJourney = new Date(Date.parse(fecha));
            var hours = dateJourney.getHours();
            if (hours < 10){
                hours = "0" + dateJourney.getHours();
            }
            var minutes = dateJourney.getMinutes();
            if(minutes < 10){
                minutes = "0" + dateJourney.getMinutes();
            }
            return hours + ":" + minutes;
        },

        obtenerTokenBearer() {
            this.tokenConBearer = Vue.$cookies.get('TokenJWT');
            if (this.tokenConBearer === null) {
                window.location.href = frontendPaths.pathIndex;
            }

        }
    },
    created: function () {
        this.obtenerTokenBearer();
        this.getLoggedUser();
        this.obtenerViajes();
    },

})