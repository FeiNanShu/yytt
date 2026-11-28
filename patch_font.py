# -*- coding: utf-8 -*-
import io
import sys
import traceback

P = "desktop-java/src/main/java/com/feinanshu/DuiyuApp.java"
FONT = "/fonts/NotoSansCJKsc-Regular.otf"


def run():
    s = io.open(P, encoding="utf-8").read()
    if "UI_FONT" in s:
        io.open("patch_result.txt", "w", encoding="utf-8").write("ALREADY size=%d\n" % len(s))
        return

    if "import java.io.InputStream;" not in s:
        assert s.count("import javafx.concurrent.Worker;") == 1
        s = s.replace("import javafx.concurrent.Worker;", "import java.io.InputStream;\nimport javafx.concurrent.Worker;", 1)

    if "import javafx.scene.text.Font;" not in s:
        assert s.count("import javafx.scene.Scene;") == 1
        s = s.replace("import javafx.scene.Scene;", "import javafx.scene.Scene;\nimport javafx.scene.text.Font;", 1)

    anchor = "    private final List<Tab> tabs = new ArrayList<>();"
    assert s.count(anchor) == 1, "anchor %d" % s.count(anchor)
    block = (
        "    static String UI_FONT = \"System\";\n"
        "\n"
        "    static {\n"
        "        try (InputStream in = DuiyuApp.class.getResourceAsStream(\"" + FONT + "\")) {\n"
        "            if (in != null) {\n"
        "                Font f = Font.loadFont(in, 13);\n"
        "                if (f != null) {\n"
        "                    UI_FONT = f.getFamily();\n"
        "                }\n"
        "            }\n"
        "        } catch (Throwable ignored) {\n"
        "        }\n"
        "    }\n"
        "\n" + anchor
    )
    s = s.replace(anchor, block, 1)

    old = '        root.setStyle("-fx-background-color: " + C_BG + ";");'
    assert s.count(old) == 1, "style %d" % s.count(old)
    s = s.replace(old, '        root.setStyle("-fx-background-color: " + C_BG + "; -fx-font-family: \'" + UI_FONT + "\';");', 1)

    io.open(P, "w", encoding="utf-8").write(s)
    io.open("patch_result.txt", "w", encoding="utf-8").write("OK size=%d\n" % len(s))


try:
    run()
except Exception:
    io.open("patch_error.txt", "w", encoding="utf-8").write(traceback.format_exc())
    sys.exit(1)
