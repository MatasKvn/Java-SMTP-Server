package org.mataskvn.smtp.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class SMTPServer extends Thread
{
    private ServerSocket serverSocket;
    private List<SMTPConnectionHandler> handlers = new LinkedList<>();
    protected List<SMTPConnectionHandler> getHandlers() {
        return handlers;
    }

    private int port;


    private String storageDirectory;
    public String getStorageDirectory() {
        return storageDirectory;
    }

    private boolean shouldClose = false;

    public SMTPServer(int port, String storageDirectory)
    {
        this.port = port;
        this.storageDirectory = storageDirectory;
    }

    public void startServer() throws IOException {
        serverSocket = new ServerSocket(port);
        Path dir = Paths.get(storageDirectory);
        if (!Files.exists(dir))
            Files.createDirectory(dir);

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
            startServer();
        } catch (IOException e) {
            System.err.println("Unable to start SMTP server!");
            e.printStackTrace();
        }
    }


    public final static String SUPPORTED_COMMANDS = "EHLO HELO MAIL RCPT DATA RSET NOOP QUIT VRFY";
}
