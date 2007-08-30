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

package com.sun.corba.se.spi.transport;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Queue;

import org.omg.CORBA.SystemException;

import com.sun.org.omg.SendingContext.CodeBase;

import com.sun.corba.se.pept.protocol.MessageMediator;
import com.sun.corba.se.pept.transport.Connection;
import com.sun.corba.se.pept.transport.ResponseWaitingRoom;

import com.sun.corba.se.spi.ior.IOR ;
import com.sun.corba.se.spi.ior.iiop.GIOPVersion;
import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.protocol.CorbaMessageMediator;
import com.sun.corba.se.spi.protocol.CorbaRequestId;

import com.sun.corba.se.impl.encoding.CodeSetComponentInfo;

/**
 * @author Harold Carr
 */
public interface CorbaConnection
    extends
	Connection,
	com.sun.corba.se.spi.legacy.connection.Connection
{
    public boolean shouldUseDirectByteBuffers();

    // NOTE: This method can throw a connection rebind SystemException.
    public ByteBuffer read(int size, int offset, int length )
	throws IOException;

    // NOTE: This method can throw a connection rebind SystemException.
    public ByteBuffer read(ByteBuffer byteBuffer, int offset,
	                  int length) throws IOException;

    public void write(ByteBuffer byteBuffer)
	throws IOException;

    public void dprint(String msg);

    //
    // From iiop.Connection.java
    //

    public int getNextRequestId();
    public ORB getBroker();
    public CodeSetComponentInfo.CodeSetContext getCodeSetContext();
    public void setCodeSetContext(CodeSetComponentInfo.CodeSetContext csc);
    
    //
    // from iiop.IIOPConnection.java
    //

    // Facade to ResponseWaitingRoom.
    public MessageMediator clientRequestMapGet(int requestId);

    public void clientReply_1_1_Put(MessageMediator x);
    public MessageMediator clientReply_1_1_Get();
    public void clientReply_1_1_Remove();

    public void serverRequest_1_1_Put(MessageMediator x);
    public MessageMediator serverRequest_1_1_Get();
    public void serverRequest_1_1_Remove();

    public boolean isPostInitialContexts();

    // Can never be unset...
    public void setPostInitialContexts();

    public void purgeCalls(SystemException systemException,
			   boolean die, boolean lockHeld);

    //
    // Connection status
    //
    public static final int OPENING = 1;
    public static final int ESTABLISHED = 2;
    public static final int CLOSE_SENT = 3;
    public static final int CLOSE_RECVD = 4;
    public static final int ABORT = 5;

    // Begin Code Base methods ---------------------------------------
    //
    // Set this connection's code base IOR.  The IOR comes from the
    // SendingContext.  This is an optional service context, but all
    // JavaSoft ORBs send it.
    //
    // The set and get methods don't need to be synchronized since the
    // first possible get would occur during reading a valuetype, and
    // that would be after the set.

    // Sets this connection's code base IOR.  This is done after
    // getting the IOR out of the SendingContext service context.
    // Our ORBs always send this, but it's optional in CORBA.

    void setCodeBaseIOR(IOR ior);

    IOR getCodeBaseIOR();

    // Get a CodeBase stub to use in unmarshaling.  The CachedCodeBase
    // won't connect to the remote codebase unless it's necessary.
    CodeBase getCodeBase();

    // End Code Base methods -----------------------------------------

    public void sendCloseConnection(GIOPVersion giopVersion)
	throws IOException;

    public void sendMessageError(GIOPVersion giopVersion)
	throws IOException;

    public void sendCancelRequest(GIOPVersion giopVersion, int requestId)
	throws
	    IOException;

    // NOTE: This method can throw a connection rebind SystemException.
    public void sendCancelRequestWithLock(GIOPVersion giopVersion,
					  int requestId)
	throws 
	    IOException;

    public ResponseWaitingRoom getResponseWaitingRoom();

    public void serverRequestMapPut(int requestId,
				    CorbaMessageMediator messageMediator);
    public CorbaMessageMediator serverRequestMapGet(int requestId);
    public void serverRequestMapRemove(int requestId);

    public Queue<CorbaMessageMediator> getFragmentList(CorbaRequestId corbaRequestId);
    public void removeFragmentList(CorbaRequestId corbaRequestId);

    // REVISIT: WRONG: should not expose sockets here.
    public SocketChannel getSocketChannel();

    // REVISIT - MessageMediator parameter?
    public void serverRequestProcessingBegins();
    public void serverRequestProcessingEnds();
}

// End of file.

