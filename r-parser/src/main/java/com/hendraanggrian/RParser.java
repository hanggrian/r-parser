package com.hendraanggrian;

import com.squareup.javapoet.ClassName;
import com.sun.source.util.Trees;
import com.sun.tools.javac.tree.JCTree;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 */
public final class RParser {

    private final Trees trees;
    private final Types typeUtils;
    private final Elements elementUtils;
    private final Collection<Class<? extends Annotation>> supportedAnnotations;
    private final Collection<String> supportedTypes;
    private final Map<QualifiedId, Id> symbols;

    private RParser(Trees trees, Types typesUtils, Elements elementUtils, Collection<Class<? extends Annotation>> supportedAnnotations, Collection<String> supportedTypes) {
        this.trees = trees;
        this.typeUtils = typesUtils;
        this.elementUtils = elementUtils;
        this.supportedAnnotations = supportedAnnotations;
        this.supportedTypes = supportedTypes;
        this.symbols = new LinkedHashMap<>();
    }

    public String parse(String packageName, int id) {
        QualifiedId qualifiedId = new QualifiedId(packageName, id);
        if (symbols.get(qualifiedId) == null)
            symbols.put(qualifiedId, new Id(qualifiedId.id));
        return symbols.get(qualifiedId).code.toString();
    }

    public void scan(RoundEnvironment env) {
        if (trees == null) return;
        RClassScanner scanner = new RClassScanner();
        for (Class<? extends Annotation> annotation : supportedAnnotations) {
            for (Element element : env.getElementsAnnotatedWith(annotation)) {
                JCTree tree = (JCTree) trees.getTree(element, getMirror(element, annotation));
                if (tree != null) { // tree can be null if the references are compiled types and not source
                    String respectivePackageName = elementUtils.getPackageOf(element).getQualifiedName().toString();
                    scanner.setCurrentPackageName(respectivePackageName);
                    tree.accept(scanner);
                }
            }
        }
        for (Map.Entry<String, Set<String>> packageNameToRClassSet : scanner.getRClasses().entrySet()) {
            String respectivePackageName = packageNameToRClassSet.getKey();
            for (String rClass : packageNameToRClassSet.getValue()) {
                parseRClass(respectivePackageName, rClass);
            }
        }
    }

    private void parseCompiledR(String respectivePackageName, TypeElement rClass) {
        for (Element element : rClass.getEnclosedElements()) {
            String innerClassName = element.getSimpleName().toString();
            if (supportedTypes.contains(innerClassName)) {
                for (Element enclosedElement : element.getEnclosedElements()) {
                    if (enclosedElement instanceof VariableElement) {
                        VariableElement variableElement = (VariableElement) enclosedElement;
                        Object value = variableElement.getConstantValue();

                        if (value instanceof Integer) {
                            int id = (Integer) value;
                            ClassName rClassName = ClassName.get(elementUtils.getPackageOf(variableElement).toString(), "R", innerClassName);
                            String resourceName = variableElement.getSimpleName().toString();
                            QualifiedId qualifiedId = new QualifiedId(respectivePackageName, id);
                            symbols.put(qualifiedId, new Id(id, rClassName, resourceName));
                        }
                    }
                }
            }
        }
    }

    private void parseRClass(String respectivePackageName, String rClass) {
        Element element;
        try {
            element = elementUtils.getTypeElement(rClass);
        } catch (MirroredTypeException mte) {
            element = typeUtils.asElement(mte.getTypeMirror());
        }
        JCTree tree = (JCTree) trees.getTree(element);
        if (tree != null) { // tree can be null if the references are compiled types and not source
            IdScanner idScanner = new IdScanner(symbols, elementUtils.getPackageOf(element).getQualifiedName().toString(), respectivePackageName, supportedTypes);
            tree.accept(idScanner);
        } else {
            parseCompiledR(respectivePackageName, (TypeElement) element);
        }
    }

    private static AnnotationMirror getMirror(Element element, Class<? extends Annotation> annotation) {
        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors())
            if (annotationMirror.getAnnotationType().toString().equals(annotation.getCanonicalName()))
                return annotationMirror;
        return null;
    }

    public static Builder builder(ProcessingEnvironment env) {
        try {
            return builder(Trees.instance(env), env.getTypeUtils(), env.getElementUtils());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Builder builder(Trees trees, Types typesUtils, Elements elementUtils) {
        return new Builder(trees, typesUtils, elementUtils);
    }

    public static final class Builder {
        private final Trees trees;
        private final Types typeUtils;
        private final Elements elementUtils;
        private Set<Class<? extends Annotation>> supportedAnnotations;
        private Set<String> supportedTypes;

        private Builder(Trees trees, Types typesUtils, Elements elementUtils) {
            this.trees = trees;
            this.typeUtils = typesUtils;
            this.elementUtils = elementUtils;
        }

        public Builder setSupportedAnnotations(Set<Class<? extends Annotation>> annotations) {
            this.supportedAnnotations = annotations;
            return this;
        }

        @SafeVarargs
        public final Builder setSupportedAnnotations(Class<? extends Annotation>... annotations) {
            return setSupportedAnnotations(new HashSet<>(Arrays.asList(annotations)));
        }

        public Builder setSupportedTypes(Set<String> types) {
            this.supportedTypes = types;
            return this;
        }

        public Builder setSupportedTypes(String... types) {
            return setSupportedTypes(new HashSet<>(Arrays.asList(types)));
        }

        public RParser build() {
            if (supportedAnnotations == null || supportedTypes == null)
                throw new IllegalStateException("Supported annotations and types must be set.");
            return new RParser(trees, typeUtils, elementUtils, supportedAnnotations, supportedTypes);
        }
    }
}