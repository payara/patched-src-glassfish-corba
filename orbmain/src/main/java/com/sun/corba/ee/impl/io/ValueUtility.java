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
/*
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

package com.sun.corba.ee.impl.io;

import com.sun.org.omg.CORBA.ValueDefPackage.FullValueDescription;
import com.sun.org.omg.CORBA.OperationDescription;
import com.sun.org.omg.CORBA.AttributeDescription;
import com.sun.org.omg.CORBA.Initializer;
import com.sun.org.omg.CORBA._IDLTypeStub;

import com.sun.corba.ee.impl.util.RepositoryId;
import org.omg.CORBA.ValueMember;
import org.omg.CORBA.ORB;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.TCKind;

import javax.rmi.CORBA.ValueHandler;

import com.sun.corba.ee.impl.misc.ClassInfoCache ;
import java.lang.reflect.Modifier;
import java.util.Stack;
import org.glassfish.pfl.basic.contain.Pair;

/**
 * Holds utility methods for converting from ObjectStreamClass to
 * FullValueDescription and generating typecodes from ObjectStreamClass.
 **/
public class ValueUtility {

    public static final short PRIVATE_MEMBER = 0;
    public static final short PUBLIC_MEMBER = 1;
        
    private static final String primitiveConstants[] = {
        null,       // tk_null         0  
        null,           // tk_void         1
        "S",            // tk_short        2
        "I",            // tk_long         3
        "S",            // tk_ushort       4
        "I",            // tk_ulong        5
        "F",            // tk_float        6
        "D",            // tk_double       7
        "Z",            // tk_boolean      8
        "C",            // tk_char         9
        "B",            // tk_octet        10
        null,           // tk_any          11
        null,           // tk_typecode     12
        null,           // tk_principal    13
        null,           // tk_objref       14
        null,           // tk_struct       15
        null,           // tk_union        16
        null,           // tk_enum         17
        null,           // tk_string       18
        null,           // tk_sequence     19
        null,           // tk_array        20
        null,           // tk_alias        21
        null,           // tk_except       22
        "J",            // tk_longlong     23
        "J",            // tk_ulonglong    24
        "D",            // tk_longdouble   25
        "C",            // tk_wchar        26
        null,           // tk_wstring      27
        null,       // tk_fixed        28
        null,       // tk_value        29 
        null,       // tk_value_box    30
        null,       // tk_native       31
        null,       // tk_abstract_interface 32
    };

    public static String getSignature(ValueMember member)
        throws ClassNotFoundException {

        // REVISIT.  Can the type be something that is
        // non-primitive yet not a value_box, value, or objref?
        // If so, should use ObjectStreamClass or throw
        // exception.

        if (member.type.kind().value() == TCKind._tk_value_box ||
            member.type.kind().value() == TCKind._tk_value ||
            member.type.kind().value() == TCKind._tk_objref) {
            Class c = RepositoryId.cache.getId(member.id).getClassFromType();
            return ObjectStreamClass.getSignature(c);

        } else {

            return primitiveConstants[member.type.kind().value()];
        }
                
    }

