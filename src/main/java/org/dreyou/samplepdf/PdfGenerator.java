package org.dreyou.samplepdf;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FormattingResults;
import org.apache.fop.fonts.FontInfo;
import org.apache.xmlgraphics.util.MimeConstants;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dre on 3/18/15.
 */
@Component
public class PdfGenerator {

    private Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());

    public static final String DEFAULT_MIME = MimeConstants.MIME_PDF;
    public static final String EMPTY_XML="<?xml version=\"1.0\"?>\n<empty/>\n";

    public static final String DEFAULT_FOP_CONFIG="/fop/fop.xml";
    public static final String DEFAULT_FOP_FONTS = "/fonts";
    public static final String DEFAULT_FOP_FONTS_LIST = "/fop/fonts.txt";
    public static final String FOP_FONTS = "/fop/fonts";
    public static final String FOP_CACHE = "/fop.cache";

    FopFactory fopFactory;
    FOUserAgent foUserAgent;

    private String fopFonts(){
        return System.getProperty("java.io.tmpdir")+FOP_FONTS;
    }

    private String fopCache(){
        return System.getProperty("java.io.tmpdir")+FOP_CACHE;
    }

    @PostConstruct
    public void init() throws Exception {
        logger.info("Initialize fop and prepare fonts");
        fopFactory = FopFactory.newInstance();
        DefaultConfigurationBuilder cfgBuilder = new DefaultConfigurationBuilder();
        Configuration cfg = cfgBuilder.build(getClass().getResourceAsStream(DEFAULT_FOP_CONFIG));
        fopFactory.setUserConfig(cfg);
        if(!Files.exists(Paths.get(fopFonts()), LinkOption.NOFOLLOW_LINKS)) {
            Files.createDirectories(Paths.get(fopFonts()));
        }
        String[] allFiles = IOUtils.toString(getClass().getResourceAsStream(DEFAULT_FOP_FONTS_LIST)).split("\n");
        for(String fontFile: allFiles) {
            if(!fontFile.isEmpty()) {
                if(!Files.exists(Paths.get(fopFonts()+"/"+fontFile), LinkOption.NOFOLLOW_LINKS)) {
                    try(InputStream is = getClass().getResourceAsStream(DEFAULT_FOP_FONTS+"/"+fontFile) ){
                        logger.trace("Copy font file to: "+fopFonts()+"/"+fontFile);
                        Files.copy(is, Paths.get(fopFonts()+"/"+fontFile));
                    }
                }
            }
        }
        logger.trace("Set font base url to: " + fopFonts());
        fopFactory.getFontManager().setFontBaseURL(fopFonts());
        fopFactory.getFontManager().setUseCache(true);
        fopFactory.getFontManager().setCacheFile(new File(fopCache()));
        foUserAgent = fopFactory.newFOUserAgent();
    }

    @PreDestroy
    public void cleanUp() throws Exception {
        logger.info("Cleaning fonts");
        FileUtils.deleteDirectory(new File(fopFonts()));
        FileUtils.deleteQuietly(new File(fopCache()));
    }

    public void generatePdf(Map<String, Object> parameters, String xml, String xsl, OutputStream out) throws Exception {
        generateOut(parameters, xml, xsl, MimeConstants.MIME_PDF, out);
    }

    public void generatePng(Map<String, Object> parameters, String xml, String xsl, OutputStream out) throws Exception {
        generateOut(parameters, xml, xsl, MimeConstants.MIME_PNG, out);
    }

    public void generateOut(Map<String, Object> parameters, String xml, String xsl, String mime, OutputStream out) throws Exception {
        if (xml == null)xml = EMPTY_XML;
        if (xsl == null)xsl = EMPTY_XML;
        if(parameters == null)parameters = new HashMap<String,Object>();
        Source xmlSource = new StreamSource(new ByteArrayInputStream(xml.getBytes()));
        Source xslSource = new StreamSource(new ByteArrayInputStream(xsl.getBytes()));
        generateOut(parameters, xmlSource, xslSource, mime, out);
    }

    public void generateOut(Map<String, Object> parameters, Source xmlSource, Source xslSource, String mime, OutputStream out) throws Exception {
        try {
            if(mime == null)mime = DEFAULT_MIME;

            String fo = xsltProcess(xmlSource,xslSource,parameters);
            Source src= new StreamSource(new ByteArrayInputStream(fo.getBytes()));

            Fop fop = fopFactory.newFop(mime, foUserAgent, out);

            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            Result res = new SAXResult(fop.getDefaultHandler());

            transformer.transform(src, res);

            FormattingResults foResults = fop.getResults();

        }catch(Exception e){
            logger.error("Error while processing pdf",e);
        }
    }

    public static String xsltProcess(Source xmlSource,Source xslSource,Map<String,Object> parameters) throws Exception{
        StringWriter result = new StringWriter();
        Result resultSource = new StreamResult(result);

        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer(xslSource);
        for(String parameterName: parameters.keySet()){
            transformer.setParameter(parameterName,parameters.get(parameterName));
        }
        transformer.transform(xmlSource,resultSource);

        return result.getBuffer().toString();
    }
}
