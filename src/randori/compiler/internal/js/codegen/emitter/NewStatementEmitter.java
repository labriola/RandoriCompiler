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

package randori.compiler.internal.js.codegen.emitter;

import java.util.HashMap;

import org.apache.flex.compiler.as.codegen.ASTokens;
import org.apache.flex.compiler.constants.IASKeywordConstants;
import org.apache.flex.compiler.definitions.IAccessorDefinition;
import org.apache.flex.compiler.definitions.IClassDefinition;
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.definitions.IFunctionDefinition;
import org.apache.flex.compiler.definitions.IParameterDefinition;
import org.apache.flex.compiler.definitions.ITypeDefinition;
import org.apache.flex.compiler.definitions.IVariableDefinition;
import org.apache.flex.compiler.internal.definitions.AppliedVectorDefinition;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.tree.as.IExpressionNode;
import org.apache.flex.compiler.tree.as.IFunctionCallNode;

import randori.compiler.internal.js.utils.DefinitionUtils;
import randori.compiler.internal.js.utils.MetaDataUtils;
import randori.compiler.internal.js.utils.MetaDataUtils.MetaData.Mode;
import randori.compiler.js.codegen.IRandoriEmitter;
import randori.compiler.js.codegen.ISubEmitter;

/**
 * Handles the production of the specialized header IFunctionCallNode requires
 * for package scope.
 * 
 * @author Michael Schmalle
 */
