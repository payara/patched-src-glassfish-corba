/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2007 Sun Microsystems, Inc. All rights reserved.
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


package com.sun.corba.se.impl.orbutil.copyobject;

import java.util.Map ;

import java.security.AccessController ;
import java.security.PrivilegedAction ;
import java.security.Permission ;

import com.sun.corba.se.spi.orbutil.copyobject.ReflectiveCopyException ;

import sun.corba.Bridge ;
import sun.corba.BridgePermission ;

/** Base class for generated class copiers.  Note that this class
 * makes use of the unsafe copier through the Bridge class.  
 * Because of this, BCELCopierBase could potentially be used
 * to bypass security restrictions.  Consequently, this class must be 
 * referenced inside a doPrivileged block, and a derived class 
 * must have the permissions needed for the
 * Bridge class.
 */
public abstract class BCELCopierBase 
{
    private static final Bridge bridge = Bridge.get() ;
    private static final Permission getBridgePermission = 
	new BridgePermission( "getBridge" ) ;
    
    private PipelineClassCopierFactory factory ;

    public BCELCopierBase( PipelineClassCopierFactory factory )
    {
	SecurityManager sman = System.getSecurityManager() ;
	if (sman != null)
	    sman.checkPermission( getBridgePermission ) ;

	this.factory = factory ;
    }

    final protected void copyObject( Map oldToNew, 
	long offset, Object src, Object dest ) 
	throws ReflectiveCopyException
    {
	Object obj = bridge.getObject( src, offset ) ;

	Object result = null ;

	if (obj != null) {
	    // This lookup must be based on the actual type, not the
	    // declared type to allow for polymorphism.
	    ClassCopier copier = factory.getClassCopier( obj.getClass()) ;
	    result = copier.copy( oldToNew, obj ) ;
	}

	bridge.putObject( dest, offset, result ) ;
    }

    final protected void copyByte( long offset, Object src, Object dest )
    {
	bridge.putByte( dest, offset, bridge.getByte( src, offset )) ;
    }

    final protected void copyChar( long offset, Object src, Object dest )
    {
	bridge.putChar( dest, offset, bridge.getChar( src, offset )) ;
    }

    final protected void copyShort( long offset, Object src, Object dest )
    {
	bridge.putShort( dest, offset, bridge.getShort( src, offset )) ;
    }

    final protected void copyInt( long offset, Object src, Object dest )
    {
	bridge.putInt( dest, offset, bridge.getInt( src, offset )) ;
    }

    final protected void copyLong( long offset, Object src, Object dest )
    {
	bridge.putLong( dest, offset, bridge.getLong( src, offset )) ;
    }

    final protected void copyFloat( long offset, Object src, Object dest )
    {
	bridge.putFloat( dest, offset, bridge.getFloat( src, offset )) ;
    }

    final protected void copyDouble( long offset, Object src, Object dest )
    {
	bridge.putDouble( dest, offset, bridge.getDouble( src, offset )) ;
    }

    final protected void copyBoolean( long offset, Object src, Object dest )
    {
	bridge.putBoolean( dest, offset, bridge.getBoolean( src, offset )) ;
    }
} ;

