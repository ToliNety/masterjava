package ru.javaops.masterjava.persist.model;

import com.bertoncelj.jdbi.entitymapper.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Created by tolikswx on 14.04.2017.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FailedEmail {
    @NonNull
    private String email;

    @Column("fail_cause")
    private String failCause;

    @NonNull
    @Column("result_id")
    private int resultId;
}
