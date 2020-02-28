
struct Foo {
    unsigned int a:1;
    unsigned int b:1;
    unsigned int c:30;
};

struct Bar {
    unsigned int x:1;
    unsigned int y:31;
    struct Foo z[1];
};
