package com.example.doruked;


import java.lang.annotation.*;


/**
 * Indicates that a given type does not provide new or different behavior.
 *
 * Any listed method are viewable for convenience, and purely delegate to it's super class
 * if behavior is provided
 *
 * Use:
 * This is often used for types that simply define the generics. These types reduce reduce generic weight.
 *They may also wish to list it's methods for easier viewing.
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface View { }
