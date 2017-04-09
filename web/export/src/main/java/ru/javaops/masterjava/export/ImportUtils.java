package ru.javaops.masterjava.export;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.IntStreamEx;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.dao.GroupDao;
import ru.javaops.masterjava.persist.dao.ProjectDao;
import ru.javaops.masterjava.persist.model.Group;
import ru.javaops.masterjava.persist.model.GroupType;
import ru.javaops.masterjava.persist.model.Project;
import ru.javaops.masterjava.xml.schema.CityType;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by tolikswx on 07.04.2017.
 */
@Slf4j
public class ImportUtils {
    private final ProjectDao projectDao;
    private final GroupDao groupDao;

    public ImportUtils() {
        projectDao = DBIProvider.getDao(ProjectDao.class);
        groupDao = DBIProvider.getDao(GroupDao.class);
    }

    public ImportUtils(ProjectDao projectDao, GroupDao groupDao) {
        this.projectDao = projectDao;
        this.groupDao = groupDao;
    }

    public void process(@NonNull InputStream is) throws XMLStreamException {
        final StaxStreamProcessor processor = new StaxStreamProcessor(is);

        List<Project> projects = getProjects(processor);
        log.info("Get {} projects from XML", projects.size());
        projects.forEach(project -> log.info("Get {} groups from XML for project {}",
                project.getGroups().size(), project.getName()));

        int[] result = projectDao.insertBatch(projects, projects.size());
        List<Project> addedProjects =
                IntStreamEx.range(0, result.length)
                .filter(i-> result[i]!=0)
                .mapToObj(projects::get)
                .collect(Collectors.toList());
        log.info("{} projects added to DB", addedProjects.size());

        List<Group> groups = addedProjects.get(0).getGroups();
        ///TODO addedProjects.get(0).getId() - NUP for projectID
        int[] groupResult = groupDao.insertBatch(groups, addedProjects.get(0).getId(), groups.size());

/*        addedProjects.forEach(project -> {
            List<Group> groups = project.getGroups();
            if (!groups.isEmpty()) {
                final int[] groupResult = groupDao.insertBatch(groups, project.getId(), groups.size());
                long count = Arrays.stream(groupResult)
                        .filter(i -> i != 0)
                        .count();
                log.info("{} groups of project:{} added to DB ", count, project.getName());
            }
        });*/

    }

    public List<Project> getProjects(StaxStreamProcessor processor) throws XMLStreamException {
        final List<Project> projects = new ArrayList<>();

        processor.doUntil(XMLEvent.START_ELEMENT, "Projects");

        while (processor.nextInnerElement("Project", "Projects")) {
            Project project = new Project();
            project.setName(processor.getAttribute("name"));
            processor.nextStartElement();
            project.setDescription(processor.getText());
            projects.add(project);

            List<Group> groups = project.getGroups();
            while (processor.nextInnerElement("Group", "Project")) {
                Group group = new Group();
                group.setName(processor.getAttribute("name"));
                group.setGroupType(GroupType.valueOf(processor.getAttribute("type")));
                groups.add(group);
            }
        }

        return projects;
    }

    public List<CityType> getCities(StaxStreamProcessor processor) throws XMLStreamException {
        final List<CityType> cities = new ArrayList<>();

        processor.doUntil(XMLEvent.START_ELEMENT, "Cities");
        while (processor.nextInnerElement("City", "Cities")) {
            CityType city = new CityType();
            city.setId(processor.getAttribute("id"));
            city.setValue(processor.getText());
            cities.add(city);
        }

        return cities;
    }

}
