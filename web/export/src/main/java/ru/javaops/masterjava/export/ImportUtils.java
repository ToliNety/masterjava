package ru.javaops.masterjava.export;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.dao.ProjectDao;
import ru.javaops.masterjava.xml.schema.CityType;
import ru.javaops.masterjava.xml.schema.GroupType;
import ru.javaops.masterjava.xml.schema.Project;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by tolikswx on 07.04.2017.
 */
@Slf4j
public class ImportUtils {
    private final StaxStreamProcessor processor;
    private final ProjectDao projectDao;

    public ImportUtils(@NonNull InputStream is) throws XMLStreamException {
        processor = new StaxStreamProcessor(is);
        projectDao = DBIProvider.getDao(ProjectDao.class);
    }

    public void process() throws XMLStreamException {
        List<Project> projects = getProjects();
        List<ru.javaops.masterjava.persist.model.Project> projToDAO =
                projects.stream()
                        .map(project -> new ru.javaops.masterjava.persist.model.Project(project.getName(), project.getDescription()))
                        .collect(Collectors.toList());
        projectDao.insertBatch(projToDAO, projToDAO.size());
    }

    public List<Project> getProjects() throws XMLStreamException {
        final List<Project> projects = new ArrayList<>();

        processor.doUntil(XMLEvent.START_ELEMENT, "Projects");

        while (processor.nextInnerElement("Project", "Projects")) {
            Project project = new Project();
            project.setName(processor.getAttribute("name"));
            processor.nextStartElement();
            project.setDescription(processor.getText());
            projects.add(project);

            List<Project.Group> groups = project.getGroup();
            while (processor.nextInnerElement("Group", "Project")) {
                Project.Group group = new Project.Group();
                group.setName(processor.getAttribute("name"));
                group.setType(GroupType.valueOf(processor.getAttribute("type")));
                groups.add(group);
            }
        }

        return projects;
    }

    public List<CityType> getCities() throws XMLStreamException {
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
