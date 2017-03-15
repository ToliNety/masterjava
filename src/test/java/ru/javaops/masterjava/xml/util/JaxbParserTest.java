package ru.javaops.masterjava.xml.util;

import com.google.common.io.Resources;
import org.junit.Test;
import ru.javaops.masterjava.xml.schema1003.*;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * gkislin
 * 23.09.2016
 */
public class JaxbParserTest {
    private static final JaxbParser JAXB_PARSER = new JaxbParser(ObjectFactory.class);

    static {
        JAXB_PARSER.setSchema(Schemas.ofClasspath("payload.xsd"));
    }

    @Test
    public void testPayload() throws Exception {
//        JaxbParserTest.class.getResourceAsStream("/city.xml")
        Payload payload = JAXB_PARSER.unmarshal(
                Resources.getResource("payload.xml").openStream());
        payload.getProjects()
                .getProject()
                .forEach(project -> System.out.println(project.getProjID() + " : " + project.getName() + " : " + project.getDescription()));

        printUsersWithGroups(payload);

        printProjectsWithGroups(payload);

        String strPayload = JAXB_PARSER.marshal(payload);
        JAXB_PARSER.validate(strPayload);
        System.out.println(strPayload);
    }

    @Test
    public void testCity() throws Exception {
        JAXBElement<CityType> cityElement = JAXB_PARSER.unmarshal(
                Resources.getResource("city.xml").openStream());
        CityType city = cityElement.getValue();
        JAXBElement<CityType> cityElement2 =
                new JAXBElement<>(new QName("http://javaops.ru", "City"), CityType.class, city);
        String strCity = JAXB_PARSER.marshal(cityElement2);
        JAXB_PARSER.validate(strCity);
        System.out.println(strCity);
    }

    @Test
    public void testProject() throws Exception {
        JAXBElement<ProjectType> projectElement = JAXB_PARSER.unmarshal(
                Resources.getResource("project.xml").openStream());
        ProjectType project = projectElement.getValue();
        System.out.println(project.getProjID() + " : " + project.getName());
    }

    private void printUsersWithGroups(Payload payload) {
        System.out.println("//////USERS//////");
        payload.getUsers()
                .getUser()
                .forEach(user -> {
                    System.out.println("User: " + user.getFullName());
                    user.getGroups().forEach(obj -> {
                        if (Objects.nonNull(obj) && obj instanceof GroupType) {
                            System.out.println("-> " + ((GroupType) obj).getGroupID() + " : " + ((GroupType) obj).getName());
                        }
                    });
                });
        System.out.println("////////////////////////");
    }

    private void printProjectsWithGroups(Payload payload) {
        System.out.println("//////PROJECTS//////");
        Map<ProjectType, List<GroupType>> groupsByProject = payload.getGroups().getGroup()
                .stream()
                .collect(Collectors.groupingBy(group -> (ProjectType) group.getProject()));

        payload.getProjects().getProject().forEach(project -> {
            System.out.println("Project: " + project.getName());
            List<GroupType> groups = groupsByProject.get(project);
            System.out.print("Groups: ");
            if (Objects.nonNull(groups)) {
                System.out.println();
                groups.forEach(group -> {
                    System.out.println("---> " + group.getName());
                });
            } else {
                System.out.println("none");
            }

        });

        System.out.println("////////////////////////");
    }
}