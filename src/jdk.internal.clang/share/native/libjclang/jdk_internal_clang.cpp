#include <stdlib.h>
#include <string.h>
#include <jni.h>
#include <clang-c/Index.h>
#include <assert.h>

static jclass clsStructType;
static jfieldID dataStructType;

static jclass clsIndex;
static jclass clsCursor;
static jclass clsType;
static jclass clsjavaLangString;
static jclass clsIAE;
static jmethodID ctorIndex;
static jmethodID ctorCursor;
static jmethodID ctorType;
static jmethodID visitorID;
static jclass clsSourceLocation;
static jmethodID ctorSourceLocation;
static jclass clsLocation;
static jmethodID ctorLocation;
static jclass clsSourceRange;
static jmethodID ctorSourceRange;


jstring CX2JString(JNIEnv *env, CXString str) {
    const char* cstr = clang_getCString(str);
    jstring rv = env->NewStringUTF(cstr);
    clang_disposeString(str);
    return rv;
}

struct LocationFactory {
    CXFile file;
    unsigned line;
    unsigned col;
    unsigned offset;

    jobject get(JNIEnv *env, CXSourceLocation * const loc,
                void (*func)(CXSourceLocation, CXFile*, unsigned*, unsigned*, unsigned*)) {
        func(*loc, &file, &line, &col, &offset);
        return env->NewObject(clsLocation, ctorLocation,
            CX2JString(env, clang_getFileName(file)), line, col, offset);
    }
};

#define J2P(env, pojo) \
    (env->GetDirectBufferAddress(env->GetObjectField(pojo, dataStructType)))

#ifdef __cplusplus
extern "C" {
#endif

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv* env;

    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
        // force unload
        return -1;
    }

    clsjavaLangString = (jclass) env->NewGlobalRef(env->FindClass("java/lang/String"));
    clsIAE = (jclass) env->NewGlobalRef(env->FindClass("java/lang/IllegalArgumentException"));

    clsIndex = (jclass) env->NewGlobalRef(env->FindClass("jdk/internal/clang/Index"));
    ctorIndex = env->GetMethodID(clsIndex, "<init>", "(J)V");

    clsStructType = (jclass) env->NewGlobalRef(env->FindClass("jdk/internal/clang/StructType"));
    dataStructType = env->GetFieldID(clsStructType, "data", "Ljava/nio/ByteBuffer;");
    assert(dataStructType != NULL);

    clsCursor = (jclass) env->NewGlobalRef(env->FindClass("jdk/internal/clang/Cursor"));
    ctorCursor = env->GetMethodID(clsCursor, "<init>", "(Ljava/nio/ByteBuffer;)V");
    visitorID = env->GetStaticMethodID(clsCursor, "visit",
        "(Ljdk/internal/clang/Cursor$Visitor;Ljava/nio/ByteBuffer;Ljava/nio/ByteBuffer;Ljava/lang/Object;)I");
    assert(visitorID != NULL);

    clsType = (jclass) env->NewGlobalRef(env->FindClass("jdk/internal/clang/Type"));
    ctorType = env->GetMethodID(clsType, "<init>", "(Ljava/nio/ByteBuffer;)V");

    clsSourceLocation = (jclass) env->NewGlobalRef(env->FindClass("jdk/internal/clang/SourceLocation"));
    ctorSourceLocation = env->GetMethodID(clsSourceLocation, "<init>", "(Ljava/nio/ByteBuffer;)V");

    clsLocation = (jclass) env->NewGlobalRef(env->FindClass("jdk/internal/clang/SourceLocation$Location"));
    ctorLocation = env->GetMethodID(clsLocation, "<init>", "(Ljava/lang/String;III)V");
    assert(ctorLocation != NULL);

    clsSourceRange = (jclass) env->NewGlobalRef(env->FindClass("jdk/internal/clang/SourceRange"));
    ctorSourceRange = env->GetMethodID(clsSourceRange, "<init>", "(Ljava/nio/ByteBuffer;)V");

    return JNI_VERSION_1_6;
}

