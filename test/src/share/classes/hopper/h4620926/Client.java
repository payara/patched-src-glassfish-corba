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
package hopper.h4620926;

import org.omg.CORBA.*;
import org.omg.CosNaming.*;
import java.util.*;
import test.*;
        
public class Client extends Thread {
    private static int numErrors = 0;

    private static synchronized void errorOccurred( int clientNo,
	Exception exc)
    {
	numErrors++ ;
	System.out.println(
	    "\nError in ClientNo " + clientNo + ": " + exc );

	System.err.println(
	    "\nError in ClientNo " + clientNo + ": " + exc );

	exc.printStackTrace();
    }

    private static int counter = 0;
    private int clientNo = 0;
    Hello ref = null;

    public Client(ORB orb) throws Exception {
        counter++;
        clientNo = counter;
        System.out.println("Creating client - " + clientNo);
        NamingContext namingContext = NamingContextHelper.narrow(
            orb.resolve_initial_references("NameService"));
        NameComponent[] name = { new NameComponent("Hello", "") };
        ref = HelloHelper.narrow(namingContext.resolve(name));
    }

    public void run() {
        for (int i = 0; i < 3; i++) {
            try {
                System.out.println("Client - " + clientNo + " : " 
                                   + ref.sayHello());
                Thread.sleep(2000);
            } catch (Exception e) {
		errorOccurred(clientNo, e) ;
	        System.exit( 1 );
            }
        }
        System.out.println( "TEST PASSED" );
        System.out.flush( );
    }

    public static void main(String[] args) {
        Client[] c = null;
        int noOfThreads = 5;

        // try {
            // noOfThreads = Integer.parseInt(args[0]);
        // } catch (NumberFormatException e) { }

        try {
            c = new Client[noOfThreads];
            for (int i = 0; i < noOfThreads; i++) {
                ORB orb = ORB.init(args, null);
                c[i] = new Client(orb);
            }

            for (int i = 0; i < noOfThreads; i++) {
                c[i].start();
            }

            for (int i = 0; i < noOfThreads; i++) {
                c[i].join();
            }
        } catch (Exception e) {
	    errorOccurred( -1, e );
        }

	System.exit( numErrors>0 ? 1 : 0 ) ;
    }
}
