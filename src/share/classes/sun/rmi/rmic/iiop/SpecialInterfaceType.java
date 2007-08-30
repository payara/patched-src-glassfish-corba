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

package sun.rmi.rmic.iiop;

import sun.tools.java.ClassNotFound;
import sun.tools.java.CompilerError;
import sun.tools.java.Identifier;
import sun.tools.java.ClassDeclaration;
import sun.tools.java.ClassDefinition;

/**
 * SpecialInterfaceType represents any one of the following types:
 * <pre>
 *    java.rmi.Remote
 *    java.io.Serializable
 *    java.io.Externalizable
 *    org.omg.CORBA.Object
 *    org.omg.CORBA.portable.IDLEntity
 * </pre>
 * all of which are treated as special cases. For all but CORBA.Object,
 * the type must match exactly. For CORBA.Object, the type must either be
 * CORBA.Object or inherit from it.
 * <p>
 * The static forSpecial(...) method must be used to obtain an instance, and
 * will return null if the type is non-conforming.
 *
 * @version 1.0, 2/27/98
 * @author  Bryan Atsatt
 */
public class SpecialInterfaceType extends InterfaceType {

    //_____________________________________________________________________
    // Public Interfaces
    //_____________________________________________________________________

    /**
     * Create a SpecialInterfaceType object for the given class.
     *
     * If the class is not a properly formed or if some other error occurs, the
     * return value will be null, and errors will have been reported to the
     * supplied BatchEnvironment.
     */
    public static SpecialInterfaceType forSpecial ( ClassDefinition theClass,
                                                    ContextStack stack) {

        if (stack.anyErrors()) return null;
        
	// Do we already have it?
        
        sun.tools.java.Type type = theClass.getType();
        Type existing = getType(type,stack);
        
        if (existing != null) {
          
	    if (!(existing instanceof SpecialInterfaceType)) return null; // False hit.
          
	    // Yep, so return it...
          
	    return (SpecialInterfaceType) existing;
        }
  
        // Is it special?
  
        if (isSpecial(type,theClass,stack)) {
  
            // Yes...
            
            SpecialInterfaceType result = new SpecialInterfaceType(stack,0,theClass);
            putType(type,result,stack);
            stack.push(result);
        
            if (result.initialize(type,stack)) {
                stack.pop(true);
                return result;
            } else {
                removeType(type,stack);
                stack.pop(false);
                return null;
            }
        }
        return null;
    }

    /**
     * Return a string describing this type.
     */
    public String getTypeDescription () {
	return "Special interface";
    }

    //_____________________________________________________________________
    // Subclass/Internal Interfaces
    //_____________________________________________________________________

    /**
     * Create an SpecialInterfaceType instance for the given class.
     */
    private SpecialInterfaceType(ContextStack stack, int typeCode,
				 ClassDefinition theClass) {
        super(stack,typeCode | TM_SPECIAL_INTERFACE | TM_INTERFACE | TM_COMPOUND, theClass);
        setNames(theClass.getName(),null,null); // Fixed in initialize.
    }

    private static boolean isSpecial(sun.tools.java.Type type,
                                     ClassDefinition theClass,
                                     ContextStack stack) {
        if (type.isType(TC_CLASS)) {
            Identifier id = type.getClassName();

            if (id.equals(idRemote)) return true;
            if (id == idJavaIoSerializable) return true;
            if (id == idJavaIoExternalizable) return true;
	    if (id == idCorbaObject) return true;
	    if (id == idIDLEntity) return true;
            BatchEnvironment env = stack.getEnv();
    	    try {
    	        if (env.defCorbaObject.implementedBy(env,theClass.getClassDeclaration())) return true;
	    } catch (ClassNotFound e) {
		classNotFound(stack,e);
	    }            
        }
        return false;
    }

    private boolean initialize(sun.tools.java.Type type, ContextStack stack) {

        int typeCode = TYPE_NONE;
        Identifier id = null;
        String idlName = null;
        String[] idlModuleName = null;
        boolean constant = stack.size() > 0 && stack.getContext().isConstant();

        if (type.isType(TC_CLASS)) {
            id = type.getClassName();

            if (id.equals(idRemote)) {
                typeCode = TYPE_JAVA_RMI_REMOTE;
                idlName = IDL_JAVA_RMI_REMOTE;
                idlModuleName = IDL_JAVA_RMI_MODULE;
            } else if (id == idJavaIoSerializable) {
                typeCode = TYPE_ANY;
                idlName = IDL_SERIALIZABLE;
                idlModuleName = IDL_JAVA_IO_MODULE;
            } else if (id == idJavaIoExternalizable) {
                typeCode = TYPE_ANY;
                idlName = IDL_EXTERNALIZABLE;
                idlModuleName = IDL_JAVA_IO_MODULE;
            } else if (id == idIDLEntity) {
                typeCode = TYPE_ANY;
                idlName = IDL_IDLENTITY;
                idlModuleName = IDL_ORG_OMG_CORBA_PORTABLE_MODULE;
            } else {
              
		typeCode = TYPE_CORBA_OBJECT;
    		    
		// Is it exactly org.omg.CORBA.Object?
    		    
		if (id == idCorbaObject) {
    		        
		    // Yes, so special case...
    		        
		    idlName = IDLNames.getTypeName(typeCode,constant);
		    idlModuleName = null;
    		    
		} else {
    		        
		    // No, so get the correct names...
    		        
                    try {

                        // These can fail if we get case-sensitive name matches...

			idlName = IDLNames.getClassOrInterfaceName(id,env);
			idlModuleName = IDLNames.getModuleNames(id,isBoxed(),env);

		    } catch (Exception e) {
			failedConstraint(7,false,stack,id.toString(),e.getMessage());
			throw new CompilerError("");
		    }
                }
            }
        }

        if (typeCode == TYPE_NONE) {
            return false;
        }

        // Reset type code...

        setTypeCode(typeCode | TM_SPECIAL_INTERFACE | TM_INTERFACE | TM_COMPOUND);

        // Set names

        if (idlName == null) {
            throw new CompilerError("Not a special type");
        }

        setNames(id,idlModuleName,idlName);

        // Initialize CompoundType...
        
        return initialize(null,null,null,stack,false);
    }
}
