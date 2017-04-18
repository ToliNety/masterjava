package ru.javaops.masterjava.persist.dao;

import com.bertoncelj.jdbi.entitymapper.EntityMapperFactory;
import one.util.streamex.StreamEx;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapperFactory;
import ru.javaops.masterjava.persist.model.Group;
import ru.javaops.masterjava.persist.model.UserGroup;

import java.util.List;
import java.util.Map;

@RegisterMapperFactory(EntityMapperFactory.class)
public abstract class GroupDao implements AbstractDao {

    @SqlUpdate("TRUNCATE groups CASCADE ")
    @Override
    public abstract void clean();

    @SqlQuery("SELECT * FROM groups ORDER BY name")
    public abstract List<Group> getAll();

    public Map<String, Group> getAsMap() {
        return StreamEx.of(getAll()).toMap(Group::getName, g -> g);
    }

    @SqlUpdate("INSERT INTO groups (name, type, project_id)  VALUES (:name, CAST(:type AS group_type), :projectId)")
    @GetGeneratedKeys
    public abstract int insertGeneratedId(@BindBean Group groups);

    @SqlBatch("INSERT INTO groups (name, type, project_id)  VALUES (:name, CAST(:type AS group_type), :projectId)")
    public abstract void insertGroupBatch(@BindBean List<Group> groups);

    @SqlBatch("INSERT INTO user_group (user_id, group_id)  VALUES (:userId, :groupId)")
    public abstract void insertBatch(@BindBean List<UserGroup> userGroups);

    public void insert(Group groups) {
        int id = insertGeneratedId(groups);
        groups.setId(id);
    }
}
