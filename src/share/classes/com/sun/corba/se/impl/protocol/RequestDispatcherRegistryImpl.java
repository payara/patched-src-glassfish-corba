/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2002-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.impl.protocol;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import com.sun.corba.se.pept.protocol.ClientRequestDispatcher ;

import com.sun.corba.se.spi.protocol.LocalClientRequestDispatcher ;
import com.sun.corba.se.spi.protocol.LocalClientRequestDispatcherFactory ;
import com.sun.corba.se.spi.protocol.CorbaServerRequestDispatcher ;
import com.sun.corba.se.spi.protocol.RequestDispatcherRegistry ;

import com.sun.corba.se.spi.oa.ObjectAdapterFactory ;

import com.sun.corba.se.spi.orb.ORB ;

import com.sun.corba.se.impl.orbutil.ORBConstants ;
import com.sun.corba.se.spi.orbutil.misc.DenseIntMapImpl ;

/**
 * This is a registry of all subcontract ID dependent objects.  This includes:
 * LocalClientRequestDispatcherFactory, ClientRequestDispatcher, ServerSubcontract, and 
 * ObjectAdapterFactory. 
 */
public class RequestDispatcherRegistryImpl implements RequestDispatcherRegistry {
    private ORB orb;

    protected int defaultId;			// The default subcontract ID to use if 
						// there is no more specific ID available.
						// This happens when invoking a foreign IOR.
    private DenseIntMapImpl SDRegistry ;	// ServerRequestDispatcher registry
    private DenseIntMapImpl CSRegistry ;	// ClientRequestDispatcher registry
    private DenseIntMapImpl OAFRegistry ;	// ObjectAdapterFactory registry
    private DenseIntMapImpl LCSFRegistry ;	// LocalClientRequestDispatcherFactory registry
    private Set objectAdapterFactories ;	// Set of all ObjectAdapterFactory instances
    private Set objectAdapterFactoriesView ;	// Read-only view of oaf instances
    private Map stringToServerSubcontract ;	// Map from obect key string to 
						// ServerSubcontract
						// for special bootstrap IORs

    public RequestDispatcherRegistryImpl(ORB orb, int defaultId ) 
    {
	this.orb = orb;
        this.defaultId = defaultId;
        SDRegistry = new DenseIntMapImpl() ;
        CSRegistry = new DenseIntMapImpl() ;
	OAFRegistry = new DenseIntMapImpl() ;
	LCSFRegistry = new DenseIntMapImpl() ;
	objectAdapterFactories = new HashSet() ;
	objectAdapterFactoriesView = Collections.unmodifiableSet( objectAdapterFactories ) ;
	stringToServerSubcontract = new HashMap() ;
    }

    public synchronized void registerClientRequestDispatcher( 
	ClientRequestDispatcher csc, int scid)
    {
	CSRegistry.set( scid, csc ) ;
    }

    public synchronized void registerLocalClientRequestDispatcherFactory( 
	LocalClientRequestDispatcherFactory csc, int scid)
    {
	LCSFRegistry.set( scid, csc ) ;
    }

    public synchronized void registerServerRequestDispatcher( 
	CorbaServerRequestDispatcher ssc, int scid)
    {
	SDRegistry.set( scid, ssc ) ;
    }

    public synchronized void registerServerRequestDispatcher(
	CorbaServerRequestDispatcher scc, String name )
    {
	stringToServerSubcontract.put( name, scc ) ;
    }

    public synchronized void registerObjectAdapterFactory( 
	ObjectAdapterFactory oaf, int scid)
    {
	objectAdapterFactories.add( oaf ) ;
	OAFRegistry.set( scid, oaf ) ;
    }

    // **************************************************
    // Methods to find the subcontract side subcontract
    // **************************************************

    // Note that both forms of getServerRequestDispatcher need to return
    // the default server delegate if no other match is found.
    // This is essential to proper handling of errors for 
    // malformed requests.  In particular, a bad MAGIC will
    // result in a lookup in the named key table (stringToServerSubcontract),
    // which must return a valid ServerRequestDispatcher.  A bad subcontract ID
    // will similarly need to return the default ServerRequestDispatcher.
    
    public CorbaServerRequestDispatcher getServerRequestDispatcher(int scid)
    {
	CorbaServerRequestDispatcher sdel =
	    (CorbaServerRequestDispatcher)(SDRegistry.get(scid)) ;
	if ( sdel == null )
            sdel = (CorbaServerRequestDispatcher)(SDRegistry.get(defaultId)) ;

	return sdel;
    }

    public CorbaServerRequestDispatcher getServerRequestDispatcher( String name ) 
    {
	CorbaServerRequestDispatcher sdel =
	    (CorbaServerRequestDispatcher)stringToServerSubcontract.get( name ) ;

	if ( sdel == null )
            sdel = (CorbaServerRequestDispatcher)(SDRegistry.get(defaultId)) ;

	return sdel;
    }

    public LocalClientRequestDispatcherFactory getLocalClientRequestDispatcherFactory( 
	int scid )
    {
	LocalClientRequestDispatcherFactory factory = 
	    (LocalClientRequestDispatcherFactory)(LCSFRegistry.get(scid)) ;
	if (factory == null) {
	    factory = (LocalClientRequestDispatcherFactory)(LCSFRegistry.get(defaultId)) ;
	}

	return factory ;
    }

    public ClientRequestDispatcher getClientRequestDispatcher( int scid )
    {
	ClientRequestDispatcher subcontract = 
	    (ClientRequestDispatcher)(CSRegistry.get(scid)) ;
	if (subcontract == null) {
	    subcontract = (ClientRequestDispatcher)(CSRegistry.get(defaultId)) ;
	}

	return subcontract ;
    }

    public ObjectAdapterFactory getObjectAdapterFactory( int scid )
    {
	ObjectAdapterFactory oaf =
            (ObjectAdapterFactory)(OAFRegistry.get(scid)) ;
	if ( oaf == null )
            oaf = (ObjectAdapterFactory)(OAFRegistry.get(defaultId)) ;

	return oaf;
    }

    public Set getObjectAdapterFactories() 
    {
	return objectAdapterFactoriesView ;
    }
}
