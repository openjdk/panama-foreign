package java.nicl.layout;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

abstract class AbstractLayout<L extends AbstractLayout<L>> implements Layout {
    private final Map<String, String> annotations;

    public AbstractLayout(Map<String, String> annotations) {
        this.annotations = annotations;
    }

    @Override
    public Map<String, String> annotations() {
        return annotations;
    }

    @Override
    public L stripAnnotations() {
        return dup(NO_ANNOS);
    }

    @Override
    public L withAnnotation(String name, String value) {
        Map<String, String> newAnnotations = new HashMap<>(annotations);
        newAnnotations.put(name, value);
        return dup(newAnnotations);
    }

    abstract L dup(Map<String, String> annotations);

    String wrapWithAnnotations(String s) {
        if (!annotations.isEmpty()) {
            return String.format("%s%s",
                    s, annotations.entrySet().stream()
                            .map(e -> e.getKey().equals(NAME) ?
                                    String.format("(%s=%s)", e.getKey(), e.getValue()) :
                                    String.format("(%s)", e.getValue()))
                            .collect(Collectors.joining()));
        } else {
            return s;
        }
    }

    static final Map<String, String> NO_ANNOS = new HashMap<>();
}
