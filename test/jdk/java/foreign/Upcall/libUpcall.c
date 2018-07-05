#include <stdlib.h>
#include <string.h>

typedef void (*visitor)(int i);

struct MyStruct {
  int field1;
  int field2;
  int field3;
  const void* field4;
  const void* field5;
  const void* field6;
};

typedef void (*struct_upcall_cb)(struct MyStruct f);
typedef double (*double_upcall_cb)(double d1, double d2);

void do_upcall(visitor v, int i) {
  v(i);
}

void struct_upcall(struct_upcall_cb cb, struct MyStruct f) {
  cb(f);
}

double double_upcall(double_upcall_cb cb, double d1, double d2) {
  return cb(d1, d2);
}

static void
swap_elems(void* e1, void* e2, size_t size) {
  void* tmp = alloca(size);

  memcpy(tmp, e1, size);
  memcpy(e1, e2, size);
  memcpy(e2, tmp, size);
}


void slowsort(void *base, size_t nmemb, size_t size,
              int(*compar)(const void *, const void *)) {
  size_t i, j;

  for (i = 0; i < nmemb; i++) {
    char* e1 = (char*)base + i * size;

    for (j = i + 1; j < nmemb; j++) {
      char* e2 = (char*)base + j * size;

      int res = compar(e1, e2);
      if (res > 0) {
        swap_elems(e1, e2, size);
      }
    }
  }
}
