# -*- coding: utf-8 -*-
import io
import sys
import traceback

P = "src/com/mycompany/yytt/MainActivity.java"


def run():
    s = io.open(P, encoding="utf-8").read()
    if "backBtn" in s:
        io.open("patch_result.txt", "w", encoding="utf-8").write("ALREADY size=%d\n" % len(s))
        return

    a1 = "    private TextView tabsBtn;\n"
    assert s.count(a1) == 1, "decl %d" % s.count(a1)
    s = s.replace(a1, a1 + "    private TextView backBtn;\n    private TextView fwdBtn;\n", 1)

    a2 = '        toolsBtn = textBtn("▤ 工具箱", 12, C_TXT2);\n'
    assert s.count(a2) == 1, "bottom %d" % s.count(a2)
    new2 = (
        '        backBtn = textBtn("←", 18, C_TXT2);\n'
        "        bottom.addView(backBtn, new LinearLayout.LayoutParams(dp(44), dp(48)));\n"
        "\n"
        '        fwdBtn = textBtn("→", 18, C_TXT2);\n'
        "        bottom.addView(fwdBtn, new LinearLayout.LayoutParams(dp(44), dp(48)));\n"
        "\n"
    )
    s = s.replace(a2, new2 + a2, 1)

    a3 = "        reloadBtn.setOnClickListener(new View.OnClickListener() {\n"
    assert s.count(a3) == 1, "click %d" % s.count(a3)
    new3 = (
        "        backBtn.setOnClickListener(new View.OnClickListener() {\n"
        "            public void onClick(View v) {\n"
        "                if (editing) {\n"
        "                    endEdit();\n"
        "                }\n"
        "                WebView wv = curView();\n"
        "                if (wv != null && wv.canGoBack()) {\n"
        "                    wv.goBack();\n"
        "                }\n"
        "            }\n"
        "        });\n"
        "\n"
        "        fwdBtn.setOnClickListener(new View.OnClickListener() {\n"
        "            public void onClick(View v) {\n"
        "                if (editing) {\n"
        "                    endEdit();\n"
        "                }\n"
        "                WebView wv = curView();\n"
        "                if (wv != null && wv.canGoForward()) {\n"
        "                    wv.goForward();\n"
        "                }\n"
        "            }\n"
        "        });\n"
        "\n"
    )
    s = s.replace(a3, new3 + a3, 1)

    io.open(P, "w", encoding="utf-8").write(s)
    io.open("patch_result.txt", "w", encoding="utf-8").write("OK size=%d\n" % len(s))


try:
    run()
except Exception:
    io.open("patch_error.txt", "w", encoding="utf-8").write(traceback.format_exc())
    sys.exit(1)
