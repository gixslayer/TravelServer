package rnd;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ThreadLocalRandom;

public class BookServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String content = StreamUtils.toString(req.getInputStream());

        log("Got book post: " + content);

        int bookingID = ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE);
        resp.setContentType("text/plain");

        try (PrintWriter writer = resp.getWriter()) {
            writer.print(Integer.toString(bookingID));
        }
    }
}
