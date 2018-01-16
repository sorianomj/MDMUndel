package com.ihis.undelete;


/*----------------------------------------------------------------------*/
/*  Copyright (c) 2008 by Initiate Systems, Inc. (INITIATE)             */
/*                         All Rights Reserved.                         */
/*         THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF INITIATE.     */
/*         The copyright notice above does not evidence any             */
/*         actual or intended publication of such source code.          */
/*----------------------------------------------------------------------*/


import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import madison.mpi.Bean;
import madison.mpi.Context;
import madison.mpi.DicStore;
import madison.mpi.MemHead;
import madison.mpi.Row;
import madison.mpi.RowIterator;
import madison.mpi.RowList;
import madison.mpi.UsrHead;

/**
 * This is a helper class for other examples. It holds some common members and
 * methods.
 */
public abstract class BaseExample
{
  private static ExContextFactory ecf = null;
  private static ExContextWrapper ecw = null;
  private static DicStore dicStore = null;
  private static UsrHead usrHead = null;
  
  /**
   * Creates a Context, UserHead, and a DicStore
   * @return Context
   * @throws Exception if the Context or a DicStore could not be created.
   */
  protected static Context getContext() throws Exception
  {
    if(ecf == null)
    {
      ecf = ExContextFactory.getInstance(null);
      ecw = ecf.getWrappedContext();
      usrHead = ecf.getUsrHead();
      try
      {
        dicStore = new DicStore(ecw);
      } catch (IOException ioe)
      {
        err("DicStore could not be created.  " +
            "Possibly due to the userId and/or password not being valid. Underlying IOException: "
            + ioe.toString());
      }
    }
    return ecw;
  }
  
  /**
   * Returns a DicStore. If the call to getContext() has not been made yet, this
   * call is made to ensure that we have a valid Context
   * 
   * @return DicStore
   * @throws Exception if there was a problem with Context or DicStore creation.
   */
  protected static DicStore getDicStore() throws Exception
  {
    if(dicStore == null)
      getContext();
    return dicStore;
  }
  
  /**
   * Returns a UsrHead object with the user and password set. If the Context has
   * not yet been created, it is created and then UsrHead is returned.
   * 
   * @return UsrHead
   * @throws Exception
   *           if Context or DicSstore could not be created.
   */
  protected static UsrHead getUsrHead() throws Exception
  {
    if(usrHead == null)
      getContext();
    return usrHead;
  }
  
  /**
   * Iterate through all the rows in a RowList, using the standard
   * output (toString) for each row
   *
   * @param rowList RowList object
   */
  protected static void dumpRows(RowList rowList, String msg)
  {
    msg = (msg == null) ? "***** " : "***** " + msg;
    Row row = null;
    info("\n" + msg);
    info("** Begin Row Dump:");
    for(RowIterator memIter = rowList.rows(); memIter.hasMoreRows();)
    {
      row = (Row) memIter.nextRow();
      info(row.toString());
    }
    info("** End Row Dump.\n");
  }
  
  /**
   * Iterate through all the row beans in a List, using the standard
   * output (toString) for each row
   *
   * @param rowList Bean object
   */
  protected static void dumpRows(List<? extends Bean> rowList, String msg)
  {
    msg = (msg == null) ? "***** " : "***** " + msg;
    Bean row = null;
    info("\n" + msg);
    info("** Begin Row Dump:");
    for(Iterator<? extends Bean> iter = rowList.iterator(); iter.hasNext();)
    {
      row = (Bean) iter.next();
      info(row.toString());
    }
    info("** End Row Dump.\n");
  }

  protected static void disconnect()
  {
    if(ecw != null)
      ecf.freeContext(ecw);
    // Since we are executing the examples one-at-a-time there is no
    // reason to keep the Context Pool around:
    ecf.destroyAllContexts();
  }
  
  /**
   * This helper method constructs a MemHead object based on the String
   * parameters passed to it.<BR>
   * If two strings are passed then params[0] is assumed to be SrcCode and
   * params[1] is assumed to be MemIdnum.<BR>
   * If one string is passed then params[0] is assumed to be the MemRecno.
   * @param params either SrcCode, MemIdnum or MemRecno
   * @return MemHead (null if params are incorrectly passed).
   */
  protected static MemHead makeMemHead(String ... params)
  {
    MemHead memHead = null;
    if(params != null && params.length >=1 && params.length <= 2)
    {
      memHead = new MemHead();
      if(params.length == 1)
        memHead.setMemRecno(Long.parseLong(params[0]));
      else
      {
        memHead.setSrcCode(params[0]);
        memHead.setMemIdnum(params[1]);
      }
    }
    return memHead;
  }
  
  protected static void ixnError(String msg, String errorCode, String errorMessage) throws Exception
  {
    StringBuffer errMsg = new StringBuffer();
    errMsg.append("***** ").append(msg);
    errMsg.append("\n ERROR: " + errorCode);
    errMsg.append("\n errText = " + errorMessage);
    err(errMsg.toString());
  }
  
  protected static void err(String msg) throws Exception
  {
    System.err.println(msg);
    throw new Exception(msg);
  }

  protected static void info(String msg)
  {
    System.out.println(msg);
  }
  
  /**
   * This is a convenience method to allow the Entity Management to finish it's
   * work. This method is implemented because some of the examples rely on work
   * to be done by the Entity Mangers, which takes a few seconds (time varies
   * based on hardware and EM configuration). During the internal unit testing
   * within the Initiate Systems unit test framework this method is overwritten
   * with proprietary checks.
   * 
   * @param srcCode
   * @param memIdnum
   * @throws Exception
   */
  protected static void waitForQueues(String srcCode, String memIdnum) throws Exception
  {
    int pause = 11000;
    info("Sleeping for " + pause / 1000 + " seconds to allow EM to complete the work related to the member " + srcCode + ":" + memIdnum);
    Thread.sleep(pause);
  }
}
