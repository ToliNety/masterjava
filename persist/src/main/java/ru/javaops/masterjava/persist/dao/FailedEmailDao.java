package ru.javaops.masterjava.persist.dao;

import com.bertoncelj.jdbi.entitymapper.EntityMapperFactory;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapperFactory;
import ru.javaops.masterjava.persist.model.FailedEmail;

import java.util.List;

/**
 * Created by tolikswx on 14.04.2017.
 */
@RegisterMapperFactory(EntityMapperFactory.class)
public abstract class FailedEmailDao implements AbstractDao {
    @SqlQuery("SELECT * FROM failed_emails WHERE result_id = :it")
    public abstract List<FailedEmail> getByResult(@Bind int result_id);

    @SqlBatch("INSERT INTO failed_emails (email, fail_cause, result_id) VALUES (:email, :failCause, :resultId)" +
            "ON CONFLICT DO NOTHING")
    public abstract void insertBatch(@BindBean List<FailedEmail> failedEmails);

    @SqlUpdate("TRUNCATE failed_emails CASCADE")
    @Override
    public abstract void clean();
}
