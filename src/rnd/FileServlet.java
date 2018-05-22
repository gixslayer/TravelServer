package rnd;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Optional;

public class FileServlet extends HttpServlet {

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = getPath(req);
        Optional<Resource> resource = Cache.getResource(path);

        if(resource.isPresent()) {
            OutputStream outputStream = resp.getOutputStream();
            resp.setContentType("application/octet-stream");

            try(FileInputStream inputStream = new FileInputStream(resource.get().getFile())) {
                StreamUtils.copy(inputStream, outputStream);
            } catch (IOException e) {
                getServletContext().log(String.format("Error sending file '%s'", path), e);

                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, path);
        }
    }

    private String getPath(HttpServletRequest req) {
        String requestURI = req.getRequestURI();

        // Strip 'files/' prefix
        return requestURI.length() < 7 ? "" : requestURI.substring(7);
    }
}
