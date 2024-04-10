package org.mataskvn.smtp.server;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;

public class SMTPConnectionHandler implements Runnable {
    private Socket socket;
    BufferedReader reader;
    BufferedWriter writer;

    @Override
    public void run() {
        try {
            handle();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SMTPConnectionHandler(Socket socket) throws IOException {
        this.socket = socket;
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    public void handle() throws IOException {
        sendLine("220 " + InetAddress.getLocalHost().getHostName() + " Connection Sucessful.");

        String input = "";

        MailConstructor mailConstructor = new MailConstructor();

        while (true)
        {
            input = reader.readLine();
            if (input.matches("(?i)^\\s*HELP\\s?"))
                sendLine("250 Supported commands: " + SMTPServer.SUPPORTED_COMMANDS);
            else if (input.matches("(?i)^\\s*EHLO\\s?"))
                System.out.println("Not implemented");

            else if (input.matches("(?i)^\\s*HELO\\s?.*")) {
                if (mailConstructor.hasHello()) {
                    sendLine("XXX Error: HELO has been already sent");
                    continue;
                }
                try {
                    mailConstructor.addHelo(input.split("\s+")[1]);
                    sendLine("250 " + InetAddress.getLocalHost().getHostName());
                } catch (Exception e) {
                    sendLine("501 Syntax: HELO identity");
                }
            }
            else if (input.matches("(?i)^\\s*MAIL\\s?.*")) {
                if (!mailConstructor.hasHello()) {
                    sendLine("503 Error: send HELO first");
                    continue;
                }
                if (!input.matches("(?i)^\\s*MAIL FROM:\\s?.*")) {
                    sendLine("501 Syntax: MAIL FROM: <address>");
                    continue;
                }
                try {
                    mailConstructor.addSender(input.split(":\s*")[1]);
                    sendLine("250 OK");
                } catch (Exception e) {
                    sendLine("501 Syntax: MAIL FROM: <address>");
                }

            }
            else if (input.matches("(?i)^\\s*RCPT\\s?.*")) {
                if (!mailConstructor.hasSender()) {
                    sendLine("503 Error: need MAIL first");
                    continue;
                }
                if (!input.matches("(?i)^\\s*RCPT TO:\\s?.*")) {
                    sendLine("501 Syntax: RCPT TO: <address>");
                    continue;
                }
                try {
                    mailConstructor.addRecipient(input.split(":\s*")[1]);
                    sendLine("250 OK");
                } catch (Exception e) {
                    sendLine("501 Syntax: RCPT TO: <address>");
                }

            }
            else if (input.matches("(?i)^\\s*DATA\\s?.*")) { // DATA
                if (!mailConstructor.hasRecipients()) {
                    sendLine("503 Error: need RCPT command");
                    continue;
                }
                sendLine("354 End data with <CR><LF>.<CR><LF>");

                readAndAddData(mailConstructor);
                sendLine("250 OK");
                StringBuilder dataBuilder = mailConstructor.getDataBuilder().deleteCharAt(mailConstructor.getDataBuilder().length()-1);
                String data = dataBuilder.toString();
                File file = new File(new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()) + ".eml");
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write(data);
                fileWriter.close();

            } else if (input.matches("(?i)^\\s*RSET\\s?")) {
                mailConstructor = new MailConstructor();
                sendLine("250 OK");
            }
            else if (input.matches("(?i)^\\s*NOOP\\s?")) {
                sendLine("250 OK");
            }
            else if (input.matches("(?i)^\\s*VRFY\\s?")) {
                sendLine("502 Error: commant not implemented");
            }
            else if (input.matches("(?i)^\\s*QUIT\\s?"))
                break;
            else
                sendLine("500 Error: command \"" + input + "\" not recognized");

        }


        closeEverything(writer, reader, socket);
    }


    private void sendLine(String str) throws IOException {
        writer.write(str);
        writer.newLine();
        writer.flush();
    }

    private void closeEverything(Closeable... closeables) throws IOException {
        for (Closeable c : closeables)
            c.close();
    }


    private void readAndAddData(MailConstructor mailConstructor) throws IOException {
        LinkedList<String> latestInput = new LinkedList<>();
        String input = reader.readLine();
        latestInput.add(input);
        mailConstructor.addData(input);

        while (!input.matches("(?i)^\\s*QUIT\\s?")) {
            input = reader.readLine();
            System.out.println(input);
            latestInput.add(input);

            if (latestInput.size() > 2)
                latestInput.removeFirst();
            System.out.println(latestInput);

            if (latestInput.getFirst().equals("") && latestInput.getLast().equals(".")
                    || latestInput.getFirst().startsWith("---------") && latestInput.getLast().equals("."))
                break;

            mailConstructor.addData(input);
        }
    }
}









