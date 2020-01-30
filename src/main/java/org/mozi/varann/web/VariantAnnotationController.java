package org.mozi.varann.web;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mozi.varann.services.AnnotationExecutor;
import org.mozi.varann.util.AnnotationException;
import org.mozi.varann.util.RegexPatterns;
import org.mozi.varann.web.data.GeneInfo;
import org.mozi.varann.web.data.VariantInfo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;

@Controller
@RequiredArgsConstructor
public class VariantAnnotationController {

    private final AnnotationExecutor annotationExec;
    private static final Logger logger = LogManager.getLogger(VariantAnnotationController.class);

    @RequestMapping(value = "/annotate/variant/{id}", method = RequestMethod.GET)
    @ResponseBody
    public VariantInfo annotateById(@PathVariable String id) throws AnnotationException, IOException {
        return annotationExec.annotateId(id);
    }

    @RequestMapping(value = "/annotate/variant", method = RequestMethod.GET)
    @ResponseBody
    public VariantInfo annotateByHgvs(@RequestParam(value = "hgvs") String hgvs) throws AnnotationException, IOException {
        if(RegexPatterns.hgvsMatch(hgvs)){
            if(hgvs.contains("chr")){
                hgvs = hgvs.substring(3);
            }
            return annotationExec.annotateHgvs(hgvs);
        } else {
            throw new AnnotationException("Unable to parse hgvs genomic reference " + hgvs + " . Please check again");
        }
    }

    @RequestMapping(value = "/annotate/variant/multi", method = RequestMethod.GET)
    @ResponseBody
    public CompletableFuture<List<VariantInfo>> annotateVariants(@RequestBody ArrayList<String> ids) throws IOException {
        logger.info("Starting multiple variant annotation");
        return annotationExec.annotateMultipleVariants(ids);
    }

    @CrossOrigin
    @RequestMapping(value = "/annotate/variant/range/", method = RequestMethod.GET)
    @ResponseBody
    public List<VariantInfo> annotateByRange(@RequestParam(value = "q") String query) throws IOException, AnnotationException {
        Matcher match =  RegexPatterns.rangePattern.matcher(query);
        if(match.matches()){
            String contig = match.group(2);
            long start = Long.parseLong(match.group(3));
            long end = Long.parseLong(match.group(4));

            return annotationExec.annotateByRange(contig, start, end);
        } else {
            throw new AnnotationException("Unable to parse query " + query + " . Please check again");
        }
    }

}
