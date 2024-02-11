package com.in.jplearning.utils;

import lombok.AllArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EmailUtils {
    private final JavaMailSender emailSender;

}
