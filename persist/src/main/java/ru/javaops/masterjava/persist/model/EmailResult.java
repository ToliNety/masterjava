package ru.javaops.masterjava.persist.model;

import com.bertoncelj.jdbi.entitymapper.Column;
import lombok.*;

/**
 * Created by tolikswx on 14.04.2017.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@RequiredArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
public class EmailResult extends BaseEntity {
    @NonNull
    private String subject;

    @NonNull
    private String body;

    private int success;

    @Column("result_error")
    private String resultError;

    public EmailResult(Integer id, String subject, String body, int success, String resultError) {
        this(subject, body, success, resultError);
        this.id = id;
    }
}
