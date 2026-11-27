import sys
import traceback

P = 'src/com/mycompany/yytt/MainActivity.java'


def swap(s, a_marker, b_marker, new):
    a = s.index(a_marker)
    b = s.index(b_marker, a)
    return s[:a] + new + s[b:]


try:
    s = open(P, encoding='utf-8').read()
    orig = s

    old_top = '''        goBtn = textBtn("→", 20, C_ACCENT_D);
        top.addView(goBtn, new LinearLayout.LayoutParams(dp(48), dp(48)));
'''
    if old_top not in s:
        raise Exception('top bar anchor missing')

    s = s.replace(old_top, '''        reloadBtn = textBtn("↻", 18, C_TXT2);
        top.addView(reloadBtn, new LinearLayout.LayoutParams(dp(44), dp(48)));

        goBtn = textBtn("→", 20, C_ACCENT_D);
        top.addView(goBtn, new LinearLayout.LayoutParams(dp(48), dp(48)));
''', 1)

    if '    private TextView goBtn;' not in s:
        raise Exception('field anchor missing')

    s = s.replace('    private TextView goBtn;',
                  '    private TextView reloadBtn;\n    private TextView goBtn;', 1)

    go_anchor = '        goBtn.setOnClickListener(new View.OnClickListener() {'
    s = s.replace(go_anchor, '''        reloadBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (editing) {
                    endEdit();
                }
                WebView wv = curView();
                if (wv != null) {
                    wv.reload();
                }
            }
        });

''' + go_anchor, 1)

    s = s.replace('''        if (lower.startsWith("http://") || lower.startsWith("https://")
                || lower.startsWith("about:") || lower.startsWith("data:")
                || lower.startsWith("file:")) {
            return false;
        }''', '''        if (lower.startsWith("http://") || lower.startsWith("https://")
                || lower.startsWith("view-source:")
                || lower.startsWith("about:") || lower.startsWith("data:")
                || lower.startsWith("file:")) {
            return false;
        }''', 1)

    s = swap(s, '    private void showSource() {', '    private void openGearMenu() {', '''    private void showSource() {
        String u = curUrl();
        if (u == null || u.length() == 0) {
            toast("当前没有打开页面");
            return;
        }
        String lower = u.toLowerCase();
        if (lower.startsWith("view-source:")) {
            toast("已经在这个页面的源码视图里");
            return;
        }
        if (!(lower.startsWith("http://") || lower.startsWith("https://"))) {
            toast("这个地址没法看源码");
            return;
        }
        loadInCurrent("view-source:" + u);
    }

''')

    s = swap(s, '    private void openGearMenu() {', '    private void toggleDesktopUa() {', '''    private void openGearMenu() {
        String[] items = {"脚本管理", "Wikidot 登录", "Wikidot 论坛",
                "设置默认首页", "清除 Cookie 与缓存", "关于"};
        new AlertDialog.Builder(this)
                .setTitle("浏览器设置")
                .setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int which) {
                        if (which == 0) {
                            openManager();
                        } else if (which == 1) {
                            wdLogin();
                        } else if (which == 2) {
                            wdForum();
                        } else if (which == 3) {
                            setHomePage();
                        } else if (which == 4) {
                            clearAll();
                        } else {
                            about();
                        }
                    }
                })
                .show();
    }

''')

    s = swap(s, '    private void openToolbox() {', '    private boolean isWikidotSite() {', '''    private void openToolbox() {
        String uaText = desktopUa ? "桌面版 UA：开" : "桌面版 UA：关";
        String[] items = {uaText, "查看页面源码"};
        new AlertDialog.Builder(this)
                .setTitle("工具箱")
                .setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int which) {
                        if (which == 0) {
                            toggleDesktopUa();
                        } else {
                            showSource();
                        }
                    }
                })
                .show();
    }

''')

    old_about = '\u9876\u90e8\u4e00\u6761\u7ec6\u680f\uff1a\u5de6\u8fb9\u8bbe\u7f6e\uff0c\u4e2d\u95f4\u5730\u5740\u80f6\u56ca\uff08\u70b9\u4e00\u4e0b\u624d\u8fdb\u5165\u7f16\u8f91\uff09\uff0c\u53f3\u8fb9\u524d\u5f80\u3002\\n\u5e95\u90e8\u4e00\u6761\u7ec6\u680f\uff1a\u5de5\u5177\u7bb1 / \u6807\u7b7e\u9875\u3002'
    new_about = '\u9876\u90e8\u4e00\u6761\u7ec6\u680f\uff1a\u5de6\u8fb9\u8bbe\u7f6e\uff08\u7ba1\u6574\u4e2a\u6d4f\u89c8\u5668\uff09\uff0c\u4e2d\u95f4\u5730\u5740\u80f6\u56ca\uff08\u70b9\u4e00\u4e0b\u624d\u8fdb\u5165\u7f16\u8f91\uff09\uff0c\u53f3\u8fb9\u5237\u65b0\u4e0e\u524d\u5f80\u3002\\n\u5e95\u90e8\u4e00\u6761\u7ec6\u680f\uff1a\u5de5\u5177\u7bb1\uff08\u7ba1\u5f53\u524d\u9875\u9762\uff09/ \u6807\u7b7e\u9875\u3002'
    s = s.replace(old_about, new_about, 1)

    if 'reloadBtn' not in s:
        raise Exception('reloadBtn missing')
    if s.count('\u67e5\u770b\u9875\u9762\u6e90\u7801') != 1:
        raise Exception('source menu item duplicated or missing')
    if '\u5237\u65b0\u5f53\u524d\u9875' in s:
        raise Exception('old refresh item still present')
    if '"\u6d4f\u89c8\u5668\u8bbe\u7f6e"' not in s:
        raise Exception('gear title missing')
    if 'loadInCurrent("view-source:" + u)' not in s:
        raise Exception('view-source jump missing')
    if s.count('lower.startsWith("view-source:")') != 2:
        raise Exception('view-source guard count wrong')
    if s == orig:
        raise Exception('nothing changed')

    open(P, 'w', encoding='utf-8').write(s)
    open('patch_result.txt', 'w', encoding='utf-8').write('OK size=%d' % len(s))
    print('OK', len(s))
except Exception:
    open('patch_error.txt', 'w', encoding='utf-8').write(traceback.format_exc())
    print(traceback.format_exc())
    sys.exit(1)
