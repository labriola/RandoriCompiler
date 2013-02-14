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

package randori.compiler.internal.js.codegen.project;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import org.apache.flex.compiler.clients.IBackend;
import org.apache.flex.compiler.internal.as.codegen.TestWalkerBase;
import org.apache.flex.compiler.internal.units.SWCCompilationUnit;
import org.apache.flex.compiler.internal.units.SourceCompilationUnitFactory;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.IClassNode;
import org.apache.flex.compiler.tree.as.IDefinitionNode;
import org.apache.flex.compiler.tree.as.IFileNode;
import org.apache.flex.compiler.tree.as.IFunctionNode;
import org.apache.flex.compiler.tree.as.IGetterNode;
import org.apache.flex.compiler.tree.as.IInterfaceNode;
import org.apache.flex.compiler.tree.as.IPackageNode;
import org.apache.flex.compiler.tree.as.IScopedNode;
import org.apache.flex.compiler.tree.as.ISetterNode;
import org.apache.flex.compiler.tree.as.ITypeNode;
import org.apache.flex.compiler.tree.as.IVariableNode;
import org.apache.flex.compiler.units.ICompilationUnit;
import org.apache.flex.compiler.utils.EnvProperties;
import org.apache.flex.utils.FilenameNormalization;
import org.junit.Assert;

import randori.compiler.internal.constants.TestConstants;
import randori.compiler.internal.js.driver.RandoriBackend;
import randori.compiler.internal.js.utils.MetaDataUtils;

/**
 * @author Michael Schmalle
 */
public abstract class RandoriTestProjectBase extends TestWalkerBase
{
    private String normalizedMainFileName;

    protected IFileNode fileNode;

    protected IClassNode classNode;

    protected IInterfaceNode interfaceNode;

    @Override
    public void setUp()
    {
        super.setUp();
        String typeUnderTest = getTypeUnderTest().replaceAll("\\.", "/");
        normalizedMainFileName = FilenameNormalization.normalize(getBasePath()
                + "\\" + typeUnderTest + ".as");

        fileNode = compile(normalizedMainFileName);
        if (fileNode == null)
            return;
        Assert.assertNotNull(fileNode);
        ITypeNode type = (ITypeNode) findFirstDescendantOfType(fileNode,
                ITypeNode.class);
        Assert.assertNotNull(type);
        if (type instanceof IClassNode)
            classNode = (IClassNode) type;
        else if (type instanceof IInterfaceNode)
            interfaceNode = (IInterfaceNode) type;
    }

    abstract protected String getTypeUnderTest();

    protected String getBasePath()
    {
        return null;
    }

    private static EnvProperties env = EnvProperties.initiate();

