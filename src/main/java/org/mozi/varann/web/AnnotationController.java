package org.mozi.varann.web;

import org.mozi.varann.AnnotationHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AnnotationController {

    @Autowired
    private AnnotationHelper annotationHelper;

    @RequestMapping(value = "/annotate", method = RequestMethod.GET)
    public String annotate(@RequestParam(value = "db", defaultValue="1k") String db, @RequestParam(value = "ref", defaultValue = "hg38") String ref,
                           @RequestParam(value = "trans", defaultValue = "ensembl") String transcript, @RequestParam(value = "variant") String variant){
        return annotationHelper.annotateVariant(variant, db, ref, transcript).toString();
    }
}
