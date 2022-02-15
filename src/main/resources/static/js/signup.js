let app = new Vue({
    el: '#appVue',
    data: {
        name: "",
        email: "",
        username: "",
        password: "",
        confirmPassword: "",
        notEqualsPassword: false,
        existsUsername: false,
        existsEmail: false
    },
    methods: {
        async signUp(e){
            e.preventDefault();
            if(this.password === this.confirmPassword){
                this.resetErrors();

                let response = await fetch(backendPaths.preffix + backendPaths.pathApiUsers, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        name: this.name,
                        email: this.email,
                        username: this.username,
                        password: this.password
                    })
                });
                if(response.status === 200){
                    window.location.href = frontendPaths.pathLogin;
                } else {
                    let data = await response.json();
                    if(data.fieldError === "USERNAME")
                        this.existsUsername = true;
                    else if(data.fieldError === "EMAIL")
                        this.existsEmail = true;
                    console.log(response);
                    console.log("Error en la respuesta");
                }
            } else {
                this.notEqualsPassword = true;
                console.log("Contraseña no iguales");
            }
        },
        resetErrors(){
            this.notEqualsPassword = false;
            this.existsUsername = false;
            this.existsEmail = false;
        }
    },
    beforeCreate: function() {
        let token = Vue.$cookies.get('TokenJWT');
        if(token !== null)
            window.location.href = frontendPaths.pathIndex;
    }
});