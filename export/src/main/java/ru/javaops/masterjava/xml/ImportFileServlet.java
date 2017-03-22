package ru.javaops.masterjava.xml;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * Created by tolikswx on 20.03.2017.
 */
@WebServlet("/import")
public class ImportFileServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        DiskFileItemFactory factory = new DiskFileItemFactory();
        // maximum size that will be stored in memory
        factory.setSizeThreshold(4096);
        // the location for saving data that is larger than getSizeThreshold()
        factory.setRepository(new File("/tmp"));

        ServletFileUpload upload = new ServletFileUpload(factory);
        // maximum size before a FileUploadException will be thrown
        upload.setSizeMax(1000000);

        List<FileItem> fileItems = null;
        try {
            fileItems = upload.parseRequest(request);
        } catch (FileUploadException e) {
            e.printStackTrace();
        }
        if (!Objects.nonNull(fileItems) || fileItems.isEmpty()) throw new IOException("No files selected");

        fileItems.forEach((fileItem) -> {
            try (StaxStreamProcessor processor =
                         new StaxStreamProcessor(fileItem.getInputStream())) {
                XMLStreamReader reader = processor.getReader();
                while (reader.hasNext()) {
                    int event = reader.next();
                    if (event == XMLEvent.START_ELEMENT) {
                        if ("User".equals(reader.getLocalName())) {
                            String flag = reader.getAttributeValue(0);
                            String email = reader.getAttributeValue(2);
                            System.out.println(reader.getElementText() +
                                    " email: " + email +
                                    " flag:" + flag);
                        }
                    }
                }

            } catch (IOException | XMLStreamException ex) {
                System.out.println("Error reading file :" + ex.getMessage());
            }
        });

        response.sendRedirect("import");

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendRedirect("import.jsp");
    }
}
