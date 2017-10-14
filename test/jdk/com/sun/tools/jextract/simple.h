int global;

/*
   Basic types exclude unsigned integer types
   N1570: 6.2.5.14
 */
struct anonymous {
    char ch;
    signed char sch;
    short s;
    int n;
    long l;
    long long ll;
    float f;
    double d;
    long double ld;
} basics;

// N1570: 6.2.5.6
struct _unsigned {
    _Bool b;
    unsigned char ch;
    unsigned short s;
    unsigned int n;
    unsigned long l;
    unsigned long long ll;
} *unsigned_int;

void func(struct anonymous s, char* str);
