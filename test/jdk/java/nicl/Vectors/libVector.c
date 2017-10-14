#include <stdio.h>

#if defined(__AVX__)
#include <stdint.h>
#include <immintrin.h>

int foo(__m256i v) {
  union {
    __m256i v;
    uint64_t a[4];
  } u;
  int i;

  u.v = v;

  return 4711;
}


__m256i reverse(__m256i v) {
  union {
    __m256i v;
    uint64_t a[4];
  } u;

  u.v = v;

  uint64_t tmp;

  tmp = u.a[0];
  u.a[0] = u.a[3];
  u.a[3] = tmp;

  tmp = u.a[1];
  u.a[1] = u.a[2];
  u.a[2] = tmp;

  return u.v;
}
#endif
