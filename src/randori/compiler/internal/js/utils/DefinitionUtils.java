/***
 * Copyright 2013 Teoti Graphix, LLC.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * 
 * @author Michael Schmalle <mschmalle@teotigraphix.com>
 */

package randori.compiler.internal.js.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.flex.compiler.definitions.IClassDefinition;
import org.apache.flex.compiler.definitions.ITypeDefinition;
import org.apache.flex.compiler.definitions.references.IReference;
import org.apache.flex.compiler.definitions.references.IResolvedQualifiersReference;
import org.apache.flex.compiler.internal.definitions.ClassDefinition;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.IClassNode;
import org.apache.flex.compiler.tree.as.IDefinitionNode;
import org.apache.flex.compiler.tree.as.IScopedNode;
import org.apache.flex.compiler.tree.as.ITypeNode;

/**
 * @author Michael Schmalle
 */
public class DefinitionUtils
{
    public static final String toBaseClassQualifiedName(ITypeDefinition inode,
            ICompilerProject project)
    {
        IClassNode typeNode = (IClassNode) inode.getNode();
        IClassDefinition bclass = typeNode.getDefinition().resolveBaseClass(
                project);
        if (bclass != null)
        {
            return bclass.getQualifiedName();
        }
        IReference reference = typeNode.getDefinition().getBaseClassReference();

        List<String> imports = resolveImports(typeNode.getDefinition());
        String qualifiedName = toQualifiedName(reference);
        if (qualifiedName.indexOf(".") != -1)
            return qualifiedName;

        // scan the imports for a match endName
        for (String imp : imports)
        {
            if (imp.endsWith(qualifiedName))
                return imp;
        }

        if (typeNode.getPackageName().indexOf(".") != -1)
            return typeNode.getPackageName() + "." + qualifiedName;

        return qualifiedName;
    }

    public static final List<String> resolveImports(ITypeDefinition type)
    {
        ClassDefinition cdefinition = (ClassDefinition) type;
        ArrayList<String> list = new ArrayList<String>();
        IScopedNode scopeNode = type.getContainedScope().getScopeNode();
        if (scopeNode != null)
        {
            scopeNode.getAllImports(list);
        }
        else
        {
            // MXML
            String[] implicitImports = cdefinition.getImplicitImports();
            for (String imp : implicitImports)
            {
                list.add(imp);
            }
        }
        return list;
    }

    public static final String toQualifiedName(IReference reference)
    {
        String qualifiedName = reference.getName();
        if (reference instanceof IResolvedQualifiersReference)
            qualifiedName = ((IResolvedQualifiersReference) reference)
                    .getDisplayString();
        return qualifiedName;
    }

    public static final ITypeNode findTypeNode(IASNode node)
    {
        IASNode parent = node.getParent();
        while (parent != null)
        {
            if (parent instanceof ITypeNode)
                return (ITypeNode) parent;
            parent = parent.getParent();
        }
        return null;
    }

    public static ITypeDefinition getTypeDefinition(IDefinitionNode node)
    {
        ITypeNode tnode = (ITypeNode) node.getAncestorOfType(ITypeNode.class);
        return (ITypeDefinition) tnode.getDefinition();
    }

    public static IClassDefinition getClassDefinition(IASNode node)
    {
        IClassNode tnode = (IClassNode) node
                .getAncestorOfType(IClassNode.class);
        return tnode.getDefinition();
    }
}
