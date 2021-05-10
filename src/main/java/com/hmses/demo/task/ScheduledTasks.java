package com.hmses.demo.task;


import com.hmses.demo.domain.EmailResponse;
import com.hmses.demo.service.rabbitmq.RabbitSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.search.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

@Component
public class ScheduledTasks {

    private final String subject = "Attendance Confirmation";

    @Autowired
    private RabbitSender rabbitSender;

    @Value("${mail.imap.host}")
    private String imapHost;

    @Value("${mail.imap.port}")
    private String imapPort;

    @Value("${mail.imap.starttls.enable}")
    private String starttls;

    @Value("${mail.imap.username}")
    private String username;

    @Value("${mail.imap.password}")
    private String password;

    @Value("${mail.imap.store.protocol}")
    private String protocol;

    @Value("${mail.imap.folder.name}")
    private String folderName;

    private Properties properties = new Properties();

    @PostConstruct
    private void initProperties() {
        properties.put("mail.imap.host", imapHost);
        properties.put("mail.imap.port", imapPort);
        properties.put("mail.imap.starttls.enable", starttls);
    }

    @Scheduled(fixedDelay = 60000)
    public void readEmails() throws MessagingException, IOException {
        Session session = Session.getDefaultInstance(properties);
        Store store = session.getStore(protocol);
        store.connect(imapHost, username, password);
        Folder folder = store.getFolder(folderName);
        folder.open(Folder.READ_ONLY);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        ReceivedDateTerm receivedDateTerm  = new ReceivedDateTerm(
                ComparisonTerm.EQ,
                new Date(cal.getTimeInMillis())
        );
        SearchTerm searchTerm = new AndTerm(receivedDateTerm, new SubjectTerm(subject));
        Message[] messages = folder.search(searchTerm);
        Arrays.stream(messages).forEach(message -> {
            EmailResponse emailResponse = new EmailResponse();
            try {
                emailResponse.setResponse(message.getContent().toString().trim().toLowerCase());
                emailResponse.setEmailAddress(message.getFrom()[0].toString());
            } catch (IOException | MessagingException e) {
                // TODO: Log.ERROR the exception along with any other relevant information
                // In production setting, these logs should be forwarded, analyzed and acted upon for remediation
                e.printStackTrace();
            }
            rabbitSender.send(emailResponse);
        });

        folder.close(false);
        store.close();

    }

}
