package ru.javaops.masterjava.persist.dao;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.javaops.masterjava.persist.EmailResultTestData;
import ru.javaops.masterjava.persist.FailedEmailTestData;
import ru.javaops.masterjava.persist.model.FailedEmail;

import java.util.List;

/**
 * Created by tolikswx on 14.04.2017.
 */
public class FailedEmailDaoTest extends AbstractDaoTest<FailedEmailDao> {
    public FailedEmailDaoTest() {
        super(FailedEmailDao.class);
    }

    @BeforeClass
    public static void init() {
        EmailResultTestData.init();
        FailedEmailTestData.init();
    }

    @Before
    public void setUp() {
        EmailResultTestData.setUp();
        FailedEmailTestData.setUp();
    }

    @Test
    public void testGet() throws Exception {
        List<FailedEmail> emails = dao.getByResult(EmailResultTestData.RESULT_ID);
        Assert.assertEquals(emails, FailedEmailTestData.FAILED_EMAILS);
    }

}
