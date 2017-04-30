package ru.javaops.masterjava.service.mail;

import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;
import ru.javaops.web.WebStateException;

import javax.jws.WebService;
import javax.xml.ws.soap.MTOM;
import java.io.File;
import java.io.IOException;
import java.util.Set;

@WebService(endpointInterface = "ru.javaops.masterjava.service.mail.MailService", targetNamespace = "http://mail.javaops.ru/"
//          , wsdlLocation = "WEB-INF/wsdl/mailService.wsdl"
)
@MTOM(threshold = 5)
@Slf4j
public class MailServiceImpl implements MailService {

    @Override
    public String sendToGroup(Set<Addressee> to, Set<Addressee> cc, String subject, String body) throws WebStateException {
        return MailSender.sendToGroup(to, cc, subject, body, writeToFile("", new byte[0]));
    }

    @Override
    public GroupResult sendBulk(Set<Addressee> to, String subject, String body, String fileName, byte[] bytes) throws WebStateException {
        return MailServiceExecutor.sendBulk(to, subject, body, writeToFile(fileName, bytes));
    }

    private File writeToFile(String fileName, byte[] bytes) {
        if (bytes.length > 0 && fileName != null && !fileName.isEmpty()) {
            try {
                File dir = Files.createTempDir();
                File file = new File(dir.getAbsolutePath() + "\\" + fileName);
                Files.write(bytes, file);
                log.info("Received mail with attachment. Create file: " + file.getAbsolutePath());
                return file;
            } catch (IOException e) {
                log.error("Error to write TMP file");
            }
        }

        return null;
    }
}