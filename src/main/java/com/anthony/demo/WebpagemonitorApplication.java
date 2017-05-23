package com.anthony.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

@SpringBootApplication
public class WebpagemonitorApplication {

	@Value("${spring.mail.host}")
	String host;
	@Value("${spring.mail.port}")
	String port;
	@Value("${spring.mail.username}")
	String username;
	@Value("${spring.mail.password}")
	String password;


	public static void main(String[] args) {
		SpringApplication.run(WebpagemonitorApplication.class, args);
	}

	@Bean
	JavaMailSender javaMailSender() {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

		mailSender.setHost(host);
		mailSender.setPort(Integer.parseInt(port));

		mailSender.setUsername(username);
		mailSender.setPassword(password);

		Properties props = mailSender.getJavaMailProperties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.debug", "true");

		return mailSender;
	}

	//TODO:every 15 min run compare and send alert

	@Bean
	//@Profile("dev")
	CommandLineRunner setup(WebPageParser pageParser, EmailService emailService, @Value("${webpagemonitor.alert.to}") String to) throws IOException {

		return (args) -> {
			while(true) {
				boolean hasSignificantChange = pageParser.evaluateChange("https://www.simpleweb.org/");
				if (hasSignificantChange) {
					String subject = "Alert: Webpage Significantly Changed";
					String message = "The webpage https://www.simpleweb.org/ has changed significantly since it was last observed.";
					emailService.sendSimpleMessage(to, subject, message);
				} else {
					String subject = "Alert: Webpage NOT Changed";
					String message = "The webpage https://www.simpleweb.org/ has NOT changed much.";
					emailService.sendSimpleMessage(to, subject, message);
				}
				Thread.sleep(54000);
			}
		};
	}


}
