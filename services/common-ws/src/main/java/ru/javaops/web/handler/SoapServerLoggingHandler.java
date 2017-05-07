package ru.javaops.web.handler;


import org.slf4j.event.Level;

public class SoapServerLoggingHandler extends SoapLoggingHandler {

    public SoapServerLoggingHandler() {
        //TODO Add logger level from hosts.conf
        super(Level.INFO);
    }

    @Override
    protected boolean isRequest(boolean isOutbound) {
        return !isOutbound;
    }
}