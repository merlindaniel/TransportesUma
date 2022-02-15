package com.uma.transportesuma.controller;

import ch.qos.logback.classic.joran.JoranConfigurator;
import com.google.gson.*;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import com.uma.transportesuma.document.Journey;
import com.uma.transportesuma.document.User;
import com.uma.transportesuma.dto.CreatePaymentResponse;
import com.uma.transportesuma.dto.StripeUrl;
import com.uma.transportesuma.service.JourneyService;
import com.uma.transportesuma.service.StripeService;
import com.uma.transportesuma.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping(path = "/api/stripe")
public class StripeController {

    public static final String METADATA_JOURNEY_ID = "METADATA_JOURNEY_ID";
    public static final String METADATA_PARTICIPANT_ID = "METADATA_PARTICIPANT_ID";

    private StripeService stripeService;
    private JourneyService journeyService;
    private UserService userService;

    @Autowired
    public void setStripeService(StripeService stripeService) {
        this.stripeService = stripeService;
    }
    @Autowired
    public void setJourneyService(JourneyService journeyService) {
        this.journeyService = journeyService;
    }
    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }


    //-------Logeado
    @GetMapping("/url")
    public ResponseEntity<StripeUrl> goToStripe(){
        try{
            String url = stripeService.getUrlToStripe();
            return ResponseEntity.status(HttpStatus.OK).body(new StripeUrl(url));
        } catch (Exception ex){
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/enable")
    public ResponseEntity<?> allConfigurated(){
        try {
            if(this.stripeService.todoConfigurado())
                return ResponseEntity.status(HttpStatus.OK).body(null);
            else
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception ex){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
    }


    //--------Stripe Web Hook
    @PostMapping("/webhook")
    public ResponseEntity<String> endPointStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader){

        String endpointSecret = "whsec_a84ba2aa65141edbc1cd8522d674c11b3038decda0408c315d921b972fa727c8";
        Event event = null;
        try {
            event = Webhook.constructEvent(
                    payload, sigHeader, endpointSecret
            );
        } catch (JsonSyntaxException e) {
            // Invalid payload.
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (SignatureVerificationException e) {
            // Invalid Signature.
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }

        if ("payment_intent.succeeded".equals(event.getType())) {
            // Deserialize the nested object inside the event
            EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
            PaymentIntent paymentIntent = null;
            if (dataObjectDeserializer.getObject().isPresent()) {

                paymentIntent = (PaymentIntent) dataObjectDeserializer.getObject().get();
                Map<String, String> conjuntoMetadatos = paymentIntent.getMetadata();

                String journeyId = conjuntoMetadatos.get(METADATA_JOURNEY_ID);
                String thisParticipantId = conjuntoMetadatos.get(METADATA_PARTICIPANT_ID);

                String email = paymentIntent.getReceiptEmail();

                try {
                    Journey journey = journeyService.findJourneyById(journeyId).get();
                    User user = userService.findUser(thisParticipantId).get();

                    this.journeyService.addParticipant(journey, user);
                } catch(Exception ex){
                    return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
                }

            } else {
                return new ResponseEntity<>("Weebhook error: API version mismatch.", HttpStatus.BAD_REQUEST);
            }
        }

        return new ResponseEntity<>(null, HttpStatus.OK);

    }


    //--------Public. Create Payment Intent
    @PostMapping("/payment-intent")
    public ResponseEntity<CreatePaymentResponse> crearIntentoDePago(@RequestBody String postBody){
        try{

            JsonObject items = JsonParser.parseString(postBody).getAsJsonObject();
            JsonArray listaItem = items.get("items").getAsJsonArray();

            JsonObject elem = listaItem.get(0).getAsJsonObject();

            CreatePaymentResponse paymentResponse = this.stripeService.createPaymentIntent(elem.get("id_journey").getAsString());
            return new ResponseEntity<>(paymentResponse, HttpStatus.OK);
        } catch (Exception ex){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

    }



}
