# -*- coding: utf-8 -*-
import io
import sys
import traceback

P = "src/com/mycompany/yytt/MainActivity.java"

B64 = "iVBORw0KGgoAAAANSUhEUgAAAGAAAABgBAMAAAAQtmoLAAAABGdBTUEAALGPC/xhBQAAACBjSFJNAAB6JgAAgIQAAPoAAACA6AAAdTAAAOpgAAA6mAAAF3CculE8AAAAG1BMVEUAAAAAESIAM2YRVbsRRIgRVZkAIkQRM3cAETNA2M+NAAAAAWJLR0QAiAUdSAAAAAd0SU1FB+oLHAUZOKPdmB8AAAJPSURBVFjD7ZVNb9pAEIZ3LWN6XAN2rkahqFcYu1yxV6AcU5w0VxsRpUekNve0qvq7O/uBv7Jg55BL5PeEvfPsOzPrHQjp1atXr17vIsreGGWF0/b4EQ9KAGCX15ev4vqz8wxQAyDaVvMapVHVlB5SaAIA4ZcCcXA9yor1/UYGNAGAROf1icsNNOH/0suvAYgSYUKv1dOKFdmcAwDuMfXH08OOYW/KtWNZ06Z8Oy3jAb5X93qoduXPaaOYTKCiG+LpX5WWKJMnmepXbKio5QlgK3/EyrDedFlG7n/GhVtHhk0xk2AkIwNbFJP7QQMYQOL6G1iP1bYCIH8FsbAhnLqz9PYVANHcPRw9WaoCZDYLa+vu0dYAANzlbCybqQGCWS6oL3toBDBqDCEjBUA5OqiVbw3AVq+xhhUpAXItagCTg1cASzI/AYcLwKDikMbic83IJGIlsG4ARB3c2oMl5fhlTwQUFQ7RvHlweAdmhYPazobCIflNDKL/NqoGDkcnc7IhaIe7H8wUT+zE3WNbVxQdhiE/4hELwJq7s7UZEJnqlESXhioluudwDtBt1YCtUhqaWlQCXulg4aHromMjMNAOWMOxkpJ92UHUwIsaSDuAKVFeK9o2fBdKFi+K1imF2uE+MAJ41qkxpeSFnJMfeGVbdUpOTi7J4RKgP0MmTzp8IS1yHrCtO+ZkeD8j9py1xRPiYg2wlRMgYm57PErcjJuDmHadwvFmiiuTqnnTTY+nUdxVVExNWLLOgJhHaj51lsMrf3Gd5L8xvlevXr0+sP4D8Miuq6RN728AAAAldEVYdGRhdGU6Y3JlYXRlADIwMjYtMTEtMjhUMDU6MjU6NTUrMDA6MDDSdWixAAAAJXRFWHRkYXRlOm1vZGlmeQAyMDI2LTExLTI4VDA1OjI1OjU1KzAwOjAwoyjQDQAAAABJRU5ErkJggg=="


def run():
    s = io.open(P, encoding="utf-8").read()
    if "data:image/png;base64" in s:
        io.open("patch_result.txt", "w", encoding="utf-8").write("ALREADY size=%d\n" % len(s))
        return

    start = '        String html = "<!doctype html><html><head><title>堆域浏览器</title>"'
    end = '        wv.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);\n'
    i = s.find(start)
    j = s.find(end)
    assert i > 0 and j > i, "slice %d %d" % (i, j)
    j = j + len(end)

    new = (
        '        String b64 = "' + B64 + '";\n'
        '        String html = "<!doctype html><html><head><title>堆域浏览器</title>"\n'
        "                + \"<meta name='viewport' content='width=device-width,initial-scale=1'>\"\n"
        '                + "<style>body{margin:0;background:#FCFDFF;color:#122337;"\n'
        '                + "font-family:sans-serif;display:flex;flex-direction:column;"\n'
        '                + "align-items:center;justify-content:center;height:100vh}"\n'
        '                + "img{width:96px;height:96px;margin:0 0 18px}"\n'
        '                + "h1{font-size:21px;margin:0 0 34px;color:#1B64BF;letter-spacing:5px;font-weight:600}"\n'
        '                + ".circles{display:flex;gap:16px}"\n'
        '                + "a.c{width:74px;height:74px;border-radius:50%;background:#E8F5FC;"\n'
        '                + "border:1px solid #A4C1D7;display:flex;align-items:center;justify-content:center;"\n'
        '                + "text-decoration:none;color:#1B64BF;font-size:13px}"\n'
        '                + "a.plus{font-size:28px}"\n'
        '                + "</style></head><body>"\n'
        '                + "<img src=\'data:image/png;base64," + b64 + "\'>"\n'
        '                + "<h1>堆域浏览器</h1>"\n'
        '                + "<div class=\'circles\'>"\n'
        '                + "<a class=\'c\' href=\'https://scp-wiki-cn.wikidot.com\'>SCP</a>"\n'
        '                + "<a class=\'c\' href=\'https://backrooms-wiki-cn.wikidot.com\'>后室</a>"\n'
        '                + "<a class=\'c plus\' href=\'yytt://addr\'>+</a>"\n'
        '                + "</div></body></html>";\n'
        '        wv.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);\n'
    )
    s = s[:i] + new + s[j:]

    old_who = '        who.setText("只支持 Wikidot 账号。登录后所有 wikidot 站点自动通行，登录页也会自动填好。");'
    assert s.count(old_who) == 1, "who %d" % s.count(old_who)
    s = s.replace(old_who, '        who.setText("请输入正确的 Wikidot 账号和密码。");', 1)

    old_catch = '        } catch (Exception e) {\n            toast("打不开这个链接：" + url);\n        }\n        return true;'
    assert s.count(old_catch) == 1, "catch %d" % s.count(old_catch)
    s = s.replace(old_catch, '        } catch (Exception e) {\n        }\n        return true;', 1)

    io.open(P, "w", encoding="utf-8").write(s)
    io.open("patch_result.txt", "w", encoding="utf-8").write("OK size=%d\n" % len(s))


try:
    run()
except Exception:
    io.open("patch_error.txt", "w", encoding="utf-8").write(traceback.format_exc())
    sys.exit(1)
