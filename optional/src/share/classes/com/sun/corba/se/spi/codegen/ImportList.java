/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.spi.codegen ;

import java.util.Collections ;
import java.util.List ;
import java.util.ArrayList ;
import java.util.Map ;
import java.util.HashMap ;

import com.sun.corba.se.spi.orbutil.generic.Pair ;
import com.sun.corba.se.spi.orbutil.generic.UnaryVoidFunction ;

import com.sun.corba.se.spi.codegen.Type ;

public interface ImportList {
    /** Return a copy of this ImportList.
     */
    ImportList copy() ;

    /** Add a fully-qualified type name to the imports.
     * Returns the Type for the name.
     */
    public Type addImport( final String name ) ;

    public void addImport( final Type type ) ;

    /** Return whether or not this Type is in the imports.
     */
    public boolean contains( final String name ) ;

    public boolean contains( final Type type ) ;

    /** Lookup just the className, without package name.
     */
    public Type lookup( final String className ) ;

    /** Return a list of imports as (packageName,className) pairs.
     * The list is sorted lexicographically.
     */
    public List<Pair<String,String>> getInOrderList() ;
}
