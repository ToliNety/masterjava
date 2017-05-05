package ru.javaops.web.handler;

import com.google.common.collect.ImmutableList;
import com.sun.xml.ws.api.handler.MessageHandlerContext;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import ru.javaops.web.Statistics;

import javax.xml.ws.handler.MessageContext;
import java.util.List;
import java.util.Map;

/**
 * Created by tolikswx on 05.05.2017.
 */
@Slf4j
public class SoapStatisticHandler extends SoapBaseHandler {
    private static final String STATISTIC = "Statistic";

    @Override
    public boolean handleMessage(MessageHandlerContext context) {
        val headers = (Map<String, List<String>>) context.get(MessageContext.HTTP_REQUEST_HEADERS);

        if (isOutbound(context)) {
            logStatistic(context, Statistics.RESULT.SUCCESS);
        } else {
            headers.put(STATISTIC, ImmutableList.of(
                    context.getMessage().getPayloadLocalPart(),
                    String.valueOf(System.currentTimeMillis())));
        }

        return true;
    }

    @Override
    public boolean handleFault(MessageHandlerContext context) {
        logStatistic(context, Statistics.RESULT.FAIL);
        return true;
    }

    private void logStatistic(MessageHandlerContext context, Statistics.RESULT result) {
        val headers = (Map<String, List<String>>) context.get(MessageContext.HTTP_REQUEST_HEADERS);

        List<String> list = headers.get(STATISTIC);
        if (list != null) {
            Statistics.count(list.get(0), Long.parseLong(list.get(1)), result);
        }
    }
}
