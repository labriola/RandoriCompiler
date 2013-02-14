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

package randori.compiler.internal.js.codegen;

import java.io.FilterWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.flex.compiler.as.codegen.ASTokens;
import org.apache.flex.compiler.constants.IASKeywordConstants;
import org.apache.flex.compiler.constants.IASLanguageConstants;
import org.apache.flex.compiler.definitions.IAccessorDefinition;
import org.apache.flex.compiler.definitions.IClassDefinition;
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.definitions.IFunctionDefinition;
import org.apache.flex.compiler.definitions.IPackageDefinition;
import org.apache.flex.compiler.definitions.ITypeDefinition;
import org.apache.flex.compiler.definitions.IVariableDefinition;
import org.apache.flex.compiler.definitions.metadata.IMetaTag;
import org.apache.flex.compiler.internal.definitions.ClassTraitsDefinition;
import org.apache.flex.compiler.internal.js.codegen.JSEmitter;
import org.apache.flex.compiler.internal.tree.as.FunctionCallNode;
import org.apache.flex.compiler.internal.tree.as.FunctionNode;
import org.apache.flex.compiler.internal.tree.as.FunctionObjectNode;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.IBinaryOperatorNode;
import org.apache.flex.compiler.tree.as.IClassNode;
import org.apache.flex.compiler.tree.as.IContainerNode;
import org.apache.flex.compiler.tree.as.IDefinitionNode;
import org.apache.flex.compiler.tree.as.IDynamicAccessNode;
import org.apache.flex.compiler.tree.as.IExpressionNode;
import org.apache.flex.compiler.tree.as.IForLoopNode;
import org.apache.flex.compiler.tree.as.IFunctionCallNode;
import org.apache.flex.compiler.tree.as.IFunctionNode;
import org.apache.flex.compiler.tree.as.IIdentifierNode;
import org.apache.flex.compiler.tree.as.ILanguageIdentifierNode;
import org.apache.flex.compiler.tree.as.ILiteralNode;
import org.apache.flex.compiler.tree.as.ILiteralNode.LiteralType;
import org.apache.flex.compiler.tree.as.IMemberAccessExpressionNode;
import org.apache.flex.compiler.tree.as.IPackageNode;
import org.apache.flex.compiler.tree.as.IParameterNode;
import org.apache.flex.compiler.tree.as.IReturnNode;
import org.apache.flex.compiler.tree.as.ITypeNode;
import org.apache.flex.compiler.tree.as.IVariableNode;
import org.apache.flex.compiler.tree.metadata.IMetaTagNode;

import randori.compiler.internal.js.codegen.emitter.BinaryOperatorEmitter;
import randori.compiler.internal.js.codegen.emitter.DynamicAccessEmitter;
import randori.compiler.internal.js.codegen.emitter.FooterEmitter;
import randori.compiler.internal.js.codegen.emitter.HeaderEmitter;
import randori.compiler.internal.js.codegen.emitter.IdentifierEmitter;
import randori.compiler.internal.js.codegen.emitter.MemberAccessExpressionEmitter;
import randori.compiler.internal.js.codegen.emitter.NewStatementEmitter;
import randori.compiler.internal.js.utils.DefinitionUtils;
import randori.compiler.internal.js.utils.ExpressionUtils;
import randori.compiler.internal.js.utils.MetaDataUtils;
import randori.compiler.internal.js.utils.RandoriUtils;
import randori.compiler.js.codegen.IRandoriEmitter;

/**
 * The base ship...
 * 
 * @author Michael Schmalle
 */
public class RandoriEmitter extends JSEmitter implements IRandoriEmitter
{
    private NewStatementEmitter newStatement;

    private IdentifierEmitter identifier;

    private MemberAccessExpressionEmitter memberAccessExpression;

    private DynamicAccessEmitter dynamicAccessEmitter;

    private BinaryOperatorEmitter binaryOperator;

    private HeaderEmitter header;

    private FooterEmitter footer;

    public RandoriEmitter(FilterWriter out)
    {
        super(out);

        identifier = new IdentifierEmitter(this);
        memberAccessExpression = new MemberAccessExpressionEmitter(this);
        dynamicAccessEmitter = new DynamicAccessEmitter(this);
        binaryOperator = new BinaryOperatorEmitter(this);

        newStatement = new NewStatementEmitter(this);

        header = new HeaderEmitter(this);
        footer = new FooterEmitter(this);

    }

    @Override
    public void emitPackageHeader(IPackageDefinition definition)
    {
        // TODO (mschmalle) emit package render comments
    }

