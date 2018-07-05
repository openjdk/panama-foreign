#include "precompiled.hpp"
#include "asm/macroAssembler.hpp"
#include "classfile/javaClasses.inline.hpp"
#include "interpreter/interpreter.hpp"
#include "interpreter/interpreterRuntime.hpp"
#include "memory/allocation.inline.hpp"
#include "memory/resourceArea.hpp"
#include "include/jvm.h"
#include "prims/nativeInvoker.hpp"
#include "runtime/javaCalls.hpp"
#include "logging/log.hpp"
#include "logging/logStream.hpp"
#include "oops/arrayOop.inline.hpp"
#include "runtime/jniHandles.inline.hpp"

#define __ _masm->

#ifdef _LP64

static const size_t INTEGER_ARGUMENT_REGISTERS_NOOF = Argument::n_int_register_parameters_c;
static const size_t INTEGER_RETURN_REGISTERS_NOOF = 2;
static const size_t VECTOR_ARGUMENT_REGISTERS_NOOF = Argument::n_float_register_parameters_c;
static const size_t VECTOR_RETURN_REGISTERS_NOOF = 2;

static Register integer_argument_registers[INTEGER_ARGUMENT_REGISTERS_NOOF] = {
  c_rarg0, c_rarg1, c_rarg2, c_rarg3,
#ifndef _WIN64
  c_rarg4, c_rarg5
#endif
};

static Register integer_return_registers[INTEGER_RETURN_REGISTERS_NOOF] = {
  rax,
#ifndef _WIN64
  rdx
#endif
};

static XMMRegister vector_argument_registers[VECTOR_ARGUMENT_REGISTERS_NOOF] = {
  c_farg0, c_farg1, c_farg2, c_farg3,
#ifndef _WIN64
  c_farg4, c_farg5, c_farg6, c_farg7
#endif
};

static XMMRegister vector_return_registers[VECTOR_RETURN_REGISTERS_NOOF] = {
  xmm0,
#ifndef _WIN64
  xmm1
#endif
};

#else // _LP64

static const size_t INTEGER_ARGUMENT_REGISTERS_NOOF = 0;
static const size_t INTEGER_RETURN_REGISTERS_NOOF = 1;
static const size_t VECTOR_ARGUMENT_REGISTERS_NOOF = 0;
static const size_t VECTOR_RETURN_REGISTERS_NOOF = 1;

static Register integer_return_registers[INTEGER_RETURN_REGISTERS_NOOF] = {
  rax
};

static XMMRegister vector_return_registers[VECTOR_RETURN_REGISTERS_NOOF] = {
  xmm0
};

#endif // _LP64

struct VectorRegister {
  static const size_t VECTOR_MAX_WIDTH_BITS = 512; // AVX-512 (64-byte) vector types
  static const size_t VECTOR_MAX_WIDTH_BYTES = VECTOR_MAX_WIDTH_BITS / 8;
  static const size_t VECTOR_MAX_WIDTH_U64S = VECTOR_MAX_WIDTH_BITS / 64;
  static const size_t VECTOR_MAX_WIDTH_FLOATS = VECTOR_MAX_WIDTH_BITS / 32;
  static const size_t VECTOR_MAX_WIDTH_DOUBLES = VECTOR_MAX_WIDTH_BITS / 64;

  union {
    uint8_t bits[VECTOR_MAX_WIDTH_BYTES];
    uint64_t u64[VECTOR_MAX_WIDTH_U64S];
    float f[VECTOR_MAX_WIDTH_FLOATS];
    double d[VECTOR_MAX_WIDTH_DOUBLES];
  };
};

static struct {
  bool inited;
  struct {
    Klass* klass;
    Symbol* name;
    Symbol* sig;
  } upcall_method;  // java.nicl.UpcallHandler::invoke
} upcall_info;

#include "classfile/symbolTable.hpp"
// FIXME: This should be initialized explicitly instead of lazily/racily
static void upcall_init(void) {
#if 0
  fprintf(stderr, "upcall_init()\n");
#endif

  TRAPS = Thread::current();
  ResourceMark rm;

  const char* cname = "jdk/internal/nicl/UpcallHandler";
  const char* mname = "invoke";
  const char* mdesc = "(Ljdk/internal/nicl/UpcallHandler;JJJJJ)V";
  Symbol* cname_sym = SymbolTable::lookup(cname, (int)strlen(cname), THREAD);
  Symbol* mname_sym = SymbolTable::lookup(mname, (int)strlen(mname), THREAD);
  Symbol* mdesc_sym = SymbolTable::lookup(mdesc, (int)strlen(mdesc), THREAD);

#if 0
  ::fprintf(stderr, "cname_sym: %p\n", cname_sym);
  ::fprintf(stderr, "mname_sym: %p\n", mname_sym);
  ::fprintf(stderr, "mdesc_sym: %p\n", mdesc_sym);
#endif

  Klass* k = SystemDictionary::resolve_or_null(cname_sym, THREAD);
#if 0
  ::fprintf(stderr, "Klass: %p\n", k);
#endif

  Method* method = k->lookup_method(mname_sym, mdesc_sym);
#if 0
  ::fprintf(stderr, "Method: %p\n", method);
#endif

  upcall_info.upcall_method.klass = k;
  upcall_info.upcall_method.name = mname_sym;
  upcall_info.upcall_method.sig = mdesc_sym;

  upcall_info.inited = true;
}

