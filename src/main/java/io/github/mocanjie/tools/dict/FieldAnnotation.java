package io.github.mocanjie.tools.dict;

import io.github.mocanjie.tools.dict.entity.Var;

public @interface FieldAnnotation {
    String fullAnnotationName();
    Var[] vars();
}
