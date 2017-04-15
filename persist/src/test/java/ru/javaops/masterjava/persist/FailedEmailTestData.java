package ru.javaops.masterjava.persist;

import com.google.common.collect.ImmutableList;
import ru.javaops.masterjava.persist.dao.FailedEmailDao;
import ru.javaops.masterjava.persist.model.FailedEmail;

import java.util.List;

import static ru.javaops.masterjava.persist.EmailResultTestData.RESULT_ID;

/**
 * Created by tolikswx on 14.04.2017.
 */
public class FailedEmailTestData {
    public static FailedEmail FAILED_EMAIL_1;
    public static FailedEmail FAILED_EMAIL_2;

    public static List<FailedEmail> FAILED_EMAILS;

    public static void init() {
        FAILED_EMAIL_1 = new FailedEmail("some@mail.ru", "connection error", RESULT_ID);
        FAILED_EMAIL_2 = new FailedEmail("asdf", "bad email", RESULT_ID);
        FAILED_EMAILS = ImmutableList.of(FAILED_EMAIL_1, FAILED_EMAIL_2);
    }

    public static void setUp() {
        FailedEmailDao dao = DBIProvider.getDao(FailedEmailDao.class);
        dao.clean();
        FAILED_EMAILS.forEach(failedEmail -> failedEmail.setResultId(RESULT_ID));
        dao.insertBatch(FAILED_EMAILS);
    }
}
