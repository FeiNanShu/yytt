# -*- coding: utf-8 -*-
import io
import sys
import traceback

P = "desktop-java/src/main/java/com/feinanshu/DuiyuApp.java"


def run():
    s = io.open(P, encoding="utf-8").read()
    if "Microsoft YaHei" not in s:
        io.open("patch_result.txt", "w", encoding="utf-8").write("ALREADY size=%d\n" % len(s))
        return

    old1 = "font-family:'Microsoft YaHei','Segoe UI',sans-serif;"
    assert s.count(old1) == 1, "html %d" % s.count(old1)
    s = s.replace(old1, "font-family:'Segoe UI',sans-serif;", 1)

    old2 = 'root.setStyle("-fx-background-color: " + C_BG + "; -fx-font-family: \'Microsoft YaHei\', \'Segoe UI\', sans-serif;");'
    assert s.count(old2) == 1, "root %d" % s.count(old2)
    s = s.replace(old2, 'root.setStyle("-fx-background-color: " + C_BG + ";");', 1)

    io.open(P, "w", encoding="utf-8").write(s)
    io.open("patch_result.txt", "w", encoding="utf-8").write("OK size=%d\n" % len(s))


try:
    run()
except Exception:
    io.open("patch_error.txt", "w", encoding="utf-8").write(traceback.format_exc())
    sys.exit(1)