public class NewStatementEmitter extends BaseSubEmitter implements
        ISubEmitter<IFunctionCallNode>
{
    private HashMap<String, ITypeDefinition> classDependencies = new HashMap<String, ITypeDefinition>();

    private void addDependency(ITypeDefinition definition)
    {
        if (MetaDataUtils.isNative(definition))
            return;

        if (!MetaDataUtils.isExport(definition))
            return;

        if (!classDependencies.containsKey(definition.getQualifiedName()))
        {
            classDependencies.put(definition.getQualifiedName(), definition);
        }
    }

    public NewStatementEmitter(IRandoriEmitter emitter)
    {
        super(emitter);
    }

    public void emit(IFunctionCallNode node)
    {
        ICompilerProject project = getWalker().getProject();
        IDefinition expression = node.resolveCalledExpression(project);
        if (!(expression instanceof IClassDefinition))
        {
            IVariableDefinition vdef = (IVariableDefinition) expression;
            if (vdef instanceof IAccessorDefinition)
            {
                // XXX HACK FIX ERROR; this is a problem when the call resolves
                // to a variable or accessor type, I know how to fix
                //System.err.println("ERROR emitNewStatement()");
                write(ASTokens.NEW);
                write(ASTokens.SPACE);
                //String name = RandoriUtils.toNewAccessorPrefix(node, (IFunctionDefinition) vdef, project);
                write("(");
                getWalker().walk(node.getNameNode());
                write(")");
                write(ASTokens.PAREN_OPEN);
                getEmitter().walkArguments(node);
                write(ASTokens.PAREN_CLOSE);

                return;
            }
            else
            {
                // XXX HACK FIX ERROR; this is a problem when the call resolves
                // to a variable or accessor type, I know how to fix
                //System.err.println("ERROR emitNewStatement()");
                write(ASTokens.NEW);
                write(ASTokens.SPACE);
                // XXX change this to walk
                write(vdef.getBaseName());
                write(ASTokens.PAREN_OPEN);
                getEmitter().walkArguments(node);
                write(ASTokens.PAREN_CLOSE);
                return;
            }

        }

        IClassDefinition newDefinition = (IClassDefinition) expression;
        IClassDefinition definiton = DefinitionUtils.getClassDefinition(node);

        // if the called expression type is NOT the same as the parent class
        if (definiton != newDefinition)
            addDependency((ITypeDefinition) newDefinition);

        if (expression instanceof IClassDefinition)
        {
            // write out Object, Array and Vector in simple form
            // TODO this is not taking into account the arguments
            String baseName = ((IClassDefinition) expression).getBaseName();
            if (baseName.equals("Object"))
            {
                write("{}");
                return;
            }
            else if (newDefinition instanceof AppliedVectorDefinition)
            {
                write("[]");
                return;
            }
            else if (baseName.equals("Array"))
            {
                //IExpressionNode[] nodes = node.getArgumentNodes();
                write("[");
                getEmitter().walkArguments(node);
                write("]");
                return;
            }
        }

        // 

        // first see if there is a JavaScript
        if (MetaDataUtils.hasJavaScriptTag(newDefinition))
        {
            // is the class an export
            if (MetaDataUtils.isClassExport(newDefinition))
            {
                // since the class is an export, get the 'mode'
                // the default is 'prototype'
                Mode mode = MetaDataUtils.getMode(newDefinition);
                switch (mode)
                {
                case GLOBAL:
                    emitGlobal(node, newDefinition);
                    break;
                case JSON:
                    emitJson(node, newDefinition);
                    break;
                default: // PROTOTYPE:
                    emitPrototype(node, newDefinition);
                    break;
                }
            }
            else
            {
                // [JavaScript(export="false",name="Object")]
                Mode mode = MetaDataUtils.getMode(newDefinition);
                switch (mode)
                {
                case JSON:
                    emitJson(node, newDefinition);
                    break;
                default:
                    String name = MetaDataUtils.getExportName(newDefinition);
                    if (name.equals("AudioContext"))
                    {
                        name = "webkit" + name;
                    }
                    write(ASTokens.NEW);
                    write(ASTokens.SPACE);
                    write(name);
                    write(ASTokens.PAREN_OPEN);
                    getEmitter().walkArguments(node);
                    write(ASTokens.PAREN_CLOSE);
                    break;
                }
            }
        }
        else
        {
            emitPrototype(node, newDefinition);
        }
    }

    private void emitPrototype(IFunctionCallNode node, IClassDefinition type)
    {
        // fully qualified name ie; bar = new foo.bar.Baz(42, foo);
        write(IASKeywordConstants.NEW);
        write(" ");

        // foo.bar.baz.A
        String proto = toPrototype(type);
        write(proto);

        //emitter.getWalker().walk(node.getNameNode());

        write(ASTokens.PAREN_OPEN);
        getEmitter().walkArguments(node);
        write(ASTokens.PAREN_CLOSE);
    }

    private void emitJson(IFunctionCallNode node, IClassDefinition type)
    {
        // we now must walk through the arguments and create the correct output
        // bar = {one:42, two:foo};
        // need to match the property name with the constructor parameter name

        IExpressionNode[] arguments = node.getArgumentNodes();
        int i = 0;
        final int len = arguments.length;
        write(ASTokens.BRACE_OPEN);
        for (IExpressionNode argument : arguments)
        {
            String paramName = getParameterName(i, type);
            write(paramName);
            write(ASTokens.COLON);
            getWalker().walk(argument);

            if (i < len - 1)
                write(", ");
            i++;
        }
        write(ASTokens.BRACE_CLOSE);

    }

    private String getParameterName(int index, IClassDefinition type)
    {
        IFunctionDefinition constructor = type.getConstructor();
        IParameterDefinition[] parameters = constructor.getParameters();
        if (index >= parameters.length)
            return null; // throw Exception

        IParameterDefinition parameter = parameters[index];
        String name = parameter.getBaseName();

        return name;
    }

    private void emitGlobal(IFunctionCallNode node, ITypeDefinition type)
    {
        write(ASTokens.NEW);
        write(ASTokens.SPACE);

        write(ASTokens.PAREN_OPEN);
        getEmitter().walkArguments(node);
        write(ASTokens.PAREN_OPEN);
    }

    private String toPrototype(ITypeDefinition definition)
    {
        // this 'would' be a IFunctionCallNode.getNameNode()
        return definition.getQualifiedName();
    }

    public HashMap<String, ITypeDefinition> getDependencies()
    {
        return classDependencies;
    }
}
