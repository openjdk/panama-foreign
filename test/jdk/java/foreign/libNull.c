#ifdef _WIN64
#define EXPORT __declspec(dllexport)
#else
#define EXPORT
#endif

#include <stddef.h>

EXPORT void* get_null() { return NULL; }