void JNI_OnUnload(JavaVM *vm, void *reserved) {
    JNIEnv* env;

    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return;
    }

    env->DeleteGlobalRef(clsIndex);
    env->DeleteGlobalRef(clsCursor);
    env->DeleteGlobalRef(clsType);
}

JNIEXPORT jobject JNICALL Java_jdk_internal_clang_LibClang_createIndex
  (JNIEnv *env, jclass cls) {
    CXIndex idx = clang_createIndex(0, 0);
    clang_toggleCrashRecovery(false);
    // CXIndex is a void*
    return env->NewObject(clsIndex, ctorIndex, (jlong) idx);
}

JNIEXPORT jstring JNICALL Java_jdk_internal_clang_LibClang_version
  (JNIEnv *env, jclass cls) {
    CXString ver = clang_getClangVersion();
    return CX2JString(env, ver);
}

JNIEXPORT void JNICALL Java_jdk_internal_clang_Index_disposeIndex
  (JNIEnv *env, jobject obj, jlong addr) {
    clang_disposeIndex((CXIndex) addr);
}

JNIEXPORT jlong JNICALL Java_jdk_internal_clang_Index_parseFile
  (JNIEnv *env, jobject obj, jlong addr, jstring path, jboolean detailed, jobjectArray args) {
    const char *filename = env->GetStringUTFChars(path, NULL);
    jsize argCnt = env->GetArrayLength(args);
    const char** cargs = (const char**) calloc(argCnt, sizeof(char*));
    jsize i;
    jstring arg;
    for (i = 0; i < argCnt; i++) {
        arg = (jstring) env->GetObjectArrayElement(args, i);
        cargs[i] = env->GetStringUTFChars(arg, NULL);
    }
    CXTranslationUnit tu = clang_parseTranslationUnit((CXIndex) addr,
        filename, cargs, argCnt, NULL, 0, detailed ? CXTranslationUnit_DetailedPreprocessingRecord : CXTranslationUnit_None);
    env->ReleaseStringUTFChars(path, filename);
    for (i = 0; i < argCnt; i++) {
        arg = (jstring) env->GetObjectArrayElement(args, i);
        env->ReleaseStringUTFChars(arg, cargs[i]);
    }
    free(cargs);
    return (jlong) tu;
}

JNIEXPORT void JNICALL Java_jdk_internal_clang_Index_disposeTranslationUnit
  (JNIEnv *env, jobject obj, jlong tu) {
    clang_disposeTranslationUnit((CXTranslationUnit) tu);
}

JNIEXPORT jobject JNICALL Java_jdk_internal_clang_Index_getTranslationUnitCursor
  (JNIEnv *env, jobject obj, jlong tu) {
    CXCursor cursor = clang_getTranslationUnitCursor((CXTranslationUnit) tu);
    jobject buffer = env->NewDirectByteBuffer(&cursor, sizeof(CXCursor));
    return env->NewObject(clsCursor, ctorCursor, buffer);
}

JNIEXPORT jobjectArray JNICALL Java_jdk_internal_clang_Index_getTranslationUnitDiagnostics
  (JNIEnv *env, jobject obj, jlong jtu) {
    CXTranslationUnit tu = (CXTranslationUnit) jtu;
    unsigned cnt = clang_getNumDiagnostics(tu);
    unsigned i;

    if (cnt == 0) {
        return NULL;
    }

    jclass clsDiagnostic = (jclass) env->NewGlobalRef(env->FindClass("jdk/internal/clang/Diagnostic"));
    jmethodID ctorDiagnostic = env->GetMethodID(clsDiagnostic, "<init>", "(J)V");

    jobjectArray rv = env->NewObjectArray(cnt, clsDiagnostic, NULL);
    jobject jdiag;

    for (i = 0; i < cnt; i++) {
        CXDiagnostic diag = clang_getDiagnostic(tu, i);
        jdiag = env->NewObject(clsDiagnostic, ctorDiagnostic, (jlong) diag);
        env->SetObjectArrayElement(rv, i, jdiag);
    }

    return rv;
}