    @Override
    public void emitPackageHeaderContents(IPackageDefinition definition)
    {
        IPackageNode node = definition.getNode();
        ITypeNode tnode = findTypeNode(node);
        if (!MetaDataUtils.isGlobal((IClassNode) tnode))
        {
            header.emit(definition);
        }
    }

    @Override
    public void emitPackageContents(IPackageDefinition definition)
    {
        IPackageNode node = definition.getNode();
        ITypeNode tnode = findTypeNode(node);
        if (tnode != null)
        {
            writeNewline();
            getWalker().walk(tnode); // IClassNode | IInterfaceNode
        }
    }

    @Override
    public void emitPackageFooter(IPackageDefinition definition)
    {
        IClassNode node = (IClassNode) findTypeNode(definition.getNode());
        if (node == null)
            return; // temp because of unit tests

        if (!MetaDataUtils.isGlobal((IClassNode) node))
        {
            footer.emitInherit(node);
            footer.emitClassName(node);
            footer.emitGetClassDependencies(node,
                    newStatement.getDependencies());
            footer.emitInjectionPoints(node);
            footer.emitLast(node);
        }
    }

    @Override
    public void emitClass(IClassNode node)
    {
        // fields, methods, namespaces
        final IDefinitionNode[] members = node.getAllMemberNodes();
        if (members.length > 0)
        {
            if (!MetaDataUtils.isGlobal(node))
            {
                IFunctionDefinition constructor = node.getDefinition()
                        .getConstructor();
                emitConstructor((IFunctionNode) constructor.getNode());
                write(";");
                writeNewline();
            }
        }

        if (members.length > 0)
        {
            //indentPush();
            writeNewline();

            final int len = members.length;
            int i = 0;
            for (IDefinitionNode mnode : members)
            {
                if (mnode.getNodeID() == ASTNodeID.FunctionID)
                {
                    IFunctionDefinition fdef = (IFunctionDefinition) mnode
                            .getDefinition();
                    if (fdef.isConstructor())
                        continue;
                }

                if (mnode.getNodeID() == ASTNodeID.VariableID)
                {
                    IVariableDefinition vdef = (IVariableDefinition) mnode
                            .getDefinition();

                    IMetaTag viewTag = MetaDataUtils.getViewTag(vdef);
                    if (viewTag != null)
                        footer.addView(viewTag);

                    IMetaTag injectTag = MetaDataUtils.getInjectTag(vdef);
                    if (injectTag != null)
                        footer.addInjectProperty(injectTag);

                    if (vdef.isStatic())
                    {
                        getWalker().walk(mnode);
                        write(SEMICOLON);

                        writeNewline();
                        writeNewline();
                    }
                }
                else if (mnode.getNodeID() == ASTNodeID.FunctionID)
                {
                    getWalker().walk(mnode);
                    write(SEMICOLON);
                    //if (i < len - 1)
                    //{
                    writeNewline();
                    writeNewline();
                    //}
                }
                else if (mnode.getNodeID() == ASTNodeID.GetterID
                        || mnode.getNodeID() == ASTNodeID.SetterID)
                {
                    getWalker().walk(mnode);
                    write(SEMICOLON);
                    if (i < len - 1)
                    {
                        writeNewline();
                        writeNewline();
                    }
                }
                i++;
            }
        }
    }

    @Override
    public void emitField(IVariableNode node)
    {
        IVariableDefinition definition = (IVariableDefinition) node
                .getDefinition();

        String prefix = RandoriUtils.toFieldPrefix(definition, getWalker()
                .getProject());
        write(prefix);
        emitAssignedValue(node.getAssignedValueNode());
    }

    @Override
    public void emitFunctionObject(IExpressionNode node)
    {
        FunctionObjectNode f = (FunctionObjectNode) node;
        FunctionNode fnode = f.getFunctionNode();
        write("$createDelegate(this, ");
        writeToken(FUNCTION);
        emitParamters(fnode.getParameterNodes());
        emitType(fnode.getTypeNode());
        emitFunctionScope(fnode.getScopedNode());
        write(")");
    }

    @Override
    public void emitFunctionBlockHeader(IFunctionNode node)
    {
        IFunctionDefinition definition = node.getDefinition();

        if (node.isConstructor())
        {
            emitConstructorFieldInitializers(definition);
            if (!hasSuperCall(node))
            {
                String qualifiedName = ExpressionUtils.toSuperQualifiedName(
                        node, getWalker().getProject());
                if (qualifiedName != null)
                {
                    write(getIndent(0) + qualifiedName + ".call(this);");
                    writeNewline();
                }
            }
        }

        emitDefaultParameterCodeBlock(node);

        String code = MetaDataUtils.findJavaScriptCodeTag(node);
        if (code != null)
        {
            write(code);
            writeNewline();
        }
    }

