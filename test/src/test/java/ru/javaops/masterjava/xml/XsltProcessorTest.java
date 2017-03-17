package ru.javaops.masterjava.xml;

import com.google.common.io.Resources;
import org.junit.Test;
import ru.javaops.masterjava.xml.util.XsltProcessor;

import java.io.InputStream;

/**
 * gkislin
 * 23.09.2016
 */
public class XsltProcessorTest {
    @Test
    public void transform() throws Exception {
        try (InputStream xslInputStream = Resources.getResource("cities.xsl").openStream();
             InputStream xmlInputStream = Resources.getResource("payload.xml").openStream()) {

            XsltProcessor processor = new XsltProcessor(xslInputStream);
            System.out.println(processor.transform(xmlInputStream));
        }
    }
}
