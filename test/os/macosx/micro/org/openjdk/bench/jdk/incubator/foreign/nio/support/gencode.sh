SYSROOT=`xcrun --show-sdk-path`
COMMON_ARGS="-C -isysroot -C $SYSROOT -J-Dforeign.restricted=permit"
jbind -n LibC $COMMON_ARGS @pkg.args @symbols headers.h
