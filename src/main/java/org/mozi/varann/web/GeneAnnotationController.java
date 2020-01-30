package org.mozi.varann.web;

import lombok.RequiredArgsConstructor;
import org.mozi.varann.data.records.TranscriptRecord;
import org.mozi.varann.services.GeneAnnotationExecutor;
import org.mozi.varann.util.AnnotationException;
import org.mozi.varann.util.RegexPatterns;
import org.mozi.varann.web.data.GeneInfo;
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
@CrossOrigin
@RequiredArgsConstructor
public class GeneAnnotationController {

    private final GeneAnnotationExecutor annotationExec;

    @RequestMapping(value = "/annotate/gene/{gene}", method = RequestMethod.GET)
    @ResponseBody
    public GeneInfo annotateByGene(@PathVariable String gene) throws AnnotationException, IOException {
        return annotationExec.annotateByGene(gene);
    }

    @RequestMapping(value = "/annotate/gene/entrez/{entrezID}", method = RequestMethod.GET)
    @ResponseBody
    public GeneInfo annotateByEntrezID(@PathVariable String entrezID) throws AnnotationException, IOException {
        return annotationExec.annotateByEntrezId(entrezID);
    }

    @RequestMapping(value = "/annotate/gene/id/{id}", method = RequestMethod.GET)
    @ResponseBody
    public GeneInfo annotateByGeneId(@PathVariable String id) throws AnnotationException, IOException {
        return annotationExec.annotateGeneById(id);
    }

    @CrossOrigin
    @RequestMapping(value = "/annotate/gene/range/", method = RequestMethod.GET)
    @ResponseBody
    public List<GeneInfo> getGenesInRange(@RequestParam(value = "q") String query) throws IOException, AnnotationException {
        Matcher match =  RegexPatterns.rangePattern.matcher(query);
        if(match.matches()){
            String contig = match.group(2);
            long start = Long.parseLong(match.group(3));
            long end = Long.parseLong(match.group(4));

            return annotationExec.getGenesInRange(contig, start, end);
        } else {
            throw new AnnotationException("Unable to parse query " + query + " . Please check again");
        }
    }

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
