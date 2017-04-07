package ru.javaops.masterjava.persist.dao;

import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.BatchChunkSize;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.model.Project;

import java.util.List;

/**
 * Created by tolikswx on 07.04.2017.
 */
public abstract class ProjectDao implements AbstractDao {
    @SqlQuery("SELECT nextval('user_seq')")
    abstract int getNextVal();

    @Transaction
    public int getSeqAndSkip(int step) {
        int id = getNextVal();
        DBIProvider.getDBI().useHandle(h -> h.execute("ALTER SEQUENCE user_seq RESTART WITH " + (id + step)));
        return id;
    }

    @SqlUpdate("TRUNCATE users")
    @Override
    public abstract void clean();

    @SqlBatch("INSERT INTO projects (name, description) VALUES (:name, :description)" +
            "ON CONFLICT DO NOTHING")
    public abstract int[] insertBatch(@BindBean List<Project> projects, @BatchChunkSize int chunkSize);
}
