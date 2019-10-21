package com.walmartlabs.concord.plugins.tool;

import io.airlift.airline.OptionType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface OptionWithEquals {
    OptionType type() default OptionType.COMMAND;

    String title() default "";

    String[] name();

    String description() default "";

    boolean required() default false;

    int arity() default -2147483648;

    boolean hidden() default false;

    String[] allowedValues() default {};
}