static int
compareSourceLocation(JNIEnv *env, CXSourceLocation loc1, CXSourceLocation loc2) {
  struct {
    CXFile file;
    unsigned line;
    unsigned column;
    unsigned offset;
  } info1, info2;

  clang_getSpellingLocation(loc1, &info1.file, &info1.line, &info1.column, &info1.offset);
  clang_getSpellingLocation(loc2, &info2.file, &info2.line, &info2.column, &info2.offset);

  CXString fileName1 = clang_getFileName(info1.file);
  CXString fileName2 = clang_getFileName(info2.file);

  int cmp = strcmp(clang_getCString(fileName1), clang_getCString(fileName2));

  clang_disposeString(fileName1);
  clang_disposeString(fileName2);

  if (cmp != 0) {
    env->ThrowNew(clsIAE, "Source locations must be in same file");
    return 0;
  }

  if (info1.line != info2.line) {
    return info1.line - info2.line;
  } else if (info1.column != info2.column) {
    return info1.column - info2.column;
  } else if (info1.offset != info2.offset) {
    return info1.offset - info2.offset;
  }

  return 0;
}

static jboolean
locationInRange(JNIEnv *env, CXSourceLocation loc, CXSourceRange range) {
  CXSourceLocation start = clang_getRangeStart(range);
  CXSourceLocation end = clang_getRangeEnd(range);

  return
    compareSourceLocation(env, loc, start) >= 0 &&
    compareSourceLocation(env, loc, end) <= 0;
}

JNIEXPORT jobjectArray JNICALL Java_jdk_internal_clang_Index_tokenize
  (JNIEnv *env, jobject obj, jlong jtu, jobject range) {
  CXTranslationUnit tu = (CXTranslationUnit) jtu;
  CXSourceRange *ptr = (CXSourceRange*) J2P(env, range);

  CXToken *tokens;
  unsigned nTokens, i;

  clang_tokenize(tu, *ptr, &tokens, &nTokens);


  // This filtering stuff is to ork-around a bug in libclang which
  // includes tokens outside of the range (off-by-one)
  // see: https://llvm.org/bugs/show_bug.cgi?id=9069
  CXToken* filteredTokens = (CXToken*) calloc(nTokens, sizeof(CXToken));
  unsigned nFilteredTokens = 0;
  jobjectArray jtokens = NULL;

  for (i = 0; i < nTokens; i++) {
    CXToken token = tokens[i];
    CXSourceLocation tokenLocation = clang_getTokenLocation(tu, token);
    if (!locationInRange(env, tokenLocation, *ptr)) {
      continue;
    }
    if (env->ExceptionCheck()) {
      goto out;
    }

    filteredTokens[nFilteredTokens++] = token;
  }

  jtokens = env->NewObjectArray(nFilteredTokens, clsjavaLangString, NULL);

  for (i = 0; i < nFilteredTokens; i++) {
    CXString tokenString = clang_getTokenSpelling(tu, filteredTokens[i]);
    jstring str = env->NewStringUTF(clang_getCString(tokenString));
    env->SetObjectArrayElement(jtokens, i, str);
    clang_disposeString(tokenString);
  }

 out:
  free(filteredTokens);
  clang_disposeTokens(tu, tokens, nTokens);

  return jtokens;
}

/*************************************
 * Diagnostic/CXDiagnostic functions
 *************************************/
JNIEXPORT jint JNICALL Java_jdk_internal_clang_Diagnostic_severity
  (JNIEnv *env, jobject obj, jlong diag) {
    return clang_getDiagnosticSeverity((CXDiagnostic) diag);
}

