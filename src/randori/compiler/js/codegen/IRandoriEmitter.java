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

package randori.compiler.js.codegen;

import org.apache.flex.compiler.js.codegen.IJSEmitter;
import org.apache.flex.compiler.tree.as.IExpressionNode;
import org.apache.flex.compiler.tree.as.IFunctionCallNode;

/**
 * The {@link IRandoriEmitter} interface allows abstraction between the base
 * JavaScript and the randori specific source code production.
 * 
 * @author Michael Schmalle
 */
public interface IRandoriEmitter extends IJSEmitter
{
    /**
     * All call back for sub emitters to walk arguments a specific function
     * call.
     * 
     * @param node The {@link IFunctionCallNode}.
     */
    void walkArguments(IFunctionCallNode node);

    /**
     * Whether the emitter is in an assignment state.
     */
    boolean isInAssignment();

    boolean setInAssignment(boolean value);

    /**
     * Hack; Whether the next operator will be skipped, this has to do with
     * reducing things like <code>Window.window</code> to <code>window</code> or
     * <code>window.setTimeout()</code> to <code>setTimeout()</code>.
     */
    boolean skipOperator();

    boolean setSkipOperator(boolean value);

    /**
     * Will swap the current write buffer with a {@link StringBuilder}, walk the
     * node passed and then return the String produced.
     * <p>
     * Important to note that the String is not actually written to the out
     * buffer.
     * 
     * @param node The node to stringify.
     */
    String toNodeString(IExpressionNode node);
}
