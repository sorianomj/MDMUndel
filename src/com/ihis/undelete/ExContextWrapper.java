package com.ihis.undelete;

/*----------------------------------------------------------------------*/
/*  Copyright (c) 2008 by Initiate Systems, Inc. (INITIATE)             */
/*                         All Rights Reserved.                         */
/*         THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF INITIATE.     */
/*         The copyright notice above does not evidence any             */
/*         actual or intended publication of such source code.          */
/*----------------------------------------------------------------------*/
import java.util.Properties;

import madison.mpi.Context;
import madison.mpi.UsrHead;

/**
 * Simple wrapper class around madison.mpi.Context Has a boolean flag to
 * indicate a check-out status as well as an int mnemonic for comparison.
 */
public class ExContextWrapper extends Context
{
  private boolean isCheckedOut_ = false;
  private int mnemonic_ = 0;
  
  ExContextWrapper(UsrHead usrHead, String host, int port, int timeout, int mnemonic, Properties props)
  {
    // There are other constructors for the Context class:
    // new Context(host, port);
    // new Context(host, port, uid, pwd);
    // consult the API documentation for more details and options.
    super(host, port, usrHead, props);
    mnemonic_ = mnemonic;
  }
  
  /**
   * Returns the checked-out status.
   * 
   * @return true if the context is checked-out, false otherwise
   */
  protected boolean isCheckedOut()
  {
    return isCheckedOut_;
  }
  
  /**
   * Sets the checked-out flag to true. 
   */
  protected void checkOut()
  {
    isCheckedOut_ = true;
  }
  
  /**
   * Sets the checked-out flag to false. 
   */
  protected void checkIn()
  {
    isCheckedOut_ = false;
  }
  
  /**
   * Returns the value of the private class variable mnemonic_
   * 
   * @return int which is this instance's mnemonic.
   */
  protected int getMnemonic()
  {
    return mnemonic_;
  }
  
  /**
   * This method compares this ExContextWrapper to another ExContextWrapper
   * based on the mnemonic_ private class variable value.
   * 
   * @param exContextWrapper
   *          another ExContextWrapper to compare against.
   * @return true if the mnemonic_ values are equal, false otherwise.
   */
  protected boolean equals(ExContextWrapper exContextWrapper)
  {
    if(exContextWrapper != null && mnemonic_ == exContextWrapper.getMnemonic())
      return true;
    return false;
  }
}