JNIEXPORT jobject JNICALL Java_jdk_internal_clang_Diagnostic_location
  (JNIEnv *env, jobject obj, jlong diag) {
    CXSourceLocation loc = clang_getDiagnosticLocation((CXDiagnostic) diag);
    if (clang_equalLocations(loc, clang_getNullLocation())) {
        return NULL;
    }

    jobject buffer = env->NewDirectByteBuffer(&loc, sizeof(CXSourceLocation));
    return env->NewObject(clsSourceLocation, ctorSourceLocation, buffer);
}

JNIEXPORT jstring JNICALL Java_jdk_internal_clang_Diagnostic_spelling
  (JNIEnv *env, jobject obj, jlong diag) {
    CXString str = clang_getDiagnosticSpelling((CXDiagnostic) diag);
    return CX2JString(env, str);
}

JNIEXPORT jstring JNICALL Java_jdk_internal_clang_Diagnostic_format
  (JNIEnv *env, jobject obj, jlong diag) {
    CXString str = clang_formatDiagnostic((CXDiagnostic) diag,
        clang_defaultDiagnosticDisplayOptions());
    return CX2JString(env, str);
}

JNIEXPORT void JNICALL Java_jdk_internal_clang_Diagnostic_dispose
  (JNIEnv *env, jobject obj, jlong diag) {
    clang_disposeDiagnostic((CXDiagnostic) diag);
}

/*************************************
 * Cursor/CXCursor functions
 *************************************/

JNIEXPORT jboolean JNICALL Java_jdk_internal_clang_Cursor_isDeclaration
  (JNIEnv *env, jobject cursor) {
    CXCursor *ptr = (CXCursor*) J2P(env, cursor);
    return clang_isDeclaration(clang_getCursorKind(*ptr));
}

JNIEXPORT jboolean JNICALL Java_jdk_internal_clang_Cursor_isPreprocessing
  (JNIEnv *env, jobject cursor) {
    CXCursor *ptr = (CXCursor*) J2P(env, cursor);
    return clang_isPreprocessing(clang_getCursorKind(*ptr));
}

JNIEXPORT jboolean JNICALL Java_jdk_internal_clang_Cursor_isInvalid
  (JNIEnv *env, jobject cursor) {
    CXCursor *ptr = (CXCursor*) J2P(env, cursor);
    return clang_isInvalid(clang_getCursorKind(*ptr));
}

JNIEXPORT jboolean JNICALL Java_jdk_internal_clang_Cursor_isDefinition
  (JNIEnv *env, jobject cursor) {
    CXCursor *ptr = (CXCursor*) J2P(env, cursor);
    return clang_isCursorDefinition(*ptr);
}

JNIEXPORT jboolean JNICALL Java_jdk_internal_clang_Cursor_isMacroFunctionLike
  (JNIEnv *env, jobject cursor) {
    CXCursor *ptr = (CXCursor*) J2P(env, cursor);
    return clang_Cursor_isMacroFunctionLike(*ptr);
}

JNIEXPORT jboolean JNICALL Java_jdk_internal_clang_Cursor_isAnonymousStruct
  (JNIEnv *env, jobject cursor) {
    CXCursor *ptr = (CXCursor*) J2P(env, cursor);
    return clang_Cursor_isAnonymous(*ptr);
}

JNIEXPORT jstring JNICALL Java_jdk_internal_clang_Cursor_spelling
  (JNIEnv *env, jobject cursor) {
    CXCursor *ptr = (CXCursor*) J2P(env, cursor);
    CXString spelling = clang_getCursorSpelling(*ptr);
    return CX2JString(env, spelling);
}

JNIEXPORT jstring JNICALL Java_jdk_internal_clang_Cursor_USR
  (JNIEnv *env, jobject cursor) {
    CXCursor *ptr = (CXCursor*) J2P(env, cursor);
    CXString usr = clang_getCursorUSR(*ptr);
    return CX2JString(env, usr);
}

