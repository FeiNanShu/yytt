# -*- coding: utf-8 -*-
import io
import sys
import traceback

P = "desktop-java/src/main/java/com/feinanshu/DuiyuApp.java"

B64 = "iVBORw0KGgoAAAANSUhEUgAAAGAAAABgBAMAAAAQtmoLAAAABGdBTUEAALGPC/xhBQAAACBjSFJNAAB6JgAAgIQAAPoAAACA6AAAdTAAAOpgAAA6mAAAF3CculE8AAAAG1BMVEUAAAAAESIAM2YRVbsRRIgRVZkAIkQRM3cAETNA2M+NAAAAAWJLR0QAiAUdSAAAAAd0SU1FB+oLHAUZOKPdmB8AAAJPSURBVFjD7ZVNb9pAEIZ3LWN6XAN2rkahqFcYu1yxV6AcU5w0VxsRpUekNve0qvq7O/uBv7Jg55BL5PeEvfPsOzPrHQjp1atXr17vIsreGGWF0/b4EQ9KAGCX15ev4vqz8wxQAyDaVvMapVHVlB5SaAIA4ZcCcXA9yor1/UYGNAGAROf1icsNNOH/0suvAYgSYUKv1dOKFdmcAwDuMfXH08OOYW/KtWNZ06Z8Oy3jAb5X93qoduXPaaOYTKCiG+LpX5WWKJMnmepXbKio5QlgK3/EyrDedFlG7n/GhVtHhk0xk2AkIwNbFJP7QQMYQOL6G1iP1bYCIH8FsbAhnLqz9PYVANHcPRw9WaoCZDYLa+vu0dYAANzlbCybqQGCWS6oL3toBDBqDCEjBUA5OqiVbw3AVq+xhhUpAXItagCTg1cASzI/AYcLwKDikMbic83IJGIlsG4ARB3c2oMl5fhlTwQUFQ7RvHlweAdmhYPazobCIflNDKL/NqoGDkcnc7IhaIe7H8wUT+zE3WNbVxQdhiE/4hELwJq7s7UZEJnqlESXhioluudwDtBt1YCtUhqaWlQCXulg4aHromMjMNAOWMOxkpJ92UHUwIsaSDuAKVFeK9o2fBdKFi+K1imF2uE+MAJ41qkxpeSFnJMfeGVbdUpOTi7J4RKgP0MmTzp8IS1yHrCtO+ZkeD8j9py1xRPiYg2wlRMgYm57PErcjJuDmHadwvFmiiuTqnnTTY+nUdxVVExNWLLOgJhHaj51lsMrf3Gd5L8xvlevXr0+sP4D8Miuq6RN728AAAAldEVYdGRhdGU6Y3JlYXRlADIwMjYtMTEtMjhUMDU6MjU6NTUrMDA6MDDSdWixAAAAJXRFWHRkYXRlOm1vZGlmeQAyMDI2LTExLTI4VDA1OjI1OjU1KzAwOjAwoyjQDQAAAABJRU5ErkJggg=="


