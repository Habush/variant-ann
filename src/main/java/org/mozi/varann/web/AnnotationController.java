package org.mozi.varann.web;

import org.mozi.varann.AnnotationHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class AnnotationController {

    @Autowired
    private AnnotationHelper annotationHelper;

    @RequestMapping(value = "/annotate", method = RequestMethod.GET)
    public String annotate(@RequestParam(value = "db", defaultValue="1k") String db, @RequestParam(value = "ref", defaultValue = "hg38") String ref,
                           @RequestParam(value = "trans", defaultValue = "hg38_ensembl") String transcript, @RequestParam(value = "variant") String variant){
        return annotationHelper.annotateVariant(variant, db, ref, transcript).toString();
    }
    @RequestMapping(value = "/annotate/{id}", method = RequestMethod.GET)
    public String annotateById(@RequestParam(value = "db", defaultValue="1k") String db, @RequestParam(value = "ref", defaultValue = "hg38") String ref,
                           @RequestParam(value = "trans", defaultValue = "hg38_ensembl") String transcript,  @PathVariable String id){
        return annotationHelper.annotateVariant(id, db, ref, transcript).toString();
    }
}
