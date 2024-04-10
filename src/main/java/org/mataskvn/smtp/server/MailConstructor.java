package org.mataskvn.smtp.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class MailConstructor {
    /* Steps:
    1. HELO/EHLO
    2. MAIL FROM
    3. RCPT TO
    4. DATA */

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


    public static void main(String[] args) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        LinkedList<String> strs = new LinkedList<>();

        while (true) {
            String input = bufferedReader.readLine();
            strs.add(input);

            if (strs.size() > 2)
                strs.removeFirst();

            if (strs.getLast().equals(".") && strs.getFirst().equals(""))
                break;
        }


    }





}
