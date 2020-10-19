struct Foo {
   struct {
       int x;
   } _struct;

   union {
       struct {
           int x;
       } _struct;
   } _union;
};
