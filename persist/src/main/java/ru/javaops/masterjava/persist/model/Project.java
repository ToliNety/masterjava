package ru.javaops.masterjava.persist.model;

import lombok.*;

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

    public Project(Integer id, String name, String description) {
        this(name, description);
        this.id = id;

    }
}
