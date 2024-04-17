package org.mataskvn.smtp.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class SMTPServer implements Runnable
{
    private ServerSocket serverSocket;
    private List<SMTPConnectionHandler> handlers = new LinkedList<>();
    protected List<SMTPConnectionHandler> getHandlers() {
        return handlers;
    }

    private int port;

    public Consumer<MailObject> getOnReceiveEmail() {
        return onReceiveEmail;
    }

    public void setOnReceiveEmail(Consumer<MailObject> onReceiveEmail) {
        this.onReceiveEmail = onReceiveEmail;
    }

    private Consumer<MailObject> onReceiveEmail = (MailObject mailObject) -> System.out.println("Received data.");

    public String getGreetingMessage() {
        return greetingMessage;
    }

    public void setGreetingMessage(String greetingMessage) {
        this.greetingMessage = greetingMessage;
    }

    private String greetingMessage = InetAddress.getLocalHost().getHostName() + " Connection Sucessful.";

    private boolean shouldClose = false;
    private String domainName;
    public String getDomainName() { return domainName; }

    /**
     * Construct a SMTP server
     *
     * @param
     * @param port - the port for the server to run on
     * @param domainName - the domain name of this server
     */
    public SMTPServer(int port, String domainName) throws UnknownHostException {
        this.port = port;
        this.domainName = domainName;
    }

    private void init() throws IOException {
        serverSocket = new ServerSocket(port);

        while (!shouldClose)
        {
            SMTPConnectionHandler clientHandler = new SMTPConnectionHandler(serverSocket.accept(), this);
            handlers.add(clientHandler);
            Thread clientHandlerThread = new Thread(clientHandler);
            clientHandlerThread.start();
        }

        serverSocket.close();
    }

    public void kill() throws IOException {
        shouldClose = true;
        for (SMTPConnectionHandler handler : handlers) {
            handler.close();
        }
        serverSocket.close();
    }

    public void run() {
        try {
            init();
        } catch (IOException e) {
            System.err.println("Unable to start SMTP server!");
            e.printStackTrace();
        }
    }
    public void start() {
        new Thread(this).start();
    }


    public final static String SUPPORTED_COMMANDS = "EHLO HELO MAIL RCPT DATA RSET NOOP QUIT VRFY";
}
