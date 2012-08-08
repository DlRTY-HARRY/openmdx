/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ZipURLConnection.java,v 1.6 2008/06/28 00:21:33 hburger Exp $
 * Description: Resource URL Connection
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/06/28 00:21:33 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * ------------------
 * 
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 * ___________________________________________________________________________ 
 *
 * This class should log as it has to be loaded by the system class loader. 
 */
package org.openmdx.kernel.url.protocol.xri;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Permission;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.openmdx.kernel.url.protocol.XriAuthorities;
import org.openmdx.kernel.url.protocol.XriProtocols;

/**
 * An delegating URLConnection support class.
 */
public class ZipURLConnection extends JarURLConnection {

	/**
	 * 
	 */
	protected URL xri;
	
	/**
	 * 
	 */
	protected JarURLConnection delegateConnection;
	   
	/**
	 * Constructor 
	 * 
	 * @param xri
	 * @param url
	 * 
	 * @throws IOException
	 */
	private ZipURLConnection(
	    final URL xri,
	    final URL url
	) throws IOException {
	      super(url);	      
	      this.xri = xri;
	      delegateConnection = (JarURLConnection)url.openConnection();
	}
	
	/**
	 * Constructor
	 * 
	 * @param xri
	 * @throws IOException
	 */
	public ZipURLConnection(
	    final URL xri
	) throws IOException {
	      this(xri,toURL(xri));
	}

	private final static URL toURL(
	    final URL xri
	) throws MalformedURLException, IOException {
        String url = xri.toExternalForm();
        int i = url.lastIndexOf(XriProtocols.ZIP_SEPARATOR);
        if(i < 0) throw new MalformedURLException(
            "No separator ('" + XriProtocols.ZIP_SEPARATOR + "' found in URL " + url
        );
        return new URL(
            JAR_PREFIX + url.substring(XriAuthorities.ZIP_AUTHORITY.length() + 8, i) + 
			JAR_SEPARATOR + url.substring(i + XriProtocols.ZIP_SEPARATOR.length())
        );
	}

	//------------------------------------------------------------------------
	// Extends JarURLConnection
	//------------------------------------------------------------------------

    /**
     * As defined by JarURLConnection.
     * <p>
     * A JAR may be embedded in a ZIP, an EAR, a WAR or a RAR.
     * 
     * @see java.net.JarURLConnection
     */
    public final static String JAR_SEPARATOR = "!/";

    /**
     * The jar protocol
     */
    public final static String JAR_PROTOCOL = "jar";

    /**
     * A derived valu the JAR prefix
     */
    public final static String JAR_PREFIX = JAR_PROTOCOL + ':';

    /**   
     * Return the entry name for this connection. This method
     * returns null if the JAR file URL corresponding to this
     * connection points to a JAR file and not a JAR file entry.
     *
     * @return the entry name for this connection, if any.  
     */
    public String getEntryName() {
        return this.delegateConnection.getEntryName();
    }

    /**   
     * Return the JAR file for this connection. The returned object is
     * not modifiable, and will throw UnsupportedOperationException
     * if the caller attempts to modify it.
     *
     * @return the JAR file for this connection. If the connection is
     * a connection to an entry of a JAR file, the JAR file object is
     * returned
     *
     * @exception IOException if an IOException occurs while trying to
     * connect to the JAR file for this connection.
     *
     * @see #connect
     */
    public JarFile getJarFile(
    ) throws IOException {
        return this.delegateConnection.getJarFile();
    }

    /**
     * Returns the Manifest for this connection, or null if none. The
     * returned object is not modifiable, and will throw
     * UnsupportedOperationException if the caller attempts to modify
     * it.
     *
     * @return the manifest object corresponding to the JAR file object
     * for this connection.
     *
     * @exception IOException if getting the JAR file for this
     * connection causes an IOException to be trown.
     *
     * @see #getJarFile
     */
    public Manifest getManifest() throws IOException {
        return this.delegateConnection.getManifest();
    }
        
