# -*- coding: utf-8 -*-
import io
import sys
import traceback

P = "src/com/mycompany/yytt/MainActivity.java"


def run():
    s = io.open(P, encoding="utf-8").read()
    if 'cn.bing.com/search?q="' in s and "urlenc" in s:
        io.open("patch_result.txt", "w", encoding="utf-8").write("ALREADY size=%d\n" % len(s))
        return

    old = '            showSearch(raw);\n'
    assert s.count(old) == 1, "call %d" % s.count(old)
    s = s.replace(
        old,
        '            loadInCurrent("https://cn.bing.com/search?q=" + urlenc(raw));\n',
        1,
    )

    i = s.find("    private void showSearch(String q) {")
    j = s.find("    private String fixUrl(String raw) {")
    assert i > 0 and j > i, "slice %d %d" % (i, j)
    s = s[:i] + s[j:]

    anchor = "    private String fixUrl(String raw) {"
    assert s.count(anchor) == 1, "anchor %d" % s.count(anchor)
    code = (
        "    private String urlenc(String s) {\n"
        "        try {\n"
        '            return java.net.URLEncoder.encode(s, "UTF-8");\n'
        "        } catch (Exception e) {\n"
        "            return s;\n"
        "        }\n"
        "    }\n"
        "\n"
    )
    s = s.replace(anchor, code + anchor, 1)

    assert s.count("showSearch") == 0, "left showSearch %d" % s.count("showSearch")
    io.open(P, "w", encoding="utf-8").write(s)
    io.open("patch_result.txt", "w", encoding="utf-8").write("OK size=%d\n" % len(s))


try:
    run()
except Exception:
    io.open("patch_error.txt", "w", encoding="utf-8").write(traceback.format_exc())
    sys.exit(1)
