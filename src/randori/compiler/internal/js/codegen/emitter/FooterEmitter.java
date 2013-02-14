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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.flex.compiler.definitions.IClassDefinition;
import org.apache.flex.compiler.definitions.IFunctionDefinition;
import org.apache.flex.compiler.definitions.IParameterDefinition;
import org.apache.flex.compiler.definitions.ITypeDefinition;
import org.apache.flex.compiler.definitions.IVariableDefinition;
import org.apache.flex.compiler.definitions.metadata.IMetaTag;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.tree.as.IClassNode;
import org.apache.flex.compiler.tree.as.IExpressionNode;
import org.apache.flex.compiler.tree.as.IVariableNode;

import randori.compiler.internal.js.utils.DefinitionUtils;
import randori.compiler.internal.js.utils.ExpressionUtils;
import randori.compiler.internal.js.utils.MetaDataUtils;
import randori.compiler.js.codegen.IRandoriEmitter;

// TODO (mschmalle) Method Inject

/**
 * Handles the production of the specialized footer Randori requires for;
 * 
 * <ul>
 * <li>Class Inheritance</li>
 * <li>Static ClassName assignment</li>
 * <li>Class dependency tracking</li>
 * </ul>
 * <strong>Injection Points</strong>
 * <ul>
 * <li>Constructor, Field and Method Injection</li>
 * <li>View Injection</li>
 * </ul>
 * 
 * @author Michael Schmalle
 */
public class FooterEmitter extends BaseSubEmitter
{
    private List<IMetaTag> propertyInjections = new ArrayList<IMetaTag>();

    private List<IMetaTag> views = new ArrayList<IMetaTag>();

    public FooterEmitter(IRandoriEmitter emitter)
    {
        super(emitter);
    }

    public void emitInherit(IClassNode tnode)
    {
        // $Inherit(foo.bar.Baz, foo.bar.SuperClass);
        if (tnode.getBaseClassName() == null)
            return;

        String baseClassName = DefinitionUtils.toBaseClassQualifiedName(
                tnode.getDefinition(), getProject());
        if (baseClassName.equals("Object"))
            return;

        write("$inherit(");
        write(tnode.getQualifiedName());
        write(", ");

        write(baseClassName);
        write(");");
        writeNewline();
    }

    public void emitClassName(IClassNode tnode)
    {
        // foo.bar.Baz.className = "foo.bar.Baz";
        write(tnode.getQualifiedName());
        write(".className = ");
        write("\"" + tnode.getQualifiedName() + "\"");
        write(";");
        writeNewline();
    }

    public void emitGetClassDependencies(IClassNode tnode,
            HashMap<String, ITypeDefinition> dependencies)
    {
        // foo.bar.Baz.getClassDependencies = function () {
        //     var p;
        //     return  [];
        // };
        write(tnode.getQualifiedName());
        write(".getClassDependencies = function(t) {");
        indentPush();
        writeNewline();
        write("var p;");
        writeNewline();

        if (dependencies.size() > 0)
        {
            write("p = [];");
            writeNewline();
            for (ITypeDefinition type : dependencies.values())
            {
                write("p.push('" + type.getQualifiedName() + "');");
                writeNewline();
            }
            write("return p;");
        }
        else
        {
            write("return [];");
        }

        indentPop();
        writeNewline();
        write("};");
        writeNewline();
    }

    public void emitInjectionPoints(IClassNode tnode)
    {
        IClassDefinition definiton = tnode.getDefinition();
        IClassDefinition baseDefinition = definiton
                .resolveBaseClass(getProject());

        write(tnode.getQualifiedName());
        write(".injectionPoints = function(t) {");
        indentPush();
        writeNewline();

        boolean hasArgs = definiton.getConstructor().getParameters().length > 0;

        if (MetaDataUtils.isNative(baseDefinition) && !hasArgs)
        {
            // XXX HACK fix this when you have the head
            write("return [];");

            //indentPop();

            indentPop();
            writeNewline();

            write("};");
            writeNewline();
            return;
        }

        write("var p;");
        writeNewline();
        write("switch (t) {");
        indentPush();
        writeNewline();

        if (hasArgs)
        {
            // case 0
            write("case 0:");
            indentPush();
            writeNewline();
            _emitInjectionConstructor(tnode);
            indentPop();
            write("break;");
            writeNewline();
        }

        if (!MetaDataUtils.isNative(baseDefinition))
        {
            // case 1
            write("case 1:");
            indentPush();
            writeNewline();
            _emitInjectionProperty(tnode);
            indentPop();
            write("break;");
            writeNewline();

            // case 3
            write("case 2:");
            indentPush();
            writeNewline();
            _emitInjectionMethod(tnode);
            indentPop();
            write("break;");
            writeNewline();

            // case 4
            write("case 3:");
            indentPush();
            writeNewline();
            _emitInjectionView(tnode);
            indentPop();
            write("break;");
            writeNewline();
        }

        // default
        write("default:");
        indentPush();
        writeNewline();
        _emitInjectionDefault(tnode);
        indentPop();
        write("break;");

        indentPop();
        writeNewline();
        write("}");

        writeNewline();
        write("return p;");
        // copy of above !!! FIX
        //indentPop();

        indentPop();
        writeNewline();

        write("};");
        writeNewline();
    }