    public static FullValueDescription translate(ORB orb, ObjectStreamClass osc, 
        ValueHandler vh){
                
        // Create FullValueDescription
        FullValueDescription result = new FullValueDescription();
        Class className = osc.forClass();

        ValueHandlerImpl vhandler = (com.sun.corba.ee.impl.io.ValueHandlerImpl) vh;
        String repId = vhandler.createForAnyType(className);

        // Set FVD name
        result.name = vhandler.getUnqualifiedName(repId);
        if (result.name == null)
            result.name = "";

        // Set FVD id _REVISIT_ : Manglings
        result.id = vhandler.getRMIRepositoryID(className);
        if (result.id == null)
            result.id = "";

        // Set FVD is_abstract
        result.is_abstract = 
            ObjectStreamClassCorbaExt.isAbstractInterface(className);
                
        // Set FVD is_custom
        result.is_custom = osc.hasWriteObject() || osc.isExternalizable();

        // Set FVD defined_in _REVISIT_ : Manglings
        result.defined_in = vhandler.getDefinedInId(repId);
        if (result.defined_in == null)
            result.defined_in = "";

        // Set FVD version 
        result.version = vhandler.getSerialVersionUID(repId);
        if (result.version == null)
            result.version = "";

        // Skip FVD operations - N/A
        result.operations = new OperationDescription[0];

        // Skip FVD attributed - N/A
        result.attributes = new AttributeDescription[0];

        // Set FVD members
        // Maps classes to repositoryIDs strings. 
        // This is used to detect recursive types.
        IdentityKeyValueStack createdIDs = new IdentityKeyValueStack();
        
        // Stores all types created for resolving indirect types at the end. 
        result.members = translateMembers(orb, osc, vh, createdIDs);
                
        // Skip FVD initializers - N/A
        result.initializers = new Initializer[0];
                
        Class interfaces[] = osc.forClass().getInterfaces();
        int abstractCount = 0;

        // Skip FVD supported_interfaces
        result.supported_interfaces =  new String[interfaces.length];
        for (int interfaceIndex = 0; interfaceIndex < interfaces.length;
             interfaceIndex++) {
            result.supported_interfaces[interfaceIndex] =
                vhandler.createForAnyType(interfaces[interfaceIndex]);
                        
            ClassInfoCache.ClassInfo cinfo = ClassInfoCache.get(
                interfaces[interfaceIndex] ) ;
            if (!cinfo.isARemote(interfaces[interfaceIndex]) ||
                (!Modifier.isPublic(interfaces[interfaceIndex].getModifiers())))
                abstractCount++;
        }
                
        // Skip FVD abstract_base_values - N/A
        result.abstract_base_values = new String[abstractCount];
        for (int interfaceIndex = 0; interfaceIndex < interfaces.length;
            interfaceIndex++) {
            ClassInfoCache.ClassInfo cinfo = ClassInfoCache.get(
                interfaces[interfaceIndex] ) ;
            if (!cinfo.isARemote(interfaces[interfaceIndex]) ||
                (!Modifier.isPublic(interfaces[interfaceIndex].getModifiers())))
                result.abstract_base_values[interfaceIndex] =
                    vhandler.createForAnyType(interfaces[interfaceIndex]);
                
        }
                
        result.is_truncatable = false;
                
        // Set FVD base_value
        Class superClass = osc.forClass().getSuperclass();
        if (ClassInfoCache.get( superClass ).isASerializable(superClass))
            result.base_value = vhandler.getRMIRepositoryID(superClass);
        else 
            result.base_value = "";
                
        // Set FVD type
        //result.type = createTypeCodeForClass(orb, osc.forClass());
        result.type = orb.get_primitive_tc(TCKind.tk_value); 

        return result;
    }

