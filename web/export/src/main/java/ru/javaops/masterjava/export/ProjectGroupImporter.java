package ru.javaops.masterjava.export;

import lombok.extern.slf4j.Slf4j;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.dao.GroupDao;
import ru.javaops.masterjava.persist.dao.ProjectDao;
import ru.javaops.masterjava.persist.model.Group;
import ru.javaops.masterjava.persist.model.GroupType;
import ru.javaops.masterjava.persist.model.Project;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by tolikswx on 18.04.2017.
 */
@Slf4j
public class ProjectGroupImporter {
    private final ProjectDao projectDao = DBIProvider.getDao(ProjectDao.class);
    private final GroupDao groupDao = DBIProvider.getDao(GroupDao.class);

    public Map<String, Group> process(StaxStreamProcessor processor) throws XMLStreamException {
        Map<String, Project> projectMap = projectDao.getAsMap();
        Map<String, Group> groupMap = groupDao.getAsMap();

        List<Group> groups = new ArrayList<>();
        String element;
        Project project = null;
        while ((element = processor.doUntilAny(XMLEvent.START_ELEMENT, "Project", "Group", "Cities")) != null) {
            if (element.equals("Cities")) break;
            if (element.equals("Project")) {
                String projectName = processor.getAttribute("name");
                if (projectMap.containsKey(projectName)) {
                    log.info("Duplicate. Project are already in DB: " + projectName);
                } else {
                    project = new Project(projectName,
                            processor.getElementValue("description"));
                    projectDao.insert(project);
                    projectMap.put(projectName, project);
                    log.info("Insert project: " + projectName);
                }
            } else if (element.equals("Group") && Objects.nonNull(project)) {
                String groupName = processor.getAttribute("name");
                if (!groupMap.containsKey(groupName)) {
                    groups.add(new Group(groupName,
                            GroupType.valueOf(processor.getAttribute("type")),
                            project.getId()));
                }
            }
        }
        groupDao.insertGroupBatch(groups);
        log.info("Insert groups batch: " + groups);

        return groupDao.getAsMap();
    }
}
