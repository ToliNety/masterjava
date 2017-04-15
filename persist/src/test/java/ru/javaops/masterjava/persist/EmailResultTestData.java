package ru.javaops.masterjava.persist;

import com.google.common.collect.ImmutableList;
import ru.javaops.masterjava.persist.dao.EmailResultDao;
import ru.javaops.masterjava.persist.model.EmailResult;

import java.util.List;

/**
 * Created by tolikswx on 14.04.2017.
 */
public class EmailResultTestData {
    public static EmailResult RESULT_1;
    public static List<EmailResult> RESULTS;

    public static int RESULT_ID;

    public static void init() {
        RESULT_1 = new EmailResult("test mail",
                "MasterJava test mail. Don't Answer on this email", 3, null);
        RESULTS = ImmutableList.of(RESULT_1);
    }

    public static void setUp() {
        EmailResultDao dao = DBIProvider.getDao(EmailResultDao.class);
        dao.clean();
        dao.insert(RESULT_1);
        RESULT_ID = RESULT_1.getId();
    }
}