struct upcall_context {
  struct {
    uintptr_t rbx;
#ifdef _LP64
    uintptr_t r12;
    uintptr_t r13;
    uintptr_t r14;
    uintptr_t r15;
#endif
  } preserved;

  struct {
#ifdef _LP64
    union {
      struct {
        uintptr_t rdi;
        uintptr_t rsi;
        uintptr_t rdx;
        uintptr_t rcx;
        uintptr_t r8;
        uintptr_t r9;
      } reg;
      uintptr_t regs[INTEGER_ARGUMENT_REGISTERS_NOOF];
    } integer;

    union {
      struct {
        VectorRegister xmm0;
        VectorRegister xmm1;
        VectorRegister xmm2;
        VectorRegister xmm3;
        VectorRegister xmm4;
        VectorRegister xmm5;
        VectorRegister xmm6;
        VectorRegister xmm7;
      } reg;
      VectorRegister regs[VECTOR_ARGUMENT_REGISTERS_NOOF];
    } vector;

    uintptr_t rax;
#endif

    uintptr_t rsp;
  } args;

  struct {
    union {
      struct {
        uintptr_t rax;
#ifdef _LP64
        uintptr_t rdx;
#endif
      } reg;
      uintptr_t regs[INTEGER_RETURN_REGISTERS_NOOF];
    } integer;

    union {
      struct {
        VectorRegister xmm0;
#ifdef _LP64
        VectorRegister xmm1;
#endif
      } reg;
      VectorRegister regs[VECTOR_RETURN_REGISTERS_NOOF];
    } vector;
  } returns;
};

static void upcall_helper(jobject rec, struct upcall_context* context) {
#if 0
  ::fprintf(stderr, "upcall_helper(%p, %p)\n", rec, context);
#endif

  JavaThread* thread = JavaThread::current();

  assert(thread->is_Java_thread(), "really?");

#if 0
  fprintf(stderr, "args.integer.regs: %p\n", context->args.integer.regs);
  for (size_t i = 0; i < INTEGER_ARGUMENT_REGISTERS_NOOF; i++) {
    fprintf(stderr, "args.integer.regs[%zd]: 0x%lx\n", i, context->args.integer.regs[i]);
  }

  fprintf(stderr, "args.vector.regs: %p\n", context->args.vector.regs);
  for (size_t i = 0; i < VECTOR_ARGUMENT_REGISTERS_NOOF; i++) {
    fprintf(stderr, "args.vector.regs[%zd]:\n", i);

    fprintf(stderr, "\traw: | ");
    for (size_t j = 0; j < VectorRegister::VECTOR_MAX_WIDTH_U64S; j++) {
      fprintf(stderr, "\t0x%016lx |", context->args.vector.regs[i].u64[j]);
    }
    fprintf(stderr, "\n");

    fprintf(stderr, "\tfloat: |");
    for (size_t j = 0; j < VectorRegister::VECTOR_MAX_WIDTH_FLOATS; j++) {
      fprintf(stderr, "%f |", context->args.vector.regs[i].f[j]);
    }
    fprintf(stderr, "\n");

    fprintf(stderr, "\tdouble: |");
    for (size_t j = 0; j < VectorRegister::VECTOR_MAX_WIDTH_DOUBLES; j++) {
      fprintf(stderr, "%f |", context->args.vector.regs[i].d[j]);
    }
    fprintf(stderr, "\n");
  }

  fprintf(stderr, "args.rsp: 0x%lx\n", context->args.rsp);
  for (int i = 0; i < 64; i += 8) {
    fprintf(stderr, "args.stack+%d: 0x%lx\n", i, *(uintptr_t*)(context->args.rsp+i));
  }
#endif

  if (!upcall_info.inited) {
    upcall_init();
  }

  ThreadInVMfromNative __tiv(thread);

  JavaValue result(T_VOID);
  JavaCallArguments args(6 * 2);

  args.push_jobject(rec);
#ifdef _LP64
  args.push_long((jlong)&context->args.integer.regs);
  args.push_long((jlong)&context->args.vector.regs);
#else
  args.push_long((jlong)0);
  args.push_long((jlong)0);
#endif
  args.push_long((jlong)context->args.rsp);
  args.push_long((jlong)&context->returns.integer.regs);
  args.push_long((jlong)&context->returns.vector.regs);

  JavaCalls::call_static(&result, upcall_info.upcall_method.klass, upcall_info.upcall_method.name, upcall_info.upcall_method.sig, &args, thread);

#if 0
  fprintf(stderr, "returns.integer.regs: %p\n", context->returns.integer.regs);
  fprintf(stderr, "returns.integer.reg.rax: 0x%lx\n", context->returns.integer.reg.rax);
  fprintf(stderr, "returns.integer.reg.rdx: 0x%lx\n", context->returns.integer.reg.rdx);
#endif
}

