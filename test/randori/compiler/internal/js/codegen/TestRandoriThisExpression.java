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

import org.apache.flex.compiler.tree.as.IBinaryOperatorNode;
import org.apache.flex.compiler.tree.as.IFileNode;
import org.apache.flex.compiler.tree.as.IFunctionNode;
import org.apache.flex.utils.StringUtils;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Michael Schmalle
 */
public class TestRandoriThisExpression extends BaseRandoriTest
{
    /*
     * foo = 42          LEFT   Binary parent
     * a = bar = foo     RIGHT  Binary parent
     * if (foo)          NA     NonBinary parent
     * 
     */
    @Test
    public void test_baseGetterPublic()
    {
        IBinaryOperatorNode node = getBinaryOperator("a = foo");
        visitor.visitBinaryOperator(node);
        assertOut("a = this.get_foo()");
    }

    @Test
    public void test_baseSetterPublic()
    {
        IBinaryOperatorNode node = getBinaryOperator("foo = '42'");
        visitor.visitBinaryOperator(node);
        assertOut("this.set_foo(\"42\")");
    }
    
    @Test
    public void test_baseSetterPublic_1()
    {
        IBinaryOperatorNode node = getBinaryOperator("a = bar = foo");
        visitor.visitBinaryOperator(node);
        assertOut("a = this.set_bar(this.get_foo())");
    }
    
    @Test
    public void test_baseSetterPublic_2()
    {
        IBinaryOperatorNode node = getBinaryOperator("a = bar[b][base] = a.foo");
        visitor.visitBinaryOperator(node);
        assertOut("a = this.get_bar()[b][this.get_base()] = a.foo");
    }
    
    @Ignore
    @Test
    public void test_baseSetterPublic_3()
    {
        IFunctionNode node = getFunction("var a:Behavior;a.basePublic = 'foo';");
        visitor.visitFunction(node);
        assertOut("Foo.prototype.aMethod = function(data) {\n\tvar a;\n\ta.set_basePublic('foo');\n}");
    }
    
    @Ignore
    @Test
    public void test_baseSetterPublic_4()
    {
        IFunctionNode node = getFunction("var a:Behavior;bar = bar[a.basePublic][base] = a.basePublic");
        visitor.visitFunction(node);
        assertOut("Foo.prototype.aMethod = function(data) {\n\tvar a;\n\tthis.set_bar = this.get_bar[a.get_basePublic][this.get_base] = a.get_basePublic;\n}");
    }
    
    @Test
    public void test_binary_1()
    {
        IBinaryOperatorNode node = getBinaryOperator("foo = bar;");
        visitor.visitBinaryOperator(node);
        assertOut("this.set_foo(this.get_bar())");
    }
    
    @Ignore
    @Test
    public void test_binary_2()
    {
        IBinaryOperatorNode node = getBinaryOperator("foo = bar.toString();");
        visitor.visitBinaryOperator(node);
        assertOut("this.set_foo(this.get_bar().toString())");
    }
    
    @Test
    public void test_binary_3()
    {
        IBinaryOperatorNode node = getBinaryOperator("foo = bar['a'].toString();");
        visitor.visitBinaryOperator(node);
        assertOut("this.set_foo(this.get_bar()[\"a\"].toString())");
    }
    
    @Test
    public void test_binary_4()
    {
        IBinaryOperatorNode node = getBinaryOperator("aVariable = 42;");
        visitor.visitBinaryOperator(node);
        assertOut("this.aVariable = 42");
    }
    
    @Ignore
    @Test
    public void test_binary_5()
    {
        IBinaryOperatorNode node = getBinaryOperator("foo = foo[a[bar[aVariable.toString()][0]]] = bar.concat(aVariable)[0];");
        visitor.visitBinaryOperator(node);
        assertOut("this.set_foo = this.get_foo[a[this.get_bar[this.aVariable.toString()][0]]] = this.get_bar.concat(this.aVariable)[0]");
    }
    // this.set_foo = this.get_foo[a[b[this.aVariable.toString()][0]]] = this.set_bar.baz.goo(aVariable)[0]
    
    /*
     * 1 foo = bar;
     * 2 foo = bar.toString()
     * 3 foo = bar['a'].toString();
     * 4 aVariable = 42
     * 5 foo = foo[a[b[aVariable.toString()][0]] = bar.baz.goo(aVariable)[0];
     */
    
    @Test
    public void test_baseGetterInContainer()
    {
        IFunctionNode node = getFunction("if (foo) {}");
        visitor.visitFunction(node);
        assertOut("Foo.prototype.aMethod = function(data) {\n\tif (this.get_foo()) {\n\t}\n}");
    }
    
    //--------------------------------------------------------------------------

    @Test
    public void test_baseAccessorPublic_hasThis()
    {
        IBinaryOperatorNode node = getBinaryOperator("foo = '42'");
        visitor.visitBinaryOperator(node);
        assertOut("this.set_foo(\"42\")");
    }

    @Test
    public void test_baseAccessProtected_hasThis()
    {
        IBinaryOperatorNode node = getBinaryOperator("bar = '42'");
        visitor.visitBinaryOperator(node);
        assertOut("this.set_bar(\"42\")");
    }

    @Test
    public void test_baseAccessPrivate_noThis()
    {
        IBinaryOperatorNode node = getBinaryOperator("baz = '42'");
        visitor.visitBinaryOperator(node);
        assertOut("baz = \"42\"");
    }

    protected IBinaryOperatorNode getBinaryOperator(String code)
    {
        IFunctionNode fnode = getFunction(code);
        IBinaryOperatorNode child = (IBinaryOperatorNode) findFirstDescendantOfType(
                fnode, IBinaryOperatorNode.class);
        return child;
    }

    protected IFunctionNode getFunction(String code)
    {
        String source = getSource(code);
        IFileNode node = getFileNode(source);
        IFunctionNode child = (IFunctionNode) findFirstDescendantOfType(node,
                IFunctionNode.class);
        return child;
    }

    private String getSource(String code)
    {
        /**
         * BehaviorA - public get/set foo:String - protected get/set bar:String
         * - private get/set baz:String
         */
        String[] result = { "package {",
                "import com.example.behaviors.BehaviorA;",
                "import com.example.behaviors.Behavior;",
                "    public class Foo extends BehaviorA {",
                "        private function aMethod(data:Object):void {",
                "            " + code, "        }", "    }", "}" };
        return StringUtils.join(result, "\n");
    }
}
