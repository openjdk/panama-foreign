#ifndef __ANONYMOUS_DECLARATION__
#define __ANONYMOUS_DECLARATION__

// Anonymous struct as golbal variable
struct {
    int width;
    int height;
} canvas_size;

// Anonymous struct typedef and anonymous struct as field
typedef struct {
    struct {
        int x;
        int y;
    } center;
    int radius;
} circle_t;

// Typedef with named struct
typedef struct point {
    int x;
    int y;
} point_t;

// Anonymous enum and union as field
typedef struct {
    enum {
        LINE,
        CIRCLE,
        POLYGON
    } kind;
    union {
        struct {
            point_t a;
            point_t b;
        } line;
        circle_t circle;
        struct {
            int sides;
            struct point *vertices;
        } polygon;
    };
} shape_t;

// Completely anonymous enum
enum {
    RED = 0xff0000,
    GREEN = 0x00ff00,
    BLUE = 0x0000ff
};

// Typedef anonymous enum
typedef enum {
   Java,
   C,
   CPP,
   Python,
   Ruby
} codetype_t;

// Named enum
enum SIZE {
   XS,
   S,
   M,
   L,
   XL,
   XXL
};

// Cannot have anonymous type in function, leads to visibility warning.
// While it's possible, make no sense to use in header file
shape_t makeCircle(point_t center, int radius);

// Make a right triangle by moving down delta.x and move right delta.y from origin.
typedef point_t delta_t;
shape_t makeRightTriangle(struct point origin, delta_t delta);

int byArea(long *area_calculator(shape_t*), shape_t *s1, shape_t *s2);

typedef int (*comparator)(shape_t *s1, shape_t *s2);
typedef long (*quantifyShape)(shape_t *shape);

comparator compareBy(quantifyShape calculator);

long area(shape_t *shape);

extern comparator compareByArea; // = compareBy(area);

#endif // __ANONYMOUS_DECLARATION__
