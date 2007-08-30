/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1993-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
/* 
 * @(#)ORBD.java	1.27 00/03/02
 *
 * Copyright 1993-1997 Sun Microsystems, Inc. 901 San Antonio Road,
 * Palo Alto, California, 94303, U.S.A.  All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Sun
 * Microsystems, Inc. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Sun.
 *
 * CopyrightVersion 1.2
 *
 */

package com.sun.corba.se.impl.activation;

import java.io.File;
import java.util.Properties;

import org.omg.CORBA.INITIALIZE;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.CompletionStatus;
import org.omg.CosNaming.NamingContext;
import org.omg.PortableServer.POA;

import com.sun.corba.se.pept.transport.Acceptor;

import com.sun.corba.se.spi.activation.Repository;
import com.sun.corba.se.spi.activation.RepositoryPackage.ServerDef;
import com.sun.corba.se.spi.activation.Locator;
import com.sun.corba.se.spi.activation.LocatorHelper;
import com.sun.corba.se.spi.activation.Activator;
import com.sun.corba.se.spi.activation.ActivatorHelper;
import com.sun.corba.se.spi.activation.ServerAlreadyRegistered;
import com.sun.corba.se.spi.legacy.connection.LegacyServerSocketEndPointInfo;
import com.sun.corba.se.spi.transport.SocketInfo;
import com.sun.corba.se.spi.orb.ORB;

import com.sun.corba.se.impl.legacy.connection.SocketFactoryAcceptorImpl;
import com.sun.corba.se.impl.naming.cosnaming.TransientNameService;
import com.sun.corba.se.impl.naming.pcosnaming.NameService;
import com.sun.corba.se.impl.orbutil.ORBConstants;
import com.sun.corba.se.impl.orbutil.CorbaResourceUtil;
import com.sun.corba.se.impl.transport.SocketOrChannelAcceptorImpl;

/**
 * 
 * @version 	1.10, 97/12/06
 * @author	Rohit Garg
 * @since	JDK1.2
 */
public class ORBD
{
    private int initSvcPort;

    protected void initializeBootNaming(ORB orb)
    {
    	// create a bootstrap server
	initSvcPort = orb.getORBData().getORBInitialPort();

	Acceptor acceptor;
	// REVISIT: see ORBConfigurator. use factory in TransportDefault.
	if (orb.getORBData().getLegacySocketFactory() == null) {
	    acceptor = 
		new SocketOrChannelAcceptorImpl(
		    orb,
		    initSvcPort,
		    LegacyServerSocketEndPointInfo.BOOT_NAMING,
		    SocketInfo.IIOP_CLEAR_TEXT);
	} else {
	    acceptor = 
		new SocketFactoryAcceptorImpl(
		    orb,
		    initSvcPort,
		    LegacyServerSocketEndPointInfo.BOOT_NAMING,
		    SocketInfo.IIOP_CLEAR_TEXT);
	}
	orb.getCorbaTransportManager().registerAcceptor(acceptor);
    }

    protected ORB createORB(String[] args)
    {
	Properties props = System.getProperties();

	// For debugging.
	//props.put( ORBConstants.DEBUG_PROPERTY, "naming" ) ;
	//props.put( ORBConstants.DEBUG_PROPERTY, "transport,giop,naming" ) ;

	props.put( ORBConstants.ORB_SERVER_ID_PROPERTY, "1000" ) ;
	props.put( ORBConstants.PERSISTENT_SERVER_PORT_PROPERTY, 
	    props.getProperty( ORBConstants.ORBD_PORT_PROPERTY,
		Integer.toString( 
		    ORBConstants.DEFAULT_ACTIVATION_PORT ) ) ) ;

	// See Bug 4396928 for more information about why we are initializing
	// the ORBClass to PIORB (now ORBImpl, but should check the bugid).
	props.put("org.omg.CORBA.ORBClass", 
	    "com.sun.corba.se.impl.orb.ORBImpl");

	return (ORB) ORB.init(args, props);
    }

