package rnd;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static rnd.Utils.exists;

public class VerifyServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/plain");

        try(PrintWriter writer = resp.getWriter()) {
            writer.print(Cache.getLastModificationInstant().toString());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String content = StreamUtils.toString(req.getInputStream());

        try {
            List<ResourceInfo> clientCache = parseContent(content);
            List<ResourceInfo> updatedCache = processContent(clientCache);
            String response = createResponse(updatedCache);

            resp.setContentType("application/json");

            try (PrintWriter writer = resp.getWriter()) {
                writer.print(response);
            }
        } catch (JSONException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format: " + content);
        }
    }

    private List<ResourceInfo> parseContent(String content) {
        List<ResourceInfo> clientCache = new ArrayList<>();
        JSONArray resourceList = new JSONArray(content);

        for(int i = 0; i < resourceList.length(); ++i) {
            JSONObject entry = resourceList.getJSONObject(i);

            clientCache.add(ResourceInfo.fromJSON(entry));
        }

        return clientCache;
    }

    private List<ResourceInfo> processContent(List<ResourceInfo> clientCache) {
        List<ResourceInfo> updatedCache = new ArrayList<>();
        Predicate<Resource> inClientCache = r -> exists(clientCache, ri -> ri.getFile().equals(r.getName()));

        clientCache.forEach(r -> {
            Optional<Resource> resource = Cache.getResource(r.getFile());

            if(resource.isPresent()) {
                String checksum = resource.get().getChecksum();

                if(!checksum.equals(r.getChecksum())) {
                    updatedCache.add(r.update(ResourceStatus.Modified, checksum));
                }
            } else {
                updatedCache.add(r.update(ResourceStatus.Removed));
            }
        });

        Cache.forEach(r -> {
            if(r.getType() == Resource.Type.Model && !inClientCache.test(r)) {
                updatedCache.add(new ResourceInfo(r.getName(), r.getChecksum(), ResourceStatus.New));
            }
        });

        return updatedCache;
    }

    private String createResponse(List<ResourceInfo> updatedCache) {
        JSONArray resourceList = new JSONArray();

        updatedCache.forEach(r -> resourceList.put(r.toJSON()));

        return resourceList.toString(0);
    }

}
