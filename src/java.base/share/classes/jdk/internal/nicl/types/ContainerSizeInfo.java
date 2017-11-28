package jdk.internal.nicl.types;

public interface ContainerSizeInfo {
    long size();
    long offset(int index);
    long[] offsets();
    long alignment();
}
