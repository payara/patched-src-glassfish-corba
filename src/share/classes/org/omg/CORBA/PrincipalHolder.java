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

package org.omg.CORBA;

import org.omg.CORBA.portable.Streamable;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;


/**
 * The Holder for <tt>Principal</tt>.  For more information on 
 * Holder files, see <a href="doc-files/generatedfiles.html#holder">
 * "Generated Files: Holder Files"</a>.<P>
 * A container class for values of type <code>Principal</code>
 * that is used to store "out" and "inout" parameters in IDL methods.
 * If an IDL method signature has an IDL <code>Principal</code> as an "out"
 * or "inout" parameter, the programmer must pass an instance of
 * <code>PrincipalHolder</code> as the corresponding
 * parameter in the method invocation; for "inout" parameters, the programmer
 * must also fill the "in" value to be sent to the server.
 * Before the method invocation returns, the ORB will fill in the
 * value corresponding to the "out" value returned from the server.
 * <P>
 * If <code>myPrincipalHolder</code> is an instance of <code>PrincipalHolder</code>,
 * the value stored in its <code>value</code> field can be accessed with
 * <code>myPrincipalHolder.value</code>.
 *
 * @version	1.14, 09/09/97
 * @since       JDK1.2
 * @deprecated Deprecated by CORBA 2.2.
 */
// @Deprecated
public final class PrincipalHolder implements Streamable {
    /**
     * The <code>Principal</code> value held by this <code>PrincipalHolder</code>
     * object.
     */
    public Principal value;

    /**
     * Constructs a new <code>PrincipalHolder</code> object with its
     * <code>value</code> field initialized to <code>null</code>.
     */
    public PrincipalHolder() {
    }

    /**
     * Constructs a new <code>PrincipalHolder</code> object with its
     * <code>value</code> field initialized to the given
     * <code>Principal</code> object.
     * @param initial the <code>Principal</code> with which to initialize
     *                the <code>value</code> field of the newly-created
     *                <code>PrincipalHolder</code> object
     */
    // @SuppressWarnings({"deprecation"})
    public PrincipalHolder(Principal initial) {
	value = initial;
    }

    public void _read(InputStream input) {
	value = input.read_Principal();
    }

    public void _write(OutputStream output) {
	output.write_Principal(value);
    }

    public org.omg.CORBA.TypeCode _type() {
	return ORB.init().get_primitive_tc(TCKind.tk_Principal);
    }

}
