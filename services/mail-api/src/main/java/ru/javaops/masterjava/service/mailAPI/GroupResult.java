package ru.javaops.masterjava.service.mailAPI;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupResult {
    private int success; // number of successfully sent email
    private List<MailResult> failed; // failed emails with causes
    private String failedCause;  // global fail cause

    @Override
    public String toString() {
        return "Success: " + success + '\n' +
                "Failed: " + failed.toString() + '\n' +
                (failedCause == null ? "" : "Failed cause" + failedCause);
    }
}
