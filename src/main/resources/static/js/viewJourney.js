let myJourneyApp = new Vue({
    el: '#journeysDetailParticipants',
    data: {
        tokenConBearer: null,
        loggedUser: {},

        journeyId: "",
        journey: {},

        date: "",
        hour: "",

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
            this.selected = this.journey.vehicle;
            this.getDate();
            
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