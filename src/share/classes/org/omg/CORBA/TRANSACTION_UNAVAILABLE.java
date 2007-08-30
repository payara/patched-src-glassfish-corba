/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

/**
 * The CORBA <code>TRANSACTION_UNAVAILABLE</code> exception is thrown
 * by the ORB when it cannot process a transaction service context because 
 * its connection to the Transaction Service has been abnormally terminated.
 *
 * It contains a minor code, which gives information about
 * what caused the exception, and a completion status. It may also contain
 * a string describing the exception.
 * The OMG CORBA core 2.4 specification has details.
 *
 * @see <A href="../../../../guide/idl/jidlExceptions.html">documentation on
 * Java&nbsp;IDL exceptions</A>
 * @version     1.0 10/22/2000
 */

public final class TRANSACTION_UNAVAILABLE extends SystemException {
    /**
     * Constructs a <code>TRANSACTION_UNAVAILABLE</code> exception  
     * with a default minor code of 0, a completion state of
     * CompletionStatus.COMPLETED_NO, and a null description.
     */
    public TRANSACTION_UNAVAILABLE() {
	this("");
    }

    /**
     * Constructs a <code>TRANSACTION_UNAVAILABLE</code> exception with the 
     * specifieddescription message, a minor code of 0, and a completion state
     * of COMPLETED_NO.
     * @param s the String containing a detail message
     */
    public TRANSACTION_UNAVAILABLE(String s) {
        this(s, 0, CompletionStatus.COMPLETED_NO);
    }

    /**
     * Constructs a <code>TRANSACTION_UNAVAILABLE</code> exception with the 
     * specified minor code and completion status.
     * @param minor the minor code
     * @param completed the completion status
     */
    public TRANSACTION_UNAVAILABLE(int minor, CompletionStatus completed) {
        this("", minor, completed);
    }

    /**
     * Constructs a <code>TRANSACTION_UNAVAILABLE</code> exception with the 
     * specified description message, minor code, and completion status.
     * @param s the String containing a description message
     * @param minor the minor code
     * @param completed the completion status
     */
    public TRANSACTION_UNAVAILABLE(String s, int minor, 
				   CompletionStatus completed) {
        super(s, minor, completed);
    }
}
