package com.example.compiler.rparser;

import com.example.annotations.rparser.Parse;
import com.google.auto.service.AutoService;
import com.hendraanggrian.RParser;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

@AutoService(Processor.class)
public class ExampleProcessor extends AbstractProcessor {

    private static final Set<Class<? extends Annotation>> SUPPORTED_ANNOTATIONS = new HashSet<>(Collections.<Class<? extends Annotation>>singletonList(Parse.class));

    private Filer filer;
    private RParser parser;

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> supported = new HashSet<>();
        for (Class<? extends Annotation> cls : SUPPORTED_ANNOTATIONS)
            supported.add(cls.getCanonicalName());
        return supported;
    }

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        filer = env.getFiler();
        parser = RParser.builder(env)
                .setSupportedAnnotations(SUPPORTED_ANNOTATIONS)
                .setSupportedTypes("layout", "string", "mipmap")
                .build();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        parser.scan(roundEnvironment);
        TypeSpec.Builder builder = TypeSpec.classBuilder("Testing")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        for (Element element : roundEnvironment.getElementsAnnotatedWith(Parse.class))
            builder.addField(FieldSpec.builder(String.class, element.getSimpleName().toString())
                    .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                    .initializer("$S", parser.parse("com.example.rparser", element.getAnnotation(Parse.class).value()))
                    .build());
        JavaFile javaFile = JavaFile.builder("com.example.rparser", builder.build())
                .build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException ignored) {
        }
        return false;
    }
}