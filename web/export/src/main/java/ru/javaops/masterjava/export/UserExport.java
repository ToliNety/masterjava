package ru.javaops.masterjava.export;

import one.util.streamex.IntStreamEx;
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
    public List<String> process(final InputStream is, int chunkSize) throws XMLStreamException {
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
                    StringBuilder sb = new StringBuilder();
                    try {
                        sb.append(fbr.get().emailRange);
                        List<User> notAddedUsers = fbr.get().notAddedUsers;
                        if (!notAddedUsers.isEmpty()) {
                            sb.append(
                                    notAddedUsers.stream()
                                    .map(User::getFullName)
                                    .collect(Collectors.joining(", ")));
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                    return sb.toString();
                }).collect(Collectors.toList());
    }

    private class BatchResult {
        List<User> notAddedUsers;
        String emailRange;

        public BatchResult(List<User> notAddedUsers, String emailRange) {
            this.notAddedUsers = notAddedUsers;
            this.emailRange = emailRange;
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
