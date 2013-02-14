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

import org.apache.flex.compiler.internal.projects.CompilerProject;
import org.apache.flex.compiler.internal.targets.Target;
import org.apache.flex.compiler.targets.ITargetProgressMonitor;
import org.apache.flex.compiler.targets.ITargetReport;
import org.apache.flex.compiler.targets.ITargetSettings;

import randori.compiler.js.driver.IRandoriTarget;

public class RandoriTarget extends Target implements IRandoriTarget
{

    public RandoriTarget(CompilerProject project,
            ITargetSettings targetSettings,
            ITargetProgressMonitor progressMonitor)
    {
        super(project, targetSettings, progressMonitor);
    }

    @Override
    public TargetType getTargetType()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected ITargetReport computeTargetReport() throws InterruptedException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected RootedCompilationUnits computeRootedCompilationUnits()
            throws InterruptedException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RootedCompilationUnits getRootedCompilationUnits()
            throws InterruptedException
    {
        // TODO Auto-generated method stub
        return null;
    }

}
