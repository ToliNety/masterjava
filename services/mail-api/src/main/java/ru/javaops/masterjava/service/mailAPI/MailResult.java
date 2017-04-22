package ru.javaops.masterjava.service.mailAPI;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MailResult {
    public static final String OK = "OK";

    private String email;
    private String result;

    public boolean isOk() {
        return OK.equals(result);
    }

    @Override
    public String toString() {
        return '(' + email + ',' + result + ')';
    }
}
