package ru.javaops.masterjava.service.mail;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * gkislin
 * 15.11.2016
 */
@Slf4j
public class MailSender {
    private static final MailServiceExecutor MAIL_EXECUTOR = new MailServiceExecutor();

    static String sendMail(List<Addressee> to, List<Addressee> cc, String subject, String body) {
        MailServiceExecutor.GroupResult groupResult = MAIL_EXECUTOR.sendToList(to, subject, body);
        log.info(groupResult.toString());
        return groupResult.toString();
    }
}
