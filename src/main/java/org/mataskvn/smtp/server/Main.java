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

    /// Demonstrative Code
    public static void main(String[] args) throws InterruptedException, IOException {
        int serverPort = 20000;
        String thisDomainName = "";
        try {
            serverPort = Integer.parseInt(args[1]);
            thisDomainName = args[0];
        } catch (NumberFormatException e) {
            System.out.println("Please provide a port for the server to run on");
        }

        SMTPServer smtpServer = new SMTPServer(serverPort, thisDomainName);
        String finalThisDomainName = thisDomainName;
        smtpServer.setOnReceiveEmail(
            (MailObject mailObject) -> {
                Path mailDir = Paths.get("mail");

                // Save files to each recipient folder
                for (String recipient : mailObject.getRecipients()) {

                    if (MailObject.getUserDomain(recipient) == null || !MailObject.getUserDomain(recipient).equals(finalThisDomainName))
                        continue;

                    String emailPath = mailDir + "/" + MailObject.getRecipient(recipient);
                    System.out.println("PAth is: " + emailPath);
                    Path emailDir = Paths.get(emailPath);

                    String data = mailObject.getData();
                    try {
                        if (!Files.exists(mailDir))
                            Files.createDirectory(mailDir);
                        if (!Files.exists(emailDir))
                            Files.createDirectory(emailDir);

                        // Write Attachments
                        Map<String, byte[]> attachments = mailObject.extractFileInfo();
                        if (attachments != null)
                        {
                            for (Map.Entry<String, byte[]> attachment : attachments.entrySet()) {
                                Files.write(Path.of(emailPath + "/" + attachment.getKey()), attachment.getValue());
                            }
                        }

                        // Write Email files
                        File file = new File(emailPath + "/" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()) + ".eml");
                        FileWriter fileWriter = new FileWriter(file);
                        fileWriter.write(data);
                        fileWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        );
        smtpServer.start();
    }
}