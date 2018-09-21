package cmsc420.pmquadtree;

public class InvalidPartitionThrowable extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InvalidPartitionThrowable() {
    }

    public InvalidPartitionThrowable(String msg) {
        super(msg);
    }
}