    /**  
     * Return the JAR entry object for this connection, if any. This
     * method returns null if the JAR file URL corresponding to this
     * connection points to a JAR file and not a JAR file entry. The
     * returned object is not modifiable, and will throw
     * UnsupportedOperationException if the caller attempts to modify
     * it.  
     *
     * @return the JAR entry object for this connection, or null if
     * the JAR URL for this connection points to a JAR file.
     *
     * @exception IOException if getting the JAR file for this
     * connection causes an IOException to be trown.
     *
     * @see #getJarFile
     * @see #getJarEntry
     */
    public JarEntry getJarEntry(
    ) throws IOException {
        return this.delegateConnection.getJarEntry();
    }

    /**
     * Return the Attributes object for this connection if the URL
     * for it points to a JAR file entry, null otherwise.
     * 
     * @return the Attributes object for this connection if the URL
     * for it points to a JAR file entry, null otherwise.  
     *
     * @exception IOException if getting the JAR entry causes an
     * IOException to be thrown.
     *
     * @see #getJarEntry
     */
    public Attributes getAttributes(
    ) throws IOException {
        return this.delegateConnection.getAttributes();
    }
  
    /**    
     * Returns the main Attributes for the JAR file for this
     * connection.
     *
     * @return the main Attributes for the JAR file for this
     * connection.
     *
     * @exception IOException if getting the manifest causes an
     * IOException to be thrown.
     *
     * @see #getJarFile
     * @see #getManifest 
     */
    public Attributes getMainAttributes(
    ) throws IOException { 
        return this.delegateConnection.getMainAttributes();
    }
   
    /**
     * Return the Certificate object for this connection if the URL
     * for it points to a JAR file entry, null otherwise. This method 
     * can only be called once
     * the connection has been completely verified by reading
     * from the input stream until the end of the stream has been
     * reached. Otherwise, this method will return <code>null</code>
     * 
     * @return the Certificate object for this connection if the URL
     * for it points to a JAR file entry, null otherwise.  
     *
     * @exception IOException if getting the JAR entry causes an
     * IOException to be thrown.
     *
     * @see #getJarEntry
     */
    public java.security.cert.Certificate[] getCertificates(
    ) throws IOException {
        return this.delegateConnection.getCertificates();
    }

	    
	//------------------------------------------------------------------------
	// Extends URLConnection
	//------------------------------------------------------------------------

   public void connect() throws IOException
   {
   	  try {
          delegateConnection.connect();
 	  } catch (IOException ioException) {
 	  	  throw new IOException(
 	  	      this.xri + ": " + ioException.getMessage()
 	  	  );
 	  }
   }
   
   public URL getURL() {
      return delegateConnection.getURL();
   }

   public int getContentLength() {
      return delegateConnection.getContentLength();
   }

   public String getContentType() {
      return delegateConnection.getContentType();
   }

   public String getContentEncoding() {
      return delegateConnection.getContentEncoding();
   }

   public long getExpiration() {
      return delegateConnection.getExpiration();
   }

   public long getDate() {
      return delegateConnection.getDate();
   }

   public long getLastModified() {
      return delegateConnection.getLastModified();
   }

   public String getHeaderField(String name) {
      return delegateConnection.getHeaderField(name);
   }

   public Map<String,List<String>> getHeaderFields() {
      return delegateConnection.getHeaderFields();
   }
   
   public int getHeaderFieldInt(String name, int _default) {
      return delegateConnection.getHeaderFieldInt(name, _default);
   }

   public long getHeaderFieldDate(String name, long _default) {
      return delegateConnection.getHeaderFieldDate(name, _default);
   }

   public String getHeaderFieldKey(int n) {
      return delegateConnection.getHeaderFieldKey(n);
   }

   public String getHeaderField(int n) {
      return delegateConnection.getHeaderField(n);
   }

