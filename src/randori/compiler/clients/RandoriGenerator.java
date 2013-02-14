package randori.compiler.clients;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.flex.compiler.internal.projects.FlexProject;
import org.apache.flex.compiler.internal.units.SourceCompilationUnitFactory;
import org.apache.flex.compiler.internal.workspaces.Workspace;
import org.apache.flex.compiler.tree.as.IFileNode;
import org.apache.flex.compiler.units.ICompilationUnit;
import org.apache.flex.utils.FilenameNormalization;

public class RandoriGenerator
{

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        RandoriGenerator generator = new RandoriGenerator();
        generator.generate();
    }

    protected static Workspace workspace = new Workspace();

    protected FlexProject project;

    private void generate()
    {
        // TODO Auto-generated method stub

    }
//
//    protected IFileNode compile(String main)
//    {
//        List<File> sourcePaths = new ArrayList<File>();
//        project.setSourcePath(sourcePaths);
//
//        List<File> libraries = new ArrayList<File>();
//        libraries.add(new File(FilenameNormalization.normalize(env.FPSDK
//                + "\\11.1\\playerglobal.swc")));
//
//        project.setLibraries(libraries);
//
//        ICompilationUnit cu = null;
//        String normalizedMainFileName = FilenameNormalization.normalize(main);
//
//        SourceCompilationUnitFactory compilationUnitFactory = project
//                .getSourceCompilationUnitFactory();
//        File normalizedMainFile = new File(normalizedMainFileName);
//        if (compilationUnitFactory.canCreateCompilationUnit(normalizedMainFile))
//        {
//            Collection<ICompilationUnit> mainFileCompilationUnits = workspace
//                    .getCompilationUnits(normalizedMainFileName, project);
//
//            for (ICompilationUnit cu2 : mainFileCompilationUnits)
//            {
//                if (cu2 != null)
//                    cu = cu2;
//            }
//        }
//
//        if (cu == null)
//            return null;
//
//        // Build the AST.
//        IFileNode fileNode = null;
//        try
//        {
//            fileNode = (IFileNode) cu.getSyntaxTreeRequest().get().getAST();
//        }
//        catch (InterruptedException e)
//        {
//            e.printStackTrace();
//        }
//
//        return fileNode;
//    }

}
