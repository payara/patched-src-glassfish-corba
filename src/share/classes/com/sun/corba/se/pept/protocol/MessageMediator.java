/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2001-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.pept.protocol;

import com.sun.corba.se.pept.broker.Broker;
import com.sun.corba.se.pept.encoding.InputObject;
import com.sun.corba.se.pept.encoding.OutputObject;
import com.sun.corba.se.pept.transport.Connection;
import com.sun.corba.se.pept.transport.ContactInfo;

import java.io.IOException;

/**
 * <code>MessageMediator</code> is a central repository for artifacts
 * associated with an individual message.
 *
 * @author Harold Carr
 */
public interface MessageMediator
{
    /**
     * The {@link com.sun.corba.se.pept.broker.Broker Broker} associated
     * with an invocation.
     *
     * @return {@link com.sun.corba.se.pept.broker.Broker Broker}
     */
    public Broker getBroker();

    /**
     * Get the
     * {@link com.sun.corba.se.pept.transport.ContactInfo ContactInfo}
     * which created this <code>MessageMediator</code>.
     *
     * @return
     * {@link com.sun.corba.se.pept.transport.ContactInfo ContactInfo}
     */
    public ContactInfo getContactInfo();

    /**
     * Get the
     * {@link com.sun.corba.se.pept.transport.Connection Connection}
     * on which this message is sent or received.
     */
    public Connection getConnection();

    /**
     * Used to initialize message headers.
     *
     * Note: this should be moved to a <code>RequestDispatcher</code>.
     */
    public void initializeMessage();
    
    /**
     * Used to send the message (or its last fragment).
     *
     * Note: this should be moved to a <code>RequestDispatcher</code>.
     */
    public void finishSendingRequest();

    /**
     * Used to wait for a response for synchronous messages.
     *
     * @deprecated
     */
    public InputObject waitForResponse();

    /**
     * Used to set the
     * {@link com.sun.corba.se.pept.encoding.OutputObject OutputObject}
     * used for the message.
     *
     * @param outputObject
     */
    public void setOutputObject(OutputObject outputObject);

    /**
     * Used to get the
     * {@link com.sun.corba.se.pept.encoding.OutputObject OutputObject}
     * used for the message.
     *
     * @return
     * {@link com.sun.corba.se.pept.encoding.OutputObject OutputObject}
     */
    public OutputObject getOutputObject();

    /**
     * Used to set the
     * {@link com.sun.corba.se.pept.encoding.InputObject InputObject}
     * used for the message.
     *
     * @param inputObject
     */
    public void setInputObject(InputObject inputObject);

    /**
     * Used to get the
     * {@link com.sun.corba.se.pept.encoding.InputObject InputObject}
     * used for the message.
     *
     * @return
     * {@link com.sun.corba.se.pept.encoding.InputObject InputObject}
     */
    public InputObject getInputObject();
}

// End of file.

