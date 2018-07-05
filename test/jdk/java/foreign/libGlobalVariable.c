#include <stdint.h>
#include <stdbool.h>
#include <stdio.h>
#if defined(__AVX__)
#include <immintrin.h>
#endif

struct MyStruct {
  int i;
};

bool     global_boolean = true;
int8_t   global_i8  = 42;
int16_t  global_i16 = 42;
uint16_t global_u16 = 42;
int32_t  global_i32 = 42;
int64_t  global_i64 = 42;
float    global_f32 = 42;
double   global_d64 = 42;

struct MyStruct global_struct = {
  .i = 42
};

#if defined(__AVX__)
__m256i global_v256;
#else
char global_v256;
#endif

void init() {
#if defined(__AVX__)
  global_v256 = _mm256_set_epi64x(0, 0, 0, 42);
#endif
}
