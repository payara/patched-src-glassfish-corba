/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * 
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 * 
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 * 
 * Contributor(s):
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

import com.sun.corba.se.spi.orbutil.logex.Chain;
import com.sun.corba.se.spi.orbutil.logex.ExceptionWrapper;
import com.sun.corba.se.spi.orbutil.logex.Log;
import com.sun.corba.se.spi.orbutil.logex.LogLevel;
import com.sun.corba.se.spi.orbutil.logex.Message;
import com.sun.corba.se.spi.orbutil.logex.WrapperGenerator;

import com.sun.corba.se.spi.orbutil.copyobject.ReflectiveCopyException ;
import com.sun.corba.se.spi.orbutil.logex.stdcorba.StandardLogger;
import java.lang.reflect.Constructor;

/** Exception wrapper class.  The logex WrapperGenerator uses this interface
 * to generate an implementation which returns the appropriate exception, and
 * generates a log report when the method is called.  This is used for all
 * implementation classes in this package.
 *
 * The exception IDs are allocated in blocks of EXCEPTIONS_PER_CLASS, which is
 * a lot more than is needed, but we have 32 bits for IDs, and multiples of
 * a suitably chosen EXCEPTIONS_PER_CLASS (like 100 here) are easy to read in
 * error messages.
 *
 * @author ken
 */
@ExceptionWrapper( idPrefix="ORBOCOPY" )
public interface Exceptions {
    static final Exceptions self = WrapperGenerator.makeWrapper(
        Exceptions.class, StandardLogger.self ) ;

    // Allow 100 exceptions per class
    static final int EXCEPTIONS_PER_CLASS = 100 ;

// FallbackCopierImpl
    static final int FB_START = 1 ;

    @Message( "Object copy failed on copy of {0} which has type {1}" )
    @Log( id = FB_START + 0, level=LogLevel.FINE )
    void failureInFallback(
        @Chain ReflectiveCopyException exc, Object obj, Class cls );

// ClassCopierFactoryArrayImpl
    static final int CCFA_START = FB_START + EXCEPTIONS_PER_CLASS ;

    @Message( "Bad primitive type {0} in array" )
    @Log( id = CCFA_START + 0, level=LogLevel.WARNING )
    void badPrimitiveTypeInArray( Class<?> compClass ) ;

// ClassCopierFactoryPipelineImpl
    static final int CCFP_START = CCFA_START + EXCEPTIONS_PER_CLASS ;

    @Message( "Could not find a class copier for class {0}" )
    @Log( id = CCFP_START + 0 )
    IllegalStateException couldNotFindClassCopier( Class<?> cls ) ;

    @Message( "Cannot create a class copier for an interface (interface is {0})")
    @Log( id = CCFP_START + 1 )
    IllegalArgumentException cannotCreateClassCopierForInterface( Class<?> cls ) ;

    @Message( "Cannot copy class {0}" )
    @Log( id = CCFP_START + 2 )
    ReflectiveCopyException cannotCopyClass( Class<?> cls ) ;

// ClassCopierOrdinaryImpl
    static final int CCO_START = CCFP_START + EXCEPTIONS_PER_CLASS ;

    @Message( "Failure in newInstance for constructor {0}" )
    @Log( id = CCO_START + 0 ) 
    ReflectiveCopyException constructorFailed( Constructor constructor,
        @Chain Exception exc ) ;

    @Message( "Externalizable class {0} does not have a suitable constructor")
    @Log( id = CCO_START + 1 )
    void noExternalizbleConstructor( Class<?> cl ) ;

    @Message( "Serializable class {0} does not have a suitable constructor")
    @Log( id = CCO_START + 2 )
    void noSerializableConstructor( Class<?> cl ) ;

    @Message( "{0} must be a primitive type" )
    @Log( id = CCO_START + 3 )
    IllegalArgumentException notAPrimitiveType( Class<?> cls ) ;

    @Message( "Could not access unsafe codegen copier constructor for class {0}" )
    @Log( id = CCO_START + 4 )
    ReflectiveCopyException noAccessCodegenCopierConstructor( Class<?> cls,
        @Chain Exception exc ) ;

    @Message( "Could not create unsafe codegen copier for class {0}" )
    @Log( id = CCO_START + 5 )
    ReflectiveCopyException couldNotCreateCodegenCopier( Class<?> cls,
        @Chain Exception exc ) ;
}