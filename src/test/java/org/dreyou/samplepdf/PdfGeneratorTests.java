package org.dreyou.samplepdf;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SamplepdfApplication.class)
@WebAppConfiguration
public class PdfGeneratorTests {

    @Autowired
    PdfGenerator pdfGenerator;

    @Test
    public void pdfGeneratotInitTest()throws Exception {
        String[] allFiles = IOUtils.toString(getClass().getResourceAsStream(PdfGenerator.DEFAULT_FOP_FONTS_LIST)).split("\n");
        for (String fontFile : allFiles) {
            File font = new File(System.getProperty("java.io.tmpdir")+PdfGenerator.FOP_FONTS+"/"+fontFile);
            if(!font.exists()) {
                fail("Font file: "+fontFile+" not found in target directory");
            }
        }
    }


    @Test
    public void xsltTransformTest()throws Exception{
        String xsl = FileUtils.readFileToString(new File(this.getClass().getResource("/xml/simple.xml").toURI()));
        assertNotNull(xsl);
        Source xmlSource = new StreamSource(new ByteArrayInputStream(PdfGenerator.EMPTY_XML.getBytes()));
        Source xslSource = new StreamSource(new ByteArrayInputStream(xsl.getBytes()));
        final Map<String,Object> data = new HashMap<String,Object>(){{
            put("local.value","VALUE");
        }};
        Map<String,Object> param = new HashMap<String,Object>(){{
            put("data",data);
        }};
        String fo = PdfGenerator.xsltProcess(xmlSource, xslSource, param);
        assertNotNull(fo);
        assertTrue(fo.contains("VALUE"));
    }

    @Test
    public void generatePdfBySampleFo()throws Exception{
        assertNotNull(pdfGenerator);
        String xsl = FileUtils.readFileToString(new File(this.getClass().getResource("/xml/sample.xml").toURI()));
        assertNotNull(xsl);
        String pdfFileName = System.getProperty("java.io.tmpdir")+"/sample.pdf";
        File pdf = new File(pdfFileName);
        if(pdf.exists()) {
            pdf.delete();
        }
        final Map<String,Object> parameters = new HashMap<String,Object>(){{
            put("data","VALUE");
            put("data_ru","Значение");
        }};
        OutputStream out = new FileOutputStream(pdfFileName);
        pdfGenerator.generatePdf(parameters,PdfGenerator.EMPTY_XML,xsl,out);
        out.close();
        assertTrue(pdf.exists());
        assertTrue(pdf.isFile());
        assertTrue(pdf.length() > 100);
    }

    @Test
    public void generatePngBySampleFo()throws Exception{
        assertNotNull(pdfGenerator);
        String xsl = FileUtils.readFileToString(new File(this.getClass().getResource("/xml/sample.xml").toURI()));
        assertNotNull(xsl);
        String pdfFileName = System.getProperty("java.io.tmpdir")+"/sample.png";
        File pdf = new File(pdfFileName);
        if(pdf.exists()) {
            pdf.delete();
        }
        final Map<String,Object> parameters = new HashMap<String,Object>(){{
            put("data","VALUE");
            put("data_ru","Значение");
        }};
        OutputStream out = new FileOutputStream(pdfFileName);
        pdfGenerator.generatePng(parameters, PdfGenerator.EMPTY_XML, xsl, out);
        out.close();
        assertTrue(pdf.exists());
        assertTrue(pdf.isFile());
        assertTrue(pdf.length() > 100);
    }

}