address NativeInvoker::generate_upcall_stub(Handle& rec_handle) {
  CodeBuffer buffer("upcall_stub", 1024, 1024);

  MacroAssembler* _masm = new MacroAssembler(&buffer);

  jobject rec = JNIHandles::make_weak_global(rec_handle);

#if 0
  fprintf(stderr, "generate_upcall_stub(%p)\n", rec);
#endif


  // stub code
  __ enter();

  // save pointer to JNI receiver handle into constant segment
  Address rec_adr = __ as_Address(InternalAddress(__ address_constant((address)rec)));

  __ subptr(rsp, sizeof(struct upcall_context));
  __ andptr(rsp, -64);

  // Save preserved registers according to calling convention
  __ movptr(Address(rsp, offsetof(struct upcall_context, preserved.rbx)), rbx);
#ifdef _LP64
  __ movptr(Address(rsp, offsetof(struct upcall_context, preserved.r12)), r12);
  __ movptr(Address(rsp, offsetof(struct upcall_context, preserved.r13)), r13);
  __ movptr(Address(rsp, offsetof(struct upcall_context, preserved.r14)), r14);
  __ movptr(Address(rsp, offsetof(struct upcall_context, preserved.r15)), r15);
#endif

  // FIXME: Tons of stuff stripped here...


#ifdef _LP64
  // Capture argument registers
  __ movptr(Address(rsp, offsetof(struct upcall_context, args.integer.reg.rdi)), rdi);
  __ movptr(Address(rsp, offsetof(struct upcall_context, args.integer.reg.rsi)), rsi);
  __ movptr(Address(rsp, offsetof(struct upcall_context, args.integer.reg.rdx)), rdx);
  __ movptr(Address(rsp, offsetof(struct upcall_context, args.integer.reg.rcx)), rcx);
  __ movptr(Address(rsp, offsetof(struct upcall_context, args.integer.reg.r8)), r8);
  __ movptr(Address(rsp, offsetof(struct upcall_context, args.integer.reg.r9)), r9);

  for (size_t i = 0; i < VECTOR_ARGUMENT_REGISTERS_NOOF; i++) {
    XMMRegister reg = vector_argument_registers[i];

    size_t offset = offsetof(struct upcall_context, args.vector.regs) + i * sizeof(VectorRegister);

    if (UseAVX >= 3) {
      __ evmovdqul(Address(rsp, (int)offset), reg, Assembler::AVX_512bit);
    } else if (UseAVX >= 1) {
      __ vmovdqu(Address(rsp, (int)offset), reg);
    } else {
      __ movdqu(Address(rsp, (int)offset), reg);
    }
  }

  __ movptr(Address(rsp, offsetof(struct upcall_context, args.rax)), rax);
#endif

  // Capture prev stack pointer (stack arguments base)
  __ lea(rax, Address(rbp, 16)); // skip frame+return address
  __ movptr(Address(rsp, offsetof(struct upcall_context, args.rsp)), rax);


  // Call upcall helper
#ifdef _LP64
  __ movptr(c_rarg0, rec_adr);
  __ movptr(c_rarg1, rsp);
#else
  __ movptr(rax, rsp);
  __ subptr(rsp, 8);
  __ movptr(Address(rsp, 4), rax);
  __ movptr(rax, rec_adr);
  __ movptr(Address(rsp, 0), rax);
#endif
  __ call(RuntimeAddress(CAST_FROM_FN_PTR(address, upcall_helper)));
#ifndef _LP64
  __ addptr(rsp, 8);
#endif


  // Handle return values
  for (size_t i = 0; i < INTEGER_RETURN_REGISTERS_NOOF; i++) {
    Register reg = integer_return_registers[i];
    ssize_t offs = offsetof(struct upcall_context, returns.integer.regs) + i * sizeof(uintptr_t);

    __ movptr(reg, Address(rsp, offs));
  }

  for (size_t i = 0; i < VECTOR_RETURN_REGISTERS_NOOF; i++) {
    XMMRegister reg = vector_return_registers[i];
    ssize_t offs = offsetof(struct upcall_context, returns.vector.regs) + i * sizeof(VectorRegister);
    if (UseAVX >= 3) {
      __ evmovdqul(reg, Address(rsp, offs), Assembler::AVX_512bit);
    } else if (UseAVX >= 1) {
      __ vmovdqu(reg, Address(rsp, offs));
    } else {
      __ movdqu(reg, Address(rsp, offs));
    }
  }


  // FIXME: More stuff stripped here

  // Restore preserved registers
#ifdef _LP64
  __ movptr(r12, Address(rsp, offsetof(struct upcall_context, preserved.r12)));
  __ movptr(r13, Address(rsp, offsetof(struct upcall_context, preserved.r13)));
  __ movptr(r14, Address(rsp, offsetof(struct upcall_context, preserved.r14)));
  __ movptr(r15, Address(rsp, offsetof(struct upcall_context, preserved.r15)));
#endif
  __ movptr(rbx, Address(rsp, offsetof(struct upcall_context, preserved.rbx)));

  // FIXME: More stuff stripped here

  __ leave();
  __ ret(0);

  _masm->flush();

  BufferBlob* blob = BufferBlob::create("upcall_stub", &buffer);

  return blob->code_begin();
}

//// shuffle recipe based (mikael style) stub ////

//#define DEBUG_OPS
//#define DEBUG_CALLS

typedef void (*InvokeNativeStub)(struct ShuffleDowncallContext* ctxt);

