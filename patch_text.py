# -*- coding: utf-8 -*-
import io
import sys
import traceback

P = "src/com/mycompany/yytt/MainActivity.java"


def run():
    s = io.open(P, encoding="utf-8").read()
    if "请登录 Wikidot 账号" in s:
        io.open("patch_result.txt", "w", encoding="utf-8").write("ALREADY size=%d\n" % len(s))
        return

    old_who = '        who.setText("只能用 Wikidot 账号，QQ、Google 等其他账号不行。");'
    assert s.count(old_who) == 1, "who %d" % s.count(old_who)
    s = s.replace(old_who, '        who.setText("请登录 Wikidot 账号");', 1)

    i = s.find('        TextView note = new TextView(this);\n        note.setText("账号密码只存在本机，不上传。");')
    assert i > 0, "note start %d" % i
    end = "        body.addView(note);\n"
    j = s.find(end, i)
    assert j > i, "note end %d" % j
    j = j + len(end)
    if s[j:j + 1] == "\n":
        j = j + 1
    s = s[:i] + s[j:]

    io.open(P, "w", encoding="utf-8").write(s)
    io.open("patch_result.txt", "w", encoding="utf-8").write("OK size=%d\n" % len(s))


try:
    run()
except Exception:
    io.open("patch_error.txt", "w", encoding="utf-8").write(traceback.format_exc())
    sys.exit(1)
