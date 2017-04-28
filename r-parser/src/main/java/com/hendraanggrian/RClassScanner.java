package com.hendraanggrian;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeScanner;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

class RClassScanner extends TreeScanner {
    // Maps the currently evaulated rPackageName to R Classes
    private final Map<String, Set<String>> rClasses = new LinkedHashMap<>();
    private String currentPackageName;

    @Override
    public void visitSelect(JCTree.JCFieldAccess jcFieldAccess) {
        Symbol symbol = jcFieldAccess.sym;
        if (symbol != null
                && symbol.getEnclosingElement() != null
                && symbol.getEnclosingElement().getEnclosingElement() != null
                && symbol.getEnclosingElement().getEnclosingElement().enclClass() != null) {
            Set<String> rClassSet = rClasses.get(currentPackageName);
            if (rClassSet == null) {
                rClassSet = new HashSet<>();
                rClasses.put(currentPackageName, rClassSet);
            }
            rClassSet.add(symbol.getEnclosingElement().getEnclosingElement().enclClass().className());
        }
    }

    Map<String, Set<String>> getRClasses() {
        return rClasses;
    }

    void setCurrentPackageName(String respectivePackageName) {
        this.currentPackageName = respectivePackageName;
    }
}