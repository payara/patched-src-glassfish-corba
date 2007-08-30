/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1999-2007 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.org.omg.CORBA;


/**
* com/sun/org/omg/CORBA/ExceptionDescriptionHelper.java
* Generated by the IDL-to-Java compiler (portable), version "3.0"
* from ir.idl
* Thursday, May 6, 1999 1:51:50 AM PDT
*/

public final class ExceptionDescriptionHelper
{
    private static String  _id = "IDL:omg.org/CORBA/ExceptionDescription:1.0";

    public ExceptionDescriptionHelper()
    {
    }

    public static void insert (org.omg.CORBA.Any a, com.sun.org.omg.CORBA.ExceptionDescription that)
    {
	org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
	a.type (type ());
	write (out, that);
	a.read_value (out.create_input_stream (), type ());
    }

    public static com.sun.org.omg.CORBA.ExceptionDescription extract (org.omg.CORBA.Any a)
    {
	return read (a.create_input_stream ());
    }

    private static org.omg.CORBA.TypeCode __typeCode = null;
    private static boolean __active = false;
    synchronized public static org.omg.CORBA.TypeCode type ()
    {
	if (__typeCode == null)
	    {
		synchronized (org.omg.CORBA.TypeCode.class)
		    {
			if (__typeCode == null)
			    {
				if (__active)
				    {
					return org.omg.CORBA.ORB.init().create_recursive_tc ( _id );
				    }
				__active = true;
				org.omg.CORBA.StructMember[] _members0 = new org.omg.CORBA.StructMember [5];
				org.omg.CORBA.TypeCode _tcOf_members0 = null;
				_tcOf_members0 = org.omg.CORBA.ORB.init ().create_string_tc (0);
				_tcOf_members0 = org.omg.CORBA.ORB.init ().create_alias_tc (com.sun.org.omg.CORBA.IdentifierHelper.id (), "Identifier", _tcOf_members0);
				_members0[0] = new org.omg.CORBA.StructMember (
									       "name",
									       _tcOf_members0,
									       null);
				_tcOf_members0 = org.omg.CORBA.ORB.init ().create_string_tc (0);
				_tcOf_members0 = org.omg.CORBA.ORB.init ().create_alias_tc (com.sun.org.omg.CORBA.RepositoryIdHelper.id (), "RepositoryId", _tcOf_members0);
				_members0[1] = new org.omg.CORBA.StructMember (
									       "id",
									       _tcOf_members0,
									       null);
				_tcOf_members0 = org.omg.CORBA.ORB.init ().create_string_tc (0);
				_tcOf_members0 = org.omg.CORBA.ORB.init ().create_alias_tc (com.sun.org.omg.CORBA.RepositoryIdHelper.id (), "RepositoryId", _tcOf_members0);
				_members0[2] = new org.omg.CORBA.StructMember (
									       "defined_in",
									       _tcOf_members0,
									       null);
				_tcOf_members0 = org.omg.CORBA.ORB.init ().create_string_tc (0);
				_tcOf_members0 = org.omg.CORBA.ORB.init ().create_alias_tc (com.sun.org.omg.CORBA.VersionSpecHelper.id (), "VersionSpec", _tcOf_members0);
				_members0[3] = new org.omg.CORBA.StructMember (
									       "version",
									       _tcOf_members0,
									       null);
				_tcOf_members0 = org.omg.CORBA.ORB.init ().get_primitive_tc (org.omg.CORBA.TCKind.tk_TypeCode);
				_members0[4] = new org.omg.CORBA.StructMember (
									       "type",
									       _tcOf_members0,
									       null);
				__typeCode = org.omg.CORBA.ORB.init ().create_struct_tc (com.sun.org.omg.CORBA.ExceptionDescriptionHelper.id (), "ExceptionDescription", _members0);
				__active = false;
			    }
		    }
	    }
	return __typeCode;
    }

    public static String id ()
    {
	return _id;
    }

    public static com.sun.org.omg.CORBA.ExceptionDescription read (org.omg.CORBA.portable.InputStream istream)
    {
	com.sun.org.omg.CORBA.ExceptionDescription value = new com.sun.org.omg.CORBA.ExceptionDescription ();
	value.name = istream.read_string ();
	value.id = istream.read_string ();
	value.defined_in = istream.read_string ();
	value.version = istream.read_string ();
	value.type = istream.read_TypeCode ();
	return value;
    }

    public static void write (org.omg.CORBA.portable.OutputStream ostream, com.sun.org.omg.CORBA.ExceptionDescription value)
    {
	ostream.write_string (value.name);
	ostream.write_string (value.id);
	ostream.write_string (value.defined_in);
	ostream.write_string (value.version);
	ostream.write_TypeCode (value.type);
    }

}