   public Object getContent() throws IOException {
   	  try {
          return delegateConnection.getContent();
 	  } catch (IOException ioException) {
 	  	  throw new IOException(
 	  	      this.xri + ": " + ioException.getMessage()
 	  	  );
 	  }
   }

   @SuppressWarnings("unchecked")
   public Object getContent(Class[] classes) throws IOException {
   	  try {
          return delegateConnection.getContent(classes);
 	  } catch (IOException ioException) {
 	  	  throw new IOException(
 	  	      this.xri + ": " + ioException.getMessage()
 	  	  );
 	  }
   }

   public Permission getPermission() throws IOException {
   	  try {
          return delegateConnection.getPermission();
 	  } catch (IOException ioException) {
 	  	  throw new IOException(
 	  	      this.xri + ": " + ioException.getMessage()
 	  	  );
 	  }
   }

   public InputStream getInputStream() throws IOException {
   	  try{
	      return delegateConnection.getInputStream();
   	  } catch (IOException ioException) {
   	  	  throw new IOException(
   	  	      this.xri + ": " + ioException.getMessage()
   	  	  );
   	  }
   }

   public OutputStream getOutputStream() throws IOException {
   	  try {
   	  	  return delegateConnection.getOutputStream();
 	  } catch (IOException ioException) {
 	  	  throw new IOException(
 	  	      this.xri + ": " + ioException.getMessage()
 	  	  );
 	  }
   }

   public String toString() {
      return super.toString() + "{ " + delegateConnection + " }";
   }

   public void setDoInput(boolean doinput) {
      delegateConnection.setDoInput(doinput);
   }
   
   public boolean getDoInput() {
      return delegateConnection.getDoInput();
   }

   public void setDoOutput(boolean dooutput) {
      delegateConnection.setDoOutput(dooutput);
    }

   public boolean getDoOutput() {
      return delegateConnection.getDoOutput();
   }

   public void setAllowUserInteraction(boolean allowuserinteraction) {
      delegateConnection.setAllowUserInteraction(allowuserinteraction);
   }

   public boolean getAllowUserInteraction() {
      return delegateConnection.getAllowUserInteraction();
   }

   public void setUseCaches(boolean usecaches) {
      delegateConnection.setUseCaches(usecaches);
   }

   public boolean getUseCaches() {
      return delegateConnection.getUseCaches();
   }

   public void setIfModifiedSince(long ifmodifiedsince) {
      delegateConnection.setIfModifiedSince(ifmodifiedsince);
   }

   public long getIfModifiedSince() {
      return delegateConnection.getIfModifiedSince();
   }

   public boolean getDefaultUseCaches() {
      return delegateConnection.getDefaultUseCaches();
   }

   public void setDefaultUseCaches(boolean defaultusecaches) {
      delegateConnection.setDefaultUseCaches(defaultusecaches);
   }

   public void setRequestProperty(String key, String value) {
      delegateConnection.setRequestProperty(key, value);
   }

   public void addRequestProperty(String key, String value) {
      delegateConnection.addRequestProperty(key, value);
   }
   
   public String getRequestProperty(String key) {
      return delegateConnection.getRequestProperty(key);
   }

   public Map<String,List<String>> getRequestProperties() {
      return delegateConnection.getRequestProperties();
   }

   /**
    * @return
    * @see java.net.URLConnection#getConnectTimeout()
    */
   public int getConnectTimeout() {
      return this.delegateConnection.getConnectTimeout();
   }
    
   /**
    * @return
    * @see java.net.URLConnection#getReadTimeout()
    */
   public int getReadTimeout() {
      return this.delegateConnection.getReadTimeout();
   }
    
   /**
    * @param timeout
    * @see java.net.URLConnection#setConnectTimeout(int)
    */
   public void setConnectTimeout(int timeout) {
      this.delegateConnection.setConnectTimeout(timeout);
   }
    
   /**
    * @param timeout
    * @see java.net.URLConnection#setReadTimeout(int)
    */
   public void setReadTimeout(int timeout) {
      this.delegateConnection.setReadTimeout(timeout);
   }

}
