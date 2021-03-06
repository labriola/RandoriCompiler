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

package randori.compiler.internal.js.codegen.project.behaviors;

import org.apache.flex.compiler.tree.as.IFunctionNode;
import org.junit.Test;

import randori.compiler.internal.js.codegen.project.RandoriTestProjectBase;

/**
 * @author Michael Schmalle
 */
public class VerticalTabsTest extends RandoriTestProjectBase
{
    @Test
    public void test_constructor()
    {
        IFunctionNode node = findFunction("VerticalTabs", classNode);
        visitor.visitFunction(node);
        assertOut("behaviors.VerticalTabs = function(walker) {\n\tthis.menuItemSelected "
                + "= null;\n\trandori.behaviors.List.call(this, walker);\n\tthis.menuItemSelected"
                + " = new randori.signal.SimpleSignal();\n}");
    }

    @Test
    public void test_renderList()
    {
        IFunctionNode node = findFunction("renderList", classNode);
        visitor.visitFunction(node);
        assertOut("behaviors.VerticalTabs.prototype.renderList = function() {"
                + "\n\trandori.behaviors.List.prototype.renderList.call(this);\n\t"
                + "var children = this.decoratedNode.children();\n\tvar first = "
                + "children.eq(0);\n\tvar last = children.eq(children.length - 1);"
                + "\n\tfirst.addClass(\"first\");\n\tlast.addClass(\"last\");\n}");
    }

    @Test
    public void test_onRegister()
    {
        IFunctionNode node = findFunction("onRegister", classNode);
        visitor.visitFunction(node);
        assertOut("behaviors.VerticalTabs.prototype.onRegister = function() {"
                + "\n\tthis.listChanged.add($createDelegate(this, this.listChangedHandler));"
                + "\n\trandori.behaviors.List.prototype.onRegister.call(this);\n}");
    }

    @Test
    public void test_listChangedHandler()
    {
        IFunctionNode node = findFunction("listChangedHandler", classNode);
        visitor.visitFunction(node);
        assertOut("behaviors.VerticalTabs.prototype.listChangedHandler = function(index, data) {"
                + "\n\tif (data != null) {\n\t\tvar menuItem = {name:data[\"name\"], url:data[\"url\"]"
                + "};\n\t\tthis.menuItemSelected.dispatch(menuItem);\n\t}\n}");
    }

    @Test
    public void test_file()
    {
        visitor.visitFile(fileNode);
    }

    protected String getBasePath()
    {
        return "C:\\Users\\Work\\Documents\\git\\RandoriAS\\DemoApplication\\src";
    }

    @Override
    protected String getTypeUnderTest()
    {
        return "behaviors.VerticalTabs";
    }
}
