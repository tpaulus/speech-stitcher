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
public class Index extends HttpServlet {
    private static final String TEMPLATE_PATH = "/templates/index.twig";
    private final JtwigRenderer renderer = JtwigRenderer.defaultRenderer();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // TODO Sett Req Attributes, if necessary

        renderer.dispatcherFor(TEMPLATE_PATH)
                .render(req, resp);
    }
}
