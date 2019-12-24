package org.mozi.varann.web;

import lombok.RequiredArgsConstructor;
import org.mozi.varann.web.data.VariantInfo;
import org.mozi.varann.util.AnnotationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class AnnotationController {


    private final AnnotationExecutor annotationExec;

    @RequestMapping(value = "/annotate/{id}", method = RequestMethod.GET)
    @ResponseBody
    public VariantInfo annotateById(@PathVariable String id) throws AnnotationException, IOException {
        return annotationExec.annotateId(id);
    }
    @RequestMapping(value = "/annotate", method = RequestMethod.GET)
    @ResponseBody
    public VariantInfo annotateByHgvs(@RequestParam(value="hgvs") String hgvs) throws AnnotationException, IOException {
        return annotationExec.annotateHgvs(hgvs);
    }

    @RequestMapping(value = "/annotate/gene/{gene}", method = RequestMethod.GET)
    @ResponseBody
    public List<VariantInfo> annotateByGene(@PathVariable String gene, @RequestParam(value = "from", defaultValue="0") int from,
                                            @RequestParam(value = "from", defaultValue="10") int size) throws AnnotationException, IOException {
        return annotationExec.annotateByGene(gene, from, size);
    }

}
