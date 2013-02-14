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

import org.apache.flex.compiler.tree.as.IFunctionNode;
import org.junit.Test;

import randori.compiler.internal.js.codegen.project.RandoriTestProjectBase;

public class RandoriFrameworkTest extends RandoriTestProjectBase
{

    @Test
    public void test_file()
    {
        // not going to put this test production in until everything is concrete and agreed upon
        visitor.visitFile(fileNode);
        //assertOut("");
    }

    @Test
    public void renderAll()
    {
        //write();
        compileFull();
        // hopefully ordered by dep
        //compileAll();
    }

    @Test
    public void test_renderList()
    {
        IFunctionNode node = findFunction("dispatch", classNode);
        visitor.visitFunction(node);
        //assertOut("views.mediators.LabsMediator.prototype.onRegister = function() {"
        //        + "\n\tthis.message.text(\"Labs Mediator Loaded and Registered\");\n}");
    }

    @Override
    protected String getBasePath()
    {
        return "C:\\Users\\Work\\Documents\\git\\RandoriAS\\Randori\\src";
    }

    @Override
    protected String getTypeUnderTest()
    {
        return "randori.signal.SimpleSignal";
    }

}