    private void emitDefaultParameterCodeBlock(IFunctionNode node)
    {
        // TODO (mschmalle|AMD) test for ... rest 
        // if default parameters exist, produce the init code
        IParameterNode[] pnodes = node.getParameterNodes();
        Map<Integer, IParameterNode> defaults = ExpressionUtils
                .getDefaults(pnodes);
        if (pnodes.length == 0)
            return;

        if (defaults != null)
        {
            boolean hasBody = node.getScopedNode().getChildCount() > 0;

            if (!hasBody)
            {
                indentPush();
                write(INDENT);
            }

            final StringBuilder code = new StringBuilder();

            List<IParameterNode> parameters = new ArrayList<IParameterNode>(
                    defaults.values());
            Collections.reverse(parameters);

            int len = defaults.size();
            // make the header in reverse order
            for (IParameterNode pnode : parameters)
            {
                if (pnode != null)
                {
                    code.setLength(0);

                    code.append(IASKeywordConstants.IF);
                    code.append(SPACE);
                    code.append(PARENTHESES_OPEN);
                    code.append(IASLanguageConstants.arguments);
                    code.append(PERIOD);
                    code.append(LENGTH);
                    code.append(SPACE);
                    code.append(LESS_THEN);
                    code.append(SPACE);
                    code.append(len);
                    code.append(PARENTHESES_CLOSE);
                    code.append(SPACE);
                    code.append(CURLYBRACE_OPEN);

                    write(code.toString());

                    indentPush();
                    writeNewline();
                }
                len--;
            }

            Collections.reverse(parameters);
            for (int i = 0, n = parameters.size(); i < n; i++)
            {
                IParameterNode pnode = parameters.get(i);

                if (pnode != null)
                {
                    code.setLength(0);

                    code.append(pnode.getName());
                    code.append(SPACE);
                    code.append(EQUALS);
                    code.append(SPACE);
                    code.append(pnode.getDefaultValue());
                    code.append(SEMICOLON);
                    write(code.toString());

                    indentPop();
                    writeNewline();

                    write(CURLYBRACE_CLOSE);

                    if (i == n - 1 && !hasBody)
                        indentPop();

                    writeNewline();
                }
            }
        }
    }

    private boolean hasSuperCall(IFunctionNode node)
    {
        IClassDefinition definition = ExpressionUtils.getBaseClassDefinition(
                node.getDefinition(), getWalker().getProject());
        if (MetaDataUtils.isNative(definition))
            return true; // fake saying we have super for native classes

        final int len = node.getScopedNode().getChildCount();
        for (int i = 0; i < len; i++)
        {
            IASNode child = node.getScopedNode().getChild(i);
            if (child instanceof FunctionCallNode)
                return ((FunctionCallNode) child).isSuperExpression();
        }
        return false;
    }

    @Override
    public void emitMethod(IFunctionNode node)
    {
        if (node.isConstructor())
        {
            emitConstructor(node);
            return;
        }

        FunctionNode fn = (FunctionNode) node;
        fn.parseFunctionBody(problems);
        IFunctionDefinition definition = node.getDefinition();

        IClassNode cnode = (IClassNode) node
                .getAncestorOfType(IClassNode.class);
        if (!MetaDataUtils.isGlobal(cnode))
        {
            String prefix = RandoriUtils.toMethodPrefix(definition, getWalker()
                    .getProject());
            write(prefix);
            write(" = function");
            emitParamters(node.getParameterNodes());
            emitMethodScope(node.getScopedNode());
        }
        else
        {
            write(definition.getBaseName());
            write(" = function");
            emitParamters(node.getParameterNodes());
            emitMethodScope(node.getScopedNode());
        }
    }

    @Override
    public void emitMetaTag(IMetaTagNode node)
    {
        //        if (node.getTagName().equals("JavaScriptCode"))
        //        {
        //            String result = MetaDataUtils.findJavaScriptCodeTag(node);
        //            write(result);
        //        }
    }

