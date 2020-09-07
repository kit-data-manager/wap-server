package edu.kit.scc.dem.wapsrv.app;

/**
 * The keys that can be used within the configuration. Their actual meaning is explained in details at the corresponding
 * variables in {@link WapServerConfig}.
 *
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public enum ConfigurationKeys {
   /**
    * @see WapServerConfig#hostname
    */
   Hostname,
   /**
    * @see WapServerConfig#wapPort
    */
   WapPort,
   /**
    * @see WapServerConfig#wapIp
    */
   WapIp,
   /**
    * @see WapServerConfig#sparqlReadPort
    */
   SparqlReadPort,
   /**
    * @see WapServerConfig#sparqlReadIp
    */
   SparqlReadIp,
   /**
    * @see WapServerConfig#sparqlWritePort
    */
   SparqlWritePort,
   /**
    * @see WapServerConfig#sparqlWriteIp
    */
   SparqlWriteIp,
   /**
    * @see WapServerConfig#enableValidation
    */
   EnableValidation,
   /**
    * @see WapServerConfig#jsonLdValidatorSchemaFolder
    */
   JsonLdValidator_SchemaFolder,
   /**
    * @see WapServerConfig#enableMandatoryLabelsInContainers
    */
   EnableMandatoryLabelInContainers,
   /**
    * @see WapServerConfig#enableMandatorySlugInContainerPost
    */
   EnableMandatorySlugInContainerPost,
   /**
    * @see WapServerConfig#enableContentNegotiation
    */
   EnableContentNegotiation,
   /**
    * @see WapServerConfig#jsonLdProfileFolder
    */
   JsonLdProfileFolder,
   /**
    * @see WapServerConfig#jsonLdCachedProfileValidityInMs
    */
   JsonLdCachedProfileValidityInMs,
   /**
    * @see WapServerConfig#dataBasePath
    */
   DataBasePath,
   /**
    * @see WapServerConfig#webClientFolder
    */
   WebClientFolder,
   /**
    * @see WapServerConfig#javaDocFolder
    */
   JavaDocFolder,
   /**
    * @see WapServerConfig#jsonLdFrameFolder
    */
   JsonLdFrameFolder,
   /**
    * @see WapServerConfig#pageSize
    */
   PageSize,
   /**
    * @see WapServerConfig#shouldAppendStackTraceToErrorMessages
    */
   ShouldAppendStackTraceToErrorMessages,
   /**
    * @see WapServerConfig#enableHttps
    */
   EnableHttps,
   /**
    * @see WapServerConfig#multipleAnnotationPost
    */
   MultipleAnnotationPost,
   /**
    * @see WapServerConfig#simpleFormatters
    */
   SimpleFormatters,
   /**
    * @see WapServerConfig#corsAllowedOriginsPath
    */
   CorsAllowedOriginsPath,
   /**
    * @see WapServerConfig#fallbackValidation
    */
   FallbackValidation
}
