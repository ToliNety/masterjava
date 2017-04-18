package ru.javaops.masterjava.export;

import com.google.common.base.Splitter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import ru.javaops.masterjava.export.PayloadImporter.FailedEmail;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.dao.GroupDao;
import ru.javaops.masterjava.persist.dao.UserDao;
import ru.javaops.masterjava.persist.model.*;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * gkislin
 * 14.10.2016
 */
@Slf4j
public class UserImporter {

    private static final int NUMBER_THREADS = 4;
    private final ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_THREADS);
    private final UserDao userDao = DBIProvider.getDao(UserDao.class);
    private final GroupDao groupDao = DBIProvider.getDao(GroupDao.class);

    public List<FailedEmail> process(StaxStreamProcessor processor, Map<String, City> cities, Map<String, Group> groups, int chunkSize) throws XMLStreamException {
        log.info("Start proseccing with chunkSize=" + chunkSize);

        return new Callable<List<FailedEmail>>() {
            @Value
            class ChunkItem {
                User user;
                List<UserGroup> userGroups;
            }

            class ChunkFuture {
                String emailRange;
                Future<List<String>> future;

                public ChunkFuture(List<User> chunk, Future<List<String>> future) {
                    this.future = future;
                    this.emailRange = chunk.get(0).getEmail();
                    if (chunk.size() > 1) {
                        this.emailRange += '-' + chunk.get(chunk.size() - 1).getEmail();
                    }
                }
            }

            @Override
            public List<FailedEmail> call() throws XMLStreamException {
                List<ChunkFuture> futures = new ArrayList<>();

                int id = userDao.getSeqAndSkip(chunkSize);
                List<ChunkItem> chunk = new ArrayList<>(chunkSize);
                List<FailedEmail> failed = new ArrayList<>();

                while (processor.doUntil(XMLEvent.START_ELEMENT, "User")) {
                    final String email = processor.getAttribute("email");
                    String cityRef = processor.getAttribute("city");
                    City city = cities.get(cityRef);
                    if (city == null) {
                        failed.add(new FailedEmail(email, "City '" + cityRef + "' is not present in DB"));
                    } else {
                        String refs = processor.getAttribute("groupRefs");
                        if (Objects.isNull(refs)) refs = "";

                        List<String> groupRefs = Splitter.on(' ')
                                .trimResults()
                                .omitEmptyStrings()
                                .splitToList(refs);

                        if (!groups.keySet().containsAll(groupRefs)) {
                            failed.add(new FailedEmail(email, "Some of userGroups '" + groupRefs + "' are not present in DB"));
                        } else {
                            final UserFlag flag = UserFlag.valueOf(processor.getAttribute("flag"));
                            final String fullName = processor.getReader().getElementText();
                            final User user = new User(id++, fullName, email, flag, city.getId());

                            chunk.add(new ChunkItem(user,
                                    StreamEx.of(groupRefs)
                                            .map(group -> new UserGroup(user.getId(), groups.get(group).getId()))
                                            .collect(Collectors.toList())));
                            if (chunk.size() == chunkSize) {
                                futures.add(submit(chunk));
                                chunk = new ArrayList<>(chunkSize);
                                id = userDao.getSeqAndSkip(chunkSize);
                            }
                        }
                    }
                }

                if (!chunk.isEmpty()) {
                    futures.add(submit(chunk));
                }

                futures.forEach(cf -> {
                    try {
                        failed.addAll(StreamEx.of(cf.future.get()).map(email -> new FailedEmail(email, "already present")).toList());
                        log.info(cf.emailRange + " successfully executed");
                    } catch (Exception e) {
                        log.error(cf.emailRange + " failed", e);
                        failed.add(new FailedEmail(cf.emailRange, e.toString()));
                    }
                });
                return failed;
            }

            private ChunkFuture submit(List<ChunkItem> chunk) {
                List<User> users = chunk.stream().map(ChunkItem::getUser).collect(Collectors.toList());
                ChunkFuture chunkFuture = new ChunkFuture(users,
                        executorService.submit(() -> {
                            List<User> conflictedUsers = userDao.insertAndGetConflictEmails(users);
                            groupDao.insertBatch(StreamEx.of(chunk)
                                    .flatMap(chunkItem -> chunkItem.getUserGroups().stream())
                                    .filter(userGroup -> !conflictedUsers.stream()
                                            .map(BaseEntity::getId).collect(Collectors.toList())
                                            .contains(userGroup.getUserId()))
                                    .collect(Collectors.toList()));
                            return StreamEx.of(conflictedUsers).map(User::getEmail).collect(Collectors.toList());
                        }));
                log.info("Submit " + chunkFuture.emailRange);
                return chunkFuture;
            }
        }.call();
    }
}