    private static ValueMember[] translateMembers( ORB orb, 
        ObjectStreamClass osc, ValueHandler vh, 
        IdentityKeyValueStack createdIDs) {

        ValueHandlerImpl vhandler = (com.sun.corba.ee.impl.io.ValueHandlerImpl) vh;
        ObjectStreamField fields[] = osc.getFields();
        int fieldsLength = fields.length;
        ValueMember[] members = new ValueMember[fieldsLength];
        // Note : fields come out of ObjectStreamClass in correct order for
        // writing.  So, we will create the same order in the members array.
        for (int i = 0; i < fieldsLength; i++) {
            String valRepId = vhandler.getRMIRepositoryID(fields[i].getClazz());
            members[i] = new ValueMember();
            members[i].name = fields[i].getName();
            
            // _REVISIT_ : Manglings
            members[i].id = valRepId; 

            // _REVISIT_ : Manglings
            members[i].defined_in = vhandler.getDefinedInId(valRepId);

            members[i].version = "1.0";

            // _REVISIT_ : IDLType implementation missing 
            members[i].type_def = new _IDLTypeStub(); 

            if (fields[i].getField() == null) {
                // When using serialPersistentFields, the class may
                // no longer have an actual Field that corresponds
                // to one of the items.  The Java to IDL spec
                // ptc-00-01-06 1.3.5.6 says that the IDL field
                // should be private in this case.
                members[i].access = PRIVATE_MEMBER;
            } else {
                int m = fields[i].getField().getModifiers();
                if (Modifier.isPublic(m))
                    members[i].access = PUBLIC_MEMBER;
                else
                    members[i].access = PRIVATE_MEMBER;
            }

            switch (fields[i].getTypeCode()) {
            case 'B':
                members[i].type = orb.get_primitive_tc(TCKind.tk_octet); 
                break;
            case 'C':
                members[i].type 
                    = orb.get_primitive_tc(vhandler.getJavaCharTCKind()); 
                break;
            case 'F':
                members[i].type = orb.get_primitive_tc(TCKind.tk_float); 
                break;
            case 'D' :
                members[i].type = orb.get_primitive_tc(TCKind.tk_double); 
                break;
            case 'I':
                members[i].type = orb.get_primitive_tc(TCKind.tk_long); 
                break;
            case 'J':
                members[i].type = orb.get_primitive_tc(TCKind.tk_longlong); 
                break;
            case 'S':
                members[i].type = orb.get_primitive_tc(TCKind.tk_short); 
                break;
            case 'Z':
                members[i].type = orb.get_primitive_tc(TCKind.tk_boolean); 
                break;
        // case '[':
        //      members[i].type = orb.get_primitive_tc(TCKind.tk_value_box); 
        //      members[i].id = RepositoryId.createForAnyType(fields[i].getType());
        //      break;
            default:
                members[i].type = createTypeCodeForClassInternal(orb, 
                    fields[i].getClazz(), vhandler, createdIDs);
                members[i].id = vhandler.createForAnyType(fields[i].getType());
                break;
            } // end switch

        } // end for loop

        return members;
    }

    private static boolean exists(String str, String strs[]){
        for (int i = 0; i < strs.length; i++)
            if (str.equals(strs[i]))
                return true;
                
        return false;
    }

    public static boolean isAssignableFrom(String clzRepositoryId, 
        FullValueDescription type, com.sun.org.omg.SendingContext.CodeBase sender){
                
        if (exists(clzRepositoryId, type.supported_interfaces))
            return true;

        if (clzRepositoryId.equals(type.id))
            return true;
                
        if ((type.base_value != null) &&
            (!type.base_value.equals(""))) {
            FullValueDescription parent = sender.meta(type.base_value);
                        
            return isAssignableFrom(clzRepositoryId, parent, sender);
        }

        return false;
    }

    public static TypeCode createTypeCodeForClass( ORB orb, java.lang.Class c, 
        ValueHandler vh) {
        
        // Maps classes to repositoryIDs strings. 
        // This is used to detect recursive types.
        IdentityKeyValueStack createdIDs = new IdentityKeyValueStack();
        
        // Stores all types created for resolving indirect types at the end. 
        TypeCode tc = createTypeCodeForClassInternal(orb, c, vh, createdIDs);

        return tc;
    }

    private static TypeCode createTypeCodeForClassInternal( ORB orb, 
        java.lang.Class c, ValueHandler vh, IdentityKeyValueStack createdIDs) {

        // This wrapper method is the protection against infinite recursion.
        TypeCode tc = null;
        String id = createdIDs.get(c);
        if (id != null) {
            return orb.create_recursive_tc(id);
        } else {
            id = vh.getRMIRepositoryID(c);
            if (id == null) id = "";
            // cache the rep id BEFORE creating a new typecode.
            // so that recursive tc can look up the rep id.
            createdIDs.push(c, id);
            tc = createTypeCodeInternal(orb, c, vh, id, createdIDs);
            createdIDs.pop();
            return tc;
        }
    }

    // Maintains a stack of key-value pairs. Compares elements using == operator.
    private static class IdentityKeyValueStack {
        Stack<Pair<Class<?>,String>> pairs = null ;

