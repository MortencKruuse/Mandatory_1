package Mail;

import java.net.*;
import java.io.*;

/**
 * Open an SMTP connection to a mailserver and send one mail.
 *
 */
public class SMTPConnection {
    /* The socket to the server */
    private Socket connection;

    /* Streams for reading and writing the socket */
    private BufferedReader fromServer;
    private DataOutputStream toServer;

    private static final int SMTP_PORT = 25;
    private static final String CRLF = "\r\n";

    /* Are we connected? Used in close() to determine what to do. */
    private boolean isConnected = false;

    /* Create an SMTPConnection object. Create the socket and the
       associated streams. Initialize SMTP connection. */
    public SMTPConnection(Envelope envelope) throws IOException {
        connection = new Socket(envelope.DestHost,25);
        fromServer = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        toServer = new DataOutputStream(connection.getOutputStream());




	/* Read a line from server and check that the reply code is 220.
	   If not, throw an IOException. */
	if (parseReply(fromServer.readLine()) != 220){
	    throw new IOException("The reply is " + fromServer.read() + " and not 220");
        }

	/* SMTP handshake. We need the name of the local machine.
	   Send the appropriate SMTP handshake command. */
        String localhost = InetAddress.getLocalHost().getHostName();
        sendCommand("HELO " + localhost + CRLF,250);

        isConnected = true;
    }

    /* Send the message. Write the correct SMTP-commands in the
       correct order. No checking for errors, just throw them to the
       caller. */
    public void send(Envelope envelope) throws IOException {
        /* Send all the necessary commands to send a message. Call
	   sendCommand() to do the dirty work. Do _not_ catch the
	   exception thrown from sendCommand(). */

        //Who the mail is from
        sendCommand("MAIL FROM:" + envelope.Sender + CRLF,250);
        //Who is the receiver
        sendCommand("RCPT TO:" + envelope.Recipient + CRLF,250);
        //Tells the server that data will be transferred
        sendCommand("DATA" + CRLF, 354);
        //The data
        sendCommand(envelope.Message + CRLF + "." + CRLF,250);


    }

    /* Close the connection. First, terminate on SMTP level, then
       close the socket. */
    public void close() {
        isConnected = false;
        try {
            sendCommand("HELO",250);
            // connection.close();
        } catch (IOException e) {
            System.out.println("Unable to close connection: " + e);
            isConnected = true;
        }
    }

    /* Send an SMTP command to the server. Check that the reply code is
       what is is supposed to be according to RFC 821. */
    private void sendCommand(String command, int rc) throws IOException {
        /* Write command to server and read reply from server. */
        //Command   Reply Code
        //DATA 			354
        //HELO 			250
        //MAIL FROM: 	250
        //QUIT 			221
        //RCPT TO: 		250

        toServer.writeBytes(command);
        toServer.flush();

        String reply = fromServer.readLine();
        System.out.println(reply);
        //Checking if its the correct reply from the server
        if(parseReply(reply) != rc){
            throw new IOException("The reply is " + fromServer.read() + " and not " + rc);
        }
    }

    /* Parse the reply line from the server. Returns the reply code. */
    private int parseReply(String reply) {
        // Takes the first 3 characters in the string received from the server and parses them to ints
        return Integer.parseInt(reply.substring(0,3));

    }

    /* Destructor. Closes the connection if something bad happens. */
    protected void finalize() throws Throwable {
        if(isConnected) {
            close();
        }
        super.finalize();
    }

}