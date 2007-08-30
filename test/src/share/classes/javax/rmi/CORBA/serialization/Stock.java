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
/* @(#)Stock.java	1.3 99/06/07 */
/*
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

package javax.rmi.CORBA.serialization;

/**
* Stock.java
* Generated by the IDL-to-Java compiler (portable), version "3.0"
* from org/omg/boxedRMI/examples/stock/seq1_Stock.idl
* Friday, May 14, 1999 12:57:04 o'clock PM EDT
*/

public abstract class Stock implements org.omg.CORBA.portable.StreamableValue
{
  protected float current = (float)0;
  protected String symbol = null;

  private static String[] _truncatable_ids = {
    StockHelper.id ()
  };

  public String[] _truncatable_ids() {
    return _truncatable_ids;
  }

  public abstract float update ();

  public void _read (org.omg.CORBA.portable.InputStream istream)
  {
    this.current = istream.read_float ();
    this.symbol = org.omg.CORBA.WStringValueHelper.read (istream);
  }

  public void _write (org.omg.CORBA.portable.OutputStream ostream)
  {
    ostream.write_float (this.current);
    org.omg.CORBA.WStringValueHelper.write (ostream, this.symbol);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return StockHelper.type ();
  }

} // class Stock
