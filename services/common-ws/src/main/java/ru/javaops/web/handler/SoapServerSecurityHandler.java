package ru.javaops.web.handler;

import com.sun.xml.ws.api.handler.MessageHandlerContext;
import lombok.extern.slf4j.Slf4j;
import ru.javaops.web.AuthUtil;

import javax.xml.ws.handler.MessageContext;
import java.util.List;
import java.util.Map;

/**
 * Created by tolikswx on 05.05.2017.
 */
@Slf4j
public class SoapServerSecurityHandler extends SoapBaseHandler {
    @Override
    public boolean handleMessage(MessageHandlerContext context) {
        if (!isOutbound(context)) {
            Map<String, List<String>> headers = (Map<String, List<String>>) context.get(MessageContext.HTTP_REQUEST_HEADERS);

            int code = AuthUtil.checkBasicAuth(headers, AuthUtil.AUTH_HEADER);
            if (code != 0) {
                context.put(MessageContext.HTTP_RESPONSE_CODE, code);
                throw new SecurityException();
            }
            log.info("User authenticated");

        }
        return true;
    }

    @Override
    public boolean handleFault(MessageHandlerContext context) {
        return true;
    }
}