enum ShuffleRecipeStorageClass {
  CLASS_BUF,
  CLASS_FIRST = CLASS_BUF,
  CLASS_STACK,
  CLASS_VECTOR,
  CLASS_INTEGER,
  CLASS_LAST = CLASS_INTEGER,
  CLASS_NOOF
};

static ShuffleRecipeStorageClass index2storage_class[CLASS_NOOF + 1] = {
  CLASS_BUF, CLASS_STACK, CLASS_VECTOR, CLASS_INTEGER, CLASS_NOOF
};

static const char* index2storage_class_name[CLASS_NOOF] = {
  "CLASS_BUF", "CLASS_STACK", "CLASS_VECTOR", "CLASS_INTEGER"
};

static ShuffleRecipeStorageClass next_storage_class(ShuffleRecipeStorageClass c) {
  int idx = (int)c + 1;
  assert(idx < CLASS_NOOF + 1, "Out of bounds");
  return index2storage_class[idx];
}


static size_t class2maxwidth(ShuffleRecipeStorageClass c) {
  switch (c) {
  case CLASS_BUF:
  case CLASS_STACK:
  case CLASS_INTEGER:
    return 1;

  case CLASS_VECTOR:
    if (UseAVX >= 3) {
      return 8;
    } else if (UseAVX >= 1) {
      return 4;
    } else {
      return 2;
    }

  default:
    assert(false, "Unexpected class");
    return 1;
  }
}

enum ShuffleRecipeOp {
  OP_STOP,
  OP_SKIP,
  OP_PULL,
  OP_PULL_LABEL,
  OP_CREATE_BUFFER,
  OP_NOP,
  OP_NOOF
};

static const char* op_name[OP_NOOF] = {
  "OP_STOP", "OP_SKIP", "OP_PULL", "OP_PULL_LABEL", "OP_CREATE_BUFFER", "OP_NOP"
};

#ifndef PRODUCT
static const char* op2name(ShuffleRecipeOp op) {
  assert(op <= OP_NOOF, "invalid op");
  return op_name[op];
}
#endif

class ShuffleRecipe : public StackObj {
public:
  ShuffleRecipe(arrayHandle recipe)
    : _recipe(recipe) {
    assert(_recipe()->length() > 0, "Empty recipe not allowed");

    init_sizes();
  }

  arrayHandle recipe() { return _recipe; }
  uint64_t word(size_t index) {
    uint64_t* bits = (uint64_t*)_recipe()->base(T_LONG);
    return bits[index];
  }

  size_t length() { return _length; }

  size_t stack_args_slots() { return _stack_args_slots; }
  size_t buffer_slots() { return _buffer_slots; }
  size_t nlabels() { return _nlabels; }

#ifndef PRODUCT
  void print(outputStream* s);
#endif

private:
  void init_sizes();

  arrayHandle _recipe; // the long[] recipe array
  size_t _length;

  size_t _buffer_slots;
  size_t _stack_args_slots;
  size_t _nlabels;
};

class ShuffleRecipeStream {
public:
  ShuffleRecipeStream(ShuffleRecipe& recipe)
    : _recipe(recipe) {
    _next_word_index = 0;
    _cur_class = CLASS_FIRST;
    _direction = ARGUMENTS;

    read_recipe_word();
  }

  void read_recipe_word() {
    _cur_bits = _recipe.word(_next_word_index);
    _next_word_index++;
  }

  bool has_more() {
    return _cur_class < CLASS_NOOF;
  }

  void init_for_returns() {
    assert(_direction == ARGUMENTS, "stream already advanced");
    _cur_class = CLASS_FIRST;
    _direction = RETURNS;
  }

  ShuffleRecipeOp next() {
    assert(has_more(), "stream empty");

    if (_cur_bits == 1) {
      read_recipe_word();
    }

    ShuffleRecipeOp op = (ShuffleRecipeOp)(_cur_bits & 7);
    _cur_bits >>= 3;

    if (op == OP_STOP) {
      _cur_class = next_storage_class(_cur_class);
    }

    return op;
  }

private:
  enum Direction { ARGUMENTS, RETURNS };

  Direction _direction;
  ShuffleRecipe& _recipe;

  size_t _next_word_index;
  uint64_t _cur_bits;

  ShuffleRecipeStorageClass _cur_class;
};

void ShuffleRecipe::init_sizes() {
  _length = _recipe()->length();

  size_t slots_for_class[CLASS_NOOF] = { 0 };
  size_t nbuffers = 0;

  int cur_class = CLASS_FIRST;

  ShuffleRecipeStream stream(*this);

  while(stream.has_more() && cur_class <= CLASS_STACK) {
    switch (stream.next()) {
    case OP_NOP:
      break;

    case OP_STOP:
      cur_class++;
      break;

    case OP_CREATE_BUFFER:
      nbuffers++;
      break;

    case OP_SKIP:
    case OP_PULL:
    case OP_PULL_LABEL:
      slots_for_class[cur_class]++;
      break;

    default:
      assert(false, "Unexpected op");
      break;
    }
  }

  _stack_args_slots = slots_for_class[CLASS_STACK];
  _buffer_slots = slots_for_class[CLASS_BUF];
  _nlabels = nbuffers;
}

