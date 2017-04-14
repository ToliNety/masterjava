package ru.javaops.masterjava.persist.dao;

import com.bertoncelj.jdbi.entitymapper.EntityMapperFactory;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapperFactory;
import ru.javaops.masterjava.persist.model.EmailResult;

import java.util.List;

/**
 * Created by tolikswx on 14.04.2017.
 */
@RegisterMapperFactory(EntityMapperFactory.class)
public abstract class EmailResultDao implements AbstractDao {
    public EmailResult insert(EmailResult emailResult) {
        if (emailResult.isNew()) {
            int id = insertGeneratedId(emailResult);
            emailResult.setId(id);
        } else {
            insertWitId(emailResult);
        }

        return emailResult;
    }

    @SqlUpdate("INSERT INTO email_results (subject, body, success, result_error) VALUES (:subject, :body, :success, :resultError)")
    @GetGeneratedKeys
    abstract int insertGeneratedId(@BindBean EmailResult emailResult);

    @SqlUpdate("INSERT INTO email_results (id, subject, body, success, result_error) VALUES (:id, :subject, :body, :success, :resultError)")
    abstract void insertWitId(@BindBean EmailResult emailResult);

    @SqlQuery("SELECT * FROM email_results ORDER BY id DESC LIMIT :it")
    public abstract List<EmailResult> getWithLimit(@Bind int limit);

    @SqlUpdate("TRUNCATE email_results CASCADE")
    @Override
    public abstract void clean();
}
