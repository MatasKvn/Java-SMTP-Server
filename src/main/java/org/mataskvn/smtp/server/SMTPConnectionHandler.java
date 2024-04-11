package org.mataskvn.smtp.server;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SMTPConnectionHandler implements Runnable {
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private SMTPServer parent;


    @Override
    public void run() {
        try {
            handle();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SMTPConnectionHandler(Socket socket, SMTPServer parent) throws IOException {
        this.socket = socket;
        this.parent = parent;
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    public void handle() throws IOException {
        sendLine("220 " + InetAddress.getLocalHost().getHostName() + " Connection Sucessful.");

        String input = "";

        MailObject mailObject = new MailObject();

        while (true)
        {
            input = reader.readLine();
            if (input.matches("(?i)^\\s*HELP\\s?"))
                sendLine("250 Supported commands: " + SMTPServer.SUPPORTED_COMMANDS);
            else if (input.matches("(?i)^\\s*EHLO\\s?"))
                System.out.println("Not implemented");

            else if (input.matches("(?i)^\\s*HELO\\s?.*")) {
                if (mailObject.hasHello()) {
                    sendLine("XXX Error: HELO has been already sent");
                    continue;
                }
                try {
                    mailObject.addHelo(input.split("\s+")[1]);
                    sendLine("250 " + InetAddress.getLocalHost().getHostName());
                } catch (Exception e) {
                    sendLine("501 Syntax: HELO identity");
                }
            }
            else if (input.matches("(?i)^\\s*MAIL\\s?.*")) {
                if (!mailObject.hasHello()) {
                    sendLine("503 Error: send HELO first");
                    continue;
                }
                if (!input.matches("(?i)^\\s*MAIL FROM:\\s?.*")) {
                    sendLine("501 Syntax: MAIL FROM: <address>");
                    continue;
                }
                try {
                    mailObject.addSender(input.split(":\s*")[1]);
                    sendLine("250 OK");
                } catch (Exception e) {
                    sendLine("501 Syntax: MAIL FROM: <address>");
                }

            }
            else if (input.matches("(?i)^\\s*RCPT\\s?.*")) {
                if (!mailObject.hasSender()) {
                    sendLine("503 Error: need MAIL first");
                    continue;
                }
                if (!input.matches("(?i)^\\s*RCPT TO:\\s?.*")) {
                    sendLine("501 Syntax: RCPT TO: <address>");
                    continue;
                }
                try {
                    mailObject.addRecipient(input.split(":\s*")[1]);
                    sendLine("250 OK");
                } catch (Exception e) {
                    sendLine("501 Syntax: RCPT TO: <address>");
                }

            }
            else if (input.matches("(?i)^\\s*DATA\\s?.*")) { // DATA
                if (!mailObject.hasRecipients()) {
                    sendLine("503 Error: need RCPT command");
                    continue;
                }
                sendLine("354 End data with <CR><LF>.<CR><LF>");

                readAndAddData(mailObject);
                String timeWhenReceived = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
                String emailPath = parent.getStorageDirectory() + "/" + timeWhenReceived;

                sendLine("250 OK");
                StringBuilder dataBuilder = mailObject.getDataBuilder().deleteCharAt(mailObject.getDataBuilder().length()-1);
                String data = dataBuilder.toString();

                Path dir = Paths.get(emailPath);
                if (!Files.exists(dir))
                    Files.createDirectory(dir);

                mailObject.saveContanedFiles(emailPath);
                File file = new File(emailPath + "/email.eml");
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write(data);
                fileWriter.close();

            } else if (input.matches("(?i)^\\s*RSET\\s?")) {
                mailObject = new MailObject();
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

        close();
    }

    public void close() throws IOException {
        closeEverything(writer, reader, socket);
        parent.getHandlers().remove(this);
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



    private void readAndAddData(MailObject mailObject) throws IOException {
        String input = "";
        mailObject.addData(input);

        while (!input.matches("(?i)^\\s*QUIT\\s?")) {
            input = reader.readLine();
            System.out.println(input);

            if (input.equals("."))
                break;

            mailObject.addData(input);
        }
    }


}