#ifndef PRODUCT
void ShuffleRecipe::print(outputStream* s) {
  ShuffleRecipeStream stream(*this);

  s->print_cr("Arguments:");
  while (stream.has_more()) {
    ShuffleRecipeOp op = stream.next();

    s->print_cr("OP: %s", op2name(op));
  }

  s->print_cr("Returns:");
  while (stream.has_more()) {
    ShuffleRecipeOp op = stream.next();

    s->print_cr("OP: %s", op2name(op));
  }
}
#endif


struct ShuffleDowncallContext {
  struct {
#ifdef _LP64
    uint64_t integer[INTEGER_ARGUMENT_REGISTERS_NOOF];
    VectorRegister vector[VECTOR_ARGUMENT_REGISTERS_NOOF];
    uintptr_t rax;
#endif
    uint64_t* stack_args;
    size_t stack_args_bytes;
    address next_pc;
  } arguments;

  struct {
    uint64_t integer[INTEGER_RETURN_REGISTERS_NOOF];
    VectorRegister vector[VECTOR_RETURN_REGISTERS_NOOF];
  } returns;
};

static void dump_vector_register(outputStream* out, XMMRegister reg, VectorRegister value) {
  out->print("%s = {", reg->name());
  for (size_t i = 0; i < VectorRegister::VECTOR_MAX_WIDTH_U64S; i++) {
    if (i != 0) {
      out->print(",");
    }
    out->print(" 0x%016" PRIx64, value.u64[i]);
  }
  out->print_cr(" }");
}

static void dump_integer_register(outputStream* out, Register reg, uint64_t value) {
  out->print_cr("%s = 0x%016" PRIx64, reg->name(), value);
}

#ifdef _LP64
static void dump_stack_slot(outputStream* out, size_t offset, uint64_t value) {
  out->print_cr("[sp+0x%zx] = 0x%016" PRIx64, offset, value);
}
#endif

static void dump_argument_registers(struct ShuffleDowncallContext* ctxt) {
#ifdef _LP64
  LogTarget(Info, panama) lt;
  if (lt.is_enabled()) {
    LogStream ls(lt);
    ls.print("Argument registers:\n");
    for (size_t i = 0; i < INTEGER_ARGUMENT_REGISTERS_NOOF; i++) {
      dump_integer_register(&ls, integer_argument_registers[i], ctxt->arguments.integer[i]);
    }
    for (size_t i = 0; i < VECTOR_ARGUMENT_REGISTERS_NOOF; i++) {
      dump_vector_register(&ls, vector_argument_registers[i], ctxt->arguments.vector[i]);
    }
    dump_integer_register(&ls, rax, ctxt->arguments.rax);

    for (size_t i = 0; i < ctxt->arguments.stack_args_bytes; i += sizeof(uint64_t)) {
      size_t slot = i / sizeof(uint64_t);
      dump_stack_slot(&ls, i, ctxt->arguments.stack_args[slot]);
    }
  }
#endif
}

static void dump_return_registers(struct ShuffleDowncallContext* ctxt) {
  LogTarget(Info, panama) lt;
  if (lt.is_enabled()) {
    LogStream ls(lt);
    ls.print("Return registers:\n");
    for (size_t i = 0; i < INTEGER_RETURN_REGISTERS_NOOF; i++) {
      dump_integer_register(&ls, integer_return_registers[i], ctxt->returns.integer[i]);
    }
    for (size_t i = 0; i < VECTOR_RETURN_REGISTERS_NOOF; i++) {
      dump_vector_register(&ls, vector_return_registers[i], ctxt->returns.vector[i]);
    }
  }
}


class ShuffleDowncall : public StackObj {
public:
  ShuffleDowncall(ShuffleRecipe& recipe, arrayHandle args, arrayHandle rets, address code, address stub)
    : _recipe(recipe), _args(args), _rets(rets), _code(code), _stub((InvokeNativeStub)stub), _stream(recipe) {
    memset(&_context.arguments, 0, sizeof(_context.arguments));
    memset(&_context.returns, 0, sizeof(_context.returns));

    _context.arguments.stack_args_bytes = _recipe.stack_args_slots() * sizeof(uint64_t);

    if (_context.arguments.stack_args_bytes == 0) {
      _context.arguments.stack_args = NULL;
    } else {
      _context.arguments.stack_args = NEW_RESOURCE_ARRAY(uint64_t, _context.arguments.stack_args_bytes / sizeof(uint64_t));
      memset(_context.arguments.stack_args, 0, _context.arguments.stack_args_bytes);
    }

    _context.arguments.next_pc = code;

    _buffers = NEW_RESOURCE_ARRAY(uint64_t, _recipe.buffer_slots());
  }

