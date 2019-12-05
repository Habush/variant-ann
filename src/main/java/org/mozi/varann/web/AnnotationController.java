package org.mozi.varann.web;

import lombok.RequiredArgsConstructor;
import org.mozi.varann.AnnotationHelper;
import org.mozi.varann.util.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.ws.Response;
import java.io.File;

@Controller
@RequiredArgsConstructor
public class AnnotationController {


    private final AnnotationHelper annotationHelper;
    private final StorageService storageService;

    @RequestMapping(value = "/annotate", method = RequestMethod.GET)
    public String annotate(@RequestParam(value = "dbs", defaultValue="1k") String[] dbs, @RequestParam(value = "ref", defaultValue = "hg38") String ref,
                           @RequestParam(value = "trans", defaultValue = "hg38_ensembl") String transcript, @RequestParam(value = "variant") String variant){
        return annotationHelper.annotateVariant(variant, dbs, ref, transcript).toString();
    }
    @RequestMapping(value = "/annotate/{id}", method = RequestMethod.GET)
    public String annotateById(@RequestParam(value = "dbs", defaultValue="1k") String[] dbs, @RequestParam(value = "ref", defaultValue = "hg38") String ref,
                           @RequestParam(value = "trans", defaultValue = "hg38_ensembl") String transcript,  @PathVariable String id){
        return annotationHelper.annotateVariantById(id, dbs, ref, transcript).toString();
    }

    @PostMapping("/annotate")
    @ResponseBody
    public ResponseEntity<Resource> annotateVcf(@RequestParam("file") MultipartFile file, @RequestParam(value = "dbs", defaultValue = "1k") String[] dbs, @RequestParam(value = "ref", defaultValue = "hg38") String ref,
                                          @RequestParam(value = "trans", defaultValue = "hg38_ensembl") String transcript) {
        storageService.store(file);
        String resultFile = annotationHelper.annotateVcf(file, dbs, ref, transcript);

        Resource resource = storageService.loadAsResource(resultFile);

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"").body(resource);
    }
}
