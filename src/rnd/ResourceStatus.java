package rnd;

enum ResourceStatus {
    New,
    Modified,
    Removed;

    @Override
    public String toString() {
        // NOTE: This return value is serialized in JSON responses, thus should be kept in sync
        // with any protocol changes.
        switch (this) {
            case New:
                return "new";
            case Modified:
                return "modified";
            case Removed:
                return "removed";
            default:
                return this.name();
        }
    }

    public static ResourceStatus fromString(String str) {
        if(str.equals("new")) {
            return New;
        } else if(str.equals("modified")) {
            return Modified;
        } else if(str.equals("removed")) {
            return Removed;
        } else {
            throw new IllegalArgumentException("Invalid ResourceStatus string");
        }
    }
}
