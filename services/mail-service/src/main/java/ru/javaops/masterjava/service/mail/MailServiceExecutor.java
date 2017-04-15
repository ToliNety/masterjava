package ru.javaops.masterjava.service.mail;

import com.typesafe.config.Config;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import ru.javaops.masterjava.persist.config.Configs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
public class MailServiceExecutor {
    private static final String OK = "OK";

    private static final String INTERRUPTED_BY_FAULTS_NUMBER = "+++ Interrupted by faults number";
    private static final String INTERRUPTED_BY_TIMEOUT = "+++ Interrupted by timeout";
    private static final String INTERRUPTED_EXCEPTION = "+++ InterruptedException";
    private static final Config MAIL_SMTP_CONF = Configs.getConfig("mail.conf", "smtp");

    private final ExecutorService mailExecutor = Executors.newFixedThreadPool(8);

    public GroupResult sendToList(final List<Addressee> to, final String subject, final String body) {
        final CompletionService<MailResult> completionService = new ExecutorCompletionService<>(mailExecutor);

        List<Future<MailResult>> futures = to.stream()
                .map(email -> completionService.submit(() -> sendToUser(email, subject, body)))
                .collect(Collectors.toList());

        return new Callable<GroupResult>() {
            private int success = 0;
            private List<MailResult> failed = new ArrayList<>();

            @Override
            public GroupResult call() {
                while (!futures.isEmpty()) {
                    try {
                        Future<MailResult> future = completionService.poll(10, TimeUnit.SECONDS);
                        if (future == null) {
                            return cancelWithFail(INTERRUPTED_BY_TIMEOUT);
                        }
                        futures.remove(future);
                        MailResult mailResult = future.get();
                        if (mailResult.isOk()) {
                            success++;
                        } else {
                            failed.add(mailResult);
                            if (failed.size() >= 5) {
                                return cancelWithFail(INTERRUPTED_BY_FAULTS_NUMBER);
                            }
                        }
                    } catch (ExecutionException e) {
                        return cancelWithFail(e.getCause().toString());
                    } catch (InterruptedException e) {
                        return cancelWithFail(INTERRUPTED_EXCEPTION);
                    }
                }
/*
                for (Future<MailResult> future : futures) {
                    MailResult mailResult;
                    try {
                        mailResult = future.get(10, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        return cancelWithFail(INTERRUPTED_EXCEPTION);
                    } catch (ExecutionException e) {
                        return cancelWithFail(e.getCause().toString());
                    } catch (TimeoutException e) {
                        return cancelWithFail(INTERRUPTED_BY_TIMEOUT);
                    }
                    if (mailResult.isOk()) {
                        success++;
                    } else {
                        failed.add(mailResult);
                        if (failed.size() >= 5) {
                            return cancelWithFail(INTERRUPTED_BY_FAULTS_NUMBER);
                        }
                    }
                }
*/
                return new GroupResult(success, failed, null);
            }

            private GroupResult cancelWithFail(String cause) {
                futures.forEach(f -> f.cancel(true));
                return new GroupResult(success, failed, cause);
            }
        }.call();
    }

    public MailResult sendToUser(Addressee to, String subject, String body) throws Exception {
        try {
            Email email = createEMail(to, subject, body);
            email.send();
            log.info("Send mail to \'" + to + "\' subject \'" + subject + (log.isDebugEnabled() ? "\nbody=" + getBody(body, to) : ""));
        } catch (EmailException e) {
            log.debug(e.getMessage());
            return MailResult.error(to.getEmail(), e.getMessage());
        }
        return MailResult.ok(to.getEmail());
    }

    private Email createEMail(Addressee to, String subject, String body) throws EmailException {
        Email email = new SimpleEmail();
        email.setAuthenticator(new DefaultAuthenticator(
                MAIL_SMTP_CONF.getString("username"),
                MAIL_SMTP_CONF.getString("password")));
        email.setHostName(MAIL_SMTP_CONF.getString("host"));
        email.setSmtpPort(MAIL_SMTP_CONF.getInt("port"));
        email.setSSLOnConnect(MAIL_SMTP_CONF.getBoolean("useSSL"));
        email.setFrom(MAIL_SMTP_CONF.getString("fromEmail"));
        email.setSubject(subject);
        email.setMsg(getBody(body, to));
        email.addTo(to.getEmail());
        return email;
    }

    private String getBody(String body, Addressee to) {
        return String.format("Hello %s!\n\n", to.getName()) +
                body +
                String.format("\n\n---\nBest regards,\n%s", MAIL_SMTP_CONF.getString("fromName"));
    }

    @Getter
    public static class MailResult {
        private final String email;
        private final String result;

        private static MailResult ok(String email) {
            return new MailResult(email, OK);
        }

        private static MailResult error(String email, String errorMsg) {
            return new MailResult(email, errorMsg);
        }

        public boolean isOk() {
            return OK.equals(result);
        }

        private MailResult(String email, String cause) {
            this.email = email;
            this.result = cause;
        }

        @Override
        public String toString() {
            return '(' + email + ',' + result + ')';
        }
    }

    @Getter
    public static class GroupResult {
        private final int success; // number of successfully sent email
        private final List<MailResult> failed; // failed emails with causes
        private final String failedCause;  // global fail cause

        @Override
        public String toString() {
            return "Success: " + success + '\n' +
                    "Failed: " + failed.toString() + '\n' +
                    (failedCause == null ? "" : "Failed cause" + failedCause);
        }

        private GroupResult(int success, List<MailResult> failed, String failedCause) {
            this.success = success;
            this.failed = failed;
            this.failedCause = failedCause;
        }
    }
}