    private void run(String[] args) 
    {
	try {
	    // parse the args and try setting the values for these
	    // properties
	    processArgs(args);

	    ORB orb = createORB(args);

	    if (orb.orbdDebugFlag) 
		System.out.println( "ORBD begins initialization." ) ;

	    boolean firstRun = createSystemDirs( ORBConstants.DEFAULT_DB_DIR );

	    startActivationObjects(orb);

	    if (firstRun) // orbd is being run the first time
		installOrbServers(getRepository(), getActivator());

	    if (orb.orbdDebugFlag) {
		System.out.println( "ORBD is ready." ) ;
	        System.out.println("ORBD serverid: " +
	                System.getProperty(ORBConstants.ORB_SERVER_ID_PROPERTY));
	        System.out.println("activation dbdir: " +
	                System.getProperty(ORBConstants.DB_DIR_PROPERTY));
	        System.out.println("activation port: " +
	                System.getProperty(ORBConstants.ORBD_PORT_PROPERTY));

                String pollingTime = System.getProperty(
                    ORBConstants.SERVER_POLLING_TIME);
                if( pollingTime == null ) {
                    pollingTime = Integer.toString( 
                        ORBConstants.DEFAULT_SERVER_POLLING_TIME );
                }
                System.out.println("activation Server Polling Time: " +
                        pollingTime + " milli-seconds ");

                String startupDelay = System.getProperty(
                    ORBConstants.SERVER_STARTUP_DELAY);
                if( startupDelay == null ) {
                    startupDelay = Integer.toString( 
                        ORBConstants.DEFAULT_SERVER_STARTUP_DELAY );
                }
	        System.out.println("activation Server Startup Delay: " +
                        startupDelay + " milli-seconds " );
	    }

	    // The following two lines start the Persistent NameService
            NameServiceStartThread theThread =
                new NameServiceStartThread( orb, dbDir );
            theThread.start( );

	    orb.run();
	} catch( org.omg.CORBA.COMM_FAILURE cex ) {
            System.out.println( CorbaResourceUtil.getText("orbd.commfailure"));
	    System.out.println( cex );
	    cex.printStackTrace();
        } catch( org.omg.CORBA.INTERNAL iex ) {
            System.out.println( CorbaResourceUtil.getText(
                "orbd.internalexception"));
	    System.out.println( iex );
	    iex.printStackTrace();
        } catch (Exception ex) {
	    System.out.println(CorbaResourceUtil.getText(
                "orbd.usage", "orbd"));
	    System.out.println( ex );
	    ex.printStackTrace();
	}
    }

    private void processArgs(String[] args)
    {
	Properties props = System.getProperties();
	for (int i=0; i < args.length; i++) {
	    if (args[i].equals("-port")) {
	        if ((i+1) < args.length) {
	            props.put(ORBConstants.ORBD_PORT_PROPERTY, args[++i]);
	        } else {
	            System.out.println(CorbaResourceUtil.getText(
			"orbd.usage", "orbd"));
	        }
	    } else if (args[i].equals("-defaultdb")) {
	        if ((i+1) < args.length) {
	            props.put(ORBConstants.DB_DIR_PROPERTY, args[++i]);
	        } else {
	            System.out.println(CorbaResourceUtil.getText(
			"orbd.usage", "orbd"));
	        }
	    } else if (args[i].equals("-serverid")) {
	        if ((i+1) < args.length) {
	            props.put(ORBConstants.ORB_SERVER_ID_PROPERTY, args[++i]);
	        } else {
	            System.out.println(CorbaResourceUtil.getText(
			"orbd.usage", "orbd"));
	        }
	    } else if (args[i].equals("-serverPollingTime")) {
	        if ((i+1) < args.length) {
	            props.put(ORBConstants.SERVER_POLLING_TIME, args[++i]);
	        } else {
	            System.out.println(CorbaResourceUtil.getText(
			"orbd.usage", "orbd"));
	        }
	    } else if (args[i].equals("-serverStartupDelay")) {
	        if ((i+1) < args.length) {
	            props.put(ORBConstants.SERVER_STARTUP_DELAY, args[++i]);
	        } else {
	            System.out.println(CorbaResourceUtil.getText(
			"orbd.usage", "orbd"));
	        }
            }
	}
    }

