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

package randori.compiler.internal.js.driver;

import java.io.FilterWriter;

import org.apache.flex.compiler.config.Configurator;
import org.apache.flex.compiler.internal.as.driver.ASBackend;
import org.apache.flex.compiler.internal.projects.CompilerProject;
import org.apache.flex.compiler.projects.IASProject;
import org.apache.flex.compiler.targets.ITargetProgressMonitor;
import org.apache.flex.compiler.targets.ITargetSettings;

import randori.compiler.internal.js.codegen.RandoriEmitter;
import randori.compiler.js.codegen.IRandoriEmitter;
import randori.compiler.js.driver.IRandoriBackend;
import randori.compiler.js.driver.IRandoriTarget;

/**
 * The backend for the {@link IRandoriEmitter}.
 * 
 * @author Michael Schmalle
 */
public class RandoriBackend extends ASBackend implements IRandoriBackend
{
    @Override
    public Configurator createConfigurator()
    {
        return new Configurator(RandoriConfiguration.class);
    }

    @Override
    public IRandoriTarget createTarget(IASProject project,
            ITargetSettings settings, ITargetProgressMonitor monitor)
    {
        return new RandoriTarget((CompilerProject) project, settings, monitor);
    }

    @Override
    public IRandoriEmitter createEmitter(FilterWriter out)
    {
        IRandoriEmitter emitter = new RandoriEmitter(out);
        emitter.setDocEmitter(createDocEmitter(emitter));
        return emitter;
    }
}
