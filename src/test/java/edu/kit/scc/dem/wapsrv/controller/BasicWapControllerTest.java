package edu.kit.scc.dem.wapsrv.controller;

import edu.kit.scc.dem.wapsrv.app.WapServerConfig;

/**
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public abstract class BasicWapControllerTest {
   /**
    * Make base URL from relative string input.
    * 
    * @param  rel
    *             relative URL without hostname.
    * @return     base URL
    */
   protected String makeUrl(String rel) {
      if (rel.startsWith("/")) {
         return getWapServerConfig().getBaseUrl() + rel;
      } else {
         return getWapServerConfig().getBaseUrl() + "/" + rel;
      }
   }

   /**
    * @return WapServerConfig instance
    */
   protected abstract WapServerConfig getWapServerConfig();
}
