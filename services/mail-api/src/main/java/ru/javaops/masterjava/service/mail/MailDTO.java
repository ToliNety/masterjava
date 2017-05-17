package ru.javaops.masterjava.service.mail;

import com.google.common.base.MoreObjects;
import com.google.common.primitives.Bytes;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Base64;
import java.util.List;

/**
 * Created by tolikswx on 17.05.2017.
 */
@Data
@AllArgsConstructor
public class MailDTO implements Serializable {
    private String users;
    private String subject;
    private String body;
    private String attachName;
    private byte[] attacheInByteArray;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("users", users)
                .add("subject", subject)
                .add("body", body)
                .add("attachName", attachName)
                .add("attachSize", attacheInByteArray.length)
                .toString();
    }

    public InputStream getInputStream() {
        return new ByteArrayInputStream(attacheInByteArray);
    }
}
