package ru.javaops.masterjava.persist.model;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tolikswx on 07.04.2017.
 */
@Data
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Project extends BaseEntity {
    @NonNull
    private String name;
    @NonNull
    private String description;

    private List<Group> groups;

    public Project(Integer id, String name, String description) {
        this(name, description);
        this.id = id;
    }

    public List<Group> getGroups() {
        if (groups == null) {
            groups = new ArrayList<>();
        }
        return groups;
    }
}
