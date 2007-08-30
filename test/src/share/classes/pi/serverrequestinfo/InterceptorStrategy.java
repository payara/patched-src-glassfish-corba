/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2000-2007 Sun Microsystems, Inc. All rights reserved.
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

package pi.serverrequestinfo;

import com.sun.corba.se.impl.interceptors.*;
import org.omg.PortableInterceptor.*;

/**
 * Base class for all server request interceptor strategies used in this 
 * test.  This allows for dyanmic behavior modifications between test cases.
 * Default method implementations do nothing.
 */
abstract public class InterceptorStrategy {

    /** True if test failed, false if not */
    public boolean failed = false;

    /** The reason for failure */
    public String failReason = null;

    /** 
     * Logs the given message as test output.
     */
    protected void log( String message ) {
	ServerCommon.server.out.println( "    + " + message );
    }

    /**
     * Reports the given message as a test failure for the given reason.
     */
    protected void fail( String reason ) {
	ServerCommon.server.out.println( "    + ERROR: " + reason );
	failReason = reason;
	failed = true;
    }
    
    /**
     * Reports a test failure, using the exception object as a reason.
     * The origin of failure is given in the origin string.
     */
    protected void failException( String origin, Exception ex ) {
        String failReason = "Exception " + 
            ex.getClass().getName() + "( " + ex.getMessage() + " )" +
            " in " + this.getClass().getName() + "." + origin + ".";
        ServerCommon.server.out.println( "    + ERROR: " + failReason );
        ex.printStackTrace();
        failed = true;
    }

    public void receive_request_service_contexts (
	SampleServerRequestInterceptor interceptor, ServerRequestInfo ri)
        throws ForwardRequest
    {
    }

    public void receive_request (
	SampleServerRequestInterceptor interceptor, ServerRequestInfo ri)
    {
    }

    public void send_reply (
	SampleServerRequestInterceptor interceptor, ServerRequestInfo ri)
    {
    }

    public void send_exception (
	SampleServerRequestInterceptor interceptor, ServerRequestInfo ri) 
	throws ForwardRequest
    {
    }

    public void send_other (
	SampleServerRequestInterceptor interceptor, ServerRequestInfo ri) 
        throws ForwardRequest
    {
    }

}
