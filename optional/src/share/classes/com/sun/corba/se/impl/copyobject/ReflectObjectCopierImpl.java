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

package com.sun.corba.se.impl.copyobject ;

import java.util.IdentityHashMap ;
import java.util.Map ;
import java.util.Iterator ;
import java.util.HashMap ;
import java.util.TreeMap ;
import java.util.Hashtable ;

import java.rmi.Remote ;

import org.omg.CORBA.portable.ObjectImpl ;
import org.omg.CORBA.portable.Delegate ;

import com.sun.corba.se.spi.orbutil.copyobject.ObjectCopier ;
import com.sun.corba.se.spi.orbutil.copyobject.ReflectiveCopyException ;

import com.sun.corba.se.spi.orb.ORB ;

import com.sun.corba.se.spi.logging.LogWrapperBase ;

import com.sun.corba.se.spi.orbutil.misc.ObjectUtility ;

import com.sun.corba.se.impl.orbutil.copyobject.DefaultClassCopiers ;
import com.sun.corba.se.impl.orbutil.copyobject.DefaultClassCopierFactories ;
import com.sun.corba.se.impl.orbutil.copyobject.FastCache ;
import com.sun.corba.se.impl.orbutil.copyobject.ClassCopierBase ;
import com.sun.corba.se.impl.orbutil.copyobject.ClassCopier ;
import com.sun.corba.se.impl.orbutil.copyobject.ClassCopierFactory ;
import com.sun.corba.se.impl.orbutil.copyobject.PipelineClassCopierFactory ;

// XXX Not good to be importing this, but seems to be necessary.
import com.sun.corba.se.impl.util.Utility ;

/** Class used to deep copy arbitrary data.  A single 
 * ReflectObjectCopierImpl
 * instance will preserve all object aliasing across multiple calls
 * to copy.
 */
public class ReflectObjectCopierImpl implements ObjectCopier {    
    // Note that this class is the only part of the copyObject
    // framework that is dependent on the ORB.  This is in
    // fact specialized just for CORBA objrefs and RMI-IIOP stubs.

    // This thread local holds an ORB that is used when
    // a Remote needs to be copied, because the autoConnect
    // call requires an ORB.  We do not want to pass an ORB
    // everywhere because that would make the ClassCopier instances
    // ORB dependent, which would prevent them from being 
    // statically scoped.  Note that this is package private so that
    // ObjectCopier can access this data member.
    static final ThreadLocal localORB = new ThreadLocal() ;

    // Special ClassCopier instances needed for CORBA
    
    // For java.rmi.Remote, we need to call autoConnect, 
    // which requires an orb.
    private static ClassCopier remoteClassCopier =
	new ClassCopierBase( "remote" ) {
	    public Object createCopy( Object source, boolean debug ) 
	    {
		ORB orb = (ORB)localORB.get() ;
		return Utility.autoConnect( source, orb, true ) ;
	    } 
	} ;

    // For ObjectImpl, we just make a shallow copy, since the Delegate
    // is mostly immutable.
    private static ClassCopier corbaClassCopier = 
	new ClassCopierBase( "corba" ) {
	    public Object createCopy( Object source, boolean debug ) 
	    {
		ObjectImpl oi = (ObjectImpl)source ;
		Delegate del = oi._get_delegate() ;

		try {
		    // Create a new object of the same type as source
		    ObjectImpl result = (ObjectImpl)source.getClass().newInstance() ;
		    result._set_delegate( del ) ;

		    return result ;
		} catch (Exception exc) {
		    // XXX log this
		    throw new RuntimeException(exc) ;
		}
	    }
	} ;

    private static ClassCopierFactory specialClassCopierFactory = 
	new ClassCopierFactory() {
	    public ClassCopier getClassCopier( Class cls ) throws ReflectiveCopyException
	    {
		// Handle Remote: this must come before CORBA.Object,
		// since a corba Object may also be a Remote.
		if (Remote.class.isAssignableFrom( cls ))
		    return remoteClassCopier ;

		// Handle org.omg.CORBA.portable.ObjectImpl
		if (ObjectImpl.class.isAssignableFrom( cls ))
		    return corbaClassCopier ;

		// Need this case to handle TypeCode.
		if (ORB.class.isAssignableFrom(cls) ||
		    LogWrapperBase.class.isAssignableFrom(cls))
		    return DefaultClassCopiers.getIdentityClassCopier() ;

		return null ;
	    }
	} ;

    // It is very important that ccf be static.  This means that
    // ccf is shared across all instances of the object copier,
    // so that any class is analyzed only once, instead of once per 
    // copier instance.  This is worth probably 20%+ in microbenchmark 
    // performance.
    private static PipelineClassCopierFactory ccf = 
	DefaultClassCopierFactories.getPipelineClassCopierFactory() ; 
    
    static {
	ccf.setSpecialClassCopierFactory( specialClassCopierFactory ) ;
    }

    private Map oldToNew ;

    /** Create an ReflectObjectCopierImpl for the given ORB.
     * The orb is used for connection Remote instances.
     */
    public ReflectObjectCopierImpl( ORB orb )
    {
	localORB.set( orb ) ;
	if (DefaultClassCopierFactories.USE_FAST_CACHE)
	    oldToNew = new FastCache( new IdentityHashMap() ) ;
	else
	    oldToNew = new IdentityHashMap() ;
    }

    /** Return a deep copy of obj.  Aliasing is preserved within
     * obj and between objects passed in multiple calls to the
     * same instance of ReflectObjectCopierImpl.
     */
    public Object copy( Object obj ) throws ReflectiveCopyException
    {
	return copy( obj, false ) ;
    }

    public Object copy( Object obj, boolean debug ) throws ReflectiveCopyException
    {
	if (obj == null)
	    return null ;

	Class cls = obj.getClass() ;
	ClassCopier copier = ccf.getClassCopier( cls ) ;

	/* too much detail!
	if (debug) {
	    System.out.println( "Contents of ClassCopier:" +
		ObjectUtility.defaultObjectToString( copier ) ) ; 
	}
	*/

	return copier.copy( oldToNew, obj, debug ) ;
    }
}
