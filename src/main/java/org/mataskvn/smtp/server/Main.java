package org.mataskvn.smtp.server;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws InterruptedException, IOException {
        SMTPServer smtpServer = new SMTPServer(20000, "mail");
        smtpServer.start();
    }
}