package com.in.jplearning.utils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EmailUtils {

    private final String email = "jplearning.noreply@gmail.com";
    private final JavaMailSender emailSender;

    public void sendVerifyCode(String to, String verification_code, String code) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message,true);
        helper.setFrom(email);
        helper.setTo(to);
        helper.setSubject(verification_code);
        String htmlMsg = "<p>Your verification code is : <b>"+ code +"</b></p>";
        message.setContent(htmlMsg,"text/html");
        emailSender.send(message);
    }
}
