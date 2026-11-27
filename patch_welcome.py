import sys
import traceback

P = 'src/com/mycompany/yytt/MainActivity.java'

try:
    s = open(P, encoding='utf-8').read()

    if 'yytt://addr' in s:
        open('patch_result.txt', 'w', encoding='utf-8').write('ALREADY size=%d' % len(s))
        print('already applied', len(s))
        sys.exit(0)

    orig = s

    a1 = '    private String homeUrl = "https://www.wikidot.com";\n'
    if a1 not in s:
        raise Exception('home field missing')
    s = s.replace(a1, '    private String homeUrl = "";\n', 1)

    a2 = '        homeUrl = sp.getString(KEY_HOME, "https://www.wikidot.com");\n'
    if a2 not in s:
        raise Exception('home pref missing')
    s = s.replace(a2, '        homeUrl = sp.getString(KEY_HOME, "");\n', 1)

    a3 = '''        tabs.add(t);
        if (url != null && url.length() > 0) {
            wv.loadUrl(url);
        }
'''
    if a3 not in s:
        raise Exception('createTab anchor missing')
    s = s.replace(a3, '''        tabs.add(t);
        if (url != null && url.length() > 0 && !"about:blank".equals(url)) {
            wv.loadUrl(url);
        } else {
            loadWelcome(wv);
        }
''', 1)

    a4 = '    private Tab createTab(String url) {\n'
    if a4 not in s:
        raise Exception('createTab decl missing')

    welcome = '''    private void loadWelcome(WebView wv) {
        if (wv == null) {
            return;
        }
        String html = "<!doctype html><html><head><title>堆域浏览器</title>"
                + "<meta name='viewport' content='width=device-width,initial-scale=1'>"
                + "<style>body{margin:0;background:#FCFDFF;color:#122337;"
                + "font-family:sans-serif;display:flex;align-items:center;"
                + "justify-content:center;height:100vh}"
                + ".box{text-align:center;padding:0 28px}"
                + "h1{font-size:22px;margin:0 0 12px;color:#1B64BF}"
                + "p{font-size:13px;color:#3A526C;line-height:1.9;margin:0 0 24px}"
                + "a{display:inline-block;padding:12px 28px;border-radius:20px;"
                + "background:#4691DC;color:#fff;text-decoration:none;font-size:14px}"
                + "</style></head><body><div class='box'><h1>堆域浏览器</h1>"
                + "<p>为 Backrooms 与 SCP 的 Wikidot 站点准备<br>点下面的按钮，输入一个站点地址</p>"
                + "<a href='yytt://addr'>输入地址</a></div></body></html>";
        wv.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
    }

'''
    s = s.replace(a4, welcome + a4, 1)

    a5 = '''        if (lower.startsWith("javascript:")) {
            return false;
        }
'''
    if a5 not in s:
        raise Exception('handleUrl anchor missing')
    s = s.replace(a5, '''        if (lower.startsWith("javascript:")) {
            return false;
        }
        if (lower.startsWith("yytt://")) {
            beginEdit();
            return true;
        }
''', 1)

    if 'yytt://addr' not in s:
        raise Exception('welcome link missing')
    if s.count('loadWelcome') != 2:
        raise Exception('loadWelcome count %d' % s.count('loadWelcome'))
    if s == orig:
        raise Exception('nothing changed')

    open(P, 'w', encoding='utf-8').write(s)
    open('patch_result.txt', 'w', encoding='utf-8').write('OK size=%d' % len(s))
    print('OK', len(s))
except Exception:
    open('patch_error.txt', 'w', encoding='utf-8').write(traceback.format_exc())
    print(traceback.format_exc())
    sys.exit(1)
