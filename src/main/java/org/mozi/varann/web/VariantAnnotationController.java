package org.mozi.varann.web;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mozi.varann.services.VariantAnnotationExecutor;
import org.mozi.varann.util.AnnotationException;
import org.mozi.varann.util.AnnotationNotFoundException;
import org.mozi.varann.util.MultipleValuesException;
import org.mozi.varann.util.RegexPatterns;
import org.mozi.varann.web.models.MultipleVariantResult;
import org.mozi.varann.web.models.VariantInfo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@CrossOrigin
@RequiredArgsConstructor
public class VariantAnnotationController {

    private static Pattern changeStr = Pattern.compile("(?:chr)?([\\d|XYMTxymt]+):(\\d+):([GCTAgcta]+)*:([GCTAgcta]+)*");

    private final VariantAnnotationExecutor annotationExec;
    private static final Logger logger = LogManager.getLogger(VariantAnnotationController.class);

    @RequestMapping(value = "/annotate/variant/{id}", method = RequestMethod.GET)
    @ResponseBody
    public VariantInfo annotateById(@PathVariable String id) throws AnnotationNotFoundException, MultipleValuesException, IOException {
        return annotationExec.annotateId(id);
    }

    @RequestMapping(value = "/annotate/variant", method = RequestMethod.GET)
    @ResponseBody
    public VariantInfo annotateByHgvs(@RequestParam(value = "hgvs") String hgvs) throws AnnotationNotFoundException, MultipleValuesException, IOException {
        if(RegexPatterns.hgvsMatch(hgvs)){
            if(hgvs.contains("chr")){
                hgvs = hgvs.substring(3);
            }
            return annotationExec.annotateHgvs(hgvs);
        } else {
            throw new AnnotationException("Unable to parse hgvs genomic reference " + hgvs + " . Please check again");
        }
    }

    @RequestMapping(value = "/annotate/variant/change/{val}", method = RequestMethod.GET)
    @ResponseBody
    public VariantInfo annotateByChangeString(@PathVariable String val) throws AnnotationNotFoundException, MultipleValuesException, IOException {
        Matcher matcher = changeStr.matcher(val);
        if(matcher.matches()){
            String ref = matcher.group(3) != null ? matcher.group(3) : "-";
            String alt = matcher.group(4) != null ? matcher.group(4) : "-";
            return annotationExec.annotateChangeString(matcher.group(1),matcher.group(2), ref,alt);
        } else {
            throw new AnnotationException("Unable to parse string " + val + " . Please check again");
        }
    }

    @RequestMapping(value = "/annotate/variant/multi", method = RequestMethod.GET)
    @ResponseBody
    public CompletableFuture<MultipleVariantResult> annotateVariants(@RequestBody ArrayList<String> ids) throws IOException {
        logger.info("Starting multiple variant annotation");
        return annotationExec.annotateMultipleVariants(ids);
    }

    @CrossOrigin
    @RequestMapping(value = "/annotate/variant/range/", method = RequestMethod.GET)
    @ResponseBody
    public List<VariantInfo> annotateByRange(@RequestParam(value = "q") String query, @RequestParam(value = "limit") int limit) throws IOException, AnnotationException {
        Matcher match =  RegexPatterns.rangePattern.matcher(query);
        if(match.matches()){
            String contig = match.group(1);
            long start = Long.parseLong(match.group(2));
            long end = Long.parseLong(match.group(3));

            return annotationExec.annotateByRange(contig, start, end, limit);
        } else {
            throw new AnnotationException("Unable to parse query " + query + " . Please check again");
        }
    }

}
