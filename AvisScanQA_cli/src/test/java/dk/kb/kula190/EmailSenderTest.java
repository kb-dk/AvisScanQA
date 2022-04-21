package dk.kb.kula190;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.activation.MimeType;
import java.io.IOException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class EmailSenderTest {

    @Test
    @Disabled("Do not send mail as automatic test")
    void send() throws IOException {
        Properties emailConfig;
//Hardcoded email config
        emailConfig = new Properties();

        emailConfig.setProperty("mail.smtp.auth", "false");
        emailConfig.setProperty("mail.smtp.starttls.enable", "true");
        emailConfig.setProperty("mail.smtp.host", "post.kb.dk");
        emailConfig.setProperty("mail.smtp.port", "25");

        EmailSender.newInstance()
                   .to("pabr@kb.dk")
                   .from("test@avisscanQA.dk") //comma-separated
                   .cc("abr@kb.dk") //comma-separated
                   //  .bcc(bcc) //comma-separated
                   .subject("subject")
//                   .bodyText("https://google.com") //Plain text
                   .bodyText("<a href='https://google.com'>https://google.com</a>",
                             "text/html") //html email
//                   .attachment(pdfFile)
                   .send(emailConfig);
    }
}