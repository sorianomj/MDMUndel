package com.ihis.undelete;


import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

import madison.mpi.MpiNetConstants;
import madison.mpi.MpiNetSecure;
import madison.mpi.UsrHead;

/**
 * This class implements a simple Context Factory that helps in management of
 * Context Pools.<BR>
 * Context Pools are created based on property files.<BR>
 * See the main method in this class for usage illustrations.
 */
public class ExContextFactory
{
  private static final String PROP_FILE_DEFAULT_PREFIX = "examples";
  private static Map<String, ExContextFactory> factories_ = new Hashtable<String, ExContextFactory>();
  private UsrHead usrHead = null;
  private ResourceBundle rb = null;
  private List<ExContextWrapper> activeContexts = new ArrayList<ExContextWrapper>();
  private String propFilePassed = "";
  
  /**
   * Command-line entry into the class. This method is used for command-line
   * testing.
   * 
   * @param args
   *          no command line arguments are excepted.
   * @throws Exception
   */
  public static void main(String[] args)
  {
    try
    {
      // First we must get an instance of the factory.
      // We can pass null as a parameter, which will cause the factory 
      // to read the ${PROP_FILE_DEFAULT_PREFIX}.properties or we can
      // specify a different property file prefix.
      ExContextFactory ecf = ExContextFactory.getInstance(null);
      // Now that we have an instance of the factory we can get a single 
      // context out of the available pool
      ExContextWrapper ecw = ecf.getWrappedContext();
      // Notice that we are getting back an instance of ExContextWrapper.
      // We created this wrapper to allow the ExContextFactory to keep
      // track of the Context objects that are being used.
      info("Context created.");
      if(ecw.isConnected())
        info("Context is connected");
      else
        err("Context is NOT connected.");
      // It is important to call the freeContext() method with the 
      // ExContextWrapper we are done with, in order to return it back to the pool.
      // We need a reference to ExContextWrapper in order to "free" it:
      ecf.freeContext(ecw);
      // The call above returned our ExContextWrapper back to the pool.
      // We must not try to use this instance again.
    } catch (Exception e)
    {
      System.err.println("Execution of the class ContextFactory haulted due to Exception: \n" + e.toString());
    }
  }
  
  /**
   * This class is protected from instantiation. Use
   * ContextFactory.getInstance().
   * 
   * @param propFileName
   *          if null or empty the default will be used.
   * @throws Exception
   *           if the ExContextFactory could not be created.
   */
  private ExContextFactory(String propFileName) throws Exception
  {
    propFilePassed = propFileName;
    refreshContexts();
  }
  
  /**
   * Creates an instance of the ExContextFactory based on the named property
   * file name.
   * 
   * @param propFileName
   *          the property file name (.properties will be added to this file
   *          name during the read).
   * @return An instance of ExContextFactory
   * @throws Exception
   *           if the ExContextFactory could not be created.
   */
  public static ExContextFactory getInstance(String propFileName) throws Exception
  {
    if(propFileName == null || propFileName.trim().equals(""))
      propFileName = PROP_FILE_DEFAULT_PREFIX;
    if (factories_.get(propFileName) == null)
      factories_.put(propFileName, new ExContextFactory(propFileName));
    return factories_.get(propFileName);
  }
  
  /**
   * Gets an ExContextWrapper out of the available pool.
   * freeContext(ExContextWrapper) must be executed in order for this
   * ExContextWrapper to be returned to the pool.
   * 
   * @return ExContextWrapper
   * @throws Exception
   *           if context pool could not be created or there are no
   *           ExContextWrapper object left unused in the pool.
   */
  protected synchronized ExContextWrapper getWrappedContext() throws Exception
  {
    ExContextWrapper ewcReturn = null;
    if (activeContexts.size() < 1)
      refreshContexts();
    for(ExContextWrapper ewc : activeContexts)
    {
      if(!ewc.isCheckedOut())
      {
        ewc.checkOut();
        ewcReturn = ewc;
      }
    }
    if(ewcReturn == null)
    {
      // A wait() call could be implemented here to wait for an ExContextWrapper
      // to be freed in the pool, but a bottle neck here is probably indicative
      // of a low ctxMax setting in the .properties file.
      err("All of the available Context objects in the pool based on the " + propFilePassed
          + ".properties are being used, and the context pool has been exhausted.");
    }
    return ewcReturn;
  }
  
  /**
   * This method must be called every time the ExContextWrapper is no longer
   * used. Not calling this method in all execution paths will cause pool
   * starvation.
   * 
   * @param exContextWrapper
   *          the ExContextWrapper to be returned to the pool.
   */
  protected synchronized void freeContext(ExContextWrapper exContextWrapper)
  {
    for (ExContextWrapper ewc : activeContexts)
    {
      if (ewc.equals(exContextWrapper))
      {
        ewc.checkIn();
        info("Context # " + ewc.getMnemonic() + " has been returned to the pool.");
      }
    }
  }
  
