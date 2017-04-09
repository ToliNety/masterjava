package ru.javaops.masterjava.persist.model;

import lombok.*;

/**
 * Created by tolikswx on 07.04.2017.
 */
@Data
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Group extends BaseEntity {
    @NonNull
    private String name;
    @NonNull
    private GroupType groupType;

    public Group(Integer id, String name, GroupType groupType) {
        this(name, groupType);
        this.id = id;
    }
}
