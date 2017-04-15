package ru.javaops.masterjava.service.mail;

import lombok.experimental.var;
import lombok.extern.slf4j.Slf4j;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.DBITestProvider;
import ru.javaops.masterjava.persist.dao.EmailResultDao;
import ru.javaops.masterjava.persist.dao.FailedEmailDao;
import ru.javaops.masterjava.persist.model.EmailResult;
import ru.javaops.masterjava.persist.model.FailedEmail;

import java.util.List;
import java.util.stream.Collectors;

/**
 * gkislin
 * 15.11.2016
 */
@Slf4j
public class MailSender {
    static {
        DBITestProvider.initDBI();
    }

    private static final MailServiceExecutor MAIL_EXECUTOR = new MailServiceExecutor();
    private static final EmailResultDao RESULT_DAO = DBIProvider.getDao(EmailResultDao.class);
    private static final FailedEmailDao FAILED_EMAIL_DAO = DBIProvider.getDao(FailedEmailDao.class);

    static String sendMail(List<Addressee> to, List<Addressee> cc, String subject, String body) {
        MailServiceExecutor.GroupResult groupResult = MAIL_EXECUTOR.sendToList(to, subject, body);
        log.info(groupResult.toString());

        EmailResult emailResult = new EmailResult(subject, body, groupResult.getSuccess(), groupResult.getFailedCause());

        DBIProvider.getDBI().useTransaction((conn, status) -> {
            RESULT_DAO.insert(emailResult);

            if (groupResult.getFailed() != null) {
                List<FailedEmail> failedEmails = groupResult.getFailed().stream()
                        .map(mailResult -> new FailedEmail(mailResult.getEmail(), mailResult.getResult(), emailResult.getId()))
                        .collect(Collectors.toList());
                emailResult.setFailedEmails(failedEmails);
                FAILED_EMAIL_DAO.insertBatch(failedEmails);
            }
        });

        log.info(emailResult.toString());
        return groupResult.toString();
    }
}
