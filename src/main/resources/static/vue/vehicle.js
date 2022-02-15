new Vue({
    el: '#vueApp',
    data: {
        // Logged user id
        loggedUserId: null,
        loggedUser: null,
        // Jose: 61c1c831437c756c894c1fe4
        // Fran: 61c28804cca492069b7eb609
        isLoggedUser: false,

        editing: false,
        vehicleId: null,
        vehicle: {},
        editionVehicle: {},
        

        errors: [],
    },
    methods: {
        async getVehicleId() {
            const queryString = window.location.search;
            const urlParams = new URLSearchParams(queryString);
            const vehicle = urlParams.get('vehicle');

            if (vehicle) {
                this.vehicleId = vehicle;
                this.editing = true;
            } else {

                // Default values
                this.editionVehicle.seats = 2;
                this.editionVehicle.owner = this.loggedUserId;
                this.editionVehicle.combustible = "GASOLINE";
                this.editionVehicle.name = "Mi vehículo";

                console.log(this.editionVehicle);
            }
        },

        async getVehicle() {
            if (this.editing) {
                const response = await axios.get('/api/vehicles/current/' + this.vehicleId, {
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization':this.tokenConBearer
                    }});
                this.vehicle = response.data;
                console.log(this.vehicle);
                
                // Checks if this user is the current logged user
                this.isLoggedUser = this.loggedUserId && this.vehicle.owner === this.loggedUserId;
    
                if (this.isLoggedUser)
                    Object.assign(this.editionVehicle, this.vehicle);
                
            } else if (this.loggedUserId) {
                this.isLoggedUser = true;
            }
        },

        async getLoggedUser() {
            const response = await axios.get('/api/users/current', {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization':this.tokenConBearer
                }});
            this.loggedUser = response.data;
            this.loggedUserId = this.loggedUser.id;
            console.log(this.loggedUser);
            
            this.isLoggedUser = true;
        },
        
        async vehicleSubmit() {
            let canSubmit = await this.fieldsValidation();

            if (canSubmit) {
                let imgUrl = this.vehicle.picture;
                const img = document.getElementById("vehicle-pic");
    
                // If image was uploaded, then uploads image to imgur
                if (img.files.length > 0) {
                    const formdata = new FormData();
                    formdata.append("image", img.files[0]);
                    await fetch("https://api.imgur.com/3/image/", {
                        method: "post",
                        headers: {
                            Authorization: "Client-ID 4f59459848bf8ef"
                        },
                        body: formdata
                    }).then(data => data.json()).then(data => {
                        imgUrl = data.data.link;

                        console.log(imgUrl);
                    });
                }

                this.editionVehicle.picture = imgUrl;

                // Set owner to logged user
                if (!this.editing) {
                    this.editionVehicle.owner = this.loggedUserId;
                }

                console.log("Edited vehicle: ");
                console.log(this.editionVehicle);

                let response;
                if (this.editing) {
                    response = await axios.put('/api/vehicles/current/' + this.vehicle.id, this.editionVehicle, {
                        headers: {
                            'Content-Type': 'application/json',
                            'Authorization':this.tokenConBearer
                        }});
                } else {
                    response = await axios.post('/api/vehicles/current/', this.editionVehicle, {
                        headers: {
                            'Content-Type': 'application/json',
                            'Authorization':this.tokenConBearer
                        }});
                }

                console.log("Response:");
                console.log(response);
                
                // Loads profile page after applying changes
                window.location.href = './profile.html?user=' + this.loggedUserId;
            }
        },

        async fieldsValidation() {
            // Reiniciar mensajes de error
            this.errors = [];

            // Model validation
            if (!this.editionVehicle.model || this.editionVehicle.model.length < 3) {
                this.errors.push(`El modelo "${this.editionVehicle.model}" no es válido.`);
            }

            // Registration validation
            if (!this.editionVehicle.registration || this.editionVehicle.registration.length < 3) {
                this.errors.push(`El número de registro "${this.editionVehicle.registration}" no es válido.`);
            }

            // Name validation
            if (!this.editionVehicle.name || this.editionVehicle.name.length < 3) {
                this.errors.push(`El nombre "${this.editionVehicle.name}" no es válido.`);
            }

            // Seats validation
            if (!this.editionVehicle.seats || this.editionVehicle.seats < 2) {
                this.errors.push(`El número de asientos "${this.editionVehicle.seats}" no es válido.`);
            }

            // Combustible validation
            if (!this.editionVehicle.combustible) {
                this.errors.push(`El tipo de combustible "${this.editionVehicle.combustible}" no es válido.`);
            }

            console.log(this.errors);
            return this.errors.length === 0;
        },

        
        async updateImg() {
            var fReader = new FileReader();
            fReader.readAsDataURL(document.getElementById("vehicle-pic").files[0]);
            fReader.onloadend = function(event) {
                var img = document.getElementById("picture");
                img.src = event.target.result;
            }
        },

        async deleteVehicle() {
            if (confirm("¿Estás seguro de que quieres eliminar este vehículo? Esta acción no se puede deshacer.")) {
                const response = await axios.delete('/api/vehicles/current/' + this.vehicle.id, {
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization':this.tokenConBearer
                    }});
                console.log(response);
                window.location.href = './profile-edition.html';
            }
        },

        getTokenBearer() {
            this.tokenConBearer = $cookies.get('TokenJWT');
            console.log(this.tokenConBearer);
            if (this.tokenConBearer===null) {
                window.location.href = './index.html';
            } else {
                console.log('User is logged');
            }
        },
    },
    created: function () {
        this.getTokenBearer();    
        this.getVehicleId();
        this.getLoggedUser();

        this.getVehicle();
    },
});