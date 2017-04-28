package com.hendraanggrian;

import com.squareup.javapoet.ClassName;
import com.sun.source.tree.ClassTree;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeScanner;

import java.util.Collection;
import java.util.Map;

class IdScanner extends TreeScanner {
    private final Map<QualifiedId, Id> ids;
    private final String rPackageName;
    private final String respectivePackageName;
    private Collection<String> supportedTypes;

    IdScanner(Map<QualifiedId, Id> ids, String rPackageName, String respectivePackageName, Collection<String> supportedTypes) {
        this.ids = ids;
        this.rPackageName = rPackageName;
        this.respectivePackageName = respectivePackageName;
        this.supportedTypes = supportedTypes;
    }

    @Override
    public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
        for (JCTree tree : jcClassDecl.defs) {
            if (tree instanceof ClassTree) {
                ClassTree classTree = (ClassTree) tree;
                String className = classTree.getSimpleName().toString();
                if (supportedTypes.contains(className)) {
                    ClassName rClassName = ClassName.get(rPackageName, "R", className);
                    VarScanner scanner = new VarScanner(ids, rClassName, respectivePackageName);
                    ((JCTree) classTree).accept(scanner);
                }
            }
        }
    }
}