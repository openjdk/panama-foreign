#include "precompiled.hpp"
#include "classfile/javaClasses.inline.hpp"
#include "classfile/stringTable.hpp"
#include "code/codeCache.hpp"
#include "code/dependencyContext.hpp"
#include "compiler/compileBroker.hpp"
#include "interpreter/interpreter.hpp"
#include "interpreter/oopMapCache.hpp"
#include "interpreter/linkResolver.hpp"
#include "memory/allocation.inline.hpp"
#include "memory/oopFactory.hpp"
#include "memory/resourceArea.hpp"
#include "oops/objArrayOop.inline.hpp"
#include "oops/oop.inline.hpp"
#include "prims/nativeInvoker.hpp"
#include "prims/methodHandles.hpp"
#include "runtime/compilationPolicy.hpp"
#include "runtime/javaCalls.hpp"
#include "runtime/jniHandles.inline.hpp"
#include "runtime/timerTrace.hpp"
#include "runtime/reflection.hpp"
#include "runtime/signature.hpp"
#include "runtime/stubRoutines.hpp"
#include "utilities/exceptions.hpp"

BufferBlob* NativeInvoker::_invoke_native_blob = NULL;

class InvokeNativeGenerator : public StubCodeGenerator {
public:
  InvokeNativeGenerator(CodeBuffer* code) : StubCodeGenerator(code, PrintMethodHandleStubs) {}

  void generate();
};

void InvokeNativeGenerator::generate() {
  NativeInvoker::generate_invoke_native(_masm);
}

void NativeInvoker::generate_adapter() {
    _invoke_native_blob = BufferBlob::create("invoke_native_blob", MethodHandles::adapter_code_size);

    CodeBuffer code2(_invoke_native_blob);
    InvokeNativeGenerator g2(&code2);
    g2.generate();
    code2.log_section_sizes("InvokeNativeBlob");
}

JVM_ENTRY(void, NI_invokeNative(JNIEnv* env, jobject _unused, jlongArray args_jh, jlongArray rets_jh, jlongArray recipe_jh, jlong nep_jh)) {
  arrayHandle recipe(THREAD, (arrayOop)JNIHandles::resolve(recipe_jh));
  arrayHandle args(THREAD, (arrayOop)JNIHandles::resolve(args_jh));
  arrayHandle rets(THREAD, (arrayOop)JNIHandles::resolve(rets_jh));

  assert(thread->thread_state() == _thread_in_vm, "thread state is: %d", thread->thread_state());
  address c = (address)nep_jh;

  NativeInvoker::invoke_native(recipe, args, rets, c, thread);
}
JVM_END

JVM_ENTRY(static jlong, NI_AllocateUpcallStub(JNIEnv *env, jobject _unused, jobject rec))
  Handle receiver(THREAD, JNIHandles::resolve(rec));
  return (jlong)NativeInvoker::generate_upcall_stub(receiver);
JVM_END

JVM_ENTRY(void, NI_FreeUpcallStub(JNIEnv *env, jobject _unused, jlong addr))
  NativeInvoker::free_upcall_stub((char*)addr);
JVM_END

JVM_ENTRY(static jlong, NI_FindNativeAddress(JNIEnv *env, jobject _unused, jstring name)) {
  ThreadToNativeFromVM ttnfv(thread);
  if (name != NULL) {
    char utfName[128];
    uint len = env->GetStringUTFLength(name);
    int unicode_len = env->GetStringLength(name);
    if (len >= sizeof (utfName)) {
      // FIXME: don't bother with memory allocation for now.
      THROW_(vmSymbols::java_lang_NullPointerException(), 0);
    }
    env->GetStringUTFRegion(name, 0, unicode_len, utfName);

#ifndef _WINDOWS
    void* handle = RTLD_DEFAULT;
#else
    void* handle = 0; // FIXME
#endif // _WINDOWS
    return (jlong)os::dll_lookup(handle, utfName);
  } else {
    THROW_(vmSymbols::java_lang_NullPointerException(), 0);
  }
}
JVM_END

#define CC (char*)  /*cast a literal from (const char*)*/
#define FN_PTR(f) CAST_FROM_FN_PTR(void*, &f)
#define LANG "Ljava/lang/"

// These are the native methods on jdk.internal.nicl.NativeInvoker.
static JNINativeMethod NI_methods[] = {
  {CC "invokeNative",       CC "([J[J[JJ)V",           FN_PTR(NI_invokeNative)},
  {CC "allocateUpcallStub", CC "(Ljdk/internal/nicl/UpcallHandler;)J",                 FN_PTR(NI_AllocateUpcallStub)},
  {CC "freeUpcallStub",     CC "(J)V",                FN_PTR(NI_FreeUpcallStub)},
  {CC "findNativeAddress",  CC "(" LANG "String;)J",   FN_PTR(NI_FindNativeAddress)}
};

/**
 * This one function is exported, used by NativeLookup.
 */
JVM_ENTRY(void, JVM_RegisterNativeInvokerMethods(JNIEnv *env, jclass NI_class)) {
  {
    ThreadToNativeFromVM ttnfv(thread);

    int status = env->RegisterNatives(NI_class, NI_methods, sizeof(NI_methods)/sizeof(JNINativeMethod));
    guarantee(status == JNI_OK && !env->ExceptionOccurred(),
              "register jdk.internal.nicl.NativeInvoker natives");
  }
}
JVM_END
