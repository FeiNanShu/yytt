import sys
import traceback

P = 'src/com/mycompany/yytt/MainActivity.java'

try:
    s = open(P, encoding='utf-8').read()

    if 'https://www.wikidot.com"' in s and '堆域浏览器' in s:
        open('patch_result.txt', 'w', encoding='utf-8').write('ALREADY size=%d' % len(s))
        print('already applied', len(s))
        sys.exit(0)

    orig = s

    a1 = '    private String homeUrl = "about:blank";\n'
    if a1 not in s:
        raise Exception('home field missing')
    s = s.replace(a1, '    private String homeUrl = "https://www.wikidot.com";\n', 1)

    a2 = '        homeUrl = sp.getString(KEY_HOME, "about:blank");\n'
    if a2 not in s:
        raise Exception('home pref missing')
    s = s.replace(a2, '        homeUrl = sp.getString(KEY_HOME, "https://www.wikidot.com");\n', 1)

    blk = '''            public void onReceivedTitle(WebView v, String title) {
                Tab t = findTab(v);
                if (t != null && title != null && title.trim().length() > 0) {
                    t.title = title.trim();
                }
                if (v == curView() && !editing) {
                    updateAddress();
                }
            }

'''
    if blk not in s:
        raise Exception('title block not matched')
    s = s.replace(blk, '', 1)

    a3 = '''            public void onProgressChanged(WebView v, int p) {
                if (v == curView()) {
                    progress.setProgress(p);
                    if (p >= 100) {
                        progress.setVisibility(View.GONE);
                    }
                }
            }
'''
    if a3 not in s:
        raise Exception('chrome anchor missing')
    s = s.replace(a3, a3 + '''
            public void onReceivedTitle(WebView v, String title) {
                Tab t = findTab(v);
                if (t != null && title != null && title.trim().length() > 0) {
                    t.title = title.trim();
                }
                if (v == curView() && !editing) {
                    updateAddress();
                }
            }
''', 1)

    a4 = '''                    if (t.title == null || t.title.trim().length() == 0) {
                        t.title = prettyUrl(url);
                    }
'''
    if a4 not in s:
        raise Exception('pageFinished title anchor missing')
    s = s.replace(a4, '''                    if (t.title == null || t.title.trim().length() == 0
                            || t.title.equals("新标签页")) {
                        t.title = prettyUrl(url);
                    }
''', 1)

    a5 = 'yytt 脚本浏览器'
    if a5 not in s:
        raise Exception('old app name in about missing')
    s = s.replace(a5, '堆域浏览器', 1)

    if s.count('onReceivedTitle') != 1:
        raise Exception('onReceivedTitle count %d' % s.count('onReceivedTitle'))
    if s.count('https://www.wikidot.com') < 2:
        raise Exception('home url missing')
    if '堆域浏览器' not in s:
        raise Exception('new name missing')
    if s == orig:
        raise Exception('nothing changed')

    open(P, 'w', encoding='utf-8').write(s)
    open('patch_result.txt', 'w', encoding='utf-8').write('OK size=%d' % len(s))
    print('OK', len(s))
except Exception:
    open('patch_error.txt', 'w', encoding='utf-8').write(traceback.format_exc())
    print(traceback.format_exc())
    sys.exit(1)
