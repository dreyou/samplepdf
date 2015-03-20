package org.dreyou.samplepdf;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.xmlgraphics.util.MimeConstants;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by dre on 3/19/15.
 */
@RestController
public class SamplesController {
    private Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());

    static final Map<String,String> samples = new HashMap<String, String>() {{
        put("welcome","/xml/sample_welcome");
    }};

    @RequestMapping(value={"/sample/{name}"},method = RequestMethod.GET)
    public @ResponseBody void sampleXmlFile(
            @PathVariable("name") String name,
            HttpServletRequest request,HttpServletResponse response) throws Exception{
        if(samples.containsKey(name)) {
            logger.debug("Generating output sample: " + name);
            JSONObject out = new JSONObject();
            out.append("xml",IOUtils.toString(getClass().getResourceAsStream(samples.get(name) + "_xml.xml")));
            out.append("xsl",IOUtils.toString(getClass().getResourceAsStream(samples.get(name) + "_xsl.xml")));
            out.append("par",IOUtils.toString(getClass().getResourceAsStream(samples.get(name) + ".yaml")));
            generateFile(out.toString(1), request, response);
        }
    }

    public void generateFile(String out, HttpServletRequest request,HttpServletResponse response) {
        try {
            response.setContentType(MimeConstants.MIME_PLAIN_TEXT);
            response.getOutputStream().write(out.getBytes());
            response.getOutputStream().flush();
            response.getOutputStream().close();
        } catch (Exception e) {
            logger.error("Can't out sample: "+e.getMessage());
            logger.trace("Can't out sample: ",e);
        }
    }

}