JNIEXPORT jint JNICALL Java_jdk_internal_clang_Cursor_kind1
  (JNIEnv *env, jobject cursor) {
    CXCursor *ptr = (CXCursor*) J2P(env, cursor);
    return clang_getCursorKind(*ptr);
}

struct visitor_data {
    JavaVM *jvm;
    jobject visitor;
    jobject data;
};

enum CXChildVisitResult visitorFunc(CXCursor cursor,
                                    CXCursor parent,
                                    CXClientData data) {
    struct visitor_data *pCtx = (struct visitor_data*) data;
    // Just to be cautious in case callback from different thread
    // although this is likely not the case
    JNIEnv *env;
    if (JNI_OK != pCtx->jvm->AttachCurrentThread(reinterpret_cast<void**>(&env), NULL)) {
        printf("Failed to attach JVM\n");
        return CXChildVisit_Break;
    };

    jobject jC = env->NewDirectByteBuffer(&cursor, sizeof(CXCursor));
    jobject jP = env->NewDirectByteBuffer(&parent, sizeof(CXCursor));
    return (CXChildVisitResult) env->CallStaticIntMethod(clsCursor,
                                                         visitorID,
                                                         pCtx->visitor,
                                                         jC, jP,
                                                         pCtx->data);
}

JNIEXPORT jint JNICALL Java_jdk_internal_clang_Cursor_visitChildren
  (JNIEnv *env, jobject cursor, jobject visitor, jobject data) {
    CXCursor *ptr = (CXCursor*) J2P(env, cursor);
    struct visitor_data ctx;
    env->GetJavaVM(&(ctx.jvm));
    ctx.visitor = visitor;
    ctx.data = data;
    return clang_visitChildren(*ptr, visitorFunc, &ctx);
}

JNIEXPORT jobject JNICALL Java_jdk_internal_clang_Cursor_type
  (JNIEnv *env, jobject cursor) {
    CXCursor *ptr = (CXCursor*) J2P(env, cursor);
    CXType type = clang_getCursorType(*ptr);
    jobject buffer = env->NewDirectByteBuffer(&type, sizeof(CXType));
    return env->NewObject(clsType, ctorType, buffer);
}

JNIEXPORT jobject JNICALL Java_jdk_internal_clang_Cursor_getEnumDeclIntegerType
  (JNIEnv *env, jobject cursor) {
    CXCursor *ptr = (CXCursor*) J2P(env, cursor);
    CXType type = clang_getEnumDeclIntegerType(*ptr);
    jobject buffer = env->NewDirectByteBuffer(&type, sizeof(CXType));
    return env->NewObject(clsType, ctorType, buffer);
}

JNIEXPORT jobject JNICALL Java_jdk_internal_clang_Cursor_getDefinition
  (JNIEnv *env, jobject cursor) {
    CXCursor *ptr = (CXCursor*) J2P(env, cursor);
    CXCursor def = clang_getCursorDefinition(*ptr);
    jobject buffer = env->NewDirectByteBuffer(&def, sizeof(CXCursor));
    return env->NewObject(clsCursor, ctorCursor, buffer);
}

JNIEXPORT jobject JNICALL Java_jdk_internal_clang_Cursor_getSourceLocation
  (JNIEnv *env, jobject cursor) {
    CXCursor *ptr = (CXCursor*) J2P(env, cursor);

    // Some CXCursor has no valid location, such as the one from TranslationUnit
    CXSourceLocation loc = clang_getCursorLocation(*ptr);
    if (clang_equalLocations(loc, clang_getNullLocation())) {
        return NULL;
    }

    jobject buffer = env->NewDirectByteBuffer(&loc, sizeof(CXSourceLocation));
    return env->NewObject(clsSourceLocation, ctorSourceLocation, buffer);
}

