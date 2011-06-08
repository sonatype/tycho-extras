package org.eclipse.tycho.osgitest.emma;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.maven.project.MavenProject;

/**
 * Searches all Java source files of a Maven projects and generates a list of class names.
 */
class ClassNameCollector {

    /**
     * Maven project to search.
     */
    private MavenProject project;

    /**
     * Constructor.
     * 
     * @param project
     *            Maven to project to search
     */
    public ClassNameCollector(MavenProject project) {
        this.project = project;
    }

    /**
     * Searches for Java source files in compile source roots and returns a set of found class
     * names.
     */
    public Set<String> collectJavaClassNames() {
        Set<String> javaClassesInSources = new TreeSet<String>();
        for (String compileSourceRoot : project.getCompileSourceRoots()) {
            addSourcePath(javaClassesInSources, new File(compileSourceRoot));
        }

        return javaClassesInSources;
    }

    private boolean addSourcePath(Set<String> javaClassesInSources, File sourcePath) {
        if (sourcePath.isDirectory()) {
            Collection<String> javaFiles = collectJavaFiles(sourcePath);

            if (!javaFiles.isEmpty()) {
                javaClassesInSources.addAll(javaFiles);
                return true;
            }
        }
        return false;
    }

    private Collection<String> collectJavaFiles(File directory) {
        List<String> classNames = new ArrayList<String>();
        collectJavaFiles("", directory, classNames);
        return classNames;
    }

    private void collectJavaFiles(String prefix, File directory, Collection<String> classNames) {
        String[] names = directory.list();
        if (names != null) {
            for (String name : names) {
                File file = new File(directory, name);
                if (file.isFile()) {
                    if (name.endsWith(".java")) {
                        classNames.add(prefix.concat(name.substring(0, name.length() - 5)));
                    }
                } else if (file.isDirectory()) {
                    collectJavaFiles(prefix + name + '.', file, classNames);
                }
            }
        }
    }

}
