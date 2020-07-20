COMMON_ARGS=-J-Dforeign.restricted=permit
jbind -n LibC $COMMON_ARGS @pkg.args @symbols headers.h
