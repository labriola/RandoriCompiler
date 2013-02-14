package randori.compiler.internal.js.driver;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.flex.compiler.definitions.IClassDefinition;
import org.apache.flex.compiler.internal.units.SWCCompilationUnit;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.IClassNode;
import org.apache.flex.compiler.tree.as.IFileNode;
import org.apache.flex.compiler.tree.as.IPackageNode;
import org.apache.flex.compiler.tree.as.IScopedNode;
import org.apache.flex.compiler.units.ICompilationUnit;

import randori.compiler.internal.js.utils.MetaDataUtils;

public class BinaryDependencyWriter
{
    ICompilerProject project;

    private Writer out;

    /*
    
    function handleClass( class, dependency ) {

         if ( !classHashMap[ class ] ) {
                 classHashMap[ class ] = {};
                 classHashMap[ class ].isRoot = true;
                 classHashMap[ class ].dependencies = [];
         }

         if ( dependency ) {
                 classHashMap[ class ].dependencies.push( dependency );
         }

         if ( class.hasSuperClass() ) {
                 //We have  super class, we arent a root
           classHashMap[ class ].isRoot = false;
                 handleClass( class.getSuperClass(), class );
         }
    }

    */

    private HashMap<IClassDefinition, BinaryEntry> classHashMap = new HashMap<IClassDefinition, BinaryEntry>();

    class BinaryEntry
    {
        private boolean isRoot = false;
        private List<IClassDefinition> dependencies = new ArrayList<IClassDefinition>();
    }

    public BinaryDependencyWriter(ICompilerProject project)
    {
        this.project = project;
    }

    private void handleClass(IClassDefinition targetClass,
            IClassDefinition dependency)
    {
        if (!classHashMap.containsKey(targetClass))
        {
            BinaryEntry entry = new BinaryEntry();
            entry.isRoot = true;
            entry.dependencies = new ArrayList<IClassDefinition>();
            classHashMap.put(targetClass, entry);

            IClassDefinition baseClass = targetClass.resolveBaseClass(project);
            if (baseClass != null && !baseClass.getBaseName().equals("Object"))
            {
                //We have  super class, we arent a root
                classHashMap.get(targetClass).isRoot = false;
                handleClass(baseClass, targetClass);
            }
        }

        if (dependency != null)
        {
            if (!classHashMap.get(targetClass).dependencies
                    .contains(dependency))
                classHashMap.get(targetClass).dependencies.add(dependency);
        }

    }

    public void write(Writer out, Collection<ICompilationUnit> units)
    {
        this.out = out;

        List<IClassNode> nodes = new ArrayList<IClassNode>();

        for (ICompilationUnit unit : units)
        {
            if (!(unit instanceof SWCCompilationUnit))
            {
                try
                {
                    IFileNode fileNode = (IFileNode) unit
                            .getSyntaxTreeRequest().get().getAST();
                    IASNode child = fileNode.getChild(0);
                    if (child instanceof IPackageNode)
                    {
                        IPackageNode pnode = (IPackageNode) child;
                        IClassNode classNode = findClassNode(pnode);
                        if (classNode != null)
                        {
                            if (MetaDataUtils.isExport(classNode
                                    .getDefinition()))
                            {
                                nodes.add(classNode);
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

        for (IClassNode classNode : nodes)
        {
            if (classNode.getDefinition().getBaseName().equals("VerticalTabs"))
            {
                handleClass(classNode.getDefinition(), null);
            }

        }

        for (Entry<IClassDefinition, BinaryEntry> entry : classHashMap
                .entrySet())
        {
            if (entry.getValue().isRoot)
            {
                outputHierarchy(entry);
            }
        }
    }

    private void outputHierarchy(Entry<IClassDefinition, BinaryEntry> entry)
    {
        write("Root------------------------------------\n");
        doWrite(entry.getKey());

        if (entry.getValue().dependencies.size() > 0)
            write("children----------------\n");
        for (IClassDefinition definition : entry.getValue().dependencies)
        {
            doWrite(definition);
        }
    }

    private void write(String value)
    {
        try
        {
            out.write(value);
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void doWrite(IClassDefinition definition)
    {
        write(definition.getQualifiedName());
        write("\n");
    }

    //
    //    for every bloody class {
    //                    handleClass( class );
    //    }
    //
    //    for every entry in classHashMap {
    //            if ( entry.isRoot ) {
    //                    //We have a root
    //                    outputHierarchy( entry );
    //            }
    //    }
    //
    //    function outputHierarchy( entry ) {
    //            //output this class
    //            for ( var subEntry in entry.dependencies ) {
    //                    outputHierarchy( subEntry );
    //            }
    //    }

    protected IClassNode findClassNode(IPackageNode node)
    {
        IScopedNode scope = node.getScopedNode();
        for (int i = 0; i < scope.getChildCount(); i++)
        {
            IASNode child = scope.getChild(i);
            if (child instanceof IClassNode)
                return (IClassNode) child;
        }
        return null;
    }
}
