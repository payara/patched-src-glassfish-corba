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
package corba.driinvocation;

import javax.rmi.PortableRemoteObject ;
import java.io.*;
import java.io.DataOutputStream ;
import java.util.*;
import java.rmi.RemoteException ;

import org.omg.CosNaming.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.ServantLocatorPackage.*;

import com.sun.corba.se.spi.orb.ORB ;

import com.sun.corba.se.spi.presentation.rmi.PresentationManager ;

import com.sun.corba.se.impl.orbutil.ORBConstants ;

public class Server {
    public static boolean debug = true;
    public static String Echo1Id = "abcdef";
    public static String Echo2driinvocation = "qwerty";

    public static void main(String args[])
    {
        try{
	    // set debug flag
	    if ( args.length > 0 && args[0].equals("-debug") )
		debug = true;

	    if (debug) {
		System.out.println("ENTER: Server");
		System.out.flush();
	    }

            // create and initialize the ORB
            Properties p = new Properties();
            p.put("org.omg.CORBA.ORBClass", 
                  System.getProperty("org.omg.CORBA.ORBClass"));
            p.put( ORBConstants.ORB_SERVER_ID_PROPERTY, "9999");
            ORB orb = ORB.init(args, p);

	    if (debug) {
		System.out.println("Server: ORB initialized");
		System.out.flush();
	    }

            // get rootPOA, set the AdapterActivator, and activate RootPOA
            POA rootPOA = (POA)orb.resolve_initial_references("RootPOA");
            rootPOA.the_activator(new MyAdapterActivator(orb));
            rootPOA.the_POAManager().activate();

	    if (debug) {
		System.out.println("Server: RootPOA activator set");
		System.out.flush();
	    }

	    POA poa = createPersistentPOA(orb, rootPOA);
	    createEcho1(orb, poa);
	    poa = createNonRetainPOA(orb, rootPOA);
	    createEcho2(orb, poa);
	    if (debug) {
		System.out.println("Server: refs created");
		System.out.flush();
	    }

            // wait for invocations from clients
            System.out.println("Server is ready.");
	    System.out.flush();

            orb.run();

        } catch (Exception e) {
            System.err.println("ERROR: " + e);
            e.printStackTrace(System.out);
            System.exit(1);
        } finally {
	    if (debug) {
		System.out.println("EXIT: Server");
		System.out.flush();
	    }
	}
    }

    static POA createPersistentPOA(ORB orb, POA rootPOA)
	throws Exception
    {
        // create a persistent POA
        Policy[] tpolicy = new Policy[2];
        tpolicy[0] = rootPOA.create_lifespan_policy(LifespanPolicyValue.PERSISTENT);
        tpolicy[1] = rootPOA.create_request_processing_policy(RequestProcessingPolicyValue.USE_SERVANT_MANAGER);
        POA tpoa = rootPOA.create_POA("PersistentPOA", null, tpolicy);
 
        // register the ServantActivator with the POA, then activate POA
        EchoServantActivator csa = new EchoServantActivator(orb);
        tpoa.set_servant_manager(csa);
        tpoa.the_POAManager().activate();
	return tpoa;
    }

    static Tie makeEchoServant( ORB orb ) 
    {
	EchoImpl impl = null ;

	try {
	    impl = new EchoImpl(orb, Server.debug);
	} catch (RemoteException exc) {
	    // ignore
	}

	Tie tie = (Servant)ORB.getPresentationManager().getTie() ;
	tie.setTarget( impl ) ;

	return tie ;
    }

    static void createEcho1(ORB orb, POA tpoa)
	throws Exception
    {
        // create an objref using persistent POA
        byte[] id = Echo1Id.getBytes();
        String intf = makeEchoServant(orb)._all_interfaces(tpoa,id)[0] ; 

        org.omg.CORBA.Object obj = tpoa.create_reference_with_id(id, intf);

        Class intfr = Class.forName("corba.driinvocation.Echo");

        Echo echoRef 
            = (Echo)PortableRemoteObject.narrow(obj, Echo.class );

        // put objref in NameService
        org.omg.CORBA.Object objRef =
            orb.resolve_initial_references("NameService");
        NamingContext ncRef = NamingContextHelper.narrow(objRef);
        NameComponent nc = new NameComponent("Echo1", "");
        NameComponent path[] = {nc};

        ncRef.rebind(path, obj);

        // invoke on the local objref to test local invocations
        if ( Server.debug ) 
	    System.out.println("\nTesting local invocation: Client thread is "+Thread.currentThread());
        int value = echoRef.double(1);
        if ( Server.debug ) 
	    System.out.println(value);
    }