    private void emitConstructor(IFunctionNode node)
    {
        FunctionNode fn = (FunctionNode) node;
        fn.parseFunctionBody(problems);
        IFunctionDefinition definition = node.getDefinition();

        String prototype = RandoriUtils.toMethodPrefix(definition, getWalker()
                .getProject());
        write(prototype);
        write(" = function");
        emitParamters(node.getParameterNodes());
        if (!isImplicit((IContainerNode) node.getScopedNode()))
        {
            emitMethodScope(node.getScopedNode());
        }
        else
        {
            // we have a synthesized constructor, implict
            write(" {");
            writeNewline();
            write("}");
        }
    }

    @Override
    public void emitFunctionCall(IFunctionCallNode node)
    {
        FunctionCallNode fnode = (FunctionCallNode) node;
        ICompilerProject project = getWalker().getProject();

        if (node.isNewExpression())
        {
            IDefinition definition = node.resolveCalledExpression(project);

            if (definition != null)
            {
                newStatement.emit(node);
                return;
            }

            // continue to produce the new expression since it is no bound to a type
            // this probably means is a variable IE var foo:Function; foo();
            write(ASTokens.NEW);
            write(ASTokens.SPACE);
        }
        else if (fnode.getNameNode() instanceof IMemberAccessExpressionNode)
        {
            IMemberAccessExpressionNode mnode = (IMemberAccessExpressionNode) fnode
                    .getNameNode();
            if (mnode.getLeftOperandNode().getNodeID() == ASTNodeID.SuperID)
            {
                String baseName = ExpressionUtils.toSuperQualifiedName(node,
                        project);
                write(baseName);
                write(".prototype");
                // TODO where is the '.' getting added?
                getWalker().walk(node.getNameNode());
                write(".call");
                write(ASTokens.PAREN_OPEN);
                write("this");
                if (node.getArgumentNodes().length > 0)
                    write(ASTokens.COMMA);
                walkArguments(node);
                write(ASTokens.PAREN_CLOSE);
                return;
            }
        }

        // this injects super transform, new transform
        // super() to 'foo.bar.Baz.call'
        getWalker().walk(node.getNameNode());

        write(ASTokens.PAREN_OPEN);
        walkArguments(node);
        write(ASTokens.PAREN_CLOSE);
    }

    @Override
    public void emitParameter(IParameterNode node)
    {
        getWalker().walk(node.getNameExpressionNode());
    }

    @Override
    protected void walkArguments(IExpressionNode[] nodes)
    {
    }

    @Override
    public void walkArguments(IFunctionCallNode node)
    {
        FunctionCallNode fnode = (FunctionCallNode) node;
        IExpressionNode[] nodes = node.getArgumentNodes();
        int len = nodes.length;
        // only add 'this' to a constructor super() call, not a super.foo() call
        if (ExpressionUtils.injectThisArgument(fnode, false))
        {
            write("this");
            if (len > 0)
                write(", ");
        }

        for (int i = 0; i < len; i++)
        {
            IExpressionNode inode = nodes[i];
            if (inode.getNodeID() == ASTNodeID.IdentifierID)
            {
                // test for Functions to be wrapped with createDelegate()
                emitArgumentIdentifier((IIdentifierNode) inode);
            }
            else
            {
                getWalker().walk(inode);
            }

            if (i < len - 1)
            {
                write(COMMA);
                write(SPACE);
            }
        }
    }

    private void emitArgumentIdentifier(IIdentifierNode node)
    {
        ITypeDefinition type = node.resolveType(getWalker().getProject());
        if (type instanceof ClassTraitsDefinition)
        {
            write(type.getQualifiedName());
        }
        else if (type instanceof IClassDefinition
                && type.getBaseName().equals("Function"))
        {
            write("$createDelegate(this, ");
            getWalker().walk(node);
            write(")");
        }
        else
        {
            // popcorn identifier
            getWalker().walk(node);
        }
    }

    @Override
    public void emitForEachLoop(IForLoopNode node)
    {
        IContainerNode xnode = (IContainerNode) node.getChild(1);
        write(IASKeywordConstants.FOR);
        write(SPACE);
        //write(IASKeywordConstants.EACH);
        //write(SPACE);
        write(PARENTHESES_OPEN);

        IContainerNode cnode = node.getConditionalsContainerNode();
        getWalker().walk(cnode.getChild(0));

        write(PARENTHESES_CLOSE);
        if (!isImplicit(xnode))
            write(SPACE);

        getWalker().walk(node.getStatementContentsNode());
    }

    @Override
    public void emitAsOperator(IBinaryOperatorNode node)
    {
        getWalker().walk(node.getLeftOperandNode());
    }