    /**
     * Ensure that the Db directory exists. If not, create the Db
     * and the log directory and return true. Otherwise return false.
     */
    protected boolean createSystemDirs(String defaultDbDir)
    {
	boolean dirCreated = false;
	Properties props = System.getProperties();
	String fileSep = props.getProperty("file.separator");

	// determine the ORB db directory
	dbDir = new File (props.getProperty( ORBConstants.DB_DIR_PROPERTY,
	    props.getProperty("user.dir") + fileSep + defaultDbDir));

	// create the db and the logs directories
        dbDirName = dbDir.getAbsolutePath();
	props.put(ORBConstants.DB_DIR_PROPERTY, dbDirName);
	if (!dbDir.exists()) {
	    dbDir.mkdir();
	    dirCreated = true;
	}

	File logDir = new File (dbDir, ORBConstants.SERVER_LOG_DIR ) ;
	if (!logDir.exists()) logDir.mkdir();

	return dirCreated;
    }

    protected File dbDir;
    protected File getDbDir()
    {
	return dbDir;
    }

    private String dbDirName;
    protected String getDbDirName()
    {
	return dbDirName;
    }

    protected void startActivationObjects(ORB orb) throws Exception
    {
	// create Initial Name Service object
	initializeBootNaming(orb);

	// create Repository object
	repository = new RepositoryImpl(orb, dbDir, orb.orbdDebugFlag );
	orb.register_initial_reference( ORBConstants.SERVER_REPOSITORY_NAME, repository );

	// create Locator and Activator objects
	ServerManagerImpl serverMgr =
	    new ServerManagerImpl( orb, 
				   orb.getCorbaTransportManager(),
				   repository, 
				   getDbDirName(), 
				   orb.orbdDebugFlag );

	locator = LocatorHelper.narrow(serverMgr);
	orb.register_initial_reference( ORBConstants.SERVER_LOCATOR_NAME, locator );

	activator = ActivatorHelper.narrow(serverMgr);
	orb.register_initial_reference( ORBConstants.SERVER_ACTIVATOR_NAME, activator );

        // start Name Service
        new TransientNameService(orb, ORBConstants.TRANSIENT_NAME_SERVICE_NAME);
    }

    protected Locator locator;
    protected Locator getLocator()
    {
	return locator;
    }

    protected Activator activator;
    protected Activator getActivator()
    {
	return activator;
    }

    protected RepositoryImpl repository;
    protected RepositoryImpl getRepository()
    {
	return repository;
    }

    /** 
     * Go through the list of ORB Servers and initialize and start
     * them up.
     */
    protected void installOrbServers(RepositoryImpl repository, 
				     Activator activator)
    {
	int serverId;
	String[] server;
	ServerDef serverDef;

	for (int i=0; i < orbServers.length; i++) {
	    try {
		server = orbServers[i];
		serverDef = new ServerDef(server[1], server[2], 
					  server[3], server[4], server[5] );

		serverId = Integer.valueOf(orbServers[i][0]).intValue();

		repository.registerServer(serverDef, serverId);

		activator.activate(serverId);

	    } catch (Exception ex) {}
	}
    }

    public static void main(String[] args) {
	ORBD orbd = new ORBD();
	orbd.run(args);
    }

    /**
     * List of servers to be auto registered and started by the ORBd.
     * 
     * Each server entry is of the form {id, name, path, args, vmargs}.
     */
    private static String[][] orbServers = {
	{""}
    };
}
