package org.mataskvn.smtp.server;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MailObject {

    private String helo = null;
    public boolean hasHello() {
        return helo != null;
    }
    public void addHelo(String helo) {
        this.helo = helo;
        System.out.println("Added identity: " + helo);
    }

    private String sender = null;
    public boolean hasSender() {
        return sender != null;
    }
    public void addSender(String sender) {
        this.sender = sender;
        System.out.println("Added sender: " + sender);
    }

    private List<String> recipients = null;
    public boolean hasRecipients() {
        return recipients != null;
    }
    public void addRecipient(String recipient) {
        if (recipients == null)
            recipients = new ArrayList<String>();
        recipients.add(recipient);
        System.out.println("Added recipient: " + recipient);
    }

    private StringBuilder data = null;



    public boolean addData(String line) {
        if (data == null)
            data = new StringBuilder();

        data.append(line).append("\n");
        System.out.println("Added data" + line);

        return true;
    }
    public StringBuilder getDataBuilder() {
        return data;
    }
    public String getData() { return data.toString(); }


    public static void main(String[] args) throws IOException {
        MailObject mailObject = new MailObject();
        mailObject.addHelo("");
        mailObject.addSender("me");
        mailObject.addRecipient("you");


        String line = "";
        BufferedReader fileReader = new BufferedReader(new FileReader("multiple_files.eml"));
        while ((line = fileReader.readLine()) != null) {
            mailObject.addData(line);
        }
        fileReader.close();

        System.out.println(mailObject.getDataBuilder().toString());

        Map<String, byte[]> fileInfo = mailObject.extractFileInfo();
        for (Map.Entry<String, byte[]> entry : fileInfo.entrySet()) {
            System.out.println(entry.getKey() + "\n" + new String(entry.getValue(), StandardCharsets.UTF_8));
        }
    }

    /**
     *
     * @return the extracted file information in a map: Key = filename, Value = file content
     */
    public Map<String, byte[]> extractFileInfo() {
        String data = this.data.toString();
        // Find Boundary
        Matcher matcher = Pattern.compile("boundary=\".+\"").matcher(data);
        if (!matcher.find())
            return null;
        String boundary = "--" + data.substring(matcher.start() + "boundary=\"".length(),matcher.end()-1);

        // Find File Occurances
        matcher = Pattern.compile(boundary + "\n.+\n.+\n.+\n\n([a-zA-Z0-9/+=]+\n)+").matcher(data);
        Map<String, byte[]> result = new HashMap<>();
        while (matcher.find()) {
            String fileInfo = matcher.group();
            String filename = extractFilename(fileInfo);
            String base64EncodedContent = extractEncodedText(fileInfo);
            if (base64EncodedContent == null) { return null; }
            byte[] decodedContent = Base64.getDecoder().decode(base64EncodedContent);
            result.put(filename, decodedContent);
        }

        return result;
    }

    private String extractFilename(String emlFileSubsection) {
        Matcher matcher = Pattern.compile("filename=\".+\"").matcher(emlFileSubsection);
        if (matcher.find()) {
            return emlFileSubsection.substring(matcher.start() + "filename=\"".length(), matcher.end() - 1);
        }
        return null;
    }

    public String extractEncodedText(String emlFileSubsection) {
        Matcher matcher = Pattern.compile("Content-Transfer-Encoding: base64\n\n([a-zA-Z0-9+=/]+\n)+").matcher(emlFileSubsection);
        if (matcher.find()) {
            return emlFileSubsection.substring(matcher.start() + "Content-Transfer-Encoding: base64\n\n".length(), matcher.end() - 1).replace("\n", "");
        }
        return null;
    }
}













