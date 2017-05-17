package ru.javaops.masterjava.service.mail.util;

import lombok.AllArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CloseShieldInputStream;
import ru.javaops.masterjava.service.mail.Attach;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Attachments {
    public static Attach getAttach(String name, InputStream inputStream) {
        return new Attach(name, new DataHandler(new InputStreamDataSource(inputStream)));
    }

    public static byte[] getBytesFromAttach(Attach attach) {
        if (attach != null) {
            try {
                InputStream is = attach.getDataHandler().getInputStream();
                return IOUtils.toByteArray(is);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new byte[0];
    }


    //    http://stackoverflow.com/questions/2830561/how-to-convert-an-inputstream-to-a-datahandler
    //    http://stackoverflow.com/a/5924019/548473

    @AllArgsConstructor
    private static class InputStreamDataSource implements ProxyDataSource {
        private InputStream inputStream;

        @Override
        public InputStream getInputStream() throws IOException {
            return new CloseShieldInputStream(inputStream);
        }
    }

    public interface ProxyDataSource extends DataSource {
        @Override
        default OutputStream getOutputStream() throws IOException {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        default String getContentType() {
            return "application/octet-stream";
        }

        @Override
        default String getName() {
            return "";
        }
    }
}
