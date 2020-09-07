package edu.kit.scc.dem.wapsrv.model.validators;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import edu.kit.scc.dem.wapsrv.app.WapServerConfig;
import edu.kit.scc.dem.wapsrv.exceptions.InternalServerException;
import edu.kit.scc.dem.wapsrv.model.formats.Format;

/**
 * This is the registry for existing input validators.<br>
 * As long as the implemented validators use the (at)Component annotation of Spring, they are automatically registered
 * for usage here. The class is final so no subclasses can exist. A single instance is needed.
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
@Component
public final class ValidatorRegistry {
   /**
    * The single instance of the registry
    */
   private static ValidatorRegistry instance = null;
   /**
    * Map of registered validators for given formats
    */
   private final Map<Format, Class<? extends Validator>> format2Validator
         = new Hashtable<Format, Class<? extends Validator>>();
   /**
    * The logger to use
    */
   private final Logger logger = LoggerFactory.getLogger(ValidatorRegistry.class);
   /**
    * The application configuration
    */
   @Autowired
   private WapServerConfig wapServerConfig;

   /**
    * The constructor
    */
   private ValidatorRegistry() {
      // Register single instance created by Spring
      instance = this;
   }

   /**
    * Returns the single validator registry instance.<br>
    * This method can be called when Spring autowire is not possible/supported.
    * 
    * @return The validator registry singleton
    */
   public static ValidatorRegistry getInstance() {
      return instance;
   }

   /**
    * Registers all validators found by Spring autowiring
    * 
    * @param validators
    *                   The found validators to register
    */
   @Autowired
   private void registerFormatters(List<Validator> validators) {
      for (Validator validator : validators) {
         registerValidator(validator);
      }
   }

   /**
    * Registers a given validator
    * 
    * @param validator
    *                  The validator to register
    */
   private void registerValidator(Validator validator) {
      format2Validator.put(validator.getFormat(), validator.getClass());
      LoggerFactory.getLogger(getClass()).info(
            "New validator registered : " + validator.getClass().getName() + " for format : " + validator.getFormat());
   }

   /**
    * Get a Set of all registered formats
    * 
    * @return The registered formats
    */
   public Set<Format> getSupportedFormats() {
      return format2Validator.keySet();
   }

   /**
    * Gets a validator implementing a given format
    * 
    * @param  format
    *                The format
    * @return        The validator implementing it, may be null
    */
   public Validator getValidator(Format format) {
      Class<? extends Validator> validatorClass = format2Validator.get(format);
      if (validatorClass == null) {
         return null;
      }
      try {
         // First check for the existence of an empty constructor
         try {
            Constructor<?> emptyConstructor = validatorClass.getConstructor(new Class<?>[] {});
            Validator newInstance = (Validator) emptyConstructor.newInstance();
            logger.info("Instantiated validator using the empty constructor");
            return newInstance;
         } catch (NoSuchMethodException e) {
            // Empty constructor not found
         }
         // Look for one that needs the servers configuration
         try {
            Constructor<?> configConstructor = validatorClass.getConstructor(new Class<?>[] {WapServerConfig.class});
            Validator newInstance = (Validator) configConstructor.newInstance(wapServerConfig);
            logger.info("Instantiated validator using the WapServerConfig");
            return newInstance;
         } catch (NoSuchMethodException ex) {
            // This one does not exist, too.
         }
         // Until additional types of constructors are implemented throw an exception
         throw new InternalServerException("No usable constructor found for validator of format " + format);
      } catch (SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException
            | InvocationTargetException e) {
         throw new InternalServerException(e.getMessage());
      }
   }
}
