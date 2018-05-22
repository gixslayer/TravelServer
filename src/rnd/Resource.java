package rnd;

import java.io.File;
import java.util.Comparator;

public class Resource implements Comparable<Resource> {
    private static final Comparator<Resource> COMPARATOR = Comparator
            .comparing(Resource::getName)
            .thenComparing(Resource::getChecksum)
            .thenComparing(Resource::getType);

    private final String name;
    private final String checksum;
    private final File file;
    private final Type type;

    public Resource(String name, String checksum, File file, Type type) {
        this.name = name;
        this.checksum = checksum;
        this.file = file;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getChecksum() {
        return checksum;
    }

    public File getFile() {
        return file;
    }

    public Type getType() {
        return type;
    }

    @Override
    public int hashCode() {
        return name.hashCode() ^ checksum.hashCode() * (type == Type.Model ? 31 : 15);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int compareTo(Resource resource) {
        return COMPARATOR.compare(this, resource);
    }

    public enum Type {
        Model,
        Content
    }
}
