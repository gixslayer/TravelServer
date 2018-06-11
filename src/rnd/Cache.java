package rnd;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;

public class Cache implements ServletContextListener{
    private static final Map<String, Resource> resources = new HashMap<>();
    private static ServletContext context;
    private static File root;
    private static File meta;
    private static MessageDigest digest;
    private static String modelPattern;
    private static Instant lastModified;

    public static Optional<Resource> getResource(String path) {
        return Optional.ofNullable(resources.getOrDefault(path, null));
    }

    public static Instant getLastModificationInstant() {
        return lastModified;
    }

    public static void forEach(Consumer<Resource> consumer) {
        List<Resource> sorted = new ArrayList<>(resources.values());
        sorted.sort(Comparator.comparing(Resource::getName));

        sorted.forEach(consumer);
    }

    private static String toHex(byte[] hash) {
        StringBuilder sb = new StringBuilder(hash.length * 2);

        for(byte b : hash) {
            sb.append(Character.forDigit((b >> 4) & 0xf, 16));
            sb.append(Character.forDigit(b & 0xf, 16));
        }

        return sb.toString();
    }

    private static String getPath(File file) {
        StringBuilder sb = new StringBuilder(128);

        for(File currentFile = file; !currentFile.equals(root); currentFile = currentFile.getParentFile()) {
            sb.insert(0, currentFile.getName() + "/");
        }

        int length = sb.length();

        return length > 0 ? sb.substring(0, length - 1) : "";
    }

    private static Resource.Type getType(String path) {
        return path.matches(modelPattern) ? Resource.Type.Model : Resource.Type.Content;
    }

    private static void processFile(File file) {
        String path = getPath(file);

        try (FileInputStream stream = new FileInputStream(file)) {
            byte[] hash = StreamUtils.digest(stream, digest);
            String checksum = toHex(hash);
            Resource.Type type = getType(path);
            Resource resource = new Resource(path, checksum, file, type);

            resources.put(path, resource);

            context.log(String.format("Added file '%s' with checksum '%s'", path, checksum));
        } catch (IOException e) {
            context.log(String.format("IO exception during checksum computation of file '%s'", path), e);
        }
    }

    private static void processDirectory(File file) {
        String path = getPath(file);
        File[] files = file.listFiles(File::isFile);
        File[] directories = file.listFiles(File::isDirectory);

        if (files != null) {
            Arrays.stream(files).forEach(Cache::processFile);
        } else {
            context.log(String.format("Could not list files in directory '%s'", path));
        }

        if(directories != null) {
            Arrays.stream(directories).forEach(Cache::processDirectory);
        } else {
            context.log(String.format("Could not list directories in directory '%s'", path));
        }
    }

    private static boolean readCacheMeta() {
        if(!meta.exists()) {
            return true;
        }

        try (FileInputStream stream = new FileInputStream(meta)) {
            String json = StreamUtils.toString(stream);
            JSONObject metaObject = new JSONObject(json);
            Set<String> metaFiles = new HashSet<>();

            lastModified = Instant.parse(metaObject.getString("lastModified"));
            JSONArray resourceList = metaObject.getJSONArray("resources");

            for(int i = 0; i < resourceList.length(); ++i) {
                JSONObject resourceObject = resourceList.getJSONObject(i);

                String path = resourceObject.getString("file");
                String checksum = resourceObject.getString("checksum");

                Optional<Resource> currentResource = getResource(path);

                if(!currentResource.isPresent()) {
                    // Deleted resource found, rewrite meta.
                    return true;
                } else if(!currentResource.get().getChecksum().equals(checksum)) {
                    // Modified resource found, rewrite meta.
                    return true;
                } else {
                    metaFiles.add(path);
                }
            }

            // Only rewrite meta if a new resource is found.
            return !metaFiles.containsAll(resources.keySet());
        } catch (IOException e) {
            context.log("IO exception during reading of cache meta file", e);

            return true;
        }
    }

    private static void writeCacheMeta() {
        JSONObject metaObject = new JSONObject();
        JSONArray resourceList = new JSONArray();

        forEach(r -> {
            JSONObject resourceObject = new JSONObject();

            resourceObject.put("file", r.getName());
            resourceObject.put("checksum", r.getChecksum());

            resourceList.put(resourceObject);
        });

        metaObject.put("lastModified", lastModified.toString());
        metaObject.put("resources", resourceList);

        try(FileOutputStream stream = new FileOutputStream(meta)) {
            StreamUtils.writeString(stream, metaObject.toString(2));
        } catch (IOException e) {
            context.log("IO exception during writing of cache meta file", e);
        }
    }

    private static void processCacheMeta() {
        if(readCacheMeta()) {
            lastModified = Instant.now(Clock.systemUTC());

            writeCacheMeta();
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        context = servletContextEvent.getServletContext();
        String cacheRoot = context.getInitParameter("cacheRoot");
        String cacheMeta = context.getInitParameter("cacheMeta");
        String digestAlgorithm = context.getInitParameter("digestAlgorithm");
        modelPattern = context.getInitParameter("modelPattern");
        root = new File(cacheRoot);
        meta = new File(cacheMeta);

        try {
            digest = MessageDigest.getInstance(digestAlgorithm);

            if(!root.exists()) {
                context.log(String.format("Cache root '%s' does not exists", cacheRoot));
            } else if(!root.isDirectory()) {
                context.log(String.format("Cache root '%s' is not a directory", cacheRoot));
            } else if(meta.exists() && !meta.isFile()) {
                context.log(String.format("Cache meta file '%s' is not a file", cacheMeta));
            } else {
                context.log(String.format("Initializing cache from '%s'", cacheRoot));

                processDirectory(root);
                processCacheMeta();
            }
        } catch (NoSuchAlgorithmException e) {
            context.log(String.format("Could not create digest for algorithm '%s'", digestAlgorithm), e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
