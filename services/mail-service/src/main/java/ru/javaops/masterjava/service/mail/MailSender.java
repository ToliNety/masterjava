package ru.javaops.masterjava.service.mail;

import lombok.extern.slf4j.Slf4j;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.dao.EmailResultDao;
import ru.javaops.masterjava.persist.model.EmailResult;

import java.util.List;

/**
 * gkislin
 * 15.11.2016
 */
@Slf4j
public class MailSender {
    private static final MailServiceExecutor MAIL_EXECUTOR = new MailServiceExecutor();
    private static final EmailResultDao DAO = DBIProvider.getDao(EmailResultDao.class);

    static String sendMail(List<Addressee> to, List<Addressee> cc, String subject, String body) {
        MailServiceExecutor.GroupResult groupResult = MAIL_EXECUTOR.sendToList(to, subject, body);
        DAO.insert(new EmailResult(subject, body, groupResult.getSuccess(), groupResult.getFailedCause()));
        log.info(groupResult.toString());
        return groupResult.toString();
    }
}
