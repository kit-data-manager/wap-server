package edu.kit.scc.dem.wapsrv.installer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

/**
 * Helper class for common operations on JAR files
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public final class JarUtilities {
   private JarUtilities() {
      throw new RuntimeException("No instance allowed");
   }

   /**
    * Gets the currently running jar file as URL. This method works only if the JarUtilities class is bundled within the
    * same jar.
    * 
    * @return The currently running jar file, null if not running from jar
    */
   public static URL getCurrentlyRunningJarUrl() {
      try {
         CodeSource codeSource = JarUtilities.class.getProtectionDomain().getCodeSource();
         if (codeSource.getLocation() != null) {
            return codeSource.getLocation().toURI().toURL();
         } else {
            return null;
         }
      } catch (URISyntaxException | MalformedURLException e) {
         return null;
      }
   }

   /**
    * Creates a jar url for a given jar file
    * 
    * @param  jarFile
    *                 The jar file
    * @return         The url, null in case of any error
    */
   public static URL generateUrl(File jarFile) {
      try {
         return new URL("jar:" + jarFile.toURI().toURL().toString() + "!/BOOT-INF/classes!/");
      } catch (MalformedURLException e) {
         e.printStackTrace();
         return null;
      }
   }

   /**
    * Gets the folder of the currently running jar file. This method works only if the JarUtilities class is bundled
    * within the same jar.
    * 
    * @return The currently running jar file, null if not running from jar
    */
   public static File getCurrentlyRunningJarFile() {
      try {
         CodeSource codeSource = JarUtilities.class.getProtectionDomain().getCodeSource();
         if (codeSource.getLocation() != null) {
            String uri = codeSource.getLocation().toURI().toString();
            // Windows :
            // jar:file:/C:/Users/andreas/Desktop/TestInstall/PSE-AA-0.0.1-SNAPSHOT.jar!/BOOT-INF/classes!/
            // linux :
            // mac :
            String[] parts = uri.split(Pattern.quote("/"));
            for (String part : parts) {
               if (part.toLowerCase().endsWith(".jar!")) {
                  System.out.println("Part found : " + part);
                  return new File("./" + part.substring(0, part.length() - 1));
               }
            }
            return guessJar();
         } else {
            return guessJar();
         }
      } catch (URISyntaxException e) {
         return guessJar();
      }
   }

   /**
    * Return the first jar found in the actual folder This works here because no other jars are expected to exist at all
    * 
    * @return The first jar found in the actual folder, null if none exist
    */
   private static File guessJar() {
      File dir = new File("./");
      File[] files = dir.listFiles(new FileFilter() {
         @Override
         public boolean accept(File pathname) {
            return !pathname.getName().toLowerCase().endsWith(".jar");
         }
      });
      if (files != null && files.length > 0) {
         return files[0];
      } else {
         return null;
      }
   }

   /**
    * @param  jarUrl
    *                      The path to the JAR as URL
    * @param  relativePath
    *                      The relative path within the JAR to extract the files from
    * @param  targetFolder
    *                      The target folder to extract the content to
    * @return              true if everything was extracted successfully, false otherwise
    * @throws IOException
    *                      on any IO error
    */
   public static boolean extractFolder(URL jarUrl, String relativePath, File targetFolder) throws IOException {
      JarURLConnection conn = (JarURLConnection) jarUrl.openConnection();
      JarFile jarfile = conn.getJarFile();
      Enumeration<JarEntry> content = jarfile.entries();
      while (content.hasMoreElements()) {
         JarEntry entry = content.nextElement();
         if (entry.toString().startsWith(relativePath)) {
            if (entry.isDirectory()) {
               if (!createDir(targetFolder, entry.toString().substring(relativePath.length()))) {
                  return false;
               }
            } else if (!extractEntry(jarfile, entry, targetFolder, entry.toString().substring(relativePath.length()))) {
               return false;
            }
         }
      }
      return true;
   }

   private static boolean createDir(File targetFolder, String relPath) {
      if (relPath == null || relPath.length() == 0) {
         return true; // has been created before
      }
      File dstFile = new File(targetFolder, relPath);
      return dstFile.mkdir();
   }

   private static boolean extractEntry(JarFile jarFile, JarEntry entry, File targetFolder, String relPath) {
      File dstFile = new File(targetFolder, relPath);
      InputStream in = null;
      OutputStream out = null;
      try {
         in = new BufferedInputStream(jarFile.getInputStream(entry));
         out = new BufferedOutputStream(new FileOutputStream(dstFile));
         byte[] buffer = new byte[2048];
         for (;;) {
            int nBytes = in.read(buffer);
            if (nBytes <= 0)
               break;
            out.write(buffer, 0, nBytes);
         }
         return true;
      } catch (IOException ex) {
         ex.printStackTrace();
         return false;
      } finally {
         if (in != null) {
            try {
               in.close();
            } catch (IOException e) {
               e.printStackTrace();
               return false;
            }
         }
         if (out != null) {
            try {
               out.flush();
               out.close();
            } catch (IOException e) {
               e.printStackTrace();
               return false;
            }
         }
      }
   }
}
