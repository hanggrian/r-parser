package com.example.compiler.rparser;

import com.example.annotations.rparser.MyAnnotation;
import com.google.auto.service.AutoService;
import com.hendraanggrian.RClassParser;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

@AutoService(Processor.class)
public class ExampleProcessor extends AbstractProcessor {

    private RClassParser parser;

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return new HashSet<>(Collections.singletonList(MyAnnotation.class.getCanonicalName()));
    }

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        parser = new RClassParser.Builder(env)
                .supportedAnnotations(MyAnnotation.class)
                .supportedTypes("layout", "string")
                .build();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        parser.scan(roundEnvironment);
        return false;
    }
}