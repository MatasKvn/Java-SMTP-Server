package org.mataskvn.smtp.server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws InterruptedException, IOException {


        SMTPServer smtpServer = new SMTPServer(20000);
        smtpServer.setOnReceiveEmail(
            (MailObject mailObject) -> {
                Path dir = Paths.get("mail");
                String emailPath = dir + "/" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
                Path emailDir = Paths.get(emailPath);

                String data = mailObject.getData();
                try {
                    if (!Files.exists(dir))
                        Files.createDirectory(dir);
                    if (!Files.exists(emailDir))
                        Files.createDirectory(emailDir);

                    // Write Attachments
                    Map<String, String> attachments = mailObject.extractFileInfo();
                    if (attachments != null)
                    {
                        for (Map.Entry<String, String> attachment : attachments.entrySet()) {
                            Files.write(Path.of(emailPath + "/" + attachment.getKey()), attachment.getValue().getBytes());
                        }
                    }

                    // Write Email files
                    File file = new File(emailPath + "/mail.eml");
                    FileWriter fileWriter = new FileWriter(file);
                    fileWriter.write(data);
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        );
        smtpServer.start();
    }
}