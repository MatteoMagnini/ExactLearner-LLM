package org.exactlearner.parser;

import org.semanticweb.owlapi.model.OWLClass;

import java.util.*;
import java.util.stream.Collectors;

abstract public class OWLParserBase implements OWLParser {
    public List<OWLClass> orderedClasses;

    @Override
    public Set<String> getClassesNamesAsString() {
        if (this.getClasses().isEmpty()) {
            return new HashSet<>();
        }
        return this.getClasses().get().stream()
                .map(OWLClass::toString)
                .map(s -> s.substring(s.indexOf("#") + 1, s.length() - 1)).map(s -> s.substring(s.lastIndexOf("/") + 1))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getObjectPropertiesAsString() {
        return getObjectProperties().stream()
                .map(Object::toString)
                .map(s -> {
                    String[] strings = s.split("/");
                    String name = strings[strings.length - 1];
                    strings = name.split("#");
                    name = strings[strings.length - 1];
                    return name.replace(">", "");
                })
                .collect(Collectors.toSet());
    }

    public List<OWLClass> getOrderedClasses() {
        if (orderedClasses == null) {
            orderedClasses = new ArrayList<>(getClasses().get().stream().sorted(Comparator.comparing(c -> c.getIRI().getFragment())).toList());
            Collections.shuffle(orderedClasses, new Random(42));
        }
        return orderedClasses;
    }
}
