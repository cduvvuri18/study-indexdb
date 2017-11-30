package com.cduvvuri.sidb.logger;

import java.util.logging.Logger;

/**
 * 
 * @author Chaitanya DS
 * 08-Nov-2017
 */
public class ILogger {
	public final static Logger LOG = Logger.getLogger("com.sidb");
	
	public static void info(String mess) {
		//LOG.log(Level.INFO, mess);
		System.out.println(mess);
	}
}
