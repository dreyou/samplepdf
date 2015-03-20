package org.dreyou.samplepdf;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.xmlgraphics.util.MimeConstants;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.yaml.snakeyaml.Yaml;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dre on 3/19/15.
 */
@RestController
public class PdfController {
    private Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());

    static final Map<String,Object> SAMPLE_PARAMETERS = new HashMap<String,Object>(){{
        put("data","Hello");
        put("data_ru","Привет");
    }};

    static final int MAX_LEN=5000;

    @Autowired
    PdfGenerator pdfGenerator;

    @RequestMapping(value={"/generate{out}"},method = RequestMethod.POST)
    public @ResponseBody void pdf(
            @PathVariable("out") String out,
            @RequestParam(value="xml",defaultValue=PdfGenerator.EMPTY_XML) String xml,
            @RequestParam(value="xsl",defaultValue=PdfGenerator.EMPTY_XML) String xsl,
            @RequestParam(value="par",defaultValue="") String yml,
            @RequestParam(value="enc",defaultValue="") String enc,
            HttpServletRequest request,HttpServletResponse response) throws Exception{
        if(xml.length() > MAX_LEN || xsl.length() > MAX_LEN || yml.length() > MAX_LEN){
            logger.error("Request data too long, skip");
            response.sendError(response.SC_BAD_REQUEST);
            return;
        }
        Yaml yaml = new Yaml();
        Map<String,Object> par =(Map<String, Object>)yaml.load(yml);
        switch (out){
            case "pdf":generateOut(par,xml,xsl, MimeConstants.MIME_PDF,"generated.pdf",request,response,enc);
                break;
            case "png":generateOut(par,xml,xsl, MimeConstants.MIME_PNG,"generated.png",request,response,enc);
                break;
        }
    }

    @RequestMapping(value={"/samplepdf"},method = RequestMethod.GET)
    public @ResponseBody void samplePdf(
            HttpServletRequest request,HttpServletResponse response) throws Exception{
        String xml = PdfGenerator.EMPTY_XML;
        String xsl = IOUtils.toString(getClass().getResourceAsStream("/xml/sample.xml"));
        generateOut(SAMPLE_PARAMETERS,xml,xsl, MimeConstants.MIME_PDF,"sample.pdf",request,response,null);
    }

    @RequestMapping(value={"/samplepng"},method = RequestMethod.GET)
    public @ResponseBody void samplePng(
            HttpServletRequest request,HttpServletResponse response) throws Exception{
        String xml = PdfGenerator.EMPTY_XML;
        String xsl = IOUtils.toString(getClass().getResourceAsStream("/xml/sample.xml"));
        generateOut(SAMPLE_PARAMETERS,xml,xsl, MimeConstants.MIME_PNG,"sample.png",request,response,null);
    }

    public void generateOut(Map<String,Object> parameters, String xml, String xsl, String mime,String fileName, HttpServletRequest request,HttpServletResponse response,String enc) {
        try {
            logger.debug("Generating output as: " + mime);
            logger.trace("xml: " + xml);
            logger.trace("xsl: " + xsl);
            response.setContentType(mime);
            response.addHeader("Content-disposition", "inline; filename=\"" + URLEncoder.encode(fileName, "UTF8") +"\"");
            if(enc != null && !enc.isEmpty()) {
                response.addHeader("Content-Transfer-Encoding","base64");
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                pdfGenerator.generateOut(parameters, xml, xsl, mime, out);
                response.getOutputStream().write(Base64.encodeBase64(out.toByteArray()));
                out.flush();
                out.close();
            }else{
                pdfGenerator.generateOut(parameters, xml, xsl, mime, response.getOutputStream());
            }
            response.getOutputStream().flush();
            response.getOutputStream().close();
        } catch (Exception e) {
            logger.error("Can't out as "+mime+": "+e.getMessage());
            logger.trace("Can't out as "+mime+": ",e);
        }
    }

}