    protected IFileNode compile(String main)
    {
        List<File> sourcePaths = new ArrayList<File>();
        addSourcePaths(sourcePaths);
        project.setSourcePath(sourcePaths);

        // Compile the code against playerglobal.swc.
        List<File> libraries = new ArrayList<File>();
        libraries.add(new File(FilenameNormalization.normalize(env.FPSDK
                + "\\11.1\\playerglobal.swc")));

        addLibrary(libraries);
        project.setLibraries(libraries);

        // Use the MXML 2009 manifest.
        //List<IMXMLNamespaceMapping> namespaceMappings = new ArrayList<IMXMLNamespaceMapping>();
        //IMXMLNamespaceMapping mxml2009 = new MXMLNamespaceMapping(
        //        "http://ns.adobe.com/mxml/2009", env.SDK
        //                + "\\frameworks\\mxml-2009-manifest.xml");
        //namespaceMappings.add(mxml2009);
        //project.setNamespaceMappings(namespaceMappings);

        ICompilationUnit cu = null;
        String normalizedMainFileName = FilenameNormalization.normalize(main);

        SourceCompilationUnitFactory compilationUnitFactory = project
                .getSourceCompilationUnitFactory();
        File normalizedMainFile = new File(normalizedMainFileName);
        if (compilationUnitFactory.canCreateCompilationUnit(normalizedMainFile))
        {
            Collection<ICompilationUnit> mainFileCompilationUnits = workspace
                    .getCompilationUnits(normalizedMainFileName, project);

            for (ICompilationUnit cu2 : mainFileCompilationUnits)
            {
                if (cu2 != null)
                    cu = cu2;
            }
        }

        if (cu == null)
            return null;

        // Build the AST.
        IFileNode fileNode = null;
        try
        {
            fileNode = (IFileNode) cu.getSyntaxTreeRequest().get().getAST();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        return fileNode;
    }

    protected void write()
    {
        @SuppressWarnings("unused")
        Collection<ICompilationUnit> units = project.getCompilationUnits();

        //        BinaryDependencyWriter writer = new BinaryDependencyWriter(project);
        //        StringWriter out = new StringWriter();
        //        writer.write(out, units);
    }

    protected Collection<ICompilationUnit> compileOrdered()
    {
        Collection<ICompilationUnit> units = project.getCompilationUnits();
        return units;
    }

    protected void compileFull()
    {
        // <packageName, <className, ICompilationUnit>>
        TreeMap<String, TreeMap<String, IFileNode>> map = new TreeMap<String, TreeMap<String, IFileNode>>();

        Collection<ICompilationUnit> compilationUnits = project
                .getCompilationUnits();

        for (ICompilationUnit iCompilationUnit : compilationUnits)
        {
            if (!(iCompilationUnit instanceof SWCCompilationUnit))
            {
                try
                {
                    IFileNode fileNode = (IFileNode) iCompilationUnit
                            .getSyntaxTreeRequest().get().getAST();
                    IASNode child = fileNode.getChild(0);
                    if (child instanceof IPackageNode)
                    {
                        IPackageNode pnode = (IPackageNode) child;

                        ITypeNode tnode = findTypeNode(pnode);
                        if (tnode != null)
                        {
                            if (MetaDataUtils.isExport(tnode.getDefinition())
                                    && tnode.getPackageName().indexOf("demo.") == -1)
                            {
                                TreeMap<String, IFileNode> packageMap = map
                                        .get(pnode.getName());
                                if (packageMap == null)
                                {
                                    packageMap = new TreeMap<String, IFileNode>();
                                    map.put(pnode.getName(), packageMap);
                                }

                                packageMap.put(tnode.getName(), fileNode);
                            }
                        }
                    }
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }

        // Globals first

        StringBuilder sb = new StringBuilder();
        sb.append("/** Compiled " + new Date().toString() + "*/\n\n");
        
        for (TreeMap<String, IFileNode> sub : map.values())
        {
            for (IFileNode fileNode : sub.values())
            {
                IASNode child = fileNode.getChild(0);
                IPackageNode pnode = (IPackageNode) child;

                ITypeNode tnode = findTypeNode(pnode);

                if (!MetaDataUtils.isGlobal((IClassNode) tnode))
                    continue;

                sb.append("\n// ====================================================\n");
                sb.append("// " + tnode.getQualifiedName() + "\n");
                sb.append("// ====================================================\n\n");
                backend = createBackend();
                writer = backend.createWriterBuffer(project);
                emitter = backend.createEmitter(writer);
                visitor = backend.createWalker(project, errors, emitter);

                visitor.visitFile(fileNode);
                //i++;

                sb.append(writer.toString());
            }
        }

        // the rest of the alphabetized framework

        for (TreeMap<String, IFileNode> sub : map.values())
        {
            for (IFileNode fileNode : sub.values())
            {
                IASNode child = fileNode.getChild(0);
                IPackageNode pnode = (IPackageNode) child;

                ITypeNode tnode = findTypeNode(pnode);

                if (MetaDataUtils.isGlobal((IClassNode) tnode))
                    continue;

                sb.append("\n// ====================================================\n");
                sb.append("// " + tnode.getQualifiedName() + "\n");
                sb.append("// ====================================================\n\n");
                backend = createBackend();
                writer = backend.createWriterBuffer(project);
                emitter = backend.createEmitter(writer);
                visitor = backend.createWalker(project, errors, emitter);

                visitor.visitFile(fileNode);
                //i++;

                sb.append(writer.toString());
            }
        }
        writeFile("C:\\Users\\Work\\Desktop\\Randori.as", sb.toString());
    }

    protected void compileAll()
    {
        Collection<ICompilationUnit> compilationUnits = compileOrdered();

        int i = 0;
        StringBuilder sb = new StringBuilder();
        for (ICompilationUnit iCompilationUnit : compilationUnits)
        {
            if (!(iCompilationUnit instanceof SWCCompilationUnit))
            {
                try
                {
                    IFileNode fileNode = (IFileNode) iCompilationUnit
                            .getSyntaxTreeRequest().get().getAST();
                    IASNode child = fileNode.getChild(0);
                    if (child instanceof IPackageNode)
                    {
                        IPackageNode pnode = (IPackageNode) child;
                        ITypeNode tnode = findTypeNode(pnode);
                        if (tnode != null)
                        {
                            if (MetaDataUtils.isExport(tnode.getDefinition()))
                            {
                                sb.append("// ====================================================\n");
                                sb.append("// "
                                        + tnode.getQualifiedName() + "\n");
                                sb.append("// ====================================================\n");
                                backend = createBackend();
                                writer = backend.createWriterBuffer(project);
                                emitter = backend.createEmitter(writer);
                                visitor = backend.createWalker(project, errors,
                                        emitter);

                                visitor.visitFile(fileNode);
                                i++;

                                sb.append(writer.toString());
                            }
                        }
                    }
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }
        //writeFile("C:\\Users\\Work\\Desktop\\Randori.as", sb.toString());
        System.err.println(i + "");
    }

    private static void writeFile(String filePath, String data)
    {
        BufferedWriter writer = null;
        try
        {
            writer = new BufferedWriter(new FileWriter(filePath));
            writer.write(data);
        }
        catch (IOException e)
        {
        }
        finally
        {
            try
            {
                if (writer != null)
                    writer.close();
            }
            catch (IOException e)
            {
            }
        }
    }

    // C:\Users\Work\Documents\git\RandoriAS\Randori\bin\Randori.swc
    @Override
    protected void addLibrary(List<File> libraries)
    {
        super.addLibrary(libraries);
        String base = TestConstants.RandoriASFramework;

        libraries.add(new File(FilenameNormalization.normalize(base
                + "HTMLCoreLib\\bin\\HTMLCoreLib.swc")));
        libraries.add(new File(FilenameNormalization.normalize(base
                + "JQuery\\bin\\JQuery.swc")));
        //        libraries.add(new File(FilenameNormalization.normalize(base
        //                + "Randori\\bin\\Randori.swc")));
        //        libraries.add(new File(FilenameNormalization.normalize(base
        //                + "RandoriGuiceJS\\bin\\RandoriGuiceJS.swc")));
    }

    @Override
    protected void addSourcePaths(List<File> sourcePaths)
    {
        super.addSourcePaths(sourcePaths);
        String base = TestConstants.RandoriASFramework;

        sourcePaths.add(new File(FilenameNormalization.normalize(base
                + "DemoApplication\\src")));
        sourcePaths.add(new File(FilenameNormalization.normalize(base
                + "Randori\\src")));
        sourcePaths.add(new File(FilenameNormalization.normalize(base
                + "RandoriGuiceJS\\src")));

    }

    @Override
    protected IBackend createBackend()
    {
        return new RandoriBackend();
    }

    protected IVariableNode findField(String name, IClassNode node)
    {
        IDefinitionNode[] nodes = node.getAllMemberNodes();
        for (IDefinitionNode inode : nodes)
        {
            if (inode.getName().equals(name))
                return (IVariableNode) inode;
        }
        return null;
    }

    protected IFunctionNode findFunction(String name, IClassNode node)
    {
        IDefinitionNode[] nodes = node.getAllMemberNodes();
        for (IDefinitionNode inode : nodes)
        {
            if (inode.getName().equals(name))
                return (IFunctionNode) inode;
        }
        return null;
    }

    protected void assertOut(String code)
    {
        mCode = writer.toString();
        //System.out.println(mCode);
        assertThat(mCode, is(code));
    }

    protected IGetterNode findGetter(String name, IClassNode node)
    {
        IDefinitionNode[] nodes = node.getAllMemberNodes();
        for (IDefinitionNode inode : nodes)
        {
            if (inode.getName().equals(name) && inode instanceof IGetterNode)
                return (IGetterNode) inode;
        }
        return null;
    }

    protected ISetterNode findSetter(String name, IClassNode node)
    {
        IDefinitionNode[] nodes = node.getAllMemberNodes();
        for (IDefinitionNode inode : nodes)
        {
            if (inode.getName().equals(name) && inode instanceof ISetterNode)
                return (ISetterNode) inode;
        }
        return null;
    }

    protected ITypeNode findTypeNode(IPackageNode node)
    {
        IScopedNode scope = node.getScopedNode();
        for (int i = 0; i < scope.getChildCount(); i++)
        {
            IASNode child = scope.getChild(i);
            if (child instanceof ITypeNode)
                return (ITypeNode) child;
        }
        return null;
    }
}
