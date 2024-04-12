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

        return true;
    }
    public StringBuilder getDataBuilder() {
        return data;
    }
    public String getData() { return data.toString(); }


    public static void main(String[] args){
//        MailObject mailObject = new MailObject();
//        mailObject.addHelo("");
//        mailObject.addSender("me");
//        mailObject.addRecipient("you");

        BufferedWriter writer = null;
        try {
            if (!Files.exists(Paths.get("mail" + "/" + "aaa"))) {
                Files.createDirectory(Paths.get("mail" + "/" + "aaa"));
            }
            writer = new BufferedWriter(new FileWriter(new File(
                    "mail" + "/" + "xdddd.txt")));
            writer.write("aaaaa");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        String line = "";
//        while ((line = fileReader.readLine()) != null) {
//            mailObject.addData(line);
//        }
//
//        System.out.println(mailObject.getDataBuilder().toString());
//        mailObject.saveContanedFiles(new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()));

//        System.out.println(new String(Base64.getEncoder().encode(new String("Sveiki lūzeriaiĄĄĄĄĄĄĄĄĄĄĄ!!!!!!!").getBytes())));

    }

//    public void saveContanedFiles(String directory) throws IOException{
//
//        Map<String, String> fileInfo = extractFileInfo();
//        if (fileInfo == null)
//            return;
//        for (Map.Entry<String, String> entry : fileInfo.entrySet()) {
//            FileWriter fileWriter = new FileWriter(new File(directory + "/" + entry.getKey()));
//            fileWriter.write(entry.getValue());
//            fileWriter.close();
//        }
//    }


    public Map<String, String> extractFileInfo() {
        String data = this.data.toString();
        // Find Boundary
        Matcher matcher = Pattern.compile("boundary=\".+\"").matcher(data);
        if (!matcher.find())
            return null;
        String boundary = "--" + data.substring(matcher.start() + "boundary=\"".length(),matcher.end()-1);

        // Find File Occurances
        matcher = Pattern.compile(boundary + "\n.+\n.+\n.+[^-]+").matcher(data);
        Map<String, String> result = new HashMap<>();
        while (matcher.find()) {
            String fileInfo = matcher.group();
            String filename = extractFilename(fileInfo);
            String base64EncodedContent = extractEncodedText(fileInfo);
            String decodedContent = new String(Base64.getDecoder().decode(base64EncodedContent), StandardCharsets.UTF_8);
            result.put(filename, decodedContent);
            System.out.println(filename + " " + decodedContent);
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
        Matcher matcher = Pattern.compile("Content-Transfer-Encoding: base64\n\n.*\n").matcher(emlFileSubsection);
        if (matcher.find()) {
            return emlFileSubsection.substring(matcher.start() + "Content-Transfer-Encoding: base64\n\n".length(), matcher.end() - 1);
        }
        return null;
    }
}













