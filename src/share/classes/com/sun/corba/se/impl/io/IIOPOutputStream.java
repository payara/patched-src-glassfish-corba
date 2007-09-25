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
/*
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

package com.sun.corba.se.impl.io;

import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.portable.OutputStream;

import java.security.AccessController ;
import java.security.PrivilegedAction ;

import java.io.IOException;
import java.io.DataOutputStream;
import java.io.Serializable;
import java.io.InvalidClassException;
import java.io.StreamCorruptedException;
import java.io.Externalizable;
import java.io.ObjectStreamException;
import java.io.NotSerializableException;
import java.io.NotActiveException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Field;

import java.util.Stack;

import javax.rmi.CORBA.ValueHandlerMultiFormat;

import sun.corba.Bridge ;

import com.sun.corba.se.spi.orb.ORB ;

import com.sun.corba.se.impl.io.ObjectStreamClass;
import com.sun.corba.se.impl.util.Utility;
import com.sun.corba.se.impl.util.RepositoryId;

import com.sun.corba.se.impl.logging.UtilSystemException ;
import com.sun.corba.se.impl.javax.rmi.CORBA.Util;

import com.sun.corba.se.impl.orbutil.ClassInfoCache ;

/**
 * IIOPOutputStream is ...
 *
 * @author  Stephen Lewallen
 * @version 0.01, 4/6/98
 * @since   JDK1.1.6
 */

