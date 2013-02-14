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

package randori.compiler.clients;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.flex.compiler.clients.IBackend;
import org.apache.flex.compiler.clients.problems.ProblemPrinter;
import org.apache.flex.compiler.clients.problems.ProblemQuery;
import org.apache.flex.compiler.clients.problems.WorkspaceProblemFormatter;
import org.apache.flex.compiler.config.Configuration;
import org.apache.flex.compiler.config.ConfigurationBuffer;
import org.apache.flex.compiler.config.Configurator;
import org.apache.flex.compiler.config.ICompilerSettingsConstants;
import org.apache.flex.compiler.exceptions.ConfigurationException;
import org.apache.flex.compiler.exceptions.ConfigurationException.IOError;
import org.apache.flex.compiler.exceptions.ConfigurationException.MustSpecifyTarget;
import org.apache.flex.compiler.internal.js.codegen.JSSharedData;
import org.apache.flex.compiler.internal.projects.FlexProject;
import org.apache.flex.compiler.internal.projects.ISourceFileHandler;
import org.apache.flex.compiler.internal.workspaces.Workspace;
import org.apache.flex.compiler.problems.ConfigurationProblem;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.compiler.problems.InternalCompilerProblem;
import org.apache.flex.compiler.problems.UnexpectedExceptionProblem;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.targets.ITarget;
import org.apache.flex.compiler.units.ICompilationUnit;

import randori.compiler.internal.js.driver.RandoriBackend;

/**
 * @author Michael Schmalle
 */
public class Randori
{
    private Workspace workspace;

    private FlexProject project;

    private ProblemQuery problems;

    private IBackend backend;

    private ITarget target;

    private ISourceFileHandler sourceFileHandler;

    private Configuration config;

    private Configurator projectConfigurator;

    private ConfigurationBuffer configBuffer;

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        long startTime = System.nanoTime();

        IBackend backend = new RandoriBackend();

        final Randori randori = new Randori(backend);
        final Set<ICompilerProblem> problems = new HashSet<ICompilerProblem>();
        final int exitCode = randori.mainNoExit(args, problems, true);

        long endTime = System.nanoTime();
        String time = (endTime - startTime) / 1e9 + " seconds";

