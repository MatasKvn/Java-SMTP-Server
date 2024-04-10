package org.mataskvn.smtp.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

public class SMTPServer
{
    private ServerSocket serverSocket;

    private int port;

    private boolean shouldClose = false;

    public SMTPServer(int port)
    {
        this.port = port;
    }


    public void start() throws IOException
    {
        serverSocket = new ServerSocket(port);

        while (!shouldClose)
        {
            SMTPConnectionHandler clientHandler = new SMTPConnectionHandler(serverSocket.accept());
            Thread clientHandlerThread = new Thread(clientHandler);
            clientHandlerThread.start();
        }

        serverSocket.close();
    }


    public static void main(String[] args)
    {
        SMTPServer smtpServer = new SMTPServer(20000);
        try
        {
            smtpServer.start();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public final static String SUPPORTED_COMMANDS = "EHLO HELO MAIL RCPT DATA RSET NOOP QUIT VRFY";
}
