package io.github.mocanjie.tools.dict;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE)
public @interface MyDict {
    String name();
    String defaultDesc() default "";

    FieldAnnotation[] fieldAnnotations() default {};

}
