package org.dreyou.samplepdf;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SpringBootApplication
@RestController
public class SamplepdfApplication {

    static final String DEFAULT_VERSION = "0.0.1";

    @RequestMapping(value={"/misc"},method = RequestMethod.GET)
    public @ResponseBody
    void sampleXmlFile(
            HttpServletRequest request,HttpServletResponse response) throws Exception{
        JSONObject out = new JSONObject();
        String version = getClass().getPackage().getImplementationVersion();
        if(version == null || version.isEmpty())version = DEFAULT_VERSION;
        out.append("version",version);
        response.getWriter().write(out.toString(1));
    }

    public static void main(String[] args) {
        SpringApplication.run(SamplepdfApplication.class, args);
    }
}
