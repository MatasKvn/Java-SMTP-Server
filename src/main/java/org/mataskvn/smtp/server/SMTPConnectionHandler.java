package org.mataskvn.smtp.server;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

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
        sendLine("220 " + parent.getGreetingMessage());

        String input = "";
        MailObject mailObject = new MailObject();

        while (true)
        {
            input = reader.readLine();
            if (!handleInput(mailObject, input))
                break;
        }

        close();
    }

    /**
     *
     * @param mailObject
     * @param input
     * @return true - continue, false - close connection
     */
    private boolean handleInput(MailObject mailObject, String input) throws IOException{
        if (input == null) return true;
        if (input.matches("(?i)^\\s*HELP\\s?"))
            sendLine("250 Supported commands: " + SMTPServer.SUPPORTED_COMMANDS);

        else if (input.matches("(?i)^\\s*EHLO\\s?"))
            System.out.println("Not implemented");

        else if (input.matches("(?i)^\\s*HELO\\s?.*"))
            handleHELO(mailObject, input);

        else if (input.matches("(?i)^\\s*MAIL\\s?.*"))
            handleMAIL(mailObject, input);

        else if (input.matches("(?i)^\\s*RCPT\\s?.*"))
            handleRCPT(mailObject, input);

        else if (input.matches("(?i)^\\s*DATA\\s?.*")) { // DATA
            handleDATA(mailObject, input);

        } else if (input.matches("(?i)^\\s*RSET\\s?"))
            handleRSET(mailObject);

        else if (input.matches("(?i)^\\s*NOOP\\s?"))
            sendLine("250 OK");

        else if (input.matches("(?i)^\\s*VRFY\\s?")) {
            sendLine("502 Error: Command not implemented. (I cannot be bothered to implement this)");
        }
        else if (input.matches("(?i)^\\s*QUIT\\s?"))
            return false;
        else
            sendLine("500 Error: Command \"" + input + "\" not recognized. Type 'HELP' to see the list of commands.");

        return true;
    }

    private void handleRSET(MailObject mailObject) throws IOException {
        mailObject = new MailObject();
        sendLine("250 OK");
    }

    private void handleDATA(MailObject mailObject, String input) throws IOException {
        if (!mailObject.hasRecipients()) {
            sendLine("503 Error: Need RCPT command.");
            return;
        }
        sendLine("354 End data with: <CR><LF>.<CR><LF>");

        readAndAddData(mailObject);
        mailObject.getDataBuilder().deleteCharAt(mailObject.getDataBuilder().length() - 1);
        sendLine("250 OK");

        parent.getOnReceiveEmail().accept(mailObject);

        relayEmail(mailObject);
    }

    private void relayEmail(MailObject mailObject) throws IOException {
        for (String email : mailObject.getRecipients()) {
            String domain = MailObject.getUserDomain(email);
            if (domain == null || domain.equals(parent.getDomainName()))
                continue;

            DNSLookupUtil.Tuple<String, Integer> addressAndPort = DNSLookupUtil.getIpv4AddressOf(domain);
            if (addressAndPort == null)
                continue;

            Socket socket = new Socket(addressAndPort.fst, addressAndPort.snd);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            reader.readLine();
            writer.write("HELO " + InetAddress.getLocalHost());
            writer.newLine();
            writer.flush();
            reader.readLine();

            writer.write("MAIL FROM:" + mailObject.getSender());
            writer.newLine();
            writer.flush();
            reader.readLine();

            writer.write("RCPT TO:" + email);
            writer.newLine();
            writer.flush();
            reader.readLine();

            writer.write("DATA");
            writer.newLine();
            writer.flush();
            reader.readLine();

            writer.write(mailObject.getData());
            writer.newLine();
            writer.write(".");
            writer.newLine();
            writer.flush();
            reader.readLine();

            writer.write("QUIT");
            writer.newLine();
            writer.flush();
            reader.readLine();

            socket.close();
        }

    }

    private void handleRCPT(MailObject mailObject, String input) throws IOException {
        if (!mailObject.hasSender()) {
            sendLine("503 Error: Need MAIL first.");
            return;
        }
        if (!input.matches("(?i)^\\s*RCPT TO:\\s?.*")) {
            sendLine("501 Syntax: RCPT TO: <address>");
            return;
        }
        try {
            mailObject.addRecipient(input.split(":\s*")[1]);
            sendLine("250 OK");
        } catch (Exception e) {
            sendLine("501 Syntax: RCPT TO: <address>");
        }
    }

    private void handleMAIL(MailObject mailObject, String input) throws IOException {
        if (!mailObject.hasHello()) {
            sendLine("503 Error: Send HELO first.");
            return;
        }
        if (!input.matches("(?i)^\\s*MAIL FROM:\\s?.*")) {
            sendLine("501 Syntax: MAIL FROM: <address>");
            return;
        }
        try {
            mailObject.addSender(input.split(":\s*")[1]);
            sendLine("250 OK");
        } catch (Exception e) {
            sendLine("501 Syntax: MAIL FROM: <address>");
        }
    }

    private void handleHELO(MailObject mailObject, String input) throws IOException {
        if (mailObject.hasHello()) {
            sendLine("XXX Error: HELO has been already sent.");
            return;
        }
        try {
            mailObject.addHelo(input.split("\s+")[1]);
            sendLine("250 " + InetAddress.getLocalHost().getHostName());
        } catch (Exception e) {
            sendLine("501 Syntax: HELO identity");
        }
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

        while (!input.matches("(?i)^\\s*QUIT\\s?")) {
            input = reader.readLine();
//            System.out.println(input);

            if (input.equals("."))
                break;

            mailObject.addData(input);
        }
    }


}