  /**
   * This method re-reads the .properties file and re-establishes all of the
   * ExContextWrapper in the pool.
   * 
   * @throws Exception
   */
  private void refreshContexts() throws Exception
  {
    // First read the properties file
    try
    {
      rb = ResourceBundle.getBundle(propFilePassed);
    } catch (MissingResourceException mre)
    {
      err("The file " + propFilePassed + ".properties must be in the classpath.");
    }
    destroyAllContexts();
    try
    {
      // Get the named properties required for a connection to be created:
      String host = (rb.containsKey("host")) ? rb.getString("host") : "localhost";
      info("host: " + host);
      int port = Integer.parseInt(rb.getString("port"));
      info("port: " + port);
      String uid = rb.getString("userId");
      info("userId: " + uid);
      String pwd = rb.getString("password");
      int maxCtx = Integer.parseInt((rb.containsKey("maxCtx")) ? rb.getString("maxCtx") : "1");
      // We do not want a lower pool size then 1.
      maxCtx = (maxCtx < 1) ? 1 : maxCtx;
      int timeout = Integer.parseInt((rb.containsKey("timeout")) ? rb.getString("timeout") : "10000");
      info("timeout: " + timeout);
      // To create a Context in an SSL-enabled environment we need the following
      String useSSLstr = (rb.containsKey("useSSL")) ? rb.getString("useSSL") : "false";
      boolean useSSL = Boolean.parseBoolean(useSSLstr);
      Properties props = null;
      props = new Properties();
      if (useSSL)
      {
        info("SSL is used.");
        props.setProperty(MpiNetSecure.SECLIB, rb.getString("ssl.seclib"));
        props.setProperty(MpiNetSecure.SSLVERSION, rb.getString("ssl.version"));
      } else
      {
        info("SSL is not used.");
      }
      
      // See the product documentation on how to enable/use
      // MpiNet over HTTP communication.
      String useHTTPstr = (rb.containsKey("useHTTP")) ? rb.getString("useHTTP") : "false";
      info("useHTTPstr: " + useHTTPstr);
      if(useHTTPstr.trim().equalsIgnoreCase("true") || useHTTPstr.trim().equalsIgnoreCase("1") || useHTTPstr.trim().equalsIgnoreCase("t"))
      {
        info("MpiNet over HTTP is used.");
        props.setProperty(MpiNetConstants.MPINET_CODEC_KEY, MpiNetConstants.MPINET_CODEC_HTTP);
      } else
      {
        info("MpiNet over HTTP is not used.");
      }
      
      for(int mnemonic = 1; mnemonic <= maxCtx; mnemonic ++)
      {
        // Create a UsrHead object
        usrHead = new UsrHead(uid, pwd);
        // The context is created in the code line below.
        ExContextWrapper tempCtx = new ExContextWrapper(usrHead, host, port, timeout, mnemonic, props);
        if(!tempCtx.isConnected())
          err("Context # " + mnemonic + " failed to connect, error code is: " + tempCtx.getErrCode() + 
              " error message is: " +  tempCtx.getErrMsg() + 
              " Master Data Engine might be down, or one of the properties in the " +
              propFilePassed + ".properties file is set incorrectly.");
        activeContexts.add(tempCtx);
      }
      info("Context Pool based on " + propFilePassed + ".properties has been created with \n" + 
          activeContexts.size() + " Context objects.");
    } catch (MissingResourceException mre)
    {
      err("The " + propFilePassed + ".properties file must contain the following properties: host, port, userId, password, and maxCtx, timeout, and useSSL.");
    } catch(NumberFormatException nfe)
    {
      err("The " + propFilePassed + ".properties file must contain a number as a value assigned to port, timeout, and maxCtx.");
    }
  }
  
  /**
   * This method will cause all of the ExContextWrapper objects in the pool to
   * be disconnected and destroyed. A possible use for this method is in
   * jspDestroy() method or equivalent in your runtime. Do not call this method
   * if there is a possibility of one of the ExContextWrappers still being in
   * use.
   */
  protected synchronized void destroyAllContexts()
  {
    if (activeContexts.size() > 0)
    {
      info("Destroying all " + activeContexts.size() + " of the Contexts in the pool based on the " + 
          propFilePassed + ".properties");
      for (ExContextWrapper ewc : activeContexts)
      {
        ewc.disconnect();
      }
      activeContexts.clear();
    }
  }
  
  /**
   * Returns a UsrHead object with values populated from the .properties file.
   * 
   * @return UsrHead
   */
  protected UsrHead getUsrHead()
  {
    return usrHead;
  }
  
  /**
   * Prints the message to the System.err and throws an Exception populated with
   * the message.
   * 
   * @param msg
   *          The error message to be printed and turned into Exception.
   * @throws Exception
   *           created around the message.
   */
  private static void err(String msg) throws Exception
  {
    System.err.println(msg);
    throw new Exception(msg);
  }
  
  /**
   * Prints a message to the System.out
   * 
   * @param msg
   *          the message to be printed.
   */
  private static void info(String msg)
  {
    System.out.println(msg);
  }
  
  public void finalize()
  {
    destroyAllContexts();
  }
}