  void invoke(JavaThread* thread) {
#ifdef DEBUG_CALLS
    ::fprintf(stderr, "calling target function\n");
#endif

    prepare_call();

    dump_argument_registers(&_context);

    {
      assert(thread->thread_state() == _thread_in_vm, "thread state is: %d", thread->thread_state());
      ThreadToNativeFromVM ttnfvm(thread);
      assert(thread->thread_state() == _thread_in_native, "thread state is: %d", thread->thread_state());
      _stub(context());
      assert(thread->thread_state() == _thread_in_native, "thread state is: %d", thread->thread_state());
    }
    assert(thread->thread_state() == _thread_in_vm, "thread state is: %d", thread->thread_state());

    dump_return_registers(&_context);

    process_returns();
  }

private:
  void copy_argument_value(void** src_addrp) {
    LogTarget(Debug, panama) lt;
    if (lt.is_enabled()) {
      LogStream ls(lt);
      uint64_t* values = (uint64_t*)*src_addrp;
      for (size_t i = 0; i < _npulls; i++) {
        ls.print_cr("Pulling %3zd times to %20s[%3zd]: 0x%" PRIx64 "\n",
          _npulls, index2storage_class_name[_cur_class], _index_in_class, values[i]);
      }
    }

    switch (_cur_class) {
    case CLASS_BUF:
      assert(_index_in_class < _recipe.buffer_slots(), "out of bounds");
      memcpy((char*)_buffers + _index_in_class * sizeof(uint64_t), *src_addrp, sizeof(void*));
      break;

    case CLASS_STACK:
      assert(_index_in_class < _recipe.stack_args_slots(), "out of bounds");
      memcpy(&_context.arguments.stack_args[_index_in_class], *src_addrp, sizeof(uint64_t));
      if (lt.is_enabled()) {
        LogStream ls(lt);
        ls.print_cr("Pulling stack value: 0x%" PRIx64 "\n", *(uint64_t*)*src_addrp);
      }
      break;

#ifdef _LP64
    case CLASS_VECTOR:
      assert(_index_in_class < VECTOR_ARGUMENT_REGISTERS_NOOF, "out of bounds");
      memcpy(&_context.arguments.vector[_index_in_class], *src_addrp, _npulls * sizeof(uint64_t));
      break;

    case CLASS_INTEGER:
      assert(_index_in_class < INTEGER_ARGUMENT_REGISTERS_NOOF, "out of bounds");
      memcpy(&_context.arguments.integer[_index_in_class], *src_addrp, sizeof(uint64_t));
      break;
#endif

    default:
      assert(false, "Invalid class");
      break;
    }

    *src_addrp = (char*)*src_addrp + _npulls * sizeof(uint64_t);
    _npulls = 0;
    _index_in_class++;
  }

  void copy_return_value(void** dst_addrp) {
    switch (_cur_class) {
    case CLASS_BUF:
      assert(_index_in_class < _recipe.buffer_slots(), "out of bounds");
      memcpy(*dst_addrp, (char*)_buffers + _index_in_class * sizeof(uint64_t), sizeof(void*));
      break;

    case CLASS_STACK:
      assert(false, "Invalid class");
      break;

    case CLASS_VECTOR:
      assert(_index_in_class < VECTOR_RETURN_REGISTERS_NOOF, "out of bounds");
      memcpy(*dst_addrp, &_context.returns.vector[_index_in_class], _npulls * sizeof(uint64_t));
      break;

    case CLASS_INTEGER:
      assert(_index_in_class < INTEGER_RETURN_REGISTERS_NOOF, "out of bounds");
      memcpy(*dst_addrp, &_context.returns.integer[_index_in_class], sizeof(uint64_t));
      break;

    default:
      assert(false, "Invalid class");
      break;
    }

    *dst_addrp = (char*)*dst_addrp + _npulls * sizeof(uint64_t);
    _npulls = 0;
    _index_in_class++;
  }

  void prepare_call() {
#ifdef DEBUG_CALLS
    ::fprintf(stderr, "preparing_call()\n");
#endif

    _cur_class = CLASS_FIRST;
    _index_in_class = 0;
    _npulls = 0;

    void* cur_value_data = _args()->base(T_LONG);
    struct {
      void** labels;
      size_t produce;
      size_t consume;
    } labels;

    labels.labels = NEW_RESOURCE_ARRAY(void*, _recipe.nlabels());
    labels.produce = labels.consume = 0;

    while(_stream.has_more()) {
      ShuffleRecipeOp op = _stream.next();

#ifdef DEBUG_OPS
      ::fprintf(stderr, "OP: %s\n", op2name(op));
#endif

      switch (op) {
      case OP_NOP:
        break;

      case OP_STOP:
        if (_npulls > 0) {
          copy_argument_value(&cur_value_data);
        }

#ifdef _LP64
        if (_cur_class == CLASS_VECTOR) {
          _context.arguments.rax = _index_in_class;
        }
#endif

        _index_in_class = 0;
        _cur_class = next_storage_class(_cur_class);
        break;

      case OP_CREATE_BUFFER:
        assert(labels.produce < _recipe.nlabels(), "out of bounds");
        _labels[labels.produce] = (void*)((char*)_buffers + _index_in_class * sizeof(uint64_t));
        labels.produce++;
        break;

      case OP_PULL_LABEL:
        assert(labels.consume < labels.produce, "out of bounds");
        copy_argument_value(&_labels[labels.consume]);
        break;

      case OP_SKIP:
        if (_npulls > 0) {
          copy_argument_value(&cur_value_data);
        } else {
          _index_in_class++;
        }
        break;

      case OP_PULL:
        _npulls++;
        if (_npulls == class2maxwidth(_cur_class)) {
          copy_argument_value(&cur_value_data);
        }
        break;

      default:
        assert(false, "Unexpected op");
        break;
      }
    }
  }

