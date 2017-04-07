package ru.javaops.masterjava.xml.util;

import com.google.common.io.Resources;
import org.junit.Test;
import ru.javaops.masterjava.export.ImportUtils;
import ru.javaops.masterjava.xml.schema.CityType;
import ru.javaops.masterjava.xml.schema.Project;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.util.List;

/**
 * gkislin
 * 23.09.2016
 */
public class StaxStreamProcessorTest {
    @Test
    public void readCities() throws Exception {
        try (StaxStreamProcessor processor =
                     new StaxStreamProcessor(Resources.getResource("payload.xml").openStream())) {
            XMLStreamReader reader = processor.getReader();
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLEvent.START_ELEMENT) {
                    if ("City".equals(reader.getLocalName())) {
                        System.out.println(reader.getElementText());
                    }
                }
            }
        }
    }

    @Test
    public void readCities2() throws Exception {
        try (StaxStreamProcessor processor =
                     new StaxStreamProcessor(Resources.getResource("payload.xml").openStream())) {
            String city;
            while ((city = processor.getElementValue("City")) != null) {
                System.out.println(city);
            }
        }
    }

    @Test
    public void readAllData() throws Exception {

        try (InputStream is =
                     Resources.getResource("payload.xml").openStream()) {
            ImportUtils importXML = new ImportUtils(is);

            List<Project> projects = importXML.getProjects();
            List<CityType> cities = importXML.getCities();

            System.out.println("///Projects");
            projects.forEach(project -> {
                System.out.println(project.getName() + " : " + project.getDescription());
                System.out.println("Groups:");
                project.getGroup().forEach(group -> System.out.println("   " + group.getName() + " : " + group.getType().value()));
            });
            System.out.println("///Cities");
            cities.forEach(c-> System.out.println(c.getValue()));
        }
    }
}