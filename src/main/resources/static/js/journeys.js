// javascript para la página que lista los trayectos buscados (journeys.html)
const app = new Vue({

    el: '#app',
    data: {

        tokenConBearer: null,
        loggedUser: '',
        origin: '',
        destination: '',
        date: '',
        nSpots: '',
        journeysList: [],
        journeysListDef: [],
        all: true
    },

    methods: {

        async getLoggedUser() {
            let response = await fetch('/api/users/current', {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': this.tokenConBearer
                }
            });
            this.loggedUser = await response.json();
        },

        async getJourneys() {

            const queryString = window.location.search;
            const urlParams = new URLSearchParams(queryString);
            const origin = urlParams.get('origin');
            const destination = urlParams.get('destination');
            const nSpots = urlParams.get('nSpots');
            const date = urlParams.get('date');

            this.getLoggedUser();

            let response;

            if (origin && destination && nSpots && date) {

                this.origin = origin;
                this.destination = destination;
                this.nSpots = nSpots;
                this.date = date;
                this.all = false;

                if (this.loggedUser) {
                    response = await fetch('/api/journeys/search/' + this.origin + "/" + this.destination + "/" + this.nSpots + "/" + this.date + "/" + this.loggedUser.id, {
                        headers: {
                            'Content-Type': 'application/json',
                            'Authorization': this.tokenConBearer
                        }
                    });
                } else {
                    response = await fetch('/api/journeys/search/' + this.origin + "/" + this.destination + "/" + this.nSpots + "/" + this.date + "/", {
                        headers: {
                            'Content-Type': 'application/json',
                            'Authorization': this.tokenConBearer
                        }
                    });
                }

            } else {

                if (this.loggedUser) {
                    response = await fetch('/api/journeys/search/' + this.loggedUser.id, {
                        headers: {
                            'Content-Type': 'application/json',
                            'Authorization': this.tokenConBearer
                        }
                    });
                } else {
                    response = await fetch('/api/journeys/', {
                        headers: {
                            'Content-Type': 'application/json',
                            'Authorization': this.tokenConBearer
                        }
                    });
                }

                this.nSpots = 1;
            }

            if (response.ok) {
                this.journeysList = await response.json();
                this.setList();
            } else {
                //error
            }

        },

        async setList() {


            for (journey of this.journeysList) {
                var h = this.hoursFunction(journey.startDate);

                let response = await fetch('/api/users/' + journey.organizer, {
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': this.tokenConBearer
                    }
                });

                if (response.ok) {
                    var org = await response.json();
                    this.journeysListDef.push({
                        id: journey.id,
                        name: journey.name,
                        description: journey.description,
                        origin: {
                            title: journey.origin.title,
                            lat: journey.origin.lat,
                            lng: journey.origin.lng,
                            address: journey.origin.address
                        },
                        destination: {
                            title: journey.destination.title,
                            lat: journey.destination.lat,
                            lng: journey.destination.lng,
                            address: journey.destination.address
                        },
                        // Organizador
                        organizer: org,
                        numberParticipants: journey.numberParticipants,
                        // Numero de asientos restantes      
                        numberSpots: journey.numberParticipants - journey.participants.length,
                        participants: journey.participants,
                        vehicle: journey.vehicle,
                        price: journey.price,
                        startDate: journey.startDate,
                        // hora
                        hour: h,
                        exam: journey.exam,
                        finished: journey.finished
                    });
                }

            }
        },

        getTokenBearer() {

            this.tokenConBearer = Vue.$cookies.get('TokenJWT');
            this.getJourneys();
            //if (this.tokenConBearer === null) {
            //    window.location.href = frontendPaths.pathIndex;
            //}else{
            //    this.getJourneys();
            //}

        },

        hoursFunction: function (string) {
            return string.split("T")[1].split(":")[0] + ":" + string.split("T")[1].split(":")[1];
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
        this.getTokenBearer();
    },

});
