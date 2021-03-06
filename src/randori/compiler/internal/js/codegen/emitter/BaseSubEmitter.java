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

import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.visitor.IASBlockWalker;

import randori.compiler.internal.js.utils.MetaDataUtils;
import randori.compiler.js.codegen.IRandoriEmitter;

/**
 * Base emitter for sub composites of the {@link IRandoriEmitter}.
 * <p>
 * This whole framework is prototype, needed a way to pull everything a part.
 * 
 * @author Michael Schmalle
 */
public abstract class BaseSubEmitter
{
    private final IRandoriEmitter emitter;

    public BaseSubEmitter(IRandoriEmitter emitter)
    {
        this.emitter = emitter;
    }

    public final IRandoriEmitter getEmitter()
    {
        return emitter;
    }

    protected final ICompilerProject getProject()
    {
        return emitter.getWalker().getProject();
    }

    protected final IASBlockWalker getWalker()
    {
        return emitter.getWalker();
    }

    protected void write(String value)
    {
        emitter.write(value);
    }

    protected void writeNewline()
    {
        emitter.writeNewline();
    }

    protected void indentPush()
    {
        emitter.indentPush();
    }

    protected void indentPop()
    {
        emitter.indentPop();
    }

    protected void writeIfNotNative(String value, IDefinition definition)
    {
        if (!MetaDataUtils.isNative(definition))
            emitter.write(value);
    }

}
