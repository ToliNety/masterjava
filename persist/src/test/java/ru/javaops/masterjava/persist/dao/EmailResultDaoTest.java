package ru.javaops.masterjava.persist.dao;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.javaops.masterjava.persist.EmailResultTestData;
import ru.javaops.masterjava.persist.model.EmailResult;

import java.util.List;

import static ru.javaops.masterjava.persist.EmailResultTestData.RESULTS;

/**
 * Created by tolikswx on 14.04.2017.
 */
public class EmailResultDaoTest extends AbstractDaoTest<EmailResultDao> {
    public EmailResultDaoTest() {
        super(EmailResultDao.class);
    }

    @BeforeClass
    public static void init() throws Exception {
        EmailResultTestData.init();
    }

    @Before
    public void setUp() throws Exception {
        EmailResultTestData.setUp();
    }

    @Test
    public void getWithLimit() {
        List<EmailResult> resultList = dao.getWithLimit(5);
        Assert.assertEquals(resultList, RESULTS);
    }
}
