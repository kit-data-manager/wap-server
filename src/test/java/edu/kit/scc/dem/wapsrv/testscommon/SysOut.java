package edu.kit.scc.dem.wapsrv.testscommon;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 * SysOut Helper class that decorates System out/err to ease debugging.
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public class SysOut extends PrintStream {
   /**
    * Instantiates a new system out.
    *
    * @param out
    *            The output
    */
   public SysOut(OutputStream out) {
      super(out);
   }

   /**
    * @see     java.io.PrintStream#println(java.lang.String)
    * @param l
    *          The string
    */
   public void println(String l) {
      StackTraceElement[] st = Thread.currentThread().getStackTrace();
      String info = "(" + st[2].getFileName() + ":" + st[2].getLineNumber() + ")";
      super.println(info + " : " + l);
   }

   /**
    * Print line method.
    *
    * @param out
    *               The output
    * @param s
    *               The string
    * @param length
    *               The length
    */
   public static void println(PrintStream out, String s, int length) {
      String newS = s;
      if (newS == null) {
         newS = "NULL";
         System.out.print(true);
      }
      if (newS.length() > length) {
         newS = newS.substring(0, length);
      } else if (newS.length() < length) {
         while (newS.length() < length) {
            newS += " ";
         }
      }
      out.println(newS);
   }

   /**
    * Prints method
    *
    * @param out
    *               The output
    * @param s
    *               The string
    * @param length
    *               The length
    */
   public static void print(PrintStream out, String s, int length) {
      String newS = s;
      if (newS == null) {
         newS = "NULL";
      }
      if (newS.length() > length) {
         newS = newS.substring(0, length);
      } else if (newS.length() < length) {
         while (newS.length() < length) {
            newS += " ";
         }
      }
      out.print(newS);
   }

   /**
    * Append method
    *
    * @param  erg
    *                The string
    * @param  s
    *                The string
    * @param  length
    *                The length
    * @return        The new string
    */
   public static String append(String erg, String s, int length) {
      String newS = s;
      if (newS == null) {
         newS = "NULL";
      }
      if (newS.length() > length) {
         newS = newS.substring(0, length);
      } else if (newS.length() < length) {
         while (newS.length() < length) {
            newS += " ";
         }
      }
      return erg + newS;
   }
}
