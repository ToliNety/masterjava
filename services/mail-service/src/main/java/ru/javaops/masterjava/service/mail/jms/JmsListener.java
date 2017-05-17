package ru.javaops.masterjava.service.mail.jms;

import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;
import ru.javaops.masterjava.service.mail.*;
import ru.javaops.masterjava.service.mail.util.Attachments;

import javax.activation.DataHandler;
import javax.jms.*;
import javax.naming.InitialContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@WebListener
@Slf4j
public class JmsListener implements ServletContextListener {
    private Thread listenerThread = null;
    private QueueConnection connection;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            InitialContext initCtx = new InitialContext();
            ActiveMQConnectionFactory factory = (ActiveMQConnectionFactory) initCtx.lookup("java:comp/env/jms/ConnectionFactory");
            factory.setTrustAllPackages(true);
            QueueConnectionFactory connectionFactory = (QueueConnectionFactory) factory;
            connection = connectionFactory.createQueueConnection();
            QueueSession queueSession = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = (Queue) initCtx.lookup("java:comp/env/jms/queue/MailQueue");
            QueueReceiver receiver = queueSession.createReceiver(queue);
            connection.start();
            log.info("Listen JMS messages ...");
            listenerThread = new Thread(() -> {
                try {
                    while (!Thread.interrupted()) {
                        Message m = receiver.receive();
                        // TODO implement mail sending
                        if (m instanceof TextMessage) {
                            TextMessage tm = (TextMessage) m;
                            String text = tm.getText();
                            log.info(String.format("Received TextMessage with text '%s'.", text));
                        }

                        if (m instanceof ObjectMessage) {
                            MailDTO mail = (MailDTO) ((ObjectMessage) m).getObject();
                            log.info(String.format("Received ObjectMessage: '%s'.", mail));

                            List<Attach> attaches;
                            String attachName = mail.getAttachName();
                            if (attachName.isEmpty()) {
                                attaches = ImmutableList.of();
                            } else {
                                Attach attach = new Attach(attachName,
                                        new DataHandler((Attachments.ProxyDataSource) mail::getInputStream));

                                attaches = ImmutableList.of(attach);
                            }

                            MailServiceExecutor.sendBulk(
                                    MailWSClient.split(mail.getUsers()),
                                    mail.getSubject(), mail.getBody(), attaches);
                        }
                    }
                } catch (Exception e) {
                    log.error("Receiving messages failed: " + e.getMessage(), e);
                }
            });
            listenerThread.start();
        } catch (Exception e) {
            log.error("JMS failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (connection != null) {
            try {
                connection.close();
            } catch (JMSException ex) {
                log.warn("Couldn't close JMSConnection: ", ex);
            }
        }
        if (listenerThread != null) {
            listenerThread.interrupt();
        }
    }
}