package ru.javaops.masterjava.service.mail;

import ru.javaops.masterjava.ExceptionType;
import ru.javaops.web.WebStateException;

import javax.jws.HandlerChain;
import javax.jws.WebService;
import java.util.List;
import java.util.Set;

@WebService(endpointInterface = "ru.javaops.masterjava.service.mail.MailService", targetNamespace = "http://mail.javaops.ru/"
//          , wsdlLocation = "WEB-INF/wsdl/mailService.wsdl"
)
//@StreamingAttachment(parseEagerly=true, memoryThreshold=40000L)
//@MTOM
@HandlerChain(file = "mailWsHandlers.xml")
public class MailServiceImpl implements MailService {

    @Override
    public String sendToGroup(Set<Addressee> to, Set<Addressee> cc, String subject, String body, List<Attach> attaches) throws WebStateException {
        String result = MailSender.sendToGroup(to, cc, subject, body, attaches);
        if (!result.equals(MailResult.OK)) {
            throw new WebStateException(result, ExceptionType.EMAIL);
        }
        return result;
    }

    @Override
    public GroupResult sendBulk(Set<Addressee> to, String subject, String body, List<Attach> attaches) throws WebStateException {
        GroupResult result = MailServiceExecutor.sendBulk(to, subject, body, attaches);
        if (result.getFailedCause() != null) {
            throw new WebStateException(result.getFailedCause(), ExceptionType.SYSTEM);
        }

        if (!result.getFailed().isEmpty()) {
            throw new WebStateException(result.toString(), ExceptionType.EMAIL);
        }

        return result;
    }
}