  void process_returns() {
#ifdef DEBUG_CALLS
    ::fprintf(stderr, "processing returns\n");
#endif

    _stream.init_for_returns();

    void* cur_value_data = _rets()->base(T_LONG);
    struct {
      size_t consume;
    } labels = { 0 };

    _cur_class = CLASS_FIRST;
    // FIXME: What about buffers?

    while(_stream.has_more()) {
      ShuffleRecipeOp op = _stream.next();

      switch (op) {
      case OP_NOP:
        break;

      case OP_STOP:
        if (_npulls > 0) {
          copy_return_value(&cur_value_data);
        }

        _index_in_class = 0;
        _cur_class = next_storage_class(_cur_class);
        break;

      case OP_CREATE_BUFFER:
        assert(false, "Creating buffer now allowed in this phase");
        break;

      case OP_PULL_LABEL:
        assert(labels.consume < _recipe.nlabels(), "out of bounds");
        copy_return_value((void**)&_labels[labels.consume]);
        break;

      case OP_SKIP:
        if (_npulls > 0) {
          copy_return_value(&cur_value_data);
        } else {
          _index_in_class++;
        }
        break;

      case OP_PULL:
        _npulls++;
        if (_npulls == class2maxwidth(_cur_class)) {
          copy_return_value(&cur_value_data);
        }
        break;

      default:
        assert(false, "Unexpected op");
        break;
      }
    }
  }

  struct ShuffleDowncallContext* context() { return &_context; }

  ShuffleRecipe& _recipe;
  ShuffleRecipeStream _stream;
  arrayHandle _args;
  arrayHandle _rets;
  address _code;
  InvokeNativeStub _stub;

  struct ShuffleDowncallContext _context;

  ShuffleRecipeStorageClass _cur_class;
  size_t _index_in_class;
  size_t _npulls;

  void* _src_addr;

  void* _cur_value_data;

  void* _buffers;
  void** _labels;
};

#ifndef PRODUCT
class ShuffleRecipeVerifier : public StackObj {
public:
  ShuffleRecipeVerifier(ShuffleRecipe& recipe, size_t args_length, size_t rets_length)
    : _recipe(recipe), _args_length(args_length), _rets_length(rets_length), _stream(_recipe) {
  }

  void verify() {
    for (size_t i = 0; i < _recipe.length(); i++) {
      assert((_recipe.word(i) >> 63) == 1, "MSB in recipe word must be set");
    }

    do_verify(ARGUMENTS);

    _stream.init_for_returns();
    do_verify(RETURNS);
  }

private:
  enum Direction {
    ARGUMENTS,
    RETURNS
  };

  void do_verify(Direction direction) {
    struct op_stats {
      size_t op_count_per_class[CLASS_NOOF][OP_NOOF];
      size_t op_count[OP_NOOF];
    };

    struct op_stats stats;

    memset(&stats, 0, sizeof(stats));

    int cur_class = CLASS_FIRST;
    bool done = false;

    while (_stream.has_more()) {
      assert(!done, "Stream unexpectedly returned additional tokens");

      ShuffleRecipeOp op = _stream.next();

      stats.op_count_per_class[cur_class][op]++;
      stats.op_count[op]++;

      switch (op) {
      case OP_NOP:
      case OP_SKIP:
      case OP_PULL:
        break;

      case OP_STOP:
        cur_class++;
        if (cur_class == CLASS_NOOF) {
          done = true;
        }
        break;

      case OP_CREATE_BUFFER:
        assert(cur_class == CLASS_BUF, "Buffers may only be created in buffer class");
        assert(direction == ARGUMENTS, "Buffers can only be created when processing arguments");
        break;

      case OP_PULL_LABEL:
        assert(cur_class != CLASS_BUF, "Must not pull pull labels in buffer class");
        assert(direction == ARGUMENTS, "Buffer labels can only be pulled when processing arguments");
        break;

      default:
        assert(false, "Unexpected op");
        break;
      }
    }

    assert(done, "Not enough STOP operations");

    assert(stats.op_count[OP_CREATE_BUFFER] == stats.op_count[OP_PULL_LABEL], "All labels must be pulled");
    assert(direction == RETURNS || stats.op_count[OP_PULL] == _args_length, "All argument values must be pulled");
    assert(direction == ARGUMENTS || stats.op_count[OP_PULL] == _rets_length, "All return values must be pulled");
  }

private:
  ShuffleRecipe& _recipe;
  ShuffleRecipeStream _stream;
  size_t _args_length;
  size_t _rets_length;
};
#endif