        System.exit(exitCode);
    }

    private int mainNoExit(String[] args, Set<ICompilerProblem> problems,
            boolean printProblems)
    {
        int exitCode = -1;
        try
        {
            exitCode = startCompile(args, problems);
        }
        catch (Exception e)
        {
        }
        finally
        {
            if (problems != null && !problems.isEmpty())
            {
                if (printProblems)
                {
                    final WorkspaceProblemFormatter formatter = new WorkspaceProblemFormatter(
                            workspace);
                    final ProblemPrinter printer = new ProblemPrinter(formatter);
                    printer.printProblems(problems);
                }
            }
        }
        return exitCode;
    }

    private int startCompile(String[] args, Set<ICompilerProblem> outProblems)
    {
        ExitCode exitCode = ExitCode.SUCCESS;
        try
        {
            final boolean continueCompilation = configure(args);

            if (continueCompilation)
            {
                compile();
                if (problems.hasFilteredProblems())
                    exitCode = ExitCode.FAILED_WITH_PROBLEMS;
            }
            else if (problems.hasFilteredProblems())
            {
                exitCode = ExitCode.FAILED_WITH_CONFIG_PROBLEMS;
            }
            else
            {
                exitCode = ExitCode.PRINT_HELP;
            }
        }
        catch (Exception e)
        {
            if (outProblems == null)
            {

            }
            else
            {
                final ICompilerProblem unexpectedExceptionProblem = new UnexpectedExceptionProblem(
                        e);
                problems.add(unexpectedExceptionProblem);
            }
            exitCode = ExitCode.FAILED_WITH_EXCEPTIONS;
        }
        finally
        {
            waitAndClose();

            if (outProblems != null && problems.hasFilteredProblems())
            {
                for (ICompilerProblem problem : problems.getFilteredProblems())
                {
                    outProblems.add(problem);
                }
            }
        }
        return exitCode.code;
    }

    public Randori(IBackend backend)
    {
        this.backend = backend;

        workspace = new Workspace();
        project = new FlexProject(workspace);
        problems = new ProblemQuery();

        sourceFileHandler = backend.getSourceFileHandlerInstance();
    }

    /**
     * Create a new Configurator. This method may be overridden to allow
     * Configurator subclasses to be created that have custom configurations.
     * 
     * @return a new instance or subclass of {@link Configurator}.
     */
    protected Configurator createConfigurator()
    {
        return JSSharedData.backend.createConfigurator();
    }

    /**
     * Load configurations from all the sources.
     * 
     * @param args command line arguments
     * @return True if mxmlc should continue with compilation.
     */
    protected boolean configure(final String[] args)
    {
        project.getSourceCompilationUnitFactory().addHandler(sourceFileHandler);
        projectConfigurator = createConfigurator();

        try
        {
            //            // Print brief usage if no arguments provided.
            //            if (args.length == 0)
            //            {
            //                final String usage = CommandLineConfigurator.brief(
            //                        getProgramName(), DEFAULT_VAR,
            //                        LocalizationManager.get(), L10N_CONFIG_PREFIX);
            //                if (usage != null)
            //                    println(usage);
            //                return false;
            //            }
            //
            projectConfigurator.setConfiguration(args,
                    ICompilerSettingsConstants.FILE_SPECS_VAR);
            projectConfigurator.applyToProject(project);
            problems = new ProblemQuery(
                    projectConfigurator.getCompilerProblemSettings());

            // Get the configuration and configBuffer which are now initialized.
            config = projectConfigurator.getConfiguration();
            configBuffer = projectConfigurator.getConfigurationBuffer();
            problems.addAll(projectConfigurator.getConfigurationProblems());

            // Print version if "-version" is present.
            if (configBuffer.getVar("version") != null) //$NON-NLS-1$
            {
                return false;
            }


            if (problems.hasErrors())
                return false;

            validateTargetFile();
            return true;
        }
        catch (ConfigurationException e)
        {
            final ICompilerProblem problem = new ConfigurationProblem(e);
            problems.add(problem);
            return false;
        }
        catch (Exception e)
        {
            final ICompilerProblem problem = new ConfigurationProblem(null, -1,
                    -1, -1, -1, e.getMessage());
            problems.add(problem);
            return false;
        }
        finally
        {
            // If we couldn't create a configuration, then create a default one
            // so we can exit without throwing an exception.
            if (config == null)
            {
                config = new Configuration();
                configBuffer = new ConfigurationBuffer(Configuration.class,
                        Configuration.getAliases());
            }
        }
    }

    protected boolean compile()
    {
        boolean compilationSuccess = false;

        try
        {
            prebuild();

            if (!setupBuild())
                return false;

            //if (config.isDumpAst())
            //    dumpAST();

            build();

            if (target != null)
            {
                Collection<ICompilerProblem> errors = new ArrayList<ICompilerProblem>();
                Collection<ICompilerProblem> warnings = new ArrayList<ICompilerProblem>();

                if (!config.getCreateTargetWithErrors())
                {
                    problems.getErrorsAndWarnings(errors, warnings);
                    if (errors.size() > 0)
                        return false;
                }

                File outputFolder = null;

                Collection<ICompilationUnit> reachableCompilationUnits = project
                        .getCompilationUnits();

                for (final ICompilationUnit cu : reachableCompilationUnits)
                {
                    if (cu.getCompilationUnitType() == ICompilationUnit.UnitType.AS_UNIT)
                    {
                    }
                }

                compilationSuccess = true;
            }
        }
        catch (Exception e)
        {
            final ICompilerProblem problem = new InternalCompilerProblem(e);
            problems.add(problem);
        }

        return compilationSuccess;
    }

    private void prebuild()
    {
        project.getSourceCompilationUnitFactory().addHandler(sourceFileHandler);
    }

    private void build()
    {

    }

    /**
     * Creates the {@link ITarget}.
     * 
     * @return Whether its a go to continue compiling.
     */
    private boolean setupBuild()
    {
        return true;
    }

    /**
     * Validate target file.
     * 
     * @throws MustSpecifyTarget
     * @throws IOError
     */
    protected void validateTargetFile() throws ConfigurationException
    {
        //        final String targetFile = config.getTargetFile();
        //        if (targetFile == null)
        //            throw new ConfigurationException.MustSpecifyTarget(null, null, -1);
        //
        //        final File file = new File(targetFile);
        //        if (!file.exists())
        //            throw new ConfigurationException.IOError(targetFile);
    }

    /**
     * Wait till the workspace to finish compilation and close.
     */
    protected void waitAndClose()
    {
        workspace.startIdleState();
        try
        {
            workspace.close();
        }
        finally
        {
            workspace.endIdleState(Collections
                    .<ICompilerProject, Set<ICompilationUnit>> emptyMap());
        }
    }

    static enum ExitCode
    {
        SUCCESS(0),
        PRINT_HELP(1),
        FAILED_WITH_PROBLEMS(2),
        FAILED_WITH_EXCEPTIONS(3),
        FAILED_WITH_CONFIG_PROBLEMS(4);

        ExitCode(int code)
        {
            this.code = code;
        }

        final int code;
    }
}
