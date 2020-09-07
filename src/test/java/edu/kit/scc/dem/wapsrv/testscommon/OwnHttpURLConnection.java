package edu.kit.scc.dem.wapsrv.testscommon;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.URL;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

/**
 * OwnHttpURLConnection
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public class OwnHttpURLConnection {
   public static enum Request {
      /**
       * Type GET request
       */
      GET,
      /**
       * Type HEAD request
       */
      HEAD,
      /**
       * Type OPTIONS request
       */
      OPTIONS,
      /**
       * Type POST request
       */
      POST,
      /**
       * Type PUT request
       */
      PUT,
      /**
       * Type DELETE request
       */
      DELETE
   }

   private String requestString;
   private String responseString;
   private final URL url;
   private List<String> ownHeaders = new Vector<String>();
   private String parameterString;
   private final Request request;
   private String body;

   /**
    * The constructor
    * 
    * @param url
    *                A URL for connection
    * @param request
    *                A request type for connection
    */
   public OwnHttpURLConnection(URL url, Request request) {
      this.url = url;
      this.request = request;
   }

   private static String toString(Exception e) {
      ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
      PrintStream printstream = new PrintStream(bytesOut);
      e.printStackTrace(printstream);
      printstream.close();
      return new String(bytesOut.toByteArray());
   }

   /**
    * Extract the header of HTTP-String.
    * 
    * @param  field
    *                        String to be filtered
    * @param  receivedString
    *                        A string to process
    * @return                The header
    */
   public static String extractHeader(String field, String receivedString) {
      if (receivedString == null)
         return null;
      String[] parts = receivedString.split(Pattern.quote("\n"));
      for (String line : parts) {
         String newLine = line;
         newLine = newLine.trim();
         if (newLine.length() == 0)
            break;
         if (newLine.startsWith(field + ":")) {
            return newLine.substring(newLine.indexOf(":") + 1).trim();
         }
      }
      return null;
   }

   /**
    * Extract lines of the header of HTTP-String.
    * 
    * @param  receivedString
    *                        A string to process
    * @return                The list of lines in header
    */
   public static List<String> extractHeaders(String receivedString) {
      if (receivedString == null)
         return null;
      String[] parts = receivedString.split(Pattern.quote("\n"));
      List<String> lines = new Vector<String>();
      for (String line : parts) {
         if (line.trim().length() == 0)
            break;
         lines.add(line);
      }
      return lines;
   }

   /**
    * Extract lines of the body of HTTP-String.
    * 
    * @param  receivedString
    *                        A string to process
    * @return                The list of lines in body
    */
   public static List<String> extractBody(String receivedString) {
      if (receivedString == null)
         return null;
      String[] parts = receivedString.split(Pattern.quote("\n"));
      List<String> lines = new Vector<String>();
      boolean skip = true;
      for (String line : parts) {
         if (skip && line.trim().length() == 0)
            skip = false;
         else if (!skip) {
            lines.add(line);
         }
      }
      return lines;
   }

   /**
    * Set a property.
    * 
    * @param key
    *              A property to be set.
    * @param value
    *              The value to be set.
    */
   public void setRequestProperty(String key, String value) {
      ownHeaders.add(key + ": " + value);
   }

   /**
    * Get the response code.
    * 
    * @return The response code.
    */
   public int getResponseCode() {
      // Thread.dumpStack();
      try {
         requestString
               = request + " " + url.getPath() + (parameterString == null ? "" : parameterString) + " HTTP/1.1\r\n";
         requestString += "Host: " + url.getHost() + (url.getPort() == -1 ? "" : ":" + url.getPort()) + "\r\n";
         for (String header : ownHeaders) {
            requestString += header + "\r\n";
         }
         requestString += "Connection: close\r\n";
         if (body != null) {
            requestString += "Content-Length: " + calculateBodyLength(body) + "\r\n";
            requestString += "\r\n";
         } else {
            requestString += "\r\n";
         }
         int port = url.getPort();
         if (port == -1)
            port = url.getDefaultPort();
         Socket socket = new Socket(url.getHost(), port);
         socket.getOutputStream().write(requestString.getBytes());
         if (body != null) {
            socket.getOutputStream().write(body.getBytes());
            requestString += body;
         }
         socket.getOutputStream().flush();
         // logger.trace("sent message:\n"+requestString);
         BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
         String line = reader.readLine();
         responseString = "";
         int returnCode = extractReturnCode(line);
         while (line != null) {
            // logger.trace(line);
            responseString += line + "\n";
            line = reader.readLine();
         }
         socket.close();
         return returnCode;
      } catch (IOException e) {
         responseString = toString(e);
         return -1;
      }
   }

   private int calculateBodyLength(String body) {
      return body.getBytes().length;
   }

   private int extractReturnCode(String line) {
      // HTTP/1.1 200 OK
      if (line == null)
         return -1;
      String[] parts = line.split(Pattern.quote(" "));
      if (parts == null || parts.length < 2)
         return -1;
      try {
         return Integer.parseInt(parts[1]);
      } catch (NumberFormatException e) {
         return -1;
      }
   }

   /**
    * Set a parameter.
    * 
    * @param parameterString
    *                        The paramter to set
    */
   public void setParameters(String parameterString) {
      this.parameterString = parameterString;
   }

   /**
    * Get the transmitted string.
    * 
    * @return The transmitted string
    */
   public String getTransmittedString() {
      return requestString;
   }

   /**
    * Get the received string.
    * 
    * @return The received string
    */
   public String getReceivedString() {
      return responseString;
   }

   /**
    * Set the body.
    * 
    * @param body
    *             A body
    */
   public void setBody(String body) {
      this.body = body;
   }

   /**
    * Get a body.
    * 
    * @return The body
    */
   public String getBody() {
      return body;
   }
}
