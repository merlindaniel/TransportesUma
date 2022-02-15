let app = new Vue({
    el: '#appVue',
    data: {
        username: "",
        password: "",
        error: false,
        data: null
    },
    methods: {
        async logIn(e){
            e.preventDefault();
            let response = await fetch(backendPaths.preffix + backendPaths.pathAuthenticationLogin, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    username: this.username,
                    password: this.password
                })
            });

            if(response.status == 200){
                this.error = false;
                this.data = await response.json();
                Vue.$cookies.set('TokenJWT', this.data.jwttoken);
                window.location.href = frontendPaths.pathIndex;
            } else {
                //Datos incorrectos
                this.error = true;
            }
        }
    },
    beforeCreate: function() {
        let token = Vue.$cookies.get('TokenJWT');
        if(token !== null)
            window.location.href = frontendPaths.pathIndex;
    }
});