def run():
    s = io.open(P, encoding="utf-8").read()
    if "LOGO_B64" in s:
        io.open("patch_result.txt", "w", encoding="utf-8").write("ALREADY size=%d\n" % len(s))
        return

    i = s.find("    static final String WELCOME =")
    j = s.find('        + "</body></html>";', i)
    assert i > 0 and j > i, "welcome %d %d" % (i, j)
    j = j + len('        + "</body></html>";')

    new = (
        '    static final String LOGO_B64 = "' + B64 + '";\n'
        "\n"
        "    static final String WELCOME =\n"
        '        "<!doctype html><html><head><meta charset=\'utf-8\'><style>"\n'
        '        + "html,body{margin:0;height:100%;background:#FCFDFF;}"\n'
        '        + "body{display:flex;flex-direction:column;align-items:center;justify-content:center;"\n'
        '        + "font-family:\'Microsoft YaHei\',\'Segoe UI\',sans-serif;color:#122337;}"\n'
        '        + "img{width:96px;height:96px;margin:0 0 18px;}"\n'
        '        + "h1{font-size:22px;letter-spacing:5px;margin:0 0 34px;color:#1B64BF;font-weight:600;}"\n'
        '        + ".circles{display:flex;gap:16px;}"\n'
        '        + "a.c{width:76px;height:76px;border-radius:50%;background:#E8F5FC;border:1px solid #A4C1D7;"\n'
        '        + "display:flex;align-items:center;justify-content:center;text-decoration:none;color:#1B64BF;font-size:13px;}"\n'
        '        + "a.plus{font-size:28px;}"\n'
        '        + "</style></head><body>"\n'
        '        + "<img src=\'data:image/png;base64," + LOGO_B64 + "\'>"\n'
        '        + "<h1>堆域浏览器</h1>"\n'
        '        + "<div class=\'circles\'>"\n'
        '        + "<a class=\'c\' href=\'https://scp-wiki-cn.wikidot.com\'>SCP</a>"\n'
        '        + "<a class=\'c\' href=\'https://backrooms-wiki-cn.wikidot.com\'>后室</a>"\n'
        '        + "<a class=\'c plus\' href=\'yytt://addr\'>+</a>"\n'
        '        + "</div></body></html>";'
    )
    s = s[:i] + new + s[j:]

    old2 = "        HBox.setHgrow(tools, Priority.ALWAYS);"
    assert s.count(old2) == 1, "grow %d" % s.count(old2)
    new2 = (
        '        Button back = flat("←", 44);\n'
        "        back.setOnAction(e2 -> {\n"
        "            javafx.scene.web.WebHistory h = current == null ? null : current.view.getEngine().getHistory();\n"
        "            if (h != null && h.getCurrentIndex() > 0) {\n"
        "                h.go(-1);\n"
        "            }\n"
        "        });\n"
        '        Button fwd = flat("→", 44);\n'
        "        fwd.setOnAction(e2 -> {\n"
        "            javafx.scene.web.WebHistory h = current == null ? null : current.view.getEngine().getHistory();\n"
        "            if (h != null && h.getCurrentIndex() < h.getEntries().size() - 1) {\n"
        "                h.go(1);\n"
        "            }\n"
        "        });\n"
        "        HBox.setHgrow(tools, Priority.ALWAYS);"
    )
    s = s.replace(old2, new2, 1)

    old3 = "        row.getChildren().addAll(tools, tabBtn);"
    assert s.count(old3) == 1, "row %d" % s.count(old3)
    s = s.replace(old3, "        row.getChildren().addAll(back, fwd, tools, tabBtn);", 1)

    old4 = 'new Label("只存 Wikidot 账号，到登录页会自动填上")'
    assert s.count(old4) == 1, "label %d" % s.count(old4)
    s = s.replace(old4, 'new Label("请输入正确的 Wikidot 账号和密码")', 1)

    anchor = '        e.titleProperty().addListener((o, a, b) -> {'
    assert s.count(anchor) == 1, "anchor %d" % s.count(anchor)
    new5 = (
        "        e.locationProperty().addListener((o, a2, b2) -> {\n"
        '            if (b2 != null && b2.startsWith("yytt://")) {\n'
        "                javafx.application.Platform.runLater(() -> {\n"
        "                    welcome(t);\n"
        "                    beginEdit();\n"
        "                });\n"
        "            }\n"
        "        });\n"
    )
    s = s.replace(anchor, new5 + anchor, 1)

    io.open(P, "w", encoding="utf-8").write(s)
    io.open("patch_result.txt", "w", encoding="utf-8").write("OK size=%d\n" % len(s))


try:
    run()
except Exception:
    io.open("patch_error.txt", "w", encoding="utf-8").write(traceback.format_exc())
    sys.exit(1)
