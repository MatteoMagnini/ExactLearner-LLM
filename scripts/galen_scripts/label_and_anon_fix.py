import xml.etree.ElementTree as ET
import re
import sys

in_file = "src/main/resources/ontologies/large/galen.owl"
out_file = "src/main/resources/ontologies/large/galen_fixed.owl"

if len(sys.argv) > 1:
    in_file = sys.argv[1]

if len(sys.argv) > 2:
    out_file = sys.argv[2]

with open(in_file) as r:
    tree = ET.parse(r)

root = tree.getroot()

pattern = re.compile(r"(?<=[a-z])(?=[A-Z]|[0-9])|(?<=[A-Z]|[0-9])(?=[A-Z][a-z])")

fixed = {
    "pH": "pH",
    "pHAlteration": "pH Alteration"
}

def get_label(name: str) -> str:
    name = name.split("#")[-1]
    if name in fixed:
        return fixed[name]
    return pattern.sub(" ", name)

classes = root.findall("{http://www.w3.org/2002/07/owl#}Class")
for c in classes:
    name = c.get("{http://www.w3.org/1999/02/22-rdf-syntax-ns#}about")
    if name == None:
        continue

    if "Anonymous-" in name:
        eq = c.findall("{http://www.w3.org/2002/07/owl#}equivalentClass")
        c.attrib.pop("{http://www.w3.org/1999/02/22-rdf-syntax-ns#}about")
        if len(eq) == 1:
            cs = eq[0].findall("{http://www.w3.org/2002/07/owl#}Class")
            if len(cs) != 1:
                print("Wrong number of classes", len(cs), name)
                continue
            intersection = cs[0].findall("{http://www.w3.org/2002/07/owl#}intersectionOf")
            if len(intersection) == 1:
                c.remove(eq[0])
                c.append(intersection[0])
            else:
                print("Wrong number of intersection", len(intersection), name)
        else:
            root.remove(c)
            print("Wrong number of eq, is removed", len(eq), name)
        continue

    short = get_label(name)

    sub = ET.SubElement(c, "{http://www.w3.org/2000/01/rdf-schema#}label")
    sub.text = short

prop = root.findall("{http://www.w3.org/2002/07/owl#}ObjectProperty")
for p in prop:
    name = p.get("{http://www.w3.org/1999/02/22-rdf-syntax-ns#}about")
    if name == None:
        print("something went wrong. No prop name")
        continue
    short = get_label(name)
    sub = ET.SubElement(p, "{http://www.w3.org/2000/01/rdf-schema#}label")
    sub.text = short

ET.indent(tree, space="\t", level=0)

tree.write(out_file)
