package ru.javaops.masterjava.webapp;

import lombok.extern.slf4j.Slf4j;
import ru.javaops.masterjava.service.mail.MailWSClient;
import ru.javaops.web.WebStateException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;

@WebServlet("/send")
@MultipartConfig
@Slf4j
public class SendServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        String users = req.getParameter("users");
        String subject = req.getParameter("subject");
        String body = req.getParameter("body");
        String fileName = req.getParameter("filename");
        Part filePart = req.getPart("file");
        byte[] buffer = null;
        if (filePart != null && fileName != null) {
            try (InputStream is = filePart.getInputStream()) {
                buffer = new byte[(int) filePart.getSize()];
                log.info("File is readed, size (bytes) = " + is.read(buffer));
            } catch (Exception ex) {
                log.error("Error reading file attachment from user list form. Message: " + ex.getMessage());
            }
        }

        String groupResult;
        try {
            groupResult = MailWSClient.sendBulk(MailWSClient.split(users), subject, body,
                    (fileName == null ? "" : fileName),
                    (buffer == null ? new byte[0] : buffer))
                    .toString();
        } catch (WebStateException e) {
            groupResult = e.toString();
        }
        resp.getWriter().write(groupResult);
    }
}
