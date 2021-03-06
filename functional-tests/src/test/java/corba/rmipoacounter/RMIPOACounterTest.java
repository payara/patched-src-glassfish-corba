/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package corba.rmipoacounter;

import test.Test;
import corba.framework.*;
import java.util.*;
import com.sun.corba.ee.spi.misc.ORBConstants;

public class RMIPOACounterTest extends CORBATest
{
    protected Controller newClientController()
    {
        return new InternalExec();
    }

    protected void doTest() throws Throwable
    {
        // try this one. the report dir was already set to gen/corba/rmipoacounter
        Options.setOutputDirectory((String)getArgs().get(test.Test.OUTPUT_DIRECTORY));
        Options.addServerArg("-debug");

        Controller orbd = createORBD();

        Properties serverProps = Options.getServerProperties();

        serverProps.setProperty(ORBConstants.PERSISTENT_SERVER_PORT_PROPERTY,
                                Options.getUnusedPort().toString());

        Controller server = createServer("corba.rmipoacounter.counterServer");

        orbd.start();
        server.start();

        // In this test, the client will kill and restart the server, so it
        // needs the reference.  Thus, the client can only be an InternalProcess
        // or a ThreadProcess.
        Hashtable clientExtra = Options.getClientExtra();
        clientExtra.put("server", server);

        /*
          This is basically a test of persistent servers.  The server 
          maintains a counter in a file, and can be restarted without
          losing it.  Plus, the reference in ORBD stays the same.
        */

        Controller client = createClient("corba.rmipoacounter.counterClient");

        client.start();

        client.waitFor();

        client.stop();

        server.stop();

        orbd.stop();
    }
}

