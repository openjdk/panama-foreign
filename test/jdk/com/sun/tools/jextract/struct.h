#ifdef __cplusplus
extern "C" {
#endif // __cplusplus

typedef struct UndefinedStruct UndefinedStruct;
UndefinedStruct* allocateUndefinedStruct();

typedef struct UndefineStructForPointer *UndefinedStructPointer;
UndefinedStructPointer getParent(UndefinedStructPointer node);
struct UndefinedStructForPointer* getSibling(UndefinedStructPointer node);
UndefinedStructPointer getFirstChild(struct UndefinedStructForPointer* node);

struct Opaque* allocate_opaque_struct();

typedef struct TypedefNamedAsIs {
    int i;
    long l;
} TypedefNamedAsIs;

typedef struct TypedefNamedDifferent {
    long (*fn)(int i, int j);
} TypedefNamedDifferent_t;

typedef struct {
    union {
        long l;
        struct {
            int x1;
            int y1;
        };
        struct {
            int x2;
            int y2;
        } p2;
    };
} TypedefAnonymous;

struct Plain {
    int x;
    int y;
};

TypedefAnonymous getAnonymous(TypedefNamedDifferent_t fn, int x, int y);

void emptyArguments();
void voidArguments(void);

typedef void* (*FunctionPointer)(void *data, void **array_data);

void* FunctionWithVoidPointer(void *data, void **array_data);

struct IncompleteArray {
    long list_length;
    void *ptr;
    void **junk;
    FunctionPointer fn;
    void *list_of_data[];
};

void** GetArrayData(struct IncompleteArray *par);

// This works with C, but incomplete array is omitted as not exist
void* GetData(struct IncompleteArray ar);

#ifdef __cplusplus
}
#endif // __cplusplus
