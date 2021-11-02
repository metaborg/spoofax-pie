package mb.spoofax.lwb.compiler.util;

import mb.common.util.ResourceUtil;
import mb.pie.api.ExecContext;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.ResourceKey;
import mb.resource.WritableResource;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;

import java.io.IOException;

public abstract class TaskCopyUtil {
    public static void copy(ExecContext context, ResourceKey input, ResourcePath output) throws IOException {
        final HierarchicalResource outputFile = context.getHierarchicalResource(output).ensureFileExists();
        ResourceUtil.copy(context.require(input), outputFile);
        context.provide(outputFile);
    }

    public static void copyAll(ExecContext context, ResourcePath sourceDirectoryPath, ResourcePath targetDirectoryPath, ResourceWalker walker, ResourceMatcher matcher) throws IOException {
        context.require(sourceDirectoryPath, ResourceStampers.hashDirRec(walker, matcher))
            .walkForEach(walker, matcher, (source) -> {
                final ResourcePath sourcePath = source.getPath();
                final String relativePath = sourceDirectoryPath.relativize(sourcePath);
                final ResourcePath targetPath = targetDirectoryPath.appendRelativePath(relativePath);
                switch(source.getType()) {
                    case File:
                        copy(context, sourcePath, targetPath);
                        break;
                    case Directory:
                        final HierarchicalResource target = context.getHierarchicalResource(targetPath);
                        target.createDirectory(true);
                        break;
                    default: // Ignore
                }
            });
    }

    public static void copyAll(ExecContext context, ResourcePath sourceDirectoryPath, ResourcePath targetDirectoryPath, ResourceMatcher matcher) throws IOException {
        copyAll(context, sourceDirectoryPath, targetDirectoryPath, ResourceWalker.ofTrue(), matcher);
    }
}
