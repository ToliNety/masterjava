package ru.javaops.masterjava;

import com.google.common.io.Resources;
import ru.javaops.masterjava.xml.schema.ObjectFactory;
import ru.javaops.masterjava.xml.schema.Payload;
import ru.javaops.masterjava.xml.schema.Project;
import ru.javaops.masterjava.xml.util.JaxbParser;
import ru.javaops.masterjava.xml.util.Schemas;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;

/**
 * User: gkislin
 * Date: 05.08.2015
 *
 * @link http://caloriesmng.herokuapp.com/
 * @link https://github.com/JavaOPs/topjava
 */
public class MainXml {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Format (1 parameter): ProjectName");
            System.exit(1);
        }

        String prjName = args[0];
        JaxbParser jaxbParser = new JaxbParser(ObjectFactory.class);
        jaxbParser.setSchema(Schemas.ofClasspath("payload.xsd"));

        try (InputStream in = Resources.getResource("payload.xml").openStream()) {
            Payload payload = jaxbParser.unmarshal(in);
            Project project = payload.getProjects().getProject().stream()
                    .filter(pr -> pr.getName().equals(prjName))
                    .findAny()
                    .orElseThrow(IllegalArgumentException::new);
        } catch (IOException | JAXBException | IllegalArgumentException ex) {
            System.out.println(ex.getMessage());
        }

    }
}
