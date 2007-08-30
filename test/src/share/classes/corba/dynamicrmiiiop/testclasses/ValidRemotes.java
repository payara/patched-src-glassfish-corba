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
package corba.dynamicrmiiiop.testclasses;

/**
 * Valid RMI/IDL Remote Interface Types
 */
public class ValidRemotes {

    public static final Class[] CLASSES = {
        java.rmi.Remote.class, 
        ValidRemote0.class, ValidRemote1.class,
        ValidRemote2.class, ValidRemote3.class,
        ValidRemote4.class, ValidRemote5.class,
        ValidRemote6.class, ValidRemote7.class,
        ValidRemote8.class, ValidRemote9.class,
        ValidRemote10.class
    };

    public interface ValidRemote0 extends java.rmi.Remote {}
    
    public interface ValidRemote1 extends ValidRemote0 {}

    public interface ValidRemote2 extends java.rmi.Remote {
        public void foo1() throws java.rmi.RemoteException, 
            java.io.IOException, java.lang.Exception, java.lang.Throwable;
    }

    public interface ValidRemote3 extends java.rmi.Remote {
        public void foo1() throws java.rmi.RemoteException;
        public void foo2() throws java.io.IOException;
        public void foo3() throws java.lang.Exception;
        public void foo4() throws java.lang.Throwable;
    }

    public interface ValidRemote4 extends ValidRemote3 {}

    public interface ValidRemote5 extends java.rmi.Remote {
        boolean a = true;
        boolean b = false;
        byte c = 0;
        char d = 'd';
        short e = 2;
        int f = 3;
        long g = 4;
        float h = 5.0f;
        double i = 6.0;
        String j = "foo";
    }

    public interface ValidRemote6 extends java.rmi.Remote {
        public void foo1() throws java.rmi.RemoteException, 
            java.lang.Exception;
            
        public void foo2() 
            throws java.rmi.RemoteException, Exception1;
        
    }

    public static class Exception1 extends java.lang.Exception {}

    public interface ValidRemote7 extends java.rmi.Remote {
        void foo() throws java.rmi.RemoteException;
        void foo(int a) throws java.rmi.RemoteException;
        void foo(String[] b) throws java.rmi.RemoteException;
    }

    public interface ValidRemote8 extends 
        ValidRemote2, ValidRemote5, ValidRemote7 {}

    public interface ValidRemote9 extends ValidRemote8 {
        void foo(int a) throws java.rmi.RemoteException;
    }

    public interface ValidRemote10 extends java.rmi.Remote, ValidRemote9 {}
}
