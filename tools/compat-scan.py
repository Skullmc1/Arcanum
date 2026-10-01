#!/usr/bin/env python3
"""Flags calls in the built jar on Bukkit types whose kind (class vs interface) differs between
two Paper API versions. Usage: compat-scan.py <classes-dir> <paper-api-old.jar> <paper-api-new.jar>
Requires `javap` on PATH. Exit code 1 if any BAD entry is found."""
import glob
import re
import subprocess
import sys

PREFIXES = ("org/bukkit", "io/papermc", "com/destroystokyo")


def main(classes_dir, old_jar, new_jar):
    classes = glob.glob(classes_dir + "/**/*.class", recursive=True)
    out = subprocess.run(["javap", "-c", "-p"] + classes, capture_output=True,
                         text=True, encoding="utf-8", errors="replace").stdout
    calls = {}
    current = None
    for line in out.splitlines():
        m = re.match(r"(?:public |final |abstract )*(?:class|interface|enum) ([\w.$]+)", line)
        if m:
            current = m.group(1)
        m = re.search(r"(invokevirtual|invokeinterface|invokestatic|invokespecial)\s+#\d+\s+// "
                      r"(?:Method|InterfaceMethod) ([\w/$]+)\.([\w<>$]+):", line)
        if m and m.group(2).startswith(PREFIXES):
            calls.setdefault((m.group(1), m.group(2)), set()).add((m.group(3), current))

    owners = sorted({o for _, o in calls})

    def kinds(jar):
        res = {}
        for i in range(0, len(owners), 40):
            names = [o.replace("/", ".") for o in owners[i:i + 40]]
            r = subprocess.run(["javap", "-cp", jar] + names, capture_output=True,
                               text=True, encoding="utf-8", errors="replace")
            for l in r.stdout.splitlines():
                mm = re.match(r"(?:public |protected |final |abstract |static |sealed )*"
                              r"(class|interface|enum) ([\w.$]+)", l)
                if mm:
                    res[mm.group(2).replace(".", "/")] = "interface" if mm.group(1) == "interface" else "class"
        return res

    old, new = kinds(old_jar), kinds(new_jar)
    bad = 0
    for (kind, owner), where in sorted(calls.items()):
        for label, k in (("old", old.get(owner)), ("new", new.get(owner))):
            if k is None:
                continue
            if (kind == "invokeinterface" and k != "interface") or (kind == "invokevirtual" and k == "interface"):
                print("BAD", label, owner, kind, sorted(where)[:4])
                bad += 1
        if kind == "invokestatic" and old.get(owner) and new.get(owner) and old[owner] != new[owner]:
            print("BAD static kind change", owner, old[owner], new[owner], sorted(where)[:3])
            bad += 1
    print("owners checked:", len(owners), "| problems:", bad)
    return 1 if bad else 0


if __name__ == "__main__":
    sys.exit(main(*sys.argv[1:4]))
