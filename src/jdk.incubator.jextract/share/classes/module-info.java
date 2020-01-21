module jdk.incubator.jextract {
    requires transitive java.compiler;
    requires transitive jdk.incubator.foreign;
    requires jdk.internal.opt;
    exports jdk.incubator.jextract;
}