JNIEXPORT jobject JNICALL Java_jdk_internal_clang_Cursor_getExtent
  (JNIEnv *env, jobject cursor) {
    CXCursor *ptr = (CXCursor*) J2P(env, cursor);

    CXSourceRange range = clang_getCursorExtent(*ptr);
    if (clang_Range_isNull(range)) {
        return NULL;
    }

    jobject buffer = env->NewDirectByteBuffer(&range, sizeof(CXSourceRange));
    return env->NewObject(clsSourceRange, ctorSourceRange, buffer);
}

JNIEXPORT jobject JNICALL Java_jdk_internal_clang_Cursor_getArgument
  (JNIEnv *env, jobject _self, jint idx) {
    CXCursor *ptr = (CXCursor*) J2P(env, _self);
    CXCursor result = clang_Cursor_getArgument(*ptr, idx);
    jobject buffer = env->NewDirectByteBuffer(&result, sizeof(CXCursor));
    return env->NewObject(clsCursor, ctorCursor, buffer);
}

JNIEXPORT jint JNICALL Java_jdk_internal_clang_Cursor_numberOfArgs
  (JNIEnv *env, jobject _self) {
    CXCursor *ptr = (CXCursor*) J2P(env, _self);
    return clang_Cursor_getNumArguments(*ptr);
}

JNIEXPORT jlong JNICALL Java_jdk_internal_clang_Cursor_getEnumConstantValue
  (JNIEnv *env, jobject _self) {
    CXCursor *ptr = (CXCursor*) J2P(env, _self);
    return clang_getEnumConstantDeclValue(*ptr);
}

JNIEXPORT jlong JNICALL Java_jdk_internal_clang_Cursor_getEnumConstantUnsignedValue
  (JNIEnv *env, jobject _self) {
    CXCursor *ptr = (CXCursor*) J2P(env, _self);
    return clang_getEnumConstantDeclUnsignedValue(*ptr);
}

JNIEXPORT bool JNICALL Java_jdk_internal_clang_Cursor_isBitField
  (JNIEnv *env, jobject _self) {
    CXCursor *ptr = (CXCursor*) J2P(env, _self);
    return clang_Cursor_isBitField(*ptr);
}

JNIEXPORT jint JNICALL Java_jdk_internal_clang_Cursor_getBitFieldWidth
  (JNIEnv *env, jobject _self) {
    CXCursor *ptr = (CXCursor*) J2P(env, _self);
    return clang_getFieldDeclBitWidth(*ptr);
}

JNIEXPORT jlong JNICALL Java_jdk_internal_clang_Cursor_getTranslationUnit0
  (JNIEnv *env, jobject _self) {
    CXCursor *ptr = (CXCursor*) J2P(env, _self);
    return (jlong) clang_Cursor_getTranslationUnit(*ptr);
}

JNIEXPORT jstring JNICALL Java_jdk_internal_clang_Cursor_getMangling
  (JNIEnv *env, jobject _self) {
    CXCursor *ptr = (CXCursor*) J2P(env, _self);
    CXString mangled = clang_Cursor_getMangling(*ptr);
    return CX2JString(env, mangled);
}

JNIEXPORT jboolean JNICALL Java_jdk_internal_clang_Cursor_equalCursor
  (JNIEnv *env, jobject cursor, jobject other) {
    CXCursor *ptr = (CXCursor*) J2P(env, cursor);
    CXCursor *ptrOther = (CXCursor*) J2P(env, other);
    return clang_equalCursors(*ptr, *ptrOther);
}

/*************************************
 * Type <-> CXType related functions
 *************************************/

JNIEXPORT jboolean JNICALL Java_jdk_internal_clang_Type_isVariadic
  (JNIEnv *env, jobject type) {
    CXType *ptr = (CXType*) J2P(env, type);
    return clang_isFunctionTypeVariadic(*ptr);
}

