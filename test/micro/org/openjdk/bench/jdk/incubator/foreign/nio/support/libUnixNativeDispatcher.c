#include <jni.h>
#include <sys/stat.h>
#include <dirent.h>

#define CHECK_NULL_RETURN(x, y)                  \
    do {                                        \
        if ((x) == NULL) {                      \
            return (y);                         \
        }                                       \
    } while (0)                                 \

static jfieldID attrs_st_mode;
static jfieldID attrs_st_ino;
static jfieldID attrs_st_dev;
static jfieldID attrs_st_rdev;
static jfieldID attrs_st_nlink;
static jfieldID attrs_st_uid;
static jfieldID attrs_st_gid;
static jfieldID attrs_st_size;
static jfieldID attrs_st_atime_sec;
static jfieldID attrs_st_atime_nsec;
static jfieldID attrs_st_mtime_sec;
static jfieldID attrs_st_mtime_nsec;
static jfieldID attrs_st_ctime_sec;
static jfieldID attrs_st_ctime_nsec;

#ifdef _DARWIN_FEATURE_64_BIT_INODE
static jfieldID attrs_st_birthtime_sec;
#endif

/**
 * Copy stat members into sun.nio.fs.UnixFileAttributes
 */
static void prepAttributes(JNIEnv* env, struct stat* buf, jobject attrs) {
    (*env)->SetIntField(env, attrs, attrs_st_mode, (jint)buf->st_mode);
    (*env)->SetLongField(env, attrs, attrs_st_ino, (jlong)buf->st_ino);
    (*env)->SetLongField(env, attrs, attrs_st_dev, (jlong)buf->st_dev);
    (*env)->SetLongField(env, attrs, attrs_st_rdev, (jlong)buf->st_rdev);
    (*env)->SetIntField(env, attrs, attrs_st_nlink, (jint)buf->st_nlink);
    (*env)->SetIntField(env, attrs, attrs_st_uid, (jint)buf->st_uid);
    (*env)->SetIntField(env, attrs, attrs_st_gid, (jint)buf->st_gid);
    (*env)->SetLongField(env, attrs, attrs_st_size, (jlong)buf->st_size);
    (*env)->SetLongField(env, attrs, attrs_st_atime_sec, (jlong)buf->st_atime);
    (*env)->SetLongField(env, attrs, attrs_st_mtime_sec, (jlong)buf->st_mtime);
    (*env)->SetLongField(env, attrs, attrs_st_ctime_sec, (jlong)buf->st_ctime);

#ifdef _DARWIN_FEATURE_64_BIT_INODE
    (*env)->SetLongField(env, attrs, attrs_st_birthtime_sec, (jlong)buf->st_birthtime);
#endif

#ifndef MACOSX
    (*env)->SetLongField(env, attrs, attrs_st_atime_nsec, (jlong)buf->st_atim.tv_nsec);
    (*env)->SetLongField(env, attrs, attrs_st_mtime_nsec, (jlong)buf->st_mtim.tv_nsec);
    (*env)->SetLongField(env, attrs, attrs_st_ctime_nsec, (jlong)buf->st_ctim.tv_nsec);
#else
    (*env)->SetLongField(env, attrs, attrs_st_atime_nsec, (jlong)buf->st_atimespec.tv_nsec);
    (*env)->SetLongField(env, attrs, attrs_st_mtime_nsec, (jlong)buf->st_mtimespec.tv_nsec);
    (*env)->SetLongField(env, attrs, attrs_st_ctime_nsec, (jlong)buf->st_ctimespec.tv_nsec);
#endif
}

/*
 * Class:     org_openjdk_bench_jdk_incubator_foreign_nio_support_UnixNativeDispatcher
 * Method:    opendirJNI
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_org_openjdk_bench_jdk_incubator_foreign_nio_support_UnixNativeDispatcher_opendirJNI
  (JNIEnv *env, jclass cls, jstring path)
{
  const char *p = (*env)->GetStringUTFChars(env, path, NULL);
  DIR* dirp = opendir(p);
  (*env)->ReleaseStringUTFChars(env, path, p);
  return (jlong) dirp;
}

/*
 * Class:     org_openjdk_bench_jdk_incubator_foreign_nio_support_UnixNativeDispatcher
 * Method:    readdirJNI
 * Signature: (J)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_org_openjdk_bench_jdk_incubator_foreign_nio_support_UnixNativeDispatcher_readdirJNI
  (JNIEnv *env, jclass cls, jlong dir)
{
  DIR * dirp = (DIR*) dir;
  struct dirent *data = readdir(dirp);
  if (data == NULL) {
    return NULL;
  }
  return (*env)->NewStringUTF(env, data->d_name);
}

/*
 * Class:     org_openjdk_bench_jdk_incubator_foreign_nio_support_UnixNativeDispatcher
 * Method:    closedirJNI
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_org_openjdk_bench_jdk_incubator_foreign_nio_support_UnixNativeDispatcher_closedirJNI
  (JNIEnv *env, jclass cls, jlong dir)
{
  DIR * dirp = (DIR*) dir;
  closedir(dirp);
}

/*
 * Class:     org_openjdk_bench_jdk_incubator_foreign_nio_support_UnixNativeDispatcher
 * Method:    statJNI
 * Signature: (Ljava/lang/String;Lorg/openjdk/bench/jdk/incubator/foreign/nio/support/UnixFileAttributes;)V
 */