void NativeInvoker::generate_invoke_native(MacroAssembler* _masm) {


#if 0
  fprintf(stderr, "generate_invoke_native()\n");
#endif

  /**
   * invoke_native_stub(struct ShuffleDowncallContext* ctxt) {
   *   rbx = ctxt;
   *
   *   stack = alloca(ctxt->arguments.stack_args_bytes);
   *
   *   load_all_registers();
   *   memcpy(stack, ctxt->arguments.stack_args, arguments.stack_args_bytes);
   *
   *   (*ctxt->arguments.next_pc)();
   *
   *   store_all_registers();
   * }
   */

  __ enter();

  // Put the context pointer in ebx/rbx - it's going to be heavily used below both before and after the call
  Register ctxt_reg = rbx;

#ifdef _LP64
  __ block_comment("init_and_alloc_stack");

  __ push(ctxt_reg); // need to preserve register

#ifdef _LP64
  __ movptr(ctxt_reg, c_rarg0);
#else
  __ movptr(ctxt_reg, Address(rbp, 8));
#endif

  __ block_comment("allocate_stack");
  __ movptr(rcx, Address(ctxt_reg, offsetof(struct ShuffleDowncallContext, arguments.stack_args_bytes)));
  __ subptr(rsp, rcx);
  __ andptr(rsp, -64);

  // Note: rcx is used below!


  __ block_comment("load_arguments");

  __ shrptr(rcx, LogBytesPerWord); // bytes -> words
  __ movptr(rsi, Address(ctxt_reg, offsetof(struct ShuffleDowncallContext, arguments.stack_args)));
  __ movptr(rdi, rsp);
  __ rep_mov();

#ifdef _LP64
  for (size_t i = 0; i < VECTOR_ARGUMENT_REGISTERS_NOOF; i++) {
    // [1] -> 64 bit -> xmm
    // [2] -> 128 bit -> xmm
    // [4] -> 256 bit -> ymm
    // [8] -> 512 bit -> zmm

    XMMRegister reg = vector_argument_registers[i];
    size_t offs = offsetof(struct ShuffleDowncallContext, arguments.vector) + i * sizeof(VectorRegister);
    if (UseAVX >= 3) {
      __ evmovdqul(reg, Address(ctxt_reg, (int)offs), Assembler::AVX_512bit);
    } else if (UseAVX >= 1) {
      __ vmovdqu(reg, Address(ctxt_reg, (int)offs));
    } else {
      __ movdqu(reg, Address(ctxt_reg, (int)offs));
    }
  }

  for (size_t i = 0; i < INTEGER_ARGUMENT_REGISTERS_NOOF; i++) {
    size_t offs = offsetof(struct ShuffleDowncallContext, arguments.integer) + i * sizeof(uintptr_t);
    __ movptr(integer_argument_registers[i], Address(ctxt_reg, (int)offs));
  }

  __ movptr(rax, Address(ctxt_reg, offsetof(struct ShuffleDowncallContext, arguments.rax)));
#endif


  // call target function
  __ block_comment("call target function");
  __ call(Address(ctxt_reg, offsetof(struct ShuffleDowncallContext, arguments.next_pc)));


  __ block_comment("store_registers");
  for (size_t i = 0; i < INTEGER_RETURN_REGISTERS_NOOF; i++) {
    ssize_t offs = offsetof(struct ShuffleDowncallContext, returns.integer) + i * sizeof(uintptr_t);
    __ movptr(Address(ctxt_reg, offs), integer_return_registers[i]);
  }

  for (size_t i = 0; i < VECTOR_RETURN_REGISTERS_NOOF; i++) {
    // [1] -> 64 bit -> xmm
    // [2] -> 128 bit -> xmm (SSE)
    // [4] -> 256 bit -> ymm (AVX)
    // [8] -> 512 bit -> zmm (AVX-512, aka AVX3)

    XMMRegister reg = vector_return_registers[i];
    size_t offs = offsetof(struct ShuffleDowncallContext, returns.vector) + i * sizeof(VectorRegister);
    if (UseAVX >= 3) {
      __ evmovdqul(Address(ctxt_reg, (int)offs), reg, Assembler::AVX_512bit);
    } else if (UseAVX >= 1) {
      __ vmovdqu(Address(ctxt_reg, (int)offs), reg);
    } else {
      __ movdqu(Address(ctxt_reg, (int)offs), reg);
    }
  }
#else
  __ hlt();
#endif

  // Restore backed up preserved register
  __ movptr(ctxt_reg, Address(rbp, -(int)sizeof(uintptr_t)));

  __ leave();
  __ ret(0);

  __ flush();
}

void NativeInvoker::invoke_native(arrayHandle recipe_arr, arrayHandle args_arr, arrayHandle rets_arr, address code, JavaThread* thread) {
  ShuffleRecipe recipe(recipe_arr);

#ifndef PRODUCT
  ShuffleRecipeVerifier verifier(recipe, args_arr()->length(), rets_arr()->length());
  verifier.verify();
#endif

  ShuffleDowncall call(recipe, args_arr, rets_arr, code, invoke_native_address());
  call.invoke(thread);
}

void NativeInvoker::free_upcall_stub(char *addr) {
  //find code blob
  CodeBlob* cb = CodeCache::find_blob(addr);
  assert(cb != NULL, "Attempting to free non-existent stub");
  //free global JNI handle
  jobject* rec_ptr = (jobject*)(void*)cb -> content_begin();
  JNIHandles::destroy_weak_global(*rec_ptr);
  //free code blob
  CodeCache::free(cb);
}

jobject NativeInvoker::get_upcall_handler(char *addr) {
  //find code blob
  CodeBlob* cb = CodeCache::find_blob(addr);
  if (cb != NULL) {
      //free global JNI handle
      jobject* rec_ptr = (jobject*)(void*)cb -> content_begin();
      return *rec_ptr;
  } else {
      return NULL;
  }
}
