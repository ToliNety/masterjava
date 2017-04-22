package ru.javaops.masterjava.service.mail;

import ru.javaops.masterjava.service.mailAPI.Addressee;
import ru.javaops.masterjava.service.mailAPI.GroupResult;
import ru.javaops.masterjava.service.mailAPI.MailService;

import javax.jws.WebService;
import java.util.Set;

@WebService(endpointInterface = "ru.javaops.masterjava.service.mailAPI.MailService", targetNamespace = "http://mail.javaops.ru/"
//          , wsdlLocation = "WEB-INF/wsdl/mailService.wsdl"
)
public class MailServiceImpl implements MailService {

    @Override
    public String sendToGroup(Set<Addressee> to, Set<Addressee> cc, String subject, String body) {
        return MailSender.sendToGroup(to, cc, subject, body);
    }

    @Override
    public GroupResult sendBulk(Set<Addressee> to, String subject, String body) {
        return MailServiceExecutor.sendBulk(to, subject, body);
    }
}