    public void emitLast(IClassNode node)
    {
        IClassDefinition definition = (IClassDefinition) node.getDefinition();
        IMetaTag tag = definition.getMetaTagByName("JavaScript");
        if (tag != null)
        {
            String main = tag.getAttributeValue("main");
            if (main != null && main.equals("true"))
            {
                write("window.onload = function() {");
                write("var main");
                write(" = ");
                write("new ");
                write(definition.getQualifiedName());
                write(";");
                write("}");
            }
        }

    }

    // Constructor, Property, Method, View

    private void _emitInjectionConstructor(IClassNode node)
    {
        IClassDefinition definition = node.getDefinition();
        IFunctionDefinition constructor = definition.getConstructor();
        IParameterDefinition[] parameters = constructor.getParameters();
        String qualifiedName = toSuperQualifiedName(definition);
        final int len = parameters.length;
        int i = 0;
        if (constructor.isImplicit() || len == 0)
        {
            write("p = " + qualifiedName + ".injectionPoints(t);");
        }
        else
        {
            write("p = [];");
            writeNewline();
            for (IParameterDefinition parameter : parameters)
            {
                ITypeDefinition rtype = parameter.resolveType(getProject());
                String exportName = MetaDataUtils.getExportName(rtype);
                write("p.push({n:'"
                        + parameter.getBaseName() + "', t:'" + exportName
                        + "'});");
                if (i < len - 1)
                    writeNewline();
                i++;
            }
        }

        writeNewline();
    }

    private void _emitInjectionProperty(IClassNode node)
    {
        IClassDefinition definition = node.getDefinition();
        String qualifiedName = toSuperQualifiedName(definition);
        write("p = " + qualifiedName + ".injectionPoints(t);");
        final int len = propertyInjections.size();
        if (len > 0)
            writeNewline();
        for (IMetaTag tag : propertyInjections)
        {
            String required = toInjectRequired(tag);
            IVariableDefinition owner = (IVariableDefinition) tag
                    .getDecoratedDefinition();
            ITypeDefinition type = owner.resolveType(getProject());

            write("p.push({n:'" + owner.getBaseName() + "'");

            if (!MetaDataUtils.isNative(type))
            {
                write(",");
                write("t:'" + type.getQualifiedName() + "'");
            }

            write(",");
            write(" r:" + required);

            String value = returnInitialValue((IVariableNode) owner.getNode());
            write(",");
            write(" v:" + value);

            write("});");
            writeNewline();
        }
        writeNewline();
    }

    protected String returnInitialValue(IVariableNode node)
    {
        ICompilerProject project = getWalker().getProject();
        IVariableDefinition definition = (IVariableDefinition) node
                .getDefinition();

        IExpressionNode valueNode = node.getAssignedValueNode();
        if (valueNode != null)
            return getEmitter().toNodeString(valueNode);
        else
            return ExpressionUtils.toInitialValue(definition, project);
    }

    private void _emitInjectionMethod(IClassNode node)
    {
        IClassDefinition definition = node.getDefinition();
        String qualifiedName = toSuperQualifiedName(definition);
        write("p = " + qualifiedName + ".injectionPoints(t);");
        // p.push({n:'giveMeSomething', p:[{n:'eventBus', t:'sap.eventBus.SAPMainEventBus'}, 
        // {n:'projectService', t:'sap.services.ProjectService'}]});
        writeNewline();
    }

    private void _emitInjectionView(IClassNode node)
    {
        IClassDefinition definition = node.getDefinition();
        String qualifiedName = toSuperQualifiedName(definition);
        write("p = " + qualifiedName + ".injectionPoints(t);");
        final int len = views.size();
        if (len > 0)
            writeNewline();
        for (IMetaTag tag : views)
        {
            String required = toViewRequired(tag);
            IVariableDefinition owner = (IVariableDefinition) tag
                    .getDecoratedDefinition();
            ITypeDefinition type = owner.resolveType(getProject());

            write("p.push({n:'" + owner.getBaseName() + "'");

            if (!MetaDataUtils.isNative(type))
            {
                write(",");
                write("t:'" + type.getQualifiedName() + "'");
            }

            if (tag.getAttributeValue("required") != null
                    && required.equals("0"))
            {
                write(",");
                write(" r:" + required);
            }

            write("});");
            writeNewline();
        }
        writeNewline();
    }

    private void _emitInjectionDefault(IClassNode tnode)
    {
        write("p = [];");
        writeNewline();
    }

    final String toSuperQualifiedName(IClassDefinition definition)
    {
        return ExpressionUtils.toSuperQualifiedName(definition, getProject());
    }

    public void addView(IMetaTag tag)
    {
        if (!views.contains(tag))
            views.add(tag);
    }

    public void addInjectProperty(IMetaTag tag)
    {
        if (!propertyInjections.contains(tag))
            propertyInjections.add(tag);
    }

    private static String toViewRequired(IMetaTag tag)
    {
        String value = tag.getAttributeValue("required");
        if (value == null)
            return "1";
        if (value.equals("true"))
            return "1";
        return "0";
    }

    private static String toInjectRequired(IMetaTag tag)
    {
        String value = tag.getAttributeValue("required");
        if (value == null)
            return "0";
        if (value.equals("true"))
            return "1";
        return "0";
    }
}