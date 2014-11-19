package com.amos;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;

/**
 * Created by lixin on 14-11-14
 */
public class AnnotationTest {

    public static void main(String args[]) throws Exception {
        Constructor<?>[] myTags = Class.forName("MyTagTest").getDeclaredConstructors();
        for(Constructor myTag:myTags){
            for(Annotation annotation:myTag.getAnnotations()){
                System.out.println(annotation);
            }
        }

    }

}
