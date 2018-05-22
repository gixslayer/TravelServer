package rnd;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class DebugServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");

        PrintWriter writer = resp.getWriter();

        writer.println("<body>\n<table>");
        writer.println("<tr>");
        writer.println("<th scope=\"col\">file</th>");
        writer.println("<th scope=\"col\">type</th>");
        writer.println("<th scope=\"col\">checksum</th>");
        writer.println("<th scope=\"col\">size</th>");
        writer.println("</tr>");

        Cache.forEach(r -> {;
            writer.println("<tr>");
            writer.printf("<td>%s</td>\n", r.getName());
            writer.printf("<td>%s</td>\n", r.getType());
            writer.printf("<td>%s</td>\n", r.getChecksum());
            writer.printf("<td>%d</td>\n", r.getFile().length());
            writer.println("</tr>");
        });

        writer.println("</table></body>");

        writer.close();
    }
}