public class IIOPOutputStream
    extends com.sun.corba.se.impl.io.OutputStreamHook
{
    private UtilSystemException wrapper = 
	ORB.getStaticLogWrapperTable().get_RPC_ENCODING_Util() ;

    private static Bridge bridge = 
	AccessController.doPrivileged(
	    new PrivilegedAction<Bridge>() {
		public Bridge run() {
		    return Bridge.get() ;
		}
	    } 
	) ;

    private org.omg.CORBA_2_3.portable.OutputStream orbStream;

    private Object currentObject = null;

    private ObjectStreamClass currentClassDesc = null;

    private int recursionDepth = 0;

    private int simpleWriteDepth = 0;

    private IOException abortIOException = null;

    private Stack<ObjectStreamClass> classDescStack = 
	new Stack<ObjectStreamClass>();

    public IIOPOutputStream()
	throws java.io.IOException
   {
	super();
    }

    // If using RMI-IIOP stream format version 2, this tells
    // the ORB stream (which must be a ValueOutputStream) to
    // begin a new valuetype to contain the optional data
    // of the writeObject method.
    protected void beginOptionalCustomData() {

        if (streamFormatVersion == 2) {
                
            org.omg.CORBA.portable.ValueOutputStream vout
                = (org.omg.CORBA.portable.ValueOutputStream)orbStream;
                
            vout.start_value(currentClassDesc.getRMIIIOPOptionalDataRepId());
        }
    }

    public final void setOrbStream(org.omg.CORBA_2_3.portable.OutputStream os) {
    	orbStream = os;
    }

    public final org.omg.CORBA_2_3.portable.OutputStream getOrbStream() {
    	return orbStream;
    }

    public final void increaseRecursionDepth(){
	recursionDepth++;
    }

    public final int decreaseRecursionDepth(){
	return --recursionDepth;
    }

    /**
     * Override the actions of the final method "writeObject()"
     * in ObjectOutputStream.
     * @since     JDK1.1.6
     */
    public final void writeObjectOverride(Object obj)
	throws IOException
    {
        writeObjectState.writeData(this);

	Util.getInstance().writeAbstractObject((OutputStream)orbStream, obj);
    }

    /**
     * Override the actions of the final method "writeObject()"
     * in ObjectOutputStream.
     * @since     JDK1.1.6
     */
    public final void simpleWriteObject(Object obj, byte formatVersion)
    /* throws IOException */
    {
        byte oldStreamFormatVersion = streamFormatVersion;

        streamFormatVersion = formatVersion;

    	Object prevObject = currentObject;
    	ObjectStreamClass prevClassDesc = currentClassDesc;
    	simpleWriteDepth++;

    	try {
	    // if (!checkSpecialClasses(obj) && !checkSubstitutableSpecialClasses(obj))
	    outputObject(obj);

    	} catch (IOException ee) {
    	    if (abortIOException == null)
		abortIOException = ee;
    	} finally {
    	    /* Restore state of previous call incase this is a nested call */
            streamFormatVersion = oldStreamFormatVersion;
    	    simpleWriteDepth--;
    	    currentObject = prevObject;
    	    currentClassDesc = prevClassDesc;
    	}

    	/* If the recursion depth is 0, test for and clear the pending exception.
    	 * If there is a pending exception throw it.
    	 */
    	IOException pending = abortIOException;
    	if (simpleWriteDepth == 0)
    	    abortIOException = null;
    	if (pending != null) {
	    bridge.throwException( pending ) ;
    	}
    }

    // Required by the superclass.
    ObjectStreamField[] getFieldsNoCopy() {
        return currentClassDesc.getFieldsNoCopy();
    }

    /**
     * Override the actions of the final method "defaultWriteObject()"
     * in ObjectOutputStream.
     * @since     JDK1.1.6
     */
    public final void defaultWriteObjectDelegate()
    /* throws IOException */
    {
        try {
	    if (currentObject == null || currentClassDesc == null)
		// XXX I18N, Logging needed.
		throw new NotActiveException("defaultWriteObjectDelegate");

	    ObjectStreamField[] fields =
		currentClassDesc.getFieldsNoCopy();
	    if (fields.length > 0) {
		outputClassFields(currentObject, currentClassDesc.forClass(),
				  fields);
	    }
        } catch(IOException ioe) {
	    bridge.throwException(ioe);
	}
    }

    /**
     * Override the actions of the final method "enableReplaceObject()"
     * in ObjectOutputStream.
     * @since     JDK1.1.6
     */
    public final boolean enableReplaceObjectDelegate(boolean enable)
    /* throws SecurityException */
    {
        return false;
		
    }


    protected final void annotateClass(Class<?> cl) throws IOException{
	// XXX I18N, Logging needed.
        throw new IOException("Method annotateClass not supported");
    }

    public final void close() throws IOException{
        // no op
    }

    protected final void drain() throws IOException{
        // no op
    }

    public final void flush() throws IOException{
        try{
            orbStream.flush();
        } catch(Error e) {
	    IOException ioexc = new IOException(e.getMessage());
	    ioexc.initCause(e) ;
	    throw ioexc ;
	}
    }

    protected final Object replaceObject(Object obj) throws IOException{
	// XXX I18N, Logging needed.
        throw new IOException("Method replaceObject not supported");
    }

    /**
     * Reset will disregard the state of any objects already written
     * to the stream.  The state is reset to be the same as a new
     * ObjectOutputStream.  The current point in the stream is marked
     * as reset so the corresponding ObjectInputStream will be reset
     * at the same point.  Objects previously written to the stream
     * will not be refered to as already being in the stream.  They
     * will be written to the stream again.
     * @since     JDK1.1
     */
    public final void reset() throws IOException{
        try{
            //orbStream.reset();

	    if (currentObject != null || currentClassDesc != null)
		// XXX I18N, Logging needed.
		throw new IOException("Illegal call to reset");

	    abortIOException = null;

	    if (classDescStack == null)
		classDescStack = new Stack<ObjectStreamClass>();
	    else
		classDescStack.setSize(0);

        } catch(Error e) {
	    IOException ioexc = new IOException(e.getMessage());
	    ioexc.initCause(e) ;
	    throw ioexc ;
	}
    }

    public final void write(byte b[]) throws IOException{
        try{
            writeObjectState.writeData(this);

            orbStream.write_octet_array(b, 0, b.length);
        } catch(Error e) {
	    IOException ioexc = new IOException(e.getMessage());
	    ioexc.initCause(e) ;
	    throw ioexc ;
	}
    }

    public final void write(byte b[], int off, int len) throws IOException{
        try{
            writeObjectState.writeData(this);

            orbStream.write_octet_array(b, off, len);
        } catch(Error e) {
	    IOException ioexc = new IOException(e.getMessage());
	    ioexc.initCause(e) ;
	    throw ioexc ;
	}
    }

    public final void write(int data) throws IOException{
        try{
            writeObjectState.writeData(this);

            orbStream.write_octet((byte)(data & 0xFF));
        } catch(Error e) {
	    IOException ioexc = new IOException(e.getMessage());
	    ioexc.initCause(e) ;
	    throw ioexc ;
	}
    }

    public final void writeBoolean(boolean data) throws IOException{
        try{
            writeObjectState.writeData(this);

            orbStream.write_boolean(data);
        } catch(Error e) {
	    IOException ioexc = new IOException(e.getMessage());
	    ioexc.initCause(e) ;
	    throw ioexc ;
	}
    }

    public final void writeByte(int data) throws IOException{
        try{
            writeObjectState.writeData(this);

            orbStream.write_octet((byte)data);
        } catch(Error e) {
	    IOException ioexc = new IOException(e.getMessage());
	    ioexc.initCause(e) ;
	    throw ioexc ;
	}
    }

    public final void writeBytes(String data) throws IOException{
        try{
            writeObjectState.writeData(this);

            byte buf[] = data.getBytes();
            orbStream.write_octet_array(buf, 0, buf.length);
        } catch(Error e) {
	    IOException ioexc = new IOException(e.getMessage());
	    ioexc.initCause(e) ;
	    throw ioexc ;
	}
    }

    public final void writeChar(int data) throws IOException{
        try{
            writeObjectState.writeData(this);

            orbStream.write_wchar((char)data);
        } catch(Error e) {
	    IOException ioexc = new IOException(e.getMessage());
	    ioexc.initCause(e) ;
	    throw ioexc ;
	}
    }

    public final void writeChars(String data) throws IOException{
        try{
            writeObjectState.writeData(this);

            char buf[] = data.toCharArray();
            orbStream.write_wchar_array(buf, 0, buf.length);
        } catch(Error e) {
	    IOException ioexc = new IOException(e.getMessage());
	    ioexc.initCause(e) ;
	    throw ioexc ;
	}
    }

    public final void writeDouble(double data) throws IOException{
        try{
            writeObjectState.writeData(this);

            orbStream.write_double(data);
        } catch(Error e) {
	    IOException ioexc = new IOException(e.getMessage());
	    ioexc.initCause(e) ;
	    throw ioexc ;
	}
    }

    public final void writeFloat(float data) throws IOException{
        try{
            writeObjectState.writeData(this);

            orbStream.write_float(data);
        } catch(Error e) {
	    IOException ioexc = new IOException(e.getMessage());
	    ioexc.initCause(e) ;
	    throw ioexc ;
	}
    }

    public final void writeInt(int data) throws IOException{
        try{
            writeObjectState.writeData(this);

            orbStream.write_long(data);
        } catch(Error e) {
	    IOException ioexc = new IOException(e.getMessage());
	    ioexc.initCause(e) ;
	    throw ioexc ;
	}
    }

    public final void writeLong(long data) throws IOException{
        try{
            writeObjectState.writeData(this);

            orbStream.write_longlong(data);
        } catch(Error e) {
	    IOException ioexc = new IOException(e.getMessage());
	    ioexc.initCause(e) ;
	    throw ioexc ;
	}
    }

    public final void writeShort(int data) throws IOException{
        try{
            writeObjectState.writeData(this);

            orbStream.write_short((short)data);
        } catch(Error e) {
	    IOException ioexc = new IOException(e.getMessage());
	    ioexc.initCause(e) ;
	    throw ioexc ;
	}
    }

    protected final void writeStreamHeader() throws IOException{
        // no op
    }

    /**
     * Helper method for correcting the Kestrel bug 4367783 (dealing
     * with larger than 8-bit chars).  The old behavior is preserved
     * in orbutil.IIOPInputStream_1_3 in order to interoperate with
     * our legacy ORBs.
     */
    protected void internalWriteUTF(org.omg.CORBA.portable.OutputStream stream,
                                    String data) 
    {
        stream.write_wstring(data);
    }

    public final void writeUTF(String data) throws IOException{
        try{
            writeObjectState.writeData(this);

            internalWriteUTF(orbStream, data);
        } catch(Error e) {
	    IOException ioexc = new IOException(e.getMessage());
	    ioexc.initCause(e) ;
	    throw ioexc ;
	}
    }

    // INTERNAL UTILITY METHODS
    /*
     * Check for special cases of serializing objects.
     * These objects are not subject to replacement.
     */
    private boolean checkSpecialClasses(Object obj) throws IOException {

    	/*
    	 * If this is a class, don't allow substitution
    	 */
    	//if (obj instanceof Class) {
        //    throw new IOException("Serialization of Class not supported");
    	//}

    	if (obj instanceof ObjectStreamClass) {
	    // XXX I18N, Logging needed.
            throw new IOException("Serialization of ObjectStreamClass not supported");
    	}

    	return false;
    }

    /*
     * Check for special cases of substitutable serializing objects.
     * These classes are replaceable.
     */
    private boolean checkSubstitutableSpecialClasses(Object obj)
	throws IOException
    {
    	if (obj instanceof String) {
    	    orbStream.write_value((java.io.Serializable)obj);
    	    return true;
    	}

    	return false;
    }

    /*
     * Write out the object
     */
    private void outputObject(final Object obj) throws IOException{

    	currentObject = obj;
    	Class currclass = obj.getClass();

    	/* Get the Class descriptor for this class,
    	 * Throw a NotSerializableException if there is none.
    	 */
    	currentClassDesc = ObjectStreamClass.lookup(currclass);
    	if (currentClassDesc == null) {
	    // XXX I18N, Logging needed.
    	    throw new NotSerializableException(currclass.getName());
    	}

    	/* If the object is externalizable,
    	 * call writeExternal.
    	 * else do Serializable processing.
    	 */
    	if (currentClassDesc.isExternalizable()) {
	    // Write format version
	    orbStream.write_octet(streamFormatVersion);

    	    Externalizable ext = (Externalizable)obj;
    	    ext.writeExternal(this);
            
    	} else {

    	    /* The object's classes should be processed from supertype to subtype
    	     * Push all the clases of the current object onto a stack.
    	     * Remember the stack pointer where this set of classes is being pushed.
    	     */
    	    int stackMark = classDescStack.size();
    	    try {
    		ObjectStreamClass next;
    		while ((next = currentClassDesc.getSuperclass()) != null) {
    		    classDescStack.push(currentClassDesc);
    		    currentClassDesc = next;
    		}

    		/*
    		 * For currentClassDesc and all the pushed class descriptors
    		 *    If the class is writing its own data
    		 *		  set blockData = true; call the class writeObject method
    		 *    If not
    		 *     invoke either the defaultWriteObject method.
    		 */
    		do {

                    WriteObjectState oldState = writeObjectState;

                    try {

                        setState(NOT_IN_WRITE_OBJECT);

                        if (currentClassDesc.hasWriteObject()) {
                            invokeObjectWriter(currentClassDesc, obj );
                        } else {
                            defaultWriteObjectDelegate();
                        }
                    } finally {
                        setState(oldState);
                    }

    		} while (classDescStack.size() > stackMark && 
		    (currentClassDesc = classDescStack.pop()) != null);
    	    } finally {
		classDescStack.setSize(stackMark);
    	    }
    	}
    }

    /*
     * Invoke writer.
     * _REVISIT_ invokeObjectWriter and invokeObjectReader behave inconsistently with each other since
     * the reader returns a boolean...fix later
     */
    private void invokeObjectWriter(ObjectStreamClass osc, Object obj)
	throws IOException
    {
	Class c = osc.forClass() ;

    	try {

	    // Write format version
            orbStream.write_octet(streamFormatVersion);

            writeObjectState.enterWriteObject(this);

	    // writeObject(obj, c, this);
	    osc.writeObjectMethod.invoke( obj, this ) ;

            writeObjectState.exitWriteObject(this);

    	} catch (InvocationTargetException e) {
    	    Throwable t = e.getTargetException();
    	    if (t instanceof IOException)
    		throw (IOException)t;
    	    else if (t instanceof RuntimeException)
    		throw (RuntimeException) t;
    	    else if (t instanceof Error)
    		throw (Error) t;
    	    else
		// XXX I18N, Logging needed.
    		throw new Error("invokeObjectWriter internal error",e);
    	} catch (IllegalAccessException e) {
    	    // cannot happen
    	}
    }

    // This is needed for the OutputStreamHook interface.
    void writeField(ObjectStreamField field, Object value) throws IOException {
        switch (field.getTypeCode()) {
            case 'B':
                if (value == null)
                    orbStream.write_octet((byte)0);
                else
                    orbStream.write_octet(((Byte)value).byteValue());
		break;
	    case 'C':
                if (value == null)
                    orbStream.write_wchar((char)0);
                else
                    orbStream.write_wchar(((Character)value).charValue());
		break;
	    case 'F':
                if (value == null)
                    orbStream.write_float((float)0);
                else
                    orbStream.write_float(((Float)value).floatValue());
		break;
            case 'D':
                if (value == null)
                    orbStream.write_double((double)0);
                else
                    orbStream.write_double(((Double)value).doubleValue());
		break;
	    case 'I':
                if (value == null)
                    orbStream.write_long((int)0);
                else
                    orbStream.write_long(((Integer)value).intValue());
		break;
	    case 'J':
                if (value == null)
                    orbStream.write_longlong((long)0);
                else
                    orbStream.write_longlong(((Long)value).longValue());
		break;
	    case 'S':
                if (value == null)
                    orbStream.write_short((short)0);
                else
                    orbStream.write_short(((Short)value).shortValue());
		break;
	    case 'Z':
                if (value == null)
                    orbStream.write_boolean(false);
                else
                    orbStream.write_boolean(((Boolean)value).booleanValue());
		break;
	    case '[':
	    case 'L':
                // What to do if it's null?
                writeObjectField(field, value);
		break;
	    default:
		// XXX I18N, Logging needed.
		throw new InvalidClassException(currentClassDesc.getName());
	    }
    }

    private void writeObjectField(ObjectStreamField field,
                                  Object objectValue) throws IOException {

        if (ObjectStreamClassCorbaExt.isAny(field.getTypeString())) {
            Util.getInstance().writeAny(orbStream, objectValue);
        }
        else {
            Class type = field.getType();
            int callType = ValueHandlerImpl.kValueType;
	    ClassInfoCache.ClassInfo cinfo = field.getClassInfo() ;

            if (cinfo.isInterface()) { 
                String className = type.getName();
                
		if (cinfo.isARemote(type)) {
                    // RMI Object reference...
                    callType = ValueHandlerImpl.kRemoteType;
		} else if (cinfo.isACORBAObject(type)) {
                    // IDL Object reference...
                    callType = ValueHandlerImpl.kRemoteType;
                } else if (RepositoryId.isAbstractBase(type)) {
                    // IDL Abstract Object reference...
                    callType = ValueHandlerImpl.kAbstractType;
                } else if (ObjectStreamClassCorbaExt.isAbstractInterface(type)) {
                    callType = ValueHandlerImpl.kAbstractType;
                }
            }
					
            switch (callType) {
            case ValueHandlerImpl.kRemoteType: 
                Util.getInstance().writeRemoteObject(orbStream, objectValue);
                break;
            case ValueHandlerImpl.kAbstractType: 
                Util.getInstance().writeAbstractObject(orbStream, objectValue);
                break;
            case ValueHandlerImpl.kValueType:
                try{
                    orbStream.write_value((java.io.Serializable)objectValue, 
			type);
                }
                catch(ClassCastException cce){
                    if (objectValue instanceof java.io.Serializable)
                        throw cce;
                    else
                        Utility.throwNotSerializableForCorba(
			    objectValue.getClass().getName());
                }
            }
        }
    }

    /* Write the fields of the specified class by invoking the appropriate
     * write* method on this class.
     */
    private void outputClassFields(Object o, Class cl,
				   ObjectStreamField[] fields)
	throws IOException, InvalidClassException {

	// replace this all with
    	// for (int i = 0; i < fields.length; i++) {
	//     fields[i].write( o, orbStream ) ;
	// Should we just put this into ObjectStreamClass?
	// Could also unroll and codegen this.

    	for (int i = 0; i < fields.length; i++) {
	    ObjectStreamField field = fields[i] ;
	    final long offset = field.getFieldID() ;
    	    if (offset == Bridge.INVALID_FIELD_OFFSET)
		// XXX I18N, Logging needed.  This should not happend.
    		throw new InvalidClassException(cl.getName(), 
		    "Nonexistent field " + fields[i].getName());
	    switch (field.getTypeCode()) {
		case 'B':
		    byte byteValue = bridge.getByte( o, offset ) ;
		    orbStream.write_octet(byteValue);
		    break;
		case 'C':
		    char charValue = bridge.getChar( o, offset ) ;
		    orbStream.write_wchar(charValue);
		    break;
		case 'F':
		    float floatValue = bridge.getFloat( o, offset ) ;
		    orbStream.write_float(floatValue);
		    break;
		case 'D' :
		    double doubleValue = bridge.getDouble( o, offset ) ;
		    orbStream.write_double(doubleValue);
		    break;
		case 'I':
		    int intValue = bridge.getInt( o, offset ) ;
		    orbStream.write_long(intValue);
		    break;
		case 'J':
		    long longValue = bridge.getLong( o, offset ) ;
		    orbStream.write_longlong(longValue);
		    break;
		case 'S':
		    short shortValue = bridge.getShort( o, offset ) ;
		    orbStream.write_short(shortValue);
		    break;
		case 'Z':
		    boolean booleanValue = bridge.getBoolean( o, offset ) ;
		    orbStream.write_boolean(booleanValue);
		    break;
		case '[':
		case 'L':
		    Object objectValue = bridge.getObject( o, offset ) ;
		    writeObjectField(fields[i], objectValue);
		    break;
		default:
		    // XXX I18N, Logging needed.
		    throw new InvalidClassException(cl.getName());
	    }
    	}
    }
}

