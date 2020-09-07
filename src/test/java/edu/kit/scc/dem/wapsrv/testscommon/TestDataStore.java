package edu.kit.scc.dem.wapsrv.testscommon;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * TestDataStore
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public final class TestDataStore {
   /**
    * When set to true, examples that are lists will not be used
    */
   // private static final boolean IGNORE_LISTS = true;
   private static final File CONTAINERFOLDER = new File("src/main/resources/testdata/container");
   private static final File ANNOTATIONFOLDER = new File("src/main/resources/testdata/annotations");

   private TestDataStore() {
      throw new RuntimeException("No usage expected");
   }

   private static String readString(File file) {
      try {
         int read = 0;
         byte[] bytes = new byte[(int) file.length()];
         FileInputStream in = new FileInputStream(file);
         while (read != bytes.length) {
            read += in.read(bytes, read, bytes.length - read);
         }
         in.close();
         return new String(bytes, "UTF-8");
      } catch (IOException e) {
         e.getMessage();
         return "Read error for " + file.getName();
      }
   }

   /**
    * Gets the container.
    *
    * @return The container
    */
   public static String getContainer() {
      File[] candidates = CONTAINERFOLDER.listFiles(new FileFilter() {
         @Override
         public boolean accept(File pathname) {
            if (pathname.getName().toLowerCase().indexOf("invalid") != -1)
               return false;
            return true;
         }
      });
      if (candidates == null || candidates.length == 0)
         return "No container candidates in " + CONTAINERFOLDER.getAbsolutePath();
      // Random rnd = new Random();
      return readString(candidates[0]);
   }

   /**
    * Gets the annotation.
    *
    * @return The annotation
    */
   public static String getAnnotation() {
      File[] candidates = ANNOTATIONFOLDER.listFiles(new FileFilter() {
         @Override
         public boolean accept(File pathname) {
            if (pathname.getName().toLowerCase().indexOf("invalid") != -1)
               return false;
            return true;
         }
      });
      if (candidates == null || candidates.length == 0)
         return "No annotation candidates in " + ANNOTATIONFOLDER.getAbsolutePath();
      Random rnd = new Random();
      return readString(candidates[rnd.nextInt(candidates.length)]);
   }

   /**
    * List containers.
    *
    * @return The array of containers
    */
   public static String[] listContainers() {
      File[] candidates = CONTAINERFOLDER.listFiles(new FileFilter() {
         @Override
         public boolean accept(File pathname) {
            if (pathname.getName().toLowerCase().indexOf("invalid") != -1)
               return false;
            return true;
         }
      });
      if (candidates == null)
         candidates = new File[0];
      String[] erg = new String[candidates.length];
      for (int n = 0; n < candidates.length; n++) {
         erg[n] = candidates[n].getName();
      }
      return erg;
   }

   /**
    * Gets the invalid container keys.
    *
    * @return The array of invalid containers
    */
   public static String[] getInvalidContainerKeys() {
      File[] candidates = CONTAINERFOLDER.listFiles(new FileFilter() {
         @Override
         public boolean accept(File pathname) {
            if (pathname.getName().toLowerCase().indexOf("invalid") == -1)
               return false;
            return true;
         }
      });
      if (candidates == null)
         candidates = new File[0];
      String[] erg = new String[candidates.length];
      for (int n = 0; n < candidates.length; n++) {
         erg[n] = candidates[n].getName();
      }
      return erg;
   }

   /**
    * Gets the container.
    *
    * @param  key
    *             The key of container
    * @return     The container
    */
   public static String getContainer(String key) {
      return readString(new File(CONTAINERFOLDER, key));
   }

   /**
    * List annotations.
    *
    * @return The array of annotations
    */
   public static String[] listAnnotations() {
      File[] candidates = ANNOTATIONFOLDER.listFiles(new FileFilter() {
         @Override
         public boolean accept(File pathname) {
            if (pathname.getName().toLowerCase().indexOf("invalid") != -1)
               return false;
            return pathname.getName().toLowerCase().indexOf("list") == -1;
         }
      });
      if (candidates == null)
         candidates = new File[0];
      String[] erg = new String[candidates.length];
      for (int n = 0; n < candidates.length; n++) {
         erg[n] = candidates[n].getName();
      }
      return erg;
   }

   /**
    * Gets the annotation.
    *
    * @param  key
    *             The key of annotation
    * @return     The annotation
    */
   public static String getAnnotation(String key) {
      return readString(new File(ANNOTATIONFOLDER, key));
   }

   /**
    * Read annotations.
    *
    * @return The array of annotations
    */
   public static String[] readAnnotations() {
      String[] annotationKeys = listAnnotations();
      String[] annotations = new String[annotationKeys.length];
      for (int n = 0; n < annotations.length; n++) {
         annotations[n] = getAnnotation(annotationKeys[n]);
      }
      return annotations;
   }

   /**
    * Gets the annotation.
    *
    * @param  number
    *                Number of annotation
    * @return        The annotation
    */
   public static String getAnnotation(int number) {
      for (String key : listAnnotations()) {
         if (key.startsWith("example" + number + "_")) {
            return getAnnotation(key);
         } else if (key.startsWith("example" + number + ".")) {
            return getAnnotation(key);
         }
      }
      return null;
   }

   /**
    * Gets the invalid annotation keys.
    *
    * @return The invalid annotation keys
    */
   public static Set<String> getInvalidAnnotationKeys() {
      File[] candidates = ANNOTATIONFOLDER.listFiles(new FileFilter() {
         @Override
         public boolean accept(File pathname) {
            if (pathname.getName().toLowerCase().indexOf("invalid") == -1)
               return false;
            return true;
         }
      });
      if (candidates == null)
         candidates = new File[0];
      Set<String> erg = new HashSet<String>();
      for (int n = 0; n < candidates.length; n++) {
         erg.add(candidates[n].getName());
      }
      return erg;
   }

   /**
    * Gets the maximum annotation number.
    *
    * @return The maximum annotation number
    */
   public static int getMaxAnnotationNumber() {
      return listAnnotations().length;
   }
}
