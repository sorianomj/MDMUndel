package com.ihis.undelete;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/*  Copyright (c) 2008 by Initiate Systems, Inc. (INITIATE)             */
/*                         All Rights Reserved.                         */
/*         THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF INITIATE.     */
/*         The copyright notice above does not evidence any             */
/*         actual or intended publication of such source code.          */
/*----------------------------------------------------------------------*/

import madison.mpi.IxnMemUndelete;
import madison.mpi.KeyType;
import madison.mpi.MemHead;
import madison.mpi.MemRowList;

/**
 * This examples shows how IxnMemUndelete interaction object can be used to
 * undelete member. This interaction returns a logically deleted member to
 * active status. Derived data is recreated so that the member can be the result
 * of a search, match, and take further updates. The memHead recStat value is
 * returned to an ACTIVE status. Attributes have the same recStat value as when
 * the member was deleted.
 * 
 * In order to test exMemUndelete successfully, first execute ExMemDelete
 * example (this will delete the member which this example will undelete).
 */
public class ExMemUndelete extends BaseExample
{
  private static final String intrName = "IxnMemUndelete";

  public static void main(String[] args) throws Exception
  {
    // This interaction returns a logically deleted member to
    // active status
    // Create a member undelete interaction object.
    IxnMemUndelete memUndelete = new IxnMemUndelete(getContext());

    // Create a member rowlist to hold input member row(s).
    
    
    try {
    	File f = new File("/home/mdm/UnDeleteAPI/input/undelteInput.txt");
    	BufferedReader b = new BufferedReader(new FileReader(f));
    	String readLine = "";
    	System.out.println("Reading file");
    	while ((readLine = b.readLine()) != null) 
		{
    		MemRowList inpMemRows = new MemRowList();
    		String record[]  = readLine.split("\\|");
    	    // MemHead models the Initiate database table mpi_memhead.
    	    MemHead memHead = new MemHead();

    	    // Set the identifiers of the member to be undeleted.
    	    memHead.setSrcCode(record[0]);
    	    memHead.setMemIdnum(record[1]);
    	    // We could use MemRecno in place of SrcCode/MemIdnum:
    	    // memHead.setMemRecno(162L);
    	    inpMemRows.addRow(memHead);

//    	    waitForQueues(memHead.getSrcCode(), memHead.getMemIdnum());
		    boolean status = memUndelete.execute(inpMemRows, KeyType.MEMIDNUM);
		    if (status)
	        info("The " + intrName + " interaction worked.  Member has been undeleted.");
	      else
	      {
	        // Disconnect from Master Data Engine server
	        disconnect();
	        ixnError("The " + intrName + " interaction failed.", memUndelete.getErrCode().toString(), memUndelete.getErrText());
	      }

    	}
	   	    // Execute the member undelete interaction.
		    // If MemRecno was used as member identifier we must use KeyType.MEMRECNO:
		    // boolean status = memUndelete.execute(inpMemRows, KeyType.MEMRECNO);
    	b.close();
    } catch (IOException e) {
		disconnect();
    	e.printStackTrace();
    }
    // Disconnect from Master Data Engine server
    disconnect();
  }
}