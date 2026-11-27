# -*- coding: utf-8 -*-
import io
import sys
import traceback

P = "src/com/mycompany/yytt/MainActivity.java"


def run():
    s = io.open(P, encoding="utf-8").read()
    if "setBuiltInZoomControls(true)" in s:
        io.open("patch_result.txt", "w", encoding="utf-8").write("ALREADY size=%d\n" % len(s))
        return

    a1 = "        s.setLoadWithOverviewMode(true);\n"
    assert s.count(a1) == 1, "anchor1 %d" % s.count(a1)
    s = s.replace(
        a1,
        a1
        + "        s.setSupportZoom(true);\n"
        + "        s.setBuiltInZoomControls(true);\n"
        + "        s.setDisplayZoomControls(false);\n",
        1,
    )

    old_items = '        String[] items = {uaText, "查看页面源码"};\n'
    assert s.count(old_items) == 1, "items %d" % s.count(old_items)
    new_items = '        String[] items = {"放大页面", "缩小页面", uaText, "查看页面源码"};\n'
    s = s.replace(old_items, new_items, 1)

    old_click = (
        "                        if (which == 0) {\n"
        "                            toggleDesktopUa();\n"
        "                        } else {\n"
        "                            showSource();\n"
        "                        }\n"
    )
    assert s.count(old_click) == 1, "click %d" % s.count(old_click)
    new_click = (
        "                        if (which == 0) {\n"
        "                            zoomPage(true);\n"
        "                        } else if (which == 1) {\n"
        "                            zoomPage(false);\n"
        "                        } else if (which == 2) {\n"
        "                            toggleDesktopUa();\n"
        "                        } else {\n"
        "                            showSource();\n"
        "                        }\n"
    )
    s = s.replace(old_click, new_click, 1)

    anchor = "    private boolean isWikidotSite() {"
    assert s.count(anchor) == 1, "anchor2 %d" % s.count(anchor)
    zoom = (
        "    private void zoomPage(boolean bigger) {\n"
        "        WebView v = curView();\n"
        "        if (v == null) {\n"
        "            return;\n"
        "        }\n"
        "        try {\n"
        "            if (bigger) {\n"
        "                v.zoomIn();\n"
        "            } else {\n"
        "                v.zoomOut();\n"
        "            }\n"
        "        } catch (Exception e) {\n"
        "        }\n"
        "    }\n"
        "\n"
    )
    s = s.replace(anchor, zoom + anchor, 1)

    io.open(P, "w", encoding="utf-8").write(s)
    io.open("patch_result.txt", "w", encoding="utf-8").write("OK size=%d\n" % len(s))


try:
    run()
except Exception:
    io.open("patch_error.txt", "w", encoding="utf-8").write(traceback.format_exc())
    sys.exit(1)
