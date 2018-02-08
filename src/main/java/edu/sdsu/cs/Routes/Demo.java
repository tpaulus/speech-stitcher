package edu.sdsu.cs.Routes;

import org.jtwig.web.servlet.JtwigRenderer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Tom Paulus
 * Created on 1/22/18.
 */
public class Demo extends HttpServlet {
    private static final String TEMPLATE_PATH = "/templates/demo.twig";
    private final JtwigRenderer renderer = JtwigRenderer.defaultRenderer();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("voice", "President Barack Obama");

        renderer.dispatcherFor(TEMPLATE_PATH)
                .render(req, resp);
    }
}
