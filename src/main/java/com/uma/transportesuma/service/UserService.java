package com.uma.transportesuma.service;

import com.sun.mail.util.MailSSLSocketFactory;
import com.uma.transportesuma.document.User;
import com.uma.transportesuma.document.chat.BasicUser;
import com.uma.transportesuma.dto.UserDTO;
import com.uma.transportesuma.exception.UserError;
import com.uma.transportesuma.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

@Service
@AllArgsConstructor
public class UserService {

    @Autowired
    private final UserRepository userRepository;

    /*@Autowired
    private JavaMailSender emailSender;*/

    // ---------- Queries ----------

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> findUserById(String id) {
        return userRepository.findById(id);
    }

    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findUser(String id) {
        Optional<User> optUser = findUserById(id);

        if (optUser.isEmpty())
            optUser = findUserByUsername(id);

        if (optUser.isEmpty())
            optUser = findUserByEmail(id);

        return optUser;
    }

    // ---------- Operations ----------

    public User addUser(UserDTO userDto) throws Exception{

        Optional<User>userOpt = this.findUserByEmail(userDto.getEmail());
        if(userOpt.isPresent())
            throw new Exception(UserError.EMAIL_ERROR);

        userOpt = this.findUserByUsername(userDto.getUsername());
        if(userOpt.isPresent())
            throw new Exception(UserError.USERNAME_ERROR);



        BCryptPasswordEncoder bcpe = new BCryptPasswordEncoder();
        userDto.setPassword(bcpe.encode(userDto.getPassword()));
        User user = User.getUserFromDto(userDto);
        return userRepository.save(user);
    }

    public void removeUser(User user) {
        userRepository.delete(user);
    }

    public void removeAllUsers() {
        userRepository.deleteAll();
    }

    public User updateUser(String id, User user) {
        Optional<User> optUserInBD = findUserById(id);
        if (optUserInBD.isPresent()) {
            User userInBD = optUserInBD.get();
            userInBD.setName(user.getName());
            userInBD.setUsername(user.getUsername());
            userInBD.setEmail(user.getEmail());

            if (!userInBD.getPassword().equals(user.getPassword())) {
                BCryptPasswordEncoder bcpe = new BCryptPasswordEncoder();
                userInBD.setPassword(bcpe.encode(user.getPassword()));
            }

            userInBD.setPicture(user.getPicture());

            return userRepository.save(userInBD);
        }

        return null;
    }

    public User updateUser(User user) {
        return updateUser(user.getId(), user);
    }



    public void recoverPasswordByEmail(String email) throws Exception{
        /*Optional<User> userOpt = userRepository.findByEmail(email);
        if(userOpt.isEmpty())
            throw new Exception("No se encontró al usuario");

        User user = userOpt.get();*/
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);

        mailSender.setUsername("sharetravel.official@gmail.com");
        mailSender.setPassword("secret");
        MailSSLSocketFactory sf = new MailSSLSocketFactory();
        sf.setTrustAllHosts(true);

        Properties props = mailSender.getJavaMailProperties();
        //props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");
        //props.put("mail.imap.ssl.socketFactory", sf);
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("sharetravel.official@gmail.com");
        message.setTo("secret@gmail.com");
        message.setSubject("Prueba subject");
        message.setText("Prueba texto");
        mailSender.send(message);
    }

    public BasicUser searchUser(String query){
        Optional<User> user = this.findUser(query);
        if(user.isPresent()){
            return new BasicUser(user.get().getId(), user.get().getName(),
                    user.get().getEmail(), user.get().getUsername(), user.get().getPicture());
        }else{
            return null;
        }
    }

}
