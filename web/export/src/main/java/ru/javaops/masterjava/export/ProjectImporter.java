package ru.javaops.masterjava.export;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.dao.ProjectDao;
import ru.javaops.masterjava.persist.model.Project;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.Map;

/**
 * Created by tolikswx on 14.04.2017.
 */
@Slf4j
public class ProjectImporter {
    private final ProjectDao dao = DBIProvider.getDao(ProjectDao.class);

    public Map<String, Project> process(StaxStreamProcessor processor) throws XMLStreamException {
        val map = dao.getAsMap();

        String element;

        while ((element = processor.doUntilAny(XMLEvent.START_ELEMENT, "Project", "Cities")) != null) {
            if (element.equals("Cities")) break;
            String name = processor.getAttribute("name");
            if (!map.containsKey(name)) {
                processor.doUntilAny(XMLEvent.START_ELEMENT, "description");
                String description = processor.getText();
                Project project = new Project(name, description);
                dao.insert(project);
                log.debug("Project added to DB: " + project);
                while ((element = processor.doUntilAny(XMLEvent.START_ELEMENT, "Group")) != null) {

                }
            }
        }


        return dao.getAsMap();
    }
}
