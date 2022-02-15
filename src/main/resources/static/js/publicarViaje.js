const app = new Vue({

    el: '#app',
    data: {

        tokenConBearer: null,
        loggedUser: {},        
        cabecera: 'Publicar un Viaje',
        journey: {
            name: '',                   
            description: '',            
            origin: {
                title: '',
                lat: '',
                lng: '',
                address: ''
            },
            destination: {
                title: '',
                lat: '',
                lng: '',
                address: ''
            },
            numberParticipants: '',     
            organizer: '',              
            participants: [],           
            vehicle: '',                
            price: '',                  
            startDate: '',
            hour: '',   
            exam: null,                 
            finished: false,
            onlinePayment: false           
        },
        data: null,
        origen: '',
        destino: '',
        listaVehiculos: [],
        listaOrigenes: [],
        listaDestinos: [],
        vehicleUser: null,
        fecha: '',
        errorDePublicacion: '',
        stripeOnBoardingCompleted: false

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

        async publicarViaje(e){
            e.preventDefault();
            console.log("Publicando...");

            await this.configurarViaje();

            let response = await fetch('/api/journeys/',
                {
                    method: 'POST',
                    body: JSON.stringify(this.journey),
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization':this.tokenConBearer
                    }
                }
            );

            if(response.ok){
                // Redirecciona a la página del viaje publicado
                this.data = await response.json();
                //console.log(this.data);
                //Vue.$cookies.set('TokenJWT', this.data.jwttoken);
                window.location.href = frontendPaths.pathmyJourney + this.data.id;
            } else {
                // Muestra un error
                this.errorDePublicacion = 'ERROR';

            }

        },
        async obtenerListaOrigenes(){

            console.log("11110000");

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

            // if(this.destino != ''){

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

            // }

            

        },
        async obtenerVehiculos(){

            let response = await fetch('/api/vehicles/user/myVehicles', {
                                    headers: {
                                        'Content-Type': 'application/json',
                                        'Authorization':this.tokenConBearer
                                    }});
            if(200===response.status){
                this.listaVehiculos = await response.json();
            } else {
                window.location.href = frontendPaths.pathIndex;
            }

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
        async configurarViaje(){

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
            this.journey.startDate = new Date(this.fecha+'T'+this.journey.hour);
            this.journey.name = 'Viaje de ' + this.journey.origin.title + ' a ' + this.journey.destination.title;
            this.journey.description = 'El viaje se realizará el día ' + this.journey.startDate + ' a las ' + this.journey.hour + ' horas.\nSe han ofertado ' + this.journey.numberParticipants + ' sitios a un precio de ' + this.journey.price + '€ por plaza.';
            //this.journey.organizer = this.userId;
            
            // Escoger el vehículo elegido en la lista de vehículos
            await this.obtenerVehiculoElegido();
            this.errorDePublicacion = '';

            // Mostrar los datos en la consola
            console.log('Name: '+this.journey.name); 
            console.log('Descripcion: '+this.journey.description); 
            console.log('Origen: '+this.journey.origin); 
            console.log('Destino: '+this.journey.destination); 
            console.log('Numero de Participantes: '+this.journey.numberParticipants); 
            console.log('Organizador: '+this.journey.organizer); 
            console.log('Vehiculo: '+this.journey.vehicle); 
            console.log('Precio: '+this.journey.price); 
            console.log('StartDate: '+this.journey.startDate);
            console.log('Hour: '+this.journey.hour);
            console.log('Exam: '+this.journey.exam); 
            console.log('Finisehd: '+this.journey.finished); 
        },
        obtenerTokenBearer(){
            this.tokenConBearer = Vue.$cookies.get('TokenJWT');
            if(this.tokenConBearer===null){
                window.location.href = frontendPaths.pathIndex;
            } 
        },
        async comprobarStripeOnBoardingCompletado(){
            let response = await fetch('/api/stripe/enable', {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization':this.tokenConBearer
                }});
            if(200===response.status){
                console.log("Stripe on boarding SI completado");
                this.stripeOnBoardingCompleted = true;
            } else {
                console.log("Stripe on boarding NO completado");
            }
            
        }
    },
    created: function (){
        this.obtenerTokenBearer();
        this.getLoggedUser();
        this.obtenerVehiculos();
        this.comprobarStripeOnBoardingCompletado();
    },
    updated() {
        if(this.listaVehiculos.length >= 1){
            console.log("entraa2");
            let tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
            let tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
                return new bootstrap.Tooltip(tooltipTriggerEl)
            });
            tooltipList.forEach(element => {
                console.log(element);
            });
        }
    },

})