JNIEXPORT jobject JNICALL Java_jdk_internal_clang_Type_resultType
  (JNIEnv *env, jobject type) {
    CXType *ptr = (CXType*) J2P(env, type);
    CXType result = clang_getResultType(*ptr);
    jobject buffer = env->NewDirectByteBuffer(&result, sizeof(CXType));
    return env->NewObject(clsType, ctorType, buffer);
}

JNIEXPORT jint JNICALL Java_jdk_internal_clang_Type_numberOfArgs
  (JNIEnv *env, jobject type) {
    CXType *ptr = (CXType*) J2P(env, type);
    return clang_getNumArgTypes(*ptr);
}

JNIEXPORT jobject JNICALL Java_jdk_internal_clang_Type_argType
  (JNIEnv *env, jobject type, jint idx) {
    CXType *ptr = (CXType*) J2P(env, type);
    CXType result = clang_getArgType(*ptr, idx);
    jobject buffer = env->NewDirectByteBuffer(&result, sizeof(CXType));
    return env->NewObject(clsType, ctorType, buffer);
}

JNIEXPORT jint JNICALL Java_jdk_internal_clang_Type_getCallingConvention1
  (JNIEnv *env, jobject type) {
    CXType *ptr = (CXType*) J2P(env, type);
    return clang_getFunctionTypeCallingConv(*ptr);
}

JNIEXPORT jobject JNICALL Java_jdk_internal_clang_Type_getPointeeType
  (JNIEnv *env, jobject type) {
    CXType *ptr = (CXType*) J2P(env, type);
    CXType result = clang_getPointeeType(*ptr);
    jobject buffer = env->NewDirectByteBuffer(&result, sizeof(CXType));
    return env->NewObject(clsType, ctorType, buffer);
}

JNIEXPORT jobject JNICALL Java_jdk_internal_clang_Type_getElementType
  (JNIEnv *env, jobject type) {
    CXType *ptr = (CXType*) J2P(env, type);
    CXType result = clang_getElementType(*ptr);
    jobject buffer = env->NewDirectByteBuffer(&result, sizeof(CXType));
    return env->NewObject(clsType, ctorType, buffer);
}

JNIEXPORT jlong JNICALL Java_jdk_internal_clang_Type_getNumberOfElements
  (JNIEnv *env, jobject type) {
    CXType *ptr = (CXType*) J2P(env, type);
    return clang_getNumElements(*ptr);
}

JNIEXPORT jobject JNICALL Java_jdk_internal_clang_Type_canonicalType
  (JNIEnv *env, jobject type) {
    CXType *ptr = (CXType*) J2P(env, type);
    CXType result = clang_getCanonicalType(*ptr);
    jobject buffer = env->NewDirectByteBuffer(&result, sizeof(CXType));
    return env->NewObject(clsType, ctorType, buffer);
}

JNIEXPORT jstring JNICALL Java_jdk_internal_clang_Type_spelling
  (JNIEnv *env, jobject type) {
    CXType *ptr = (CXType*) J2P(env, type);
    CXString spelling = clang_getTypeSpelling(*ptr);
    return CX2JString(env, spelling);
}

JNIEXPORT jint JNICALL Java_jdk_internal_clang_Type_kind1
  (JNIEnv *env, jobject type) {
    CXType *ptr = (CXType*) J2P(env, type);
    return ptr->kind;
}

JNIEXPORT jlong JNICALL Java_jdk_internal_clang_Type_size0
  (JNIEnv *env, jobject type) {
    CXType *ptr = (CXType*) J2P(env, type);
    return clang_Type_getSizeOf(*ptr);
}

JNIEXPORT jlong JNICALL Java_jdk_internal_clang_Type_getOffsetOf0
  (JNIEnv *env, jobject type, jstring field_name) {
    CXType *ptr = (CXType*) J2P(env, type);
    const char *name = env->GetStringUTFChars(field_name, NULL);
    long long offset = clang_Type_getOffsetOf(*ptr, name);
    env->ReleaseStringUTFChars(field_name, name);
    return offset;
}

