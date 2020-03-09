package Mail;

import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.text.*;

/* $Id: Message.java,v 1.5 1999/07/22 12:10:57 kangasha Exp $ */

/**
 * Mail message.
 *
 * @author Jussi Kangasharju
 */
public class Message {
    /* The headers and the body of the message. */
    public String Headers;
    public String Body;

    /* Sender and recipient. With these, we don't need to extract them
       from the headers. */
    private String From;
    private String To;

    /* To make it look nicer */
    private static final String CRLF = "\r\n";

    /* Create the message object by inserting the required headers from
       RFC 822 (From, To, Date). */
    public Message(String from, String to, String subject, String text){
        /* Remove whitespace */
        From = from.trim();
        To = to.trim();
        Headers = "From: " + From + CRLF;
        Headers += "To: " + To + CRLF;
        Headers += "Subject: " + subject.trim() + CRLF;

	/* A close approximation of the required format. Unfortunately
	   only GMT. */
        SimpleDateFormat format =
                new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'");
        String dateString = format.format(new Date());
        Headers += "Date: " + dateString + CRLF;
        Headers += "MIME-Version: 1.0" + CRLF;
        //Allows different types of messages in the mail with the boundary being frontier
        Headers += "Content-Type: multipart/mixed; boundary=frontier" + CRLF;
        //Stating that we will send text
        Body = "--frontier" + CRLF;
        Body += "Content-Type: text/plain" + CRLF + CRLF;
        //The text message
        Body += text + CRLF;
        //New type of message
        Body += "--frontier" + CRLF;
        //Stating what type of file it is
        Body += "Content-Type: Image/jpeg" + CRLF;
        //Telling the mail server that what its receiving is encoded with base 64 and needs to be decoded
        Body += "Content-Transfer-Encoding: base64" + CRLF + CRLF;
        String encodestring = "";
        //Adding the picture and converting it to Base64
        try {
            File file = new File("/zhome/46/1/136938/Downloads/Datacom.jpeg");
            byte[] data = Files.readAllBytes(file.toPath());
            encodestring = Base64.getEncoder().encodeToString(data);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        //Adding the picture to the mail message
        Body += encodestring + CRLF;
        //Tells the server that there will be no further data
        Body += "--frontier--";
    }

    /* Two functions to access the sender and recipient. */
    public String getFrom() {
        return From;
    }

    public String getTo() {
        return To;
    }

    /* Check whether the message is valid. In other words, check that
       both sender and recipient contain only one @-sign. */
    public boolean isValid() {
        int fromat = From.indexOf('@');
        int toat = To.indexOf('@');

        if(fromat < 1 || (From.length() - fromat) <= 1) {
            System.out.println("Sender address is invalid");
            return false;
        }
        if(toat < 1 || (To.length() - toat) <= 1) {
            System.out.println("Recipient address is invalid");
            return false;
        }
        if(fromat != From.lastIndexOf('@')) {
            System.out.println("Sender address is invalid");
            return false;
        }
        if(toat != To.lastIndexOf('@')) {
            System.out.println("Recipient address is invalid");
            return false;
        }
        return true;
    }

    /* For printing the message. */
    public String toString() {
        String res;

        res = Headers + CRLF;
        res += Body;
        return res;
    }
}