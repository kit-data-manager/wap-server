package edu.kit.scc.dem.wapsrv.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpUpgradeHandler;
import jakarta.servlet.http.Part;

/**
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public class HttpServletRequestAdapter implements HttpServletRequest {
   private String url = null;
   private String httpMethod = null;
   private Map<String, String[]> paramsMap = null;
   private String acceptHeader;

   /**
    * Default Constructor
    */
   public HttpServletRequestAdapter() {
   }

   /**
    * Constructor for String
    * 
    * @param url
    *            .
    */
   public HttpServletRequestAdapter(String url) {
      this.url = url;
   }

   /**
    * Constructor for String String
    * 
    * @param url
    *                   .
    * @param httpMethod
    *                   .
    */
   public HttpServletRequestAdapter(String url, String httpMethod) {
      this.httpMethod = httpMethod;
      this.url = url;
   }

   /**
    * Constructor for String String Map<String, String[]>
    * 
    * @param url
    *                   .
    * @param httpMethod
    *                   .
    * @param paramsMap
    *                   .
    */
   public HttpServletRequestAdapter(String url, String httpMethod, Map<String, String[]> paramsMap) {
      this.httpMethod = httpMethod;
      this.url = url;
      this.paramsMap = paramsMap;
   }

   /**
    * Constructor for String String Map<String, String[]> String
    * 
    * @param url
    *                     .
    * @param httpMethod
    *                     .
    * @param paramsMap
    *                     .
    * @param acceptHeader
    *                     .
    */
   public HttpServletRequestAdapter(String url, String httpMethod, Map<String, String[]> paramsMap,
         String acceptHeader) {
      this.httpMethod = httpMethod;
      this.url = url;
      this.paramsMap = paramsMap;
      this.acceptHeader = acceptHeader;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.ServletRequest#getAttribute(java.lang.String)
    */
   @Override
   public Object getAttribute(String name) {
      return null;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.ServletRequest#getAttributeNames()
    */
   @Override
   public Enumeration<String> getAttributeNames() {
      return null;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.ServletRequest#getCharacterEncoding()
    */
   @Override
   public String getCharacterEncoding() {
      return null;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.ServletRequest#setCharacterEncoding(java.lang.String)
    */
   @Override
   public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.ServletRequest#getContentLength()
    */
   @Override
   public int getContentLength() {
      return 0;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.ServletRequest#getContentLengthLong()
    */
   @Override
   public long getContentLengthLong() {
      return 0;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.ServletRequest#getContentType()
    */
   @Override
   public String getContentType() {
      return null;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.ServletRequest#getInputStream()
    */
   @Override
   public ServletInputStream getInputStream() throws IOException {
      return null;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.ServletRequest#getParameter(java.lang.String)
    */
   @Override
   public String getParameter(String name) {
      if (this.paramsMap != null) {
         String[] values = paramsMap.get(name);
         if (values == null || values.length == 0)
            return null;
         else
            return values[0];
      }
      return null;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.ServletRequest#getParameterNames()
    */
   @Override
   public Enumeration<String> getParameterNames() {
      return null;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.ServletRequest#getParameterValues(java.lang.String)
    */
   @Override
   public String[] getParameterValues(String name) {
      return null;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.ServletRequest#getParameterMap()
    */
   @Override
   public Map<String, String[]> getParameterMap() {
      return paramsMap;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.ServletRequest#getProtocol()
    */
   @Override
   public String getProtocol() {
      return null;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.ServletRequest#getScheme()
    */
   @Override
   public String getScheme() {
      return null;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.ServletRequest#getServerName()
    */
   @Override
   public String getServerName() {
      return null;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.ServletRequest#getServerPort()
    */
   @Override
   public int getServerPort() {
      return 0;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.ServletRequest#getReader()
    */
   @Override
   public BufferedReader getReader() throws IOException {
      return null;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.ServletRequest#getRemoteAddr()
    */
   @Override
   public String getRemoteAddr() {
      return null;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.ServletRequest#getRemoteHost()
    */
   @Override
   public String getRemoteHost() {
      return null;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.ServletRequest#setAttribute(java.lang.String, java.lang.Object)
    */
   @Override
   public void setAttribute(String name, Object o) {
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.ServletRequest#removeAttribute(java.lang.String)
    */
   @Override
   public void removeAttribute(String name) {
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.ServletRequest#getLocale()
    */
   @Override
   public Locale getLocale() {
      return null;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.ServletRequest#getLocales()
    */
   @Override
   public Enumeration<Locale> getLocales() {
      return null;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.ServletRequest#isSecure()
    */
   @Override
   public boolean isSecure() {
      return false;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.ServletRequest#getRequestDispatcher(java.lang.String)
    */
   @Override
   public RequestDispatcher getRequestDispatcher(String path) {
      return null;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.ServletRequest#getRemotePort()
    */
   @Override
   public int getRemotePort() {
      return 0;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.ServletRequest#getLocalName()
    */
   @Override
   public String getLocalName() {
      return null;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.ServletRequest#getLocalAddr()
    */
   @Override
   public String getLocalAddr() {
      return null;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.ServletRequest#getLocalPort()
    */
   @Override
   public int getLocalPort() {
      return 0;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.ServletRequest#getServletContext()
    */
   @Override
   public ServletContext getServletContext() {
      return null;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.ServletRequest#startAsync()
    */
   @Override
   public AsyncContext startAsync() throws IllegalStateException {
      return null;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.ServletRequest#startAsync(javax.servlet.ServletRequest, javax.servlet.ServletResponse)
    */
   @Override
   public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse)
         throws IllegalStateException {
      return null;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.ServletRequest#isAsyncStarted()
    */
   @Override
   public boolean isAsyncStarted() {
      return false;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.ServletRequest#isAsyncSupported()
    */
   @Override
   public boolean isAsyncSupported() {
      return false;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.ServletRequest#getAsyncContext()
    */
   @Override
   public AsyncContext getAsyncContext() {
      return null;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.ServletRequest#getDispatcherType()
    */
   @Override
   public DispatcherType getDispatcherType() {
      return null;
   }

   @Override
   public String getRequestId() {
      return "";
   }

   @Override
   public String getProtocolRequestId() {
      return "";
   }

   @Override
   public ServletConnection getServletConnection() {
      return null;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.http.HttpServletRequest#getAuthType()
    */
   @Override
   public String getAuthType() {
      return null;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.http.HttpServletRequest#getCookies()
    */
   @Override
   public Cookie[] getCookies() {
      return null;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.http.HttpServletRequest#getDateHeader(java.lang.String)
    */
   @Override
   public long getDateHeader(String name) {
      return 0;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.http.HttpServletRequest#getHeader(java.lang.String)
    */
   @Override
   public String getHeader(String name) {
      if ("Accept".equals(name))
         return acceptHeader;
      return null;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.http.HttpServletRequest#getHeaders(java.lang.String)
    */
   @Override
   public Enumeration<String> getHeaders(String name) {
      return null;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.http.HttpServletRequest#getHeaderNames()
    */
   @Override
   public Enumeration<String> getHeaderNames() {
      return null;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.http.HttpServletRequest#getIntHeader(java.lang.String)
    */
   @Override
   public int getIntHeader(String name) {
      return 0;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.http.HttpServletRequest#getMethod()
    */
   @Override
   public String getMethod() {
      return httpMethod;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.http.HttpServletRequest#getPathInfo()
    */
   @Override
   public String getPathInfo() {
      return null;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.http.HttpServletRequest#getPathTranslated()
    */
   @Override
   public String getPathTranslated() {
      return null;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.http.HttpServletRequest#getContextPath()
    */
   @Override
   public String getContextPath() {
      return null;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.http.HttpServletRequest#getQueryString()
    */
   @Override
   public String getQueryString() {
      return null;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.http.HttpServletRequest#getRemoteUser()
    */
   @Override
   public String getRemoteUser() {
      return null;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.http.HttpServletRequest#isUserInRole(java.lang.String)
    */
   @Override
   public boolean isUserInRole(String role) {
      return false;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.http.HttpServletRequest#getUserPrincipal()
    */
   @Override
   public Principal getUserPrincipal() {
      return null;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.http.HttpServletRequest#getRequestedSessionId()
    */
   @Override
   public String getRequestedSessionId() {
      return null;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.http.HttpServletRequest#getRequestURI()
    */
   @Override
   public String getRequestURI() {
      return null;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.http.HttpServletRequest#getRequestURL()
    */
   @Override
   public StringBuffer getRequestURL() {
      if (url == null)
         return null;
      else
         return new StringBuffer(url);
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.http.HttpServletRequest#getServletPath()
    */
   @Override
   public String getServletPath() {
      return null;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.http.HttpServletRequest#getSession(boolean)
    */
   @Override
   public HttpSession getSession(boolean create) {
      return null;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.http.HttpServletRequest#getSession()
    */
   @Override
   public HttpSession getSession() {
      return null;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.http.HttpServletRequest#changeSessionId()
    */
   @Override
   public String changeSessionId() {
      return null;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.http.HttpServletRequest#isRequestedSessionIdValid()
    */
   @Override
   public boolean isRequestedSessionIdValid() {
      return false;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.http.HttpServletRequest#isRequestedSessionIdFromCookie()
    */
   @Override
   public boolean isRequestedSessionIdFromCookie() {
      return false;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.http.HttpServletRequest#isRequestedSessionIdFromURL()
    */
   @Override
   public boolean isRequestedSessionIdFromURL() {
      return false;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.http.HttpServletRequest#authenticate(javax.servlet.http.HttpServletResponse)
    */
   @Override
   public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
      return false;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.http.HttpServletRequest#login(java.lang.String, java.lang.String)
    */
   @Override
   public void login(String username, String password) throws ServletException {
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.http.HttpServletRequest#logout()
    */
   @Override
   public void logout() throws ServletException {
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.http.HttpServletRequest#getParts()
    */
   @Override
   public Collection<Part> getParts() throws IOException, ServletException {
      return null;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.http.HttpServletRequest#getPart(java.lang.String)
    */
   @Override
   public Part getPart(String name) throws IOException, ServletException {
      return null;
   }

   /*
    * (non-Javadoc)
    * @see jakarta.servlet.http.HttpServletRequest#upgrade(java.lang.Class)
    */
   @Override
   public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
      return null;
   }
}
