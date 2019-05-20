typedef unsigned char uchar_t;
typedef _Atomic(int) atomic_int_t;

struct SomeTypes {
  _Atomic uchar_t auc;
  volatile unsigned int vui;
  _Atomic atomic_int_t aai;
};

typedef _Atomic struct SomeTypes atomic_some_types_t;
