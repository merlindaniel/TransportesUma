const app = new Vue({
    el: '#vueApp',
    data: {
        tokenConBearer: null,
        
        // Logged user id
        loggedUserId: null,
        loggedUser: null,

        // Current user data:
        // Example id = 61c1c831437c756c894c1fe4
        userId: null, // Eliminar id por defecto (ahora está para hacer pruebas)
        user: null,
        editionUser: {},
        vehicles: [],
        isLoggedUser: false,

        errors: [],
        stripeOnBoardingCompleted: false,
        stripeUrl: ''
    },
    methods: {
        async getUserId() {
            const queryString = window.location.search;
            const urlParams = new URLSearchParams(queryString);
            const userId = urlParams.get('user');

            if (userId)
                this.userId = userId;
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
            
            if (this.user == null || this.user === {}) {
                this.user = this.loggedUser;
                this.userId = this.loggedUser.id;
            }

            // Checks if this user is the current logged user
            this.isLoggedUser = this.user.id === this.loggedUserId;

            if (this.isLoggedUser) {
                Object.assign(this.editionUser, this.user); 
                this.editionUser.password = '';
            }

            this.getVehicles();
        },

        async getUser() {
            const response = await axios.get('/api/users/' + this.userId, {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization':this.tokenConBearer
                }});
            this.user = response.data;
            console.log(this.user);
            
            // Checks if this user is the current logged user
            this.isLoggedUser = this.user.id === this.loggedUserId;

            if (this.isLoggedUser)
                Object.assign(this.editionUser, this.user);
        },

        async getVehicles() {
            console.log("this.userId");
            console.log(this.userId);
            const response = await axios.get('/api/vehicles/user/' + this.userId, {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization':this.tokenConBearer
            }});
            this.vehicles = response.data;
            console.log(this.vehicles);
        },

        async userEditionSubmit() {
            let canEdit = await this.editionValidation();

            if (canEdit) {
                let imgUrl = this.user.picture;
                const img = document.getElementById("profile-pic");
    
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

                this.editionUser.picture = imgUrl;

                console.log("Edited user: ");
                console.log(this.editionUser);

                // Proteger el nombre de usuario para evitar que se pueda editar
                this.editionUser.username = this.user.username;

                // Si la contraseña esta vacia asumimos que el usuario no queria cambiarla
                if (!this.editionUser.password || this.editionUser.password === '')
                    this.editionUser.password = this.user.password;

                const response = await axios.put('/api/users/current', this.editionUser, {
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization':this.tokenConBearer
                    }});

                console.log("Response:");
                console.log(response);
                
                // Loads profile page after applying changes
                window.location.href = './profile.html';
            }
        },

        async editionValidation() {
            // Reiniciar mensajes de error
            this.errors = [];

            this.validateUsername();
            this.validateEmail();
            this.validateName();
            this.validatePassword();

            console.log(this.errors);
            return this.errors.length === 0;
        },

        async validateUsername() {
            // Comprobar que el nombre de usuario no está en uso o es igual al actual
            const editionUsername = document.getElementById('edition-username').value;
            if (editionUsername.length < 3 || editionUsername.length > 20) {
                this.errors.push(`El nombre de usuario "${editionUsername}" no es válido`);
            }
            else if (editionUsername !== this.user.username) {
                let response;
                response = await axios.get('/api/users/' + editionUsername, {
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization':this.tokenConBearer
                    }}).catch(error => {
                    return {};
                });

                if (response && response.data) {
                    this.errors.push(`El nombre de usuario "${editionUsername}" no está disponible`);
                    console.log(this.errors);
                }
            }
        },

        async validateEmail() {
            // Comprobar que el email del usuario no está en uso o es igual al actual
            const editionEmail = document.getElementById('edition-email').value;
            if (!editionEmail.includes('@') || !editionEmail.includes('.')) {
                this.errors.push(`El email "${editionEmail}" no es un email válido`);
            }
            else if (editionEmail !== this.user.email) {
                let response = await axios.get('/api/users/' + editionEmail, {
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization':this.tokenConBearer
                    }});
                if (response && response.data) {
                    this.errors.push(`El email "${editionEmail}" no está disponible`);
                    console.log(this.errors);
                }
            }
        },

        async validateName() {
            // Comprobar que el nombre del usuario es válido
            const editionName = document.getElementById('edition-name').value;
            if (editionName.length < 1 || editionName.length > 100) {
                this.errors.push(`El nombre "${editionName}" no es válido`);
            }
        },

        async validatePassword() {
            // Comprobar que el email del usuario no está en uso o es igual al actual
            const editionPassword = document.getElementById('edition-password').value;
            const editionPasswordRepeat = document.getElementById('edition-password-repeat').value;

            if (editionPassword !== editionPasswordRepeat || (editionPassword.length < 3 && editionPassword.length > 0)) {
                this.errors.push(`Las contraseñas no coinciden o no son válidas`);
            }
        },

        async deleteUser() {
            if (confirm("¿Estás seguro de que quieres eliminar tu cuenta? Esta acción no se puede deshacer.")) {
                const response = await axios.delete('/api/users/' + this.user.id, {
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization':this.tokenConBearer
                    }});
                console.log(response);
                // Logout
                Vue.$cookies.remove('TokenJWT');

                window.location.href = './index.html';
            }
        },

        async updateImg() {
            var fReader = new FileReader();
            fReader.readAsDataURL(document.getElementById("profile-pic").files[0]);
            fReader.onloadend = function(event) {
                var img = document.getElementById("picture");
                img.src = event.target.result;
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
        },
        async getUrlStripe(){
            let response = await fetch('/api/stripe/url', {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization':this.tokenConBearer
                }});
            if(200===response.status){
                console.log("Stripe url obtenida");
                let u = await response.json();
                this.stripeUrl = u.url;
            }
        }
    },
    mounted: function () {
        this.getTokenBearer();    
        this.getUserId();
        if (this.userId != null) this.getUser();
        this.getLoggedUser();
        this.comprobarStripeOnBoardingCompletado();
        this.getUrlStripe();

    },
});