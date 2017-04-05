package ru.javaops.masterjava.export;

import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.dao.UserDao;
import ru.javaops.masterjava.persist.model.User;
import ru.javaops.masterjava.persist.model.UserFlag;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * gkislin
 * 14.10.2016
 */
public class UserExport {

    private UserDao userDao = DBIProvider.getDao(UserDao.class);

    public static final int THREADS = 4;


    /**
     * @param is        thr payload input stream
     * @param chunkSize the batch chunk size
     * @return users, already present in DB
     * @throws XMLStreamException
     */
    public List<BatchResult> process(final InputStream is, int chunkSize) throws XMLStreamException {
        final StaxStreamProcessor processor = new StaxStreamProcessor(is);
        final ExecutorService executorService = Executors.newFixedThreadPool(THREADS);

        List<Future<BatchResult>> futureBatchResults = new ArrayList<>();

        List<User> users = new ArrayList<>();

        ///Processing xml doc
        while (processor.doUntil(XMLEvent.START_ELEMENT, "User")) {
            final String email = processor.getAttribute("email");
            final UserFlag flag = UserFlag.valueOf(processor.getAttribute("flag"));
            final String fullName = processor.getReader().getElementText();
            final User user = new User(fullName, email, flag);
            users.add(user);

            if (users.size() == chunkSize) {
                addToDB(new ArrayList<>(users), futureBatchResults, executorService, chunkSize);
                users.clear();
            }
        }

        if (!users.isEmpty()) {
            addToDB(new ArrayList<>(users), futureBatchResults, executorService, chunkSize);
        }

        return futureBatchResults.stream()
                .map((fbr) -> {
                    try {
                        return fbr.get();
                    } catch (InterruptedException | ExecutionException e) {
                        return new BatchResult(e.getMessage());
                    }
                })
                .collect(Collectors.toList());
    }

    public class BatchResult {
        public List<User> users;
        public String emailRange;
        public String message;

        public BatchResult(List<User> notAddedUsers, String emailRange) {
            this.users = notAddedUsers;
            this.emailRange = emailRange;
            this.message = String.format("Already present user. Email range: %s. Users: ", emailRange);
        }

        public BatchResult(String message) {
            this.message = message;
        }
    }

    private void addToDB(List<User> users,
                         List<Future<BatchResult>> futureBatchResults,
                         ExecutorService executorService,
                         int chunkSize) {
        futureBatchResults.add(executorService.submit(() -> {
            List<User> notAddedUsers = userDao.insertBatchAndGetNotAddedUsers(users, chunkSize);

            StringBuilder emailRange = new StringBuilder(users.get(0).getEmail());
            if (users.size() > 1) {
                emailRange.append(" - ");
                emailRange.append(users.get(users.size() - 1).getEmail());
            }
            emailRange.append(" : ");

            return new BatchResult(notAddedUsers, emailRange.toString());
        }));
    }
}