        String get(Class<?> key) {
            if (pairs == null) {
                return null;
            }

            for (Pair<Class<?>,String> pair : pairs ) {
                if (pair.first() == key) {
                    return pair.second();
                }
            }

            return null;
        }

        void push(Class key, String value) {
            if (pairs == null) {
                pairs = new Stack<Pair<Class<?>,String>>();
            }
            pairs.push(new Pair<Class<?>,String>(key, value));
        }

        void pop() {
            pairs.pop();
        }
    }

    private static TypeCode createTypeCodeInternal (ORB orb, java.lang.Class c, 
        ValueHandler vh, String id, IdentityKeyValueStack createdIDs) {

        ClassInfoCache.ClassInfo cinfo = ClassInfoCache.get( c ) ;
        if (cinfo.isArray()) {
            // Arrays - may recurse for multi-dimensional arrays
            Class componentClass = c.getComponentType();
            TypeCode embeddedType;
            if (componentClass.isPrimitive()) {
                embeddedType = ValueUtility.getPrimitiveTypeCodeForClass( orb, 
                    componentClass, vh);
            } else {
                embeddedType = createTypeCodeForClassInternal(orb, 
                    componentClass, vh, createdIDs);
            }

            TypeCode t = orb.create_sequence_tc (0, embeddedType);
            return orb.create_value_box_tc (id, "Sequence", t);
        } else if ( c == java.lang.String.class ) {
            // Strings
            TypeCode t = orb.create_string_tc (0);
            return orb.create_value_box_tc (id, "StringValue", t);
        } else if (cinfo.isARemote(c)) {
            return orb.get_primitive_tc(TCKind.tk_objref);
        } else if (cinfo.isACORBAObject(c)) {
            return orb.get_primitive_tc(TCKind.tk_objref);
        } 
                
        // Anything else

        ObjectStreamClass osc = ObjectStreamClass.lookup(c);

        if (osc == null) {
            return orb.create_value_box_tc (id, "Value", 
                orb.get_primitive_tc (TCKind.tk_value));
        }

        // type modifier
        // REVISIT truncatable and abstract?
        short modifier = (osc.isCustomMarshaled() ? 
            org.omg.CORBA.VM_CUSTOM.value : org.omg.CORBA.VM_NONE.value);

        // concrete base
        TypeCode base = null;
        Class superClass = c.getSuperclass();
        if (superClass != null && 
            ClassInfoCache.get( superClass ).isASerializable( superClass )) {
            base = createTypeCodeForClassInternal(orb, superClass, vh, 
                createdIDs);
        }

        // members
        ValueMember[] members = translateMembers (orb, osc, vh, createdIDs);

        return orb.create_value_tc(id, c.getName(), modifier, base, members);
    }

    public static TypeCode getPrimitiveTypeCodeForClass (ORB orb, 
        Class c, ValueHandler vh) {
                
        if (c == Integer.TYPE) {
            return orb.get_primitive_tc(TCKind.tk_long);
        } else if (c == Byte.TYPE) {
            return orb.get_primitive_tc(TCKind.tk_octet);
        } else if (c == Long.TYPE) {
            return orb.get_primitive_tc(TCKind.tk_longlong);
        } else if (c == Float.TYPE) {
            return orb.get_primitive_tc(TCKind.tk_float);
        } else if (c == Double.TYPE) {
            return orb.get_primitive_tc(TCKind.tk_double);
        } else if (c == Short.TYPE) {
            return orb.get_primitive_tc (TCKind.tk_short);
        } else if (c == Character.TYPE) {
            return orb.get_primitive_tc(
                ((ValueHandlerImpl)vh).getJavaCharTCKind());
        } else if (c == Boolean.TYPE) {
            return orb.get_primitive_tc (TCKind.tk_boolean);
        } else {
            // _REVISIT_ Not sure if this is right.
            return orb.get_primitive_tc (TCKind.tk_any);
        }
    }
}