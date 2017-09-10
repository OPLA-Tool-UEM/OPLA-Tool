package arquitetura.helpers;

import arquitetura.representation.Variability;
import arquitetura.representation.Variant;

import java.util.List;
import java.util.stream.Collectors;

public class Strings {

    public static String spliterVariants(List<Variant> list) {
        return list.stream().map(Variant::getName).collect(Collectors.joining(", "));
    }

    public static String spliterVariabilities(List<Variability> list) {
        return list.stream().map(Variability::getName).collect(Collectors.joining(", "));
    }

}
