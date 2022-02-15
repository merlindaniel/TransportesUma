let myJourneyApp = new Vue({
    el: '#journeysDetail',
    data: {
        tokenConBearer: null,
        loggedUser: {},

        journeyId: "",
        journey: {},
        participants: [],

        date: "",
        hour: "",

        origen: '',
        destino: '',
        listaVehiculos: [],
        listaOrigenes: [],
        listaDestinos: [],
        vehicleUser: null,
        selected: null,
        fecha: '',
        hora: '',

        errors: []
    },
    methods: {
        async getLoggedUser() {
            let response = await fetch('/api/users/current', {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization':this.tokenConBearer
                }});
            this.loggedUser = await response.json();
            this.getJourneyId();
            console.log("Logged user: " + this.loggedUser);

        },

        async getJourneyId() {
            const queryString = window.location.search;
            const urlParams = new URLSearchParams(queryString);
            const journey = urlParams.get('journey');

            if (journey) {
                this.journeyId = journey;
                this.getJourney();
            } else {
                console.log("Error, no journey specified");
            }
        },

        async getJourney() {
            const response = await axios.get('/api/journeys/' + this.journeyId, {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization':this.tokenConBearer
            }});

            this.journey = response.data;
            if(this.journey.organizer != this.loggedUser.id){
                window.location.href = './your-travels.html';
            }else{
                this.origen = this.journey.origin.address;
                this.destino = this.journey.destination.address;
                this.selected = this.journey.vehicle;
                this.getParticipants();
                this.obtenerVehiculos();
                this.getDate();
            }
            
        },

        async deleteJourney() {
            const response = await axios.delete('/api/journeys/' + this.journeyId, {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization':this.tokenConBearer
            }});
            window.location.href = './your-travels.html';
        },

        async updateJourney() {
            await this.obtenerListaOrigenes();
            await this.obtenerListaDestinos();

            let canSubmit = await this.fieldsValidation();
            if(canSubmit){
                // Configuración del Origen
                this.journey.origin.title = this.listaOrigenes[0].title;
                this.journey.origin.lat = this.listaOrigenes[0].lat;
                this.journey.origin.lng = this.listaOrigenes[0].lng;
                this.journey.origin.address = this.listaOrigenes[0].address;

                // Configuración del Destino
                this.journey.destination.title = this.listaDestinos[0].title;
                this.journey.destination.lat = this.listaDestinos[0].lat;
                this.journey.destination.lng = this.listaDestinos[0].lng;
                this.journey.destination.address = this.listaDestinos[0].address;


                // Configuración de la fecha, nombre, descripción y organizador del viaje
                this.journey.startDate = new Date(this.fecha+'T'+this.hour);
                this.journey.name = 'Viaje de ' + this.journey.origin.title + ' a ' + this.journey.destination.title;
                this.journey.description = 'El viaje se realizará el día ' + this.fecha+ ' a las ' + this.getHours(this.journey.startDate) + ' horas.\nSe han ofertado ' + this.journey.numberParticipants + ' sitios a un precio de ' + this.journey.price + '€ por plaza.';
                
                // Escoger el vehículo elegido en la lista de vehículos
                await this.obtenerVehiculoElegido();

                const response = await axios.put('/api/journeys/' + this.journey.id, this.journey, {
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization':this.tokenConBearer
                }});

                console.log("Updated journey: ");
                console.log(this.journey);
                window.location.href = './my-journey.html?journey='+this.journeyId;
            }
        },

        async fieldsValidation() {
            // Reiniciar mensajes de error
            this.errors = [];

            // Origin validation
            if (!this.origen || this.origen.length == 0 ) {
                this.errors.push(`Por favor introduzca un origen válido.`);
            }

            // Destination validation
            if (!this.destino || this.destino.length == 0) {
                this.errors.push(`Por favor introduzca un destino válido.`);
            }

            // Número de plazas
            if (!this.journey.numberParticipants || this.journey.numberParticipants < 0 || this.journey.numberParticipants > 10 || this.journey.numberParticipants == undefined) {
                this.errors.push(`Por favor introduzca un número de plazas válido.`);
            }

            // Precio
            if (!this.journey.price ||!this.journey.price < 0 || this.journey.price == undefined) {
                this.errors.push(`Por favor introduzca un precio válido.`);
            }

            console.log(this.errors);
            return this.errors.length === 0;
        },

        async obtenerListaOrigenes(){
            let response = await fetch('/api/opendata/get/places/40.416729/-3.703339/'+this.origen, {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization':this.tokenConBearer
                }});

            if(response.ok){
                // Devuelve la lista con posibles origenes
                this.listaOrigenes = await response.json();
            } else {
                // Muestra un error

            }
        },

        async obtenerListaDestinos(){
            let response = await fetch('/api/opendata/get/places/40.416729/-3.703339/'+this.destino, {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization':this.tokenConBearer
                }});

            if(response.ok){
                // Devuelve la lista con posibles destinos
                this.listaDestinos = await response.json();
            
            } else {
                // Muestra un error
            }
        },

        async obtenerVehiculos(){
            let response = await fetch('/api/vehicles/user/myVehicles', {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization':this.tokenConBearer
                }});
            this.listaVehiculos = await response.json();
        },

        async obtenerVehiculoElegido(){
            let response = await fetch('/api/vehicles/'+this.vehicleUser, {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization':this.tokenConBearer
                }});

            if(response.ok){
                // Devuelve el vehiculo elegido para el viaje
                this.journey.vehicle = await response.json();
            } else {
                // Muestra un error

            }
        },

        async getDate() {
            var dateJourney = new Date(Date.parse(this.journey.startDate));
            var day = dateJourney.getDate();
            if (day < 10){
                day = "0" + day;
            }
            var month = dateJourney.getMonth() + 1;
            if(month < 10){
                month = "0" + month;
            }
            this.date = day + "/" + month + "/" + dateJourney.getFullYear();

            this.hour = this.hoursFunction(this.journey.startDate);
            this.fecha = dateJourney.getFullYear() + "-" + month +"-"+day;
        },

        hoursFunction: function (string) {   
            return string.split("T")[1].split(":")[0] + ":" + string.split("T")[1].split(":")[1];
        },

        async getParticipants() {
            if(this.journey.participants.length > 0){
                const response = await axios.get('/api/journeys/participants/' + this.journeyId, {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization':this.tokenConBearer
                }});
                this.participants = response.data;
                if(this.participants != null && participants != undefined){
                    console.log(participants);
                }
            }
        },

        getTokenBearer() {
            this.tokenConBearer = Vue.$cookies.get('TokenJWT');
            console.log(this.tokenConBearer);
            if (this.tokenConBearer===null) {
                window.location.href = './index.html';
            } else {
                this.getLoggedUser();
            }
        },

    },
    created: function () {
        this.getTokenBearer();
    },
});