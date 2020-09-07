package edu.kit.scc.dem.wapsrv.testscommon;

/**
 * OwnResponse
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public class OwnResponse {
   private final int status;
   private final String transmittedString;
   private final String receivedString;

   /**
    * Creates a new OwnResponse Object with the given values
    * 
    * @param status
    *                          The http return code, -1 if any I/O error occured
    * @param transmittedString
    *                          The raw string transmitted to the server
    * @param receivedString
    *                          The raw string received from the server
    */
   public OwnResponse(int status, String transmittedString, String receivedString) {
      this.status = status;
      this.transmittedString = transmittedString;
      this.receivedString = receivedString;
   }

   /**
    * Gets the status.
    *
    * @return the status
    */
   public int getStatus() {
      return status;
   }

   /**
    * Gets the transmitted string.
    *
    * @return the transmitted string
    */
   public String getTransmittedString() {
      return transmittedString;
   }

   /**
    * Gets the received string.
    *
    * @return the received string
    */
   public String getReceivedString() {
      return receivedString;
   }
}
