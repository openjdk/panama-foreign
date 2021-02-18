#include <stdarg.h>

#ifdef __cplusplus
extern "C" {
#endif // __cplusplus

#ifdef _WIN64
#define EXPORT __declspec(dllexport)
#else
#define EXPORT
#endif

typedef void (*Foo)(int arg);

struct Bar {
   Foo foo;
};

EXPORT Foo f;

#ifdef __cplusplus
}
#endif // __cplusplus