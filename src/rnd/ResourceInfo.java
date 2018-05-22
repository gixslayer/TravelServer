package rnd;

import org.json.JSONObject;

class ResourceInfo {
    private final String file;
    private final String checksum;
    private final ResourceStatus status;

    ResourceInfo(String file, String checksum, ResourceStatus status) {
        this.file = file;
        this.checksum = checksum;
        this.status = status;
    }

    public String getFile() {
        return file;
    }

    public String getChecksum() {
        return checksum;
    }

    public ResourceStatus getStatus() {
        return status;
    }

    public ResourceInfo update(ResourceStatus newStatus) {
        return new ResourceInfo(file, checksum, newStatus);
    }

    public ResourceInfo update(ResourceStatus newStatus, String newChecksum) {
        return new ResourceInfo(file, newChecksum, newStatus);
    }

    public JSONObject toJSON() {
        JSONObject object = new JSONObject();

        object.put("file", file);
        object.put("checksum", checksum);
        object.put("status", status.toString());

        return object;
    }

    public static ResourceInfo fromJSON(JSONObject object) {
        String file = object.getString("file");
        String checksum = object.getString("checksum");
        ResourceStatus status = ResourceStatus.New;

        if(object.has("status")) {
            status = ResourceStatus.fromString(object.getString("status"));
        }

        return new ResourceInfo(file, checksum, status);
    }
}
