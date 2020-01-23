package org.mozi.varann.web;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mozi.varann.services.AnnotationExecutor;
import org.mozi.varann.util.AnnotationException;
import org.mozi.varann.web.data.GeneInfo;
import org.mozi.varann.web.data.VariantInfo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@RequiredArgsConstructor
public class AnnotationController {

    private static final Pattern pat = Pattern.compile("(chr)?(\\d+):(\\d+)-(\\d+)");
    private final AnnotationExecutor annotationExec;
    private static final Logger logger = LogManager.getLogger(AnnotationController.class);

    @RequestMapping(value = "/annotate/{id}", method = RequestMethod.GET)
    @ResponseBody
    public VariantInfo annotateById(@PathVariable String id) throws AnnotationException, IOException {
        return annotationExec.annotateId(id);
    }

    @RequestMapping(value = "/annotate", method = RequestMethod.GET)
    @ResponseBody
    public VariantInfo annotateByHgvs(@RequestParam(value = "hgvs") String hgvs) throws AnnotationException, IOException {
        return annotationExec.annotateHgvs(hgvs);
    }

    @RequestMapping(value = "/annotate-multi", method = RequestMethod.GET)
    @ResponseBody
    public CompletableFuture<List<VariantInfo>> annotateVariants(@RequestBody ArrayList<String> ids) throws IOException {
        logger.info("Starting multiple variant annotation");
        return annotationExec.annotateMultipleVariants(ids);
    }

    @RequestMapping(value = "/annotate/range/", method = RequestMethod.GET)
    @ResponseBody
    public List<VariantInfo> annotateByRange(@RequestParam(value = "q") String query) throws IOException, AnnotationException {
        Matcher match =  pat.matcher(query);
        if(match.matches()){
            String contig = match.group(2);
            long start = Long.parseLong(match.group(3));
            long end = Long.parseLong(match.group(4));

            return annotationExec.annotateByRange(contig, start, end);
        } else {
            throw new AnnotationException("Unable to parse query " + query + " . Please check again");
        }
    }

    @RequestMapping(value = "/annotate/gene/{gene}", method = RequestMethod.GET)
    @ResponseBody
    public GeneInfo annotateByGene(@PathVariable String gene) throws AnnotationException, IOException {
        return annotationExec.annotateByGene(gene);
    }

    @RequestMapping(value = "/annotate/entrez/{entrezID}", method = RequestMethod.GET)
    @ResponseBody
    public GeneInfo annotateByEntrezID(@PathVariable String entrezID) throws AnnotationException, IOException {
        return annotationExec.annotateByEntrezId(entrezID);
    }

    @RequestMapping(value = "/annotate/gene/id/{id}", method = RequestMethod.GET)
    @ResponseBody
    public GeneInfo annotateByGeneId(@PathVariable String id) throws AnnotationException, IOException {
        return annotationExec.annotateGeneById(id);
    }

}
