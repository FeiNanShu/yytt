# -*- coding: utf-8 -*-
import io, sys, traceback

P = "src/com/mycompany/yytt/MainActivity.java"
RES = "patch_result.txt"
ERR = "patch_error.txt"


def w(name, text):
    io.open(name, "w", encoding="utf-8").write(text)


def run():
    s = io.open(P, encoding="utf-8").read()
    if "setBuiltInZoomControls(true)" in s:
        w(RES, "ALREADY size=%d\n" % len(s))
        return

    a1 = "        s.setLoadWithOverviewMode(true);\n"
    assert s.count(a1) == 1, "anchor1 %d" % s.count(a1)
    s = s.replace(a1, a1 + "        s.setSupportZoom(true);\n        s.setBuiltInZoomControls(true);\n        s.setDisplayZoomControls(false);\n", 1)

    a2 = "        String[] items = {uaText, \"查省顸接只源\"};\n"

    old_items = "        String[] items = {uaText, \"查省顸接只源\"};\n"
    # 用真实串重写（上面两行仅占位，实际以文件内容为准）
    old_items = None
    for line in s.split("\n"):
        if line.strip().startswith("String[] items = {uaText,"):
            old_items = line + "\n"
            break
    assert old_items is not None, "items not found"
    assert s.count(old_items) == 1, "items multi %d" % s.count(old_items)

    print("OLD ITEMS: " + repr(old_items))
    return


try:
    run()
except Exception:
    w(ERR, traceback.format_exc())
    sys.exit(1)
