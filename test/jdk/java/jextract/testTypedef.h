typedef unsigned long count_t;
typedef int (*int_op)(int);
typedef int (*int_op2)(int, int);

typedef struct {
    count_t times;
    int_op op;
} op_sequence;

int_op add;

// Global variable with unnamed function type
int (*another_int_op)(int);

// Function prototype
op_sequence do_ops(int_op op, count_t times);

// anonymous typedef
typedef union {
    int i;
    float f;
} IntOrFloat;

// Completely anonymous enum
enum {
    RED = 0xff0000,
    GREEN = 0x00ff00,
    BLUE = 0x0000ff
};

typedef enum SIZE {
    XS,
    S,
    M,
    L,
    XL
} SIZE;

// Typedef anonymous enum
typedef enum {
   Java,
   C,
   CPP,
   Python,
   Ruby
} codetype_t;

// declaration only
struct Point;
// definition
struct Point {
   int i;
   int j;
};
// different name struct typedef
typedef struct Point POINT;
// layered typedef
typedef POINT point_t;
typedef point_t rectangle[4];

rectangle canvas;

typedef int cordinate_t;
typedef cordinate_t location2D[2];
typedef count_t dimensions[];
typedef count_t *count_ptr;

// same name struct typedef
typedef struct Point3D {
    int i;
    int j;
    int k;
} Point3D;
// User of same name typedef
void drawParamid(Point3D vertices[4]);

// anonymous types not references
struct {
    int foo;
    int bar;
};

static union {
    int i;
    long l;
};

// No way to declare anonymous function type
// But here is a function getFn to return a function type
void (*getFn(void))(int, count_t, int_op);