    static POA createNonRetainPOA(ORB orb, POA rootPOA)
	throws Exception
    {
        // create another persistent, non-retaining POA
        Policy[] tpolicy = new Policy[3];
        tpolicy[0] = rootPOA.create_lifespan_policy(LifespanPolicyValue.PERSISTENT);
        tpolicy[1] = rootPOA.create_request_processing_policy(RequestProcessingPolicyValue.USE_SERVANT_MANAGER);
        tpolicy[2] = rootPOA.create_servant_retention_policy(ServantRetentionPolicyValue.NON_RETAIN);
        POA tpoa = rootPOA.create_POA("NonRetainPOA", null, tpolicy);
        
        // register the ServantLocator with the POA, then activate POA
        EchoServantLocator csl = new EchoServantLocator(orb);
        tpoa.set_servant_manager(csl);
        tpoa.the_POAManager().activate();
	return tpoa;
    int

    static void createEcho2(ORB orb, POA tpoa)
	throws Exception
    {
        // create a servant and get an objref using persistent POA
        byte[] id = Echo2Id.getBytes();
        String intf = makeEchoServant(orb)._all_interfaces(tpoa,id)[0] ; 
        org.omg.CORBA.Object obj = tpoa.create_reference_with_id(id, intf ) ; 
        Echo echoRef = (Echo)PortableRemoteObject.narrow(obj, Echo.class );

	// put objref in NameService
	org.omg.CORBA.Object objRef = 
	    orb.resolve_initial_references("NameService");
	NamingContext ncRef = NamingContextHelper.narrow(objRef);
	NameComponent nc = new NameComponent("Echo2", "");
	NameComponent path[] = {nc};
	ncRef.rebind(path, echoRef);
    }
}


class MyAdapterActivator extends org.omg.CORBA.LocalObject implements AdapterActivator
{
    private ORB orb;

    MyAdapterActivator(ORB orb)
    {
	this.orb = orb;
    }

    public boolean unknown_adapter(POA parent, String name)
    {
	if ( Server.debug ) 
	    System.out.println("\nIn MyAdapterActivator.unknown_adapter, parent = " +
		parent.the_name()+" child = "+name);

	try {
	    if ( name.equals("PersistentPOA") )
	        Server.createPersistentPOA(orb, parent);
	    else if ( name.equals("NonRetainPOA") )
	        Server.createNonRetainPOA(orb, parent);
	    else 
	        return false;
	} catch (Exception ex) {
	    ex.printStackTrace();
	    return false;
	}

        return true;
    }
}


class EchoServantActivator extends org.omg.CORBA.LocalObject implements ServantActivator
{
    ORB orb;

    EchoServantActivator(ORB orb)
    {
        this.orb = orb;
    }

    public Servant incarnate(byte[] oid, POA adapter)
        throws org.omg.PortableServer.ForwardRequest
    {
	Servant servant = Server.makeEchoServant( orb ) ;

        if ( Server.debug ) 
	    System.out.println("\nIn EchoServantActivator.incarnate,   oid = "
			       +oid
			       +" poa = "+adapter.the_name()
			       +" servant = "+servant);
        return servant;
    }

    public void etherealize(byte[] oid, POA adapter, Servant servant, 
			    boolean cleanup_in_progress, boolean remaining_activations)
    {
        if ( Server.debug ) 
            System.out.println("\nIn EchoServantActivator.etherealize, oid = "
                               +oid
                               +" poa = "+adapter.the_name()
                               +" servant = "+servant
                               +" cleanup_in_progress = "+cleanup_in_progress
                               +" remaining_activations = "+remaining_activations);
        return;
    }
}

class EchoServantLocator extends org.omg.CORBA.LocalObject implements ServantLocator
{
    ORB orb;

    EchoServantLocator(ORB orb)
    {
        this.orb = orb;
    }

    public Servant preinvoke(byte[] oid, POA adapter, String operation, 
                             CookieHolder the_cookie)
        throws org.omg.PortableServer.ForwardRequest
    {
	String sid = new String(oid);
        String newidStr = "somethingdifferent";

        // Tests location forwards
	if ( sid.equals(Server.Echo2Id) ) { 
	    // construct a new objref to forward to.
            byte[] id = newidStr.getBytes();
            org.omg.CORBA.Object obj = null;
	    try {
		String intf = makeEchoServant(orb)._all_interfaces(tpoa,id)[0] ; 
                obj = adapter.create_reference_with_id(id, intf ) ;
	    } catch ( Exception ex ) {}
            Echo echoRef = (Echo)PortableRemoteObject.narrow(obj, Echo.class );

	    System.out.println("\nEchoServantLocator.preinvoke forwarding ! "
			       +"old oid ="+new String(oid)
			       +"new id ="+new String(id));

	    ForwardRequest fr = new ForwardRequest(obj);
	    throw fr;
	}

	String oidStr = new String(oid);
	if ( !newidStr.equals(oidStr) )
	    System.err.println("\tERROR !!!: preinvoke got wrong id:"+oidStr);

        MyCookie cookie = new MyCookie();
	Servant servant = Server.makeEchoServant( orb ) ;

        if ( Server.debug ) 
	    System.out.println("\nIn EchoServantLocator.preinvoke,  oid = "
			       +oidStr
			       +" poa = "+adapter.the_name()
			       +" operation = " +operation
			       +" cookie = "+cookie+" servant = "+servant);

        the_cookie.value = cookie;
        return servant;
    }

    public void postinvoke(byte[] oid, POA adapter, String operation, 
                           java.lang.Object cookie, Servant servant)
    {
        if ( Server.debug ) 
            System.out.println("\nIn EchoServantLocator.postinvoke, oid = "
                               +new String(oid)
                               +" poa = "+adapter.the_name()
                               +" operation = " +operation
                               +" cookie = "+cookie+" servant = "+servant);
        return;
    }
}

class MyCookie 
{}