JNIEXPORT jboolean JNICALL Java_jdk_internal_clang_Type_equalType
  (JNIEnv *env, jobject type, jobject other) {
    CXType *ptr = (CXType*) J2P(env, type);
    CXType *ptrOther = (CXType*) J2P(env, other);
    return clang_equalTypes(*ptr, *ptrOther);
}

JNIEXPORT jobject JNICALL Java_jdk_internal_clang_Type_getDeclarationCursor
  (JNIEnv *env, jobject type) {
    CXType *ptr = (CXType*) J2P(env, type);
    CXCursor result = clang_getTypeDeclaration(*ptr);
    jobject buffer = env->NewDirectByteBuffer(&result, sizeof(CXCursor));
    return env->NewObject(clsCursor, ctorCursor, buffer);
}

/*************************************
 * Location related functions
 *************************************/

JNIEXPORT jobject JNICALL Java_jdk_internal_clang_SourceLocation_getFileLocation
  (JNIEnv *env, jobject loc) {
    CXSourceLocation *ptr = (CXSourceLocation*) J2P(env, loc);
    struct LocationFactory f;
    return f.get(env, ptr, clang_getFileLocation);
}

JNIEXPORT jobject JNICALL Java_jdk_internal_clang_SourceLocation_getExpansionLocation
  (JNIEnv *env, jobject loc) {
    CXSourceLocation *ptr = (CXSourceLocation*) J2P(env, loc);
    struct LocationFactory f;
    return f.get(env, ptr, clang_getExpansionLocation);
}

JNIEXPORT jobject JNICALL Java_jdk_internal_clang_SourceLocation_getSpellingLocation
  (JNIEnv *env, jobject loc) {
    CXSourceLocation *ptr = (CXSourceLocation*) J2P(env, loc);
    struct LocationFactory f;
    return f.get(env, ptr, clang_getSpellingLocation);
}

JNIEXPORT bool JNICALL Java_jdk_internal_clang_SourceLocation_isInSystemHeader
  (JNIEnv *env, jobject loc) {
    CXSourceLocation *ptr = (CXSourceLocation*) J2P(env, loc);
    return clang_Location_isInSystemHeader(*ptr);
}

JNIEXPORT bool JNICALL Java_jdk_internal_clang_SourceLocation_isFromMainFile
  (JNIEnv *env, jobject loc) {
    CXSourceLocation *ptr = (CXSourceLocation*) J2P(env, loc);
    return clang_Location_isFromMainFile(*ptr);
}

JNIEXPORT jobject JNICALL Java_jdk_internal_clang_SourceRange_getBegin
  (JNIEnv *env, jobject range) {
    CXSourceRange *ptr = (CXSourceRange*) J2P(env, range);

    // Some CXCursor has no valid location, such as the one from TranslationUnit
    CXSourceLocation loc = clang_getRangeStart(*ptr);
    if (clang_equalLocations(loc, clang_getNullLocation())) {
        return NULL;
    }

    jobject buffer = env->NewDirectByteBuffer(&loc, sizeof(CXSourceLocation));
    return env->NewObject(clsSourceLocation, ctorSourceLocation, buffer);
}

JNIEXPORT jobject JNICALL Java_jdk_internal_clang_SourceRange_getEnd
  (JNIEnv *env, jobject range) {
    CXSourceRange *ptr = (CXSourceRange*) J2P(env, range);

    // Some CXCursor has no valid location, such as the one from TranslationUnit
    CXSourceLocation loc = clang_getRangeEnd(*ptr);
    if (clang_equalLocations(loc, clang_getNullLocation())) {
        return NULL;
    }

    jobject buffer = env->NewDirectByteBuffer(&loc, sizeof(CXSourceLocation));
    return env->NewObject(clsSourceLocation, ctorSourceLocation, buffer);
}

#ifdef __cplusplus
}
#endif
