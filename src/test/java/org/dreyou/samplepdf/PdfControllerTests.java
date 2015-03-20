package org.dreyou.samplepdf;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SamplepdfApplication.class)
@WebAppConfiguration
public class PdfControllerTests {

    @Autowired
    PdfGenerator pdfGenerator;

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext wac;

    @Before
    public void prepare(){
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        assertNotNull(pdfGenerator);
    }

    @Test
    public void testGeneratePdf() throws Exception {
        String xml = PdfGenerator.EMPTY_XML;
        String xsl = FileUtils.readFileToString(new File(this.getClass().getResource("/xml/sample.xml").toURI()));
        this.mockMvc.perform(post("/generatepdf").param("xml",xml).param("xsl",xsl)).andExpect(content().contentType("application/pdf"));
    }

    @Test
    public void testGeneratePdfEnc() throws Exception {
        String xml = PdfGenerator.EMPTY_XML;
        String xsl = FileUtils.readFileToString(new File(this.getClass().getResource("/xml/sample.xml").toURI()));
        this.mockMvc.perform(post("/generatepdf").param("xml",xml).param("xsl",xsl).param("enc", "base64")).andExpect(content().contentType("application/pdf"));
    }

    @Test
    public void testGenerateSamplePdf() throws Exception {
        this.mockMvc.perform(get("/samplepdf")).andExpect(content().contentType("application/pdf"));
    }

    @Test
    public void testGeneratePng() throws Exception {
        String xml = PdfGenerator.EMPTY_XML;
        String xsl = FileUtils.readFileToString(new File(this.getClass().getResource("/xml/sample.xml").toURI()));
        this.mockMvc.perform(post("/generatepng").param("xml",xml).param("xsl",xsl)).andExpect(content().contentType("image/png"));
    }

    @Test
    public void testGeneratePngEnc() throws Exception {
        String xml = PdfGenerator.EMPTY_XML;
        String xsl = FileUtils.readFileToString(new File(this.getClass().getResource("/xml/sample.xml").toURI()));
        this.mockMvc.perform(post("/generatepng").param("xml", xml).param("xsl", xsl).param("enc", "base64")).andExpect(content().contentType("image/png"));
    }

    @Test
    public void testGenerateSamplePng() throws Exception {
        this.mockMvc.perform(get("/samplepng")).andExpect(content().contentType("image/png"));
    }

}
