package ru.javaops.masterjava.persist.dao;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlBatch;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.BatchChunkSize;
import ru.javaops.masterjava.persist.model.Group;
import ru.javaops.masterjava.persist.model.Project;

import java.util.List;

/**
 * Created by tolikswx on 08.04.2017.
 */
public abstract class GroupDao implements AbstractDao {
    @SqlBatch("INSERT INTO groups (name, type, project_id) VALUES (:name, CAST(:groupType AS GROUP_TYPE), :it)" +
            "ON CONFLICT DO NOTHING")
    public abstract int[] insertBatch(@BindBean List<Group> groups, @Bind int projectID, @BatchChunkSize int chunkSize);

    @SqlUpdate("TRUNCATE groups")
    @Override
    public abstract void clean();
}
