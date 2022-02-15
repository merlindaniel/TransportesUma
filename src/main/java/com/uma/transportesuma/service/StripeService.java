package com.uma.transportesuma.service;

import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import com.stripe.model.LoginLink;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.AccountLinkCreateParams;
import com.stripe.param.LoginLinkCreateOnAccountParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.uma.transportesuma.controller.StripeController;
import com.uma.transportesuma.document.Journey;
import com.uma.transportesuma.document.User;
import com.uma.transportesuma.dto.CreatePaymentResponse;
import com.uma.transportesuma.repository.JourneyRepository;
import com.uma.transportesuma.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class StripeService {
    private UserRepository userRepository;
    private JourneyRepository journeyRepository;

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @Autowired
    public void setJourneyRepository(JourneyRepository journeyRepository) {
        this.journeyRepository = journeyRepository;
    }


    /**
     * Hay 3 casos:
     * - CASO 1: Si el usuario NO posee una cuenta de stripe, se generará la cuenta y posteriormente devolverá la url a Stripe On Boarding
     * - Si el usuario SI posee una cuenta de stripe pero no lo ha completado, devolverá el link de Stripe On Boarding
     * - Si el usuario SI posee una cuenta de stripe y si lo ha completado, devolverá el link al Dashboard de stripe
     * @return
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)  //Region critica transaccional
    public String getUrlToStripe() throws Exception {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String usuarioActual = userDetails.getUsername();

        Optional<User> u = userRepository.findByUsername(usuarioActual);

        if(!u.isPresent())
            throw new Exception("El usuario no ha sido encontrado");

        User thisUser = u.get();

        if(thisUser.getStripeAccount() == null || thisUser.getStripeAccount().isEmpty()){
            //CASO 1
            AccountCreateParams paramsAcc = AccountCreateParams
                    .builder()
                    .setType(AccountCreateParams.Type.EXPRESS)
                    .setCapabilities(
                            AccountCreateParams.Capabilities
                                    .builder()
                                    .setCardPayments(
                                            AccountCreateParams.Capabilities.CardPayments
                                                    .builder()
                                                    .setRequested(true)
                                                    .build()
                                    )
                                    .setTransfers(
                                            AccountCreateParams.Capabilities.Transfers
                                                    .builder()
                                                    .setRequested(true)
                                                    .build()
                                    )
                                    .build()
                    )
                    .setEmail(thisUser.getEmail())
                    .build();

            Account account = Account.create(paramsAcc);
            thisUser.setStripeAccount(account.getId());
            userRepository.save(thisUser);
            return this.createAccountLink(account.getId());
        } else {
            Account account = Account.retrieve(thisUser.getStripeAccount());

            if(account.getDetailsSubmitted() && account.getChargesEnabled()){
                //CASO 3
                return createLoginLink(thisUser.getStripeAccount());
            } else {
                //CASO 2
                return this.createAccountLink(thisUser.getStripeAccount());
            }

        }

    }


    public CreatePaymentResponse createPaymentIntent(String journeyId) throws Exception {
        Optional<Journey> journeyOptional = this.journeyRepository.findById(journeyId);
        if(!journeyOptional.isPresent())
            throw new Exception("Viaje no encontrado");

        Journey thisJourney = journeyOptional.get();


        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String usuarioActual = userDetails.getUsername();

        Optional<User> u = this.userRepository.findByUsername(usuarioActual);
        Optional<User> uOrganizer = this.userRepository.findById(thisJourney.getOrganizer());

        if(!u.isPresent() || !uOrganizer.isPresent())
            throw new Exception("El usuario no ha sido encontrado");

        User thisUser = u.get();
        User organizerUser = uOrganizer.get();

        //METADATA
        Map<String, String> metadatos = new HashMap<>();
        metadatos.put(StripeController.METADATA_JOURNEY_ID, thisJourney.getId());
        metadatos.put(StripeController.METADATA_PARTICIPANT_ID, thisUser.getId());

        //PAYMENT METHODS
        List<String> listaMetodosPago = new ArrayList<>();
        listaMetodosPago.add("card");
        //listaMetodosPago.add("alipay");
        //listaMetodosPago.add("giropay");

        //PAYMENT INTENT
        PaymentIntentCreateParams params =
                PaymentIntentCreateParams.builder()
                        .setAmount((long) (thisJourney.getPrice() * 100))
                        .setApplicationFeeAmount(0L)
                        .setCurrency("eur")
                        .addAllPaymentMethodType(listaMetodosPago)
                        .setDescription("Journey.")
                        .putAllMetadata(metadatos)
                        .build();

        //REQUEST OPTIONS
        RequestOptions requestOptions =
                RequestOptions.builder()
                        .setStripeAccount(organizerUser.getStripeAccount())
                        .build();

        // Create a PaymentIntent with the order amount and currency
        PaymentIntent paymentIntent = PaymentIntent.create(params, requestOptions);

        return new CreatePaymentResponse(paymentIntent.getClientSecret());
    }



    /**
     * Devuelve el url del Account Link (link para On Boarding)
     * @param accountId
     * @return
     * @throws StripeException
     */
    private String createAccountLink(String accountId) throws StripeException {
        AccountLinkCreateParams params = AccountLinkCreateParams
                .builder()
                .setAccount(accountId)
                .setRefreshUrl("https://share-travel-pr4.herokuapp.com/profile.html")
                .setReturnUrl("https://share-travel-pr4.herokuapp.com/profile.html")
                .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                .build();
        AccountLink accountLink = AccountLink.create(params);
        return accountLink.getUrl();
    }

    /**
     * Devuelve el url del Login Link (link para Stripe Dashboard)
     * @return
     */
    private String createLoginLink(String accountId) throws StripeException {
        LoginLinkCreateOnAccountParams params = LoginLinkCreateOnAccountParams
                .builder()
                .setRedirectUrl("https://share-travel-pr4.herokuapp.com/profile.html")
                .build();

        LoginLink loginLink = LoginLink.createOnAccount(accountId, params, null);
        return loginLink.getUrl();
    }


    public boolean todoConfigurado() throws Exception {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String usuarioActual = userDetails.getUsername();

        Optional<User> u = userRepository.findByUsername(usuarioActual);

        if(!u.isPresent())
            throw new Exception("El usuario no ha sido encontrado");

        User thisUser = u.get();
        if(thisUser.getStripeAccount() == null || thisUser.getStripeAccount().isEmpty())
            return false;

        Account account = Account.retrieve(thisUser.getStripeAccount());

        if(account.getDetailsSubmitted() && account.getChargesEnabled())
            return true;
        else
            return false;
    }



}
