package ru.javaops.masterjava.service.mail.rest;


import com.google.common.collect.ImmutableList;
import org.glassfish.jersey.media.multipart.BodyPartEntity;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.hibernate.validator.constraints.NotBlank;
import ru.javaops.masterjava.service.mail.Attach;
import ru.javaops.masterjava.service.mail.GroupResult;
import ru.javaops.masterjava.service.mail.MailServiceExecutor;
import ru.javaops.masterjava.service.mail.MailWSClient;
import ru.javaops.masterjava.service.mail.util.Attachments;
import ru.javaops.web.WebStateException;

import javax.activation.DataHandler;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Path("/")
public class MailRS {
    @GET
    @Path("test")
    @Produces(MediaType.TEXT_PLAIN)
    public String test() {
        return "Test";
    }

    @POST
    @Path("send")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public GroupResult send(@NotBlank @FormDataParam("users") String users,
                            @FormDataParam("subject") String subject,
                            @NotBlank @FormDataParam("body") String body,
                            @FormDataParam("attach") FormDataBodyPart fileBody) throws WebStateException {

        final List<Attach> attaches;

        String fileName = fileBody.getFormDataContentDisposition().getFileName();

        if (fileName == null || fileName.isEmpty()) {
            attaches = ImmutableList.of();
        } else {
            BodyPartEntity bodyPartEntity = (BodyPartEntity) fileBody.getEntity();
            attaches = ImmutableList.of(new Attach(fileName,
                    new DataHandler((Attachments.ProxyDataSource) bodyPartEntity::getInputStream)));
        }

        return MailServiceExecutor.sendBulk(MailWSClient.split(users), subject, body, attaches);
    }
}