    @Override
    public void emitIsOperator(IBinaryOperatorNode node)
    {
        getWalker().walk(node.getLeftOperandNode());
        write(" instanceof ");
        getWalker().walk(node.getRightOperandNode());
    }

    @Override
    public void emitBinaryOperator(IBinaryOperatorNode node)
    {
        binaryOperator.emit(node);
    }

    @Override
    public void emitMemberAccessExpression(IMemberAccessExpressionNode node)
    {
        memberAccessExpression.emit(node);
    }

    @Override
    public void emitDynamicAccess(IDynamicAccessNode node)
    {
        dynamicAccessEmitter.emit(node);
    }

    @Override
    public void emitIdentifier(IIdentifierNode node)
    {
        identifier.emit(node);
    }

    @Override
    protected void emitType(IExpressionNode node)
    {
    }

    @Override
    public void emitReturn(IReturnNode node)
    {
        // TODO Auto-generated method stub
        super.emitReturn(node);
    }

    @Override
    public void emitLanguageIdentifier(ILanguageIdentifierNode node)
    {
        if (node.getKind() == ILanguageIdentifierNode.LanguageIdentifierKind.ANY_TYPE)
        {
            write("");
        }
        else if (node.getKind() == ILanguageIdentifierNode.LanguageIdentifierKind.REST)
        {
            write("");
        }
        else if (node.getKind() == ILanguageIdentifierNode.LanguageIdentifierKind.SUPER)
        {
            IIdentifierNode inode = (IIdentifierNode) node;
            if (inode.getParent() instanceof IMemberAccessExpressionNode)
            {
                // emitFunctionCall() takes care of super.foo()
            }
            else
            {
                IClassNode typeNode = (IClassNode) DefinitionUtils
                        .findTypeNode(inode.getParent());
                String qualifiedName = DefinitionUtils
                        .toBaseClassQualifiedName(typeNode.getDefinition(),
                                getWalker().getProject());
                write(qualifiedName + ".call");
            }
        }
        else if (node.getKind() == ILanguageIdentifierNode.LanguageIdentifierKind.THIS)
        {
            IIdentifierNode inode = (IIdentifierNode) node;
            if (!(inode.getParent() instanceof IMemberAccessExpressionNode))
                write("this");
        }
        else if (node.getKind() == ILanguageIdentifierNode.LanguageIdentifierKind.VOID)
        {
            write("");
        }
    }

    @Override
    public void emitLiteral(ILiteralNode node)
    {
        String value = node.getValue(false);
        if (node.getLiteralType() == LiteralType.STRING)
        {
            value = "\"" + StringEscapeUtils.escapeEcmaScript(value) + "\"";
        }
        write(value);
    }

    protected void emitFieldInitialValue(IVariableNode node)
    {
        ICompilerProject project = getWalker().getProject();
        IVariableDefinition definition = (IVariableDefinition) node
                .getDefinition();

        IExpressionNode valueNode = node.getAssignedValueNode();
        if (valueNode != null)
            getWalker().walk(valueNode);
        else
            write(ExpressionUtils.toInitialValue(definition, project));
    }

    protected void emitConstructorFieldInitializers(
            IFunctionDefinition definition)
    {
        IClassDefinition type = (IClassDefinition) definition
                .getAncestorOfType(IClassDefinition.class);
        // emit public fields init values
        List<IVariableDefinition> fields = ExpressionUtils.getFields(type,
                false);

        final int len = fields.size();
        boolean hasIndent = maybeIndent(len);

        int i = 0;
        for (IVariableDefinition field : fields)
        {
            if (field instanceof IAccessorDefinition)
                continue;
            if (ExpressionUtils.isVariableAParameter(field,
                    definition.getParameters()))
                continue;
            write("this.");
            write(field.getBaseName());
            write(" = ");
            emitFieldInitialValue((IVariableNode) field.getNode());
            write(";");

            loopIndent(i, len, hasIndent);
            i++;
        }
    }

    private boolean inAssignment;

    @Override
    public boolean isInAssignment()
    {
        return inAssignment;
    }

    @Override
    public boolean setInAssignment(boolean value)
    {
        return inAssignment = value;
    }

    private boolean skipOperator;

    @Override
    public boolean skipOperator()
    {
        return skipOperator;
    }

    @Override
    public boolean setSkipOperator(boolean value)
    {
        return skipOperator = value;
    }

    @Override
    public String toNodeString(IExpressionNode node)
    {
        setBufferWrite(true);
        getWalker().walk(node);
        String result = getBuilder().toString();
        getBuilder().setLength(0);
        return result;
    }
}
