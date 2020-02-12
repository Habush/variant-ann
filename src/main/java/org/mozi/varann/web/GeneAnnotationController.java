package org.mozi.varann.web;

import lombok.RequiredArgsConstructor;
import org.mozi.varann.data.records.TranscriptRecord;
import org.mozi.varann.services.GeneAnnotationExecutor;
import org.mozi.varann.util.AnnotationException;
import org.mozi.varann.util.AnnotationNotFoundException;
import org.mozi.varann.util.MultipleValuesException;
import org.mozi.varann.util.RegexPatterns;
import org.mozi.varann.web.models.VariantInfo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;

/**
 * @author <a href="mailto:hsamireh@gmail.com">Abdulrahman Semrie</a>
 * 1/29/20
 */
@Controller
@RequiredArgsConstructor
public class GeneAnnotationController {

    private final GeneAnnotationExecutor annotationExec;

    @CrossOrigin
    @RequestMapping(value = "/annotate/gene/{gene}", method = RequestMethod.GET)
    @ResponseBody
    public List<VariantInfo> annotateByGene(@PathVariable String gene) throws AnnotationNotFoundException, IOException {
        return annotationExec.annotateByGene(gene);
    }


    @CrossOrigin
    @RequestMapping(value = "/annotate/gene/id/{id}", method = RequestMethod.GET)
    @ResponseBody
    public List<VariantInfo> annotateByGeneId(@PathVariable String id) throws AnnotationNotFoundException, IOException {
        return annotationExec.annotateGeneById(id);
    }


    @CrossOrigin
    @RequestMapping(value = "/annotate/transcript/range/", method = RequestMethod.GET)
    @ResponseBody
    public List<TranscriptRecord> getTranscriptsInRange(@RequestParam(value = "q") String query) throws IOException, AnnotationException {
        Matcher match =  RegexPatterns.rangePattern.matcher(query);
        if(match.matches()){
            String contig = match.group(2);
            long start = Long.parseLong(match.group(3));
            long end = Long.parseLong(match.group(4));

            return annotationExec.getTranscriptsInRange(contig, start, end);
        } else {
            throw new AnnotationException("Unable to parse query " + query + " . Please check again");
        }
    }
}
