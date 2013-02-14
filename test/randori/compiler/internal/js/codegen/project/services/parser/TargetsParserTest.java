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

package randori.compiler.internal.js.codegen.project.services.parser;

import org.apache.flex.compiler.tree.as.IFunctionNode;
import org.junit.Test;

import randori.compiler.internal.js.codegen.project.RandoriTestProjectBase;

/**
 * @author Michael Schmalle
 */
public class TargetsParserTest extends RandoriTestProjectBase
{
    @Test
    public void test_constructor()
    {
        IFunctionNode node = findFunction("TargetsParser", classNode);
        visitor.visitFunction(node);
        assertOut("services.parser.TargetsParser = function() {"
                + "\n\trandori.service.parser.AbstractParser.call(this);\n}");
    }

    @Test
    public void test_parseResult()
    {
        IFunctionNode node = findFunction("parseResult", classNode);
        visitor.visitFunction(node);
        assertOut("services.parser.TargetsParser.prototype.parseResult = function(result) {"
                + "\n\tvar json = JSON.parse(result);\n\treturn json;\n}");
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
        return "services.parser.TargetsParser";
    }
}
