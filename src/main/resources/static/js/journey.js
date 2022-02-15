// javascript para la página de un trayecto (journey.html) desde buscar
const app = new Vue({

    el: '#app',
    data: {

        tokenConBearer: null,
        id: '',
        journey: '',
        organizer: '',
        nSpots: '',
        hour: '',
        date: '',
        showModal: false,
        stripe: null,
        stripeElements: null
    },

    methods: {
        async getJourney() {
            const queryString = window.location.search;
            const urlParams = new URLSearchParams(queryString);
            const id = urlParams.get('journey');
            const nspots = urlParams.get('nSpots');
            if (id && nspots) {

                this.id = id;
                this.nSpots = nspots;

                let response = await fetch('/api/journeys/' + this.id, {
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': this.tokenConBearer
                    }
                });

                if (response.ok) {
                    this.journey = await response.json();
                    this.setArgs();
                } else {
                    //error
                    this.journey = 'TERRIBLE';
                }

            } else {
                //error
            }
        },
        async setArgs() {

            let response = await fetch('/api/users/' + this.journey.organizer, {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': this.tokenConBearer
                }
            });

            if (response.ok) {
                this.organizer = await response.json();
                if (this.journey.onlinePayment && this.tokenConBearer !== null) {
                    console.log("Es online");
                    this.configureStripe(this.organizer.stripeAccount);
                }
            } else {
                //error
            }
            this.date = this.journey.startDate.split("T")[0];
            this.hour = this.hoursFunction(this.journey.startDate);

        },
        async goReservation() {
            window.location.href = frontendPaths.pathReservation + "?journey=" + this.journey.id + "&nSpots=" + this.nSpots;
        },
        getTokenBearer() {

            this.tokenConBearer = Vue.$cookies.get('TokenJWT');
            this.getJourney();
            //if (this.tokenConBearer === null) {
            //    window.location.href = frontendPaths.pathIndex;
            //} else {
            //    this.getJourney();
            //}

        },
        hoursFunction: function (string) {
            return string.split("T")[1].split(":")[0] + ":" + string.split("T")[1].split(":")[1];
        },

        //-------STRIPE
        async configureStripe(stripeAccountId) {
            console.log(stripeAccountId);
            this.stripe = Stripe("pk_test_51KT4GvD15nb6EUWAmpbc8NiyChfQHf8tgwuc03kD7torMLkopl5WRHT02Pa0y9S9o6HMBU3YDBhBodiksVDWhK7200rf8MyR6q",
                {
                    stripeAccount: stripeAccountId
                });

            // The items the customer wants to buy
            const items = [{ id_journey: this.journey.id }];

            const response = await fetch(backendPaths.preffix + "/api/stripe/payment-intent", {
                method: "POST",
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': this.tokenConBearer
                },
                body: JSON.stringify({ items }),
            });
            const fetchData = await response.json();

            const appearance = {
                theme: 'stripe',
            };
            this.stripeElements = this.stripe.elements({
                appearance,
                clientSecret: fetchData.clientSecret
            });

            const paymentElement = this.stripeElements.create("payment");
            paymentElement.mount("#payment-element");
        },

        showMessage(messageText) {
            const messageContainer = document.querySelector("#payment-message");

            messageContainer.classList.remove("hidden");
            messageContainer.textContent = messageText;

            setTimeout(function () {
                messageContainer.classList.add("hidden");
                messageText.textContent = "";
            }, 4000);
        },
        setLoading(isLoading) {
            if (isLoading) {
                // Disable the button and show a spinner
                document.querySelector("#submit").disabled = true;
                document.querySelector("#spinner").classList.remove("hidden");
                document.querySelector("#button-text").classList.add("hidden");
            } else {
                document.querySelector("#submit").disabled = false;
                document.querySelector("#spinner").classList.add("hidden");
                document.querySelector("#button-text").classList.remove("hidden");
            }
        },
        async handleSubmit(e) {
            e.preventDefault();
            this.setLoading(true);
            const { error } = await this.stripe.confirmPayment({
                elements: this.stripeElements,
                confirmParams: {
                    // Make sure to change this to your payment completion page
                    return_url: "http://localhost:5500/",
                },
            });

            // This point will only be reached if there is an immediate error when
            // confirming the payment. Otherwise, your customer will be redirected to
            // your `return_url`. For some payment methods like iDEAL, your customer will
            // be redirected to an intermediate site first to authorize the payment, then
            // redirected to the `return_url`.
            if (error.type === "card_error" || error.type === "validation_error") {
                this.showMessage(error.message);
            } else {
                this.showMessage("Ha ocurrido un error.");
            }

            this.setLoading(false);
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