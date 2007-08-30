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


/**
* The Stub for <tt>Policy</tt>.  For more information on 
* Stub files, see <a href="doc-files/generatedfiles.html#stub">
* "Generated Files: Stubs"</a>.<P>
* org/omg/CORBA/_PolicyStub.java
* Generated by the IDL-to-Java compiler (portable), version "3.0"
* from ../../../../../src/share/classes/org/omg/PortableServer/corba.idl.
* Saturday, July 17, 1999 12:26:20 AM PDT
*/

public class _PolicyStub extends org.omg.CORBA.portable.ObjectImpl implements org.omg.CORBA.Policy
{
  /** Constructors
   * NOTE:  If the default constructor is used, the
   *        object is useless until _set_delegate (...)
   *        is called.
   */
  public _PolicyStub ()
  {
    super ();
  }

  /** Constructs a <code>_PolicyStub</code> object initialized
   * with the given <code>Delegate</code> object.
   *
   * @param delegate a Delegate Object
   */
  public _PolicyStub (org.omg.CORBA.portable.Delegate delegate)
  {
    super ();
    _set_delegate (delegate);
  }


  /** Returns the constant value that corresponds to the 
  		* type of the policy object.  The values of 
  		* the polivy objectys are allocated by the OMG.
  		* New values for PolicyType should be obtained from the OMG by
  		* sending mail to request@omg.org.  In general the constant
  		* values that are allocated are defined in conjunction with
  		* the definition of the corresponding policy object.
  		* @return the constant value that corresponds to the type of
  		* the policy object.
  		*/
  public int policy_type ()
  {
    org.omg.CORBA.portable.InputStream _in = null;
    try {
       org.omg.CORBA.portable.OutputStream _out = _request ("_get_policy_type", true);
       _in = _invoke (_out);
       int __result = org.omg.CORBA.PolicyTypeHelper.read (_in);
       return __result;
    } catch (org.omg.CORBA.portable.ApplicationException _ex) {
       _in = _ex.getInputStream ();
       String _id = _ex.getId ();
       throw new org.omg.CORBA.MARSHAL (_id);
    } catch (org.omg.CORBA.portable.RemarshalException _rm) {
       return policy_type ();
    } finally {
        _releaseReply (_in);
    }
  } // policy_type


  /** Copies the policy object. The copy does not retain any
  		* relationships that the policy had with any domain or object.
  		* @return the copy of the policy object.
  		*/
  public org.omg.CORBA.Policy copy ()
  {
    org.omg.CORBA.portable.InputStream _in = null;
    try {
       org.omg.CORBA.portable.OutputStream _out = _request ("copy", true);
       _in = _invoke (_out);
       org.omg.CORBA.Policy __result = org.omg.CORBA.PolicyHelper.read (_in);
       return __result;
    } catch (org.omg.CORBA.portable.ApplicationException _ex) {
       _in = _ex.getInputStream ();
       String _id = _ex.getId ();
       throw new org.omg.CORBA.MARSHAL (_id);
    } catch (org.omg.CORBA.portable.RemarshalException _rm) {
       return copy ();
    } finally {
        _releaseReply (_in);
    }
  } // copy


  /** Destroys the policy object.  It is the responsibility of
  		* the policy object to determine whether it can be destroyed.
  		*/
  public void destroy ()
  {
    org.omg.CORBA.portable.InputStream _in = null;
    try {
       org.omg.CORBA.portable.OutputStream _out = _request ("destroy", true);
       _in = _invoke (_out);
    } catch (org.omg.CORBA.portable.ApplicationException _ex) {
       _in = _ex.getInputStream ();
       String _id = _ex.getId ();
       throw new org.omg.CORBA.MARSHAL (_id);
    } catch (org.omg.CORBA.portable.RemarshalException _rm) {
       destroy ();
    } finally {
        _releaseReply (_in);
    }
  } // destroy

  // Type-specific CORBA::Object operations
  private static String[] __ids = {
    "IDL:omg.org/CORBA/Policy:1.0"};

  public String[] _ids ()
  {
    return (String[])__ids.clone ();
  }

  private void readObject (java.io.ObjectInputStream s)
  {
     try 
     {
       String str = s.readUTF ();
       org.omg.CORBA.Object obj = org.omg.CORBA.ORB.init ().string_to_object (str);
       org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl) obj)._get_delegate ();
       _set_delegate (delegate);
     } catch (java.io.IOException e) {}
  }

  private void writeObject (java.io.ObjectOutputStream s)
  {
     try 
     {
       String str = org.omg.CORBA.ORB.init ().object_to_string (this);
       s.writeUTF (str);
     } catch (java.io.IOException e) {}
  }
} // class _PolicyStub
