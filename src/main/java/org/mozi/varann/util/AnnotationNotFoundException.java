package org.mozi.varann.util;

/**
 * @author <a href="mailto:hsamireh@gmail.com">Abdulrahman Semrie</a>
 * 2/8/20
 */
public class AnnotationNotFoundException extends Exception{
    public String value;

    public AnnotationNotFoundException(String msg) {
        super(msg);
    }
}