JNIEXPORT void JNICALL Java_org_openjdk_bench_jdk_incubator_foreign_nio_support_UnixNativeDispatcher_statJNI
  (JNIEnv *env, jclass cls, jstring path, jobject attrs)
{
  struct stat data;
  const char *file = (*env)->GetStringUTFChars(env, path, NULL);
  stat(file, &data);
  (*env)->ReleaseStringUTFChars(env, path, file);
  prepAttributes(env, &data, attrs);
}

JNIEXPORT jint JNICALL Java_org_openjdk_bench_jdk_incubator_foreign_nio_support_UnixNativeDispatcher_initJNI
  (JNIEnv* env, jclass this)
{
    jclass clazz;

    clazz = (*env)->FindClass(env, "org/openjdk/bench/jdk/incubator/foreign/nio/support/UnixFileAttributes");
    CHECK_NULL_RETURN(clazz, 0);
    attrs_st_mode = (*env)->GetFieldID(env, clazz, "st_mode", "I");
    CHECK_NULL_RETURN(attrs_st_mode, 0);
    attrs_st_ino = (*env)->GetFieldID(env, clazz, "st_ino", "J");
    CHECK_NULL_RETURN(attrs_st_ino, 0);
    attrs_st_dev = (*env)->GetFieldID(env, clazz, "st_dev", "J");
    CHECK_NULL_RETURN(attrs_st_dev, 0);
    attrs_st_rdev = (*env)->GetFieldID(env, clazz, "st_rdev", "J");
    CHECK_NULL_RETURN(attrs_st_rdev, 0);
    attrs_st_nlink = (*env)->GetFieldID(env, clazz, "st_nlink", "I");
    CHECK_NULL_RETURN(attrs_st_nlink, 0);
    attrs_st_uid = (*env)->GetFieldID(env, clazz, "st_uid", "I");
    CHECK_NULL_RETURN(attrs_st_uid, 0);
    attrs_st_gid = (*env)->GetFieldID(env, clazz, "st_gid", "I");
    CHECK_NULL_RETURN(attrs_st_gid, 0);
    attrs_st_size = (*env)->GetFieldID(env, clazz, "st_size", "J");
    CHECK_NULL_RETURN(attrs_st_size, 0);
    attrs_st_atime_sec = (*env)->GetFieldID(env, clazz, "st_atime_sec", "J");
    CHECK_NULL_RETURN(attrs_st_atime_sec, 0);
    attrs_st_atime_nsec = (*env)->GetFieldID(env, clazz, "st_atime_nsec", "J");
    CHECK_NULL_RETURN(attrs_st_atime_nsec, 0);
    attrs_st_mtime_sec = (*env)->GetFieldID(env, clazz, "st_mtime_sec", "J");
    CHECK_NULL_RETURN(attrs_st_mtime_sec, 0);
    attrs_st_mtime_nsec = (*env)->GetFieldID(env, clazz, "st_mtime_nsec", "J");
    CHECK_NULL_RETURN(attrs_st_mtime_nsec, 0);
    attrs_st_ctime_sec = (*env)->GetFieldID(env, clazz, "st_ctime_sec", "J");
    CHECK_NULL_RETURN(attrs_st_ctime_sec, 0);
    attrs_st_ctime_nsec = (*env)->GetFieldID(env, clazz, "st_ctime_nsec", "J");
    CHECK_NULL_RETURN(attrs_st_ctime_nsec, 0);

#ifdef _DARWIN_FEATURE_64_BIT_INODE
    attrs_st_birthtime_sec = (*env)->GetFieldID(env, clazz, "st_birthtime_sec", "J");
    CHECK_NULL_RETURN(attrs_st_birthtime_sec, 0);
#endif
    return 0;
}
