import re
import sys
import traceback

P = 'src/com/mycompany/yytt/MainActivity.java'

try:
    s = open(P, encoding='utf-8').read()
    orig = s

    s = s.replace(
        'private static final String KEY_HOME = "home_url";',
        'private static final String KEY_HOME = "home_url";\n'
        '    private static final String KEY_TABS = "tabs";\n'
        '    private static final String KEY_TAB_INDEX = "tab_index";', 1)

    s = re.sub(
        r'String\[\] items = \{"Wikidot 登录", "Wikidot 页面选项",[\s\S]*?"Wikidot Cookie 一览"\};',
        'String[] items = {"Wikidot 登录", "Wikidot 论坛"};', s, count=1)

    s = re.sub(
        r'if \(which == 0\) \{\s*wdLogin\(\);[\s\S]*?wdCookies\(\);\s*\}',
        'if (which == 0) {\n'
        '                            wdLogin();\n'
        '                        } else {\n'
        '                            wdForum();\n'
        '                        }', s, count=1)

    start = s.index('    private void wdLogin() {')
    end = s.index('    private void openTabsPanel() {')

    newm = '''    private void wdLogin() {
        final WebView login = new WebView(this);
        WebSettings s1 = login.getSettings();
        s1.setJavaScriptEnabled(true);
        s1.setDomStorageEnabled(true);
        s1.setUserAgentString(desktopUa ? DESKTOP_UA : defaultUa);
        if (Build.VERSION.SDK_INT >= 21) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(login, true);
        }

        FrameLayout wrap = new FrameLayout(this);
        int h = (int) (getResources().getDisplayMetrics().heightPixels * 0.62f);
        wrap.addView(login, new FrameLayout.LayoutParams(MATCH, h));

        final AlertDialog dlg = new AlertDialog.Builder(this)
                .setTitle("登录 Wikidot")
                .setView(wrap)
                .setNegativeButton("关闭", null)
                .create();

        login.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView v, String url) {
                return false;
            }

            public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                return false;
            }

            public void onPageFinished(WebView v, String url) {
                if (url == null) {
                    return;
                }
                if (url.indexOf("wikidot.com") >= 0 && url.indexOf("/account/login") < 0) {
                    toast("已登录，所有 wikidot 站点通用");
                    try {
                        dlg.dismiss();
                    } catch (Exception e) {
                    }
                    WebView main = curView();
                    if (main != null) {
                        main.reload();
                    }
                }
            }
        });

        login.loadUrl("https://www.wikidot.com/account/login");
        dlg.show();
    }

    private void wdForum() {
        String u = curUrl();
        String lower = u == null ? "" : u.toLowerCase();
        if (lower.length() == 0 || lower.indexOf("wikidot.com") < 0) {
            toast("先在地址栏打开任意一个 wikidot 站点");
            return;
        }
        String host = "";
        try {
            host = Uri.parse(u).getHost();
        } catch (Exception e) {
            host = "";
        }
        if (host != null && host.toLowerCase().startsWith("www.wikidot.com")) {
            toast("请先打开某个具体站点，再进它的论坛");
            return;
        }
        String origin = originOf(u);
        if (origin.length() == 0) {
            toast("取不到站点地址");
            return;
        }
        loadInCurrent(origin + "/forum");
    }

'''
    s = s[:start] + newm + s[end:]

    pat = re.compile(r'    private Tab addNewTab\(String url\) \{[\s\S]*?return t;\n    \}\n')
    newadd = '''    private Tab createTab(String url) {
        WebView wv = new WebView(this);
        setupWebView(wv);
        Tab t = new Tab();
        t.view = wv;
        t.url = url == null ? "" : url;
        t.title = "新标签页";
        tabs.add(t);
        if (url != null && url.length() > 0) {
            wv.loadUrl(url);
        }
        return t;
    }

    private Tab addNewTab(String url) {
        Tab t = createTab(url);
        switchTab(t);
        return t;
    }

    private boolean restoreTabs() {
        String raw = sp.getString(KEY_TABS, "");
        if (raw.length() == 0) {
            return false;
        }
        try {
            JSONArray arr = new JSONArray(raw);
            if (arr.length() == 0) {
                return false;
            }
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                String u = o.optString("url", "");
                String ti = o.optString("title", "新标签页");
                Tab t = createTab(u);
                if (ti.length() > 0) {
                    t.title = ti;
                }
            }
            int idx = sp.getInt(KEY_TAB_INDEX, tabs.size() - 1);
            if (idx < 0 || idx >= tabs.size()) {
                idx = tabs.size() - 1;
            }
            switchTab(tabs.get(idx));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void saveTabs() {
        if (tabs.size() == 0) {
            return;
        }
        try {
            JSONArray arr = new JSONArray();
            for (int i = 0; i < tabs.size(); i++) {
                JSONObject o = new JSONObject();
                o.put("url", tabs.get(i).url);
                o.put("title", tabs.get(i).title);
                arr.put(o);
            }
            sp.edit()
                    .putString(KEY_TABS, arr.toString())
                    .putInt(KEY_TAB_INDEX, tabs.indexOf(current))
                    .apply();
        } catch (Exception e) {
        }
    }
'''
    s = pat.sub(newadd, s, count=1)

    s = s.replace('        addNewTab(homeUrl);\n    }',
                  '        if (!restoreTabs()) {\n'
                  '            addNewTab(homeUrl);\n'
                  '        }\n'
                  '    }', 1)

    s = s.replace('    public void onBackPressed() {',
                  '    protected void onPause() {\n'
                  '        saveTabs();\n'
                  '        super.onPause();\n'
                  '    }\n\n'
                  '    protected void onStop() {\n'
                  '        saveTabs();\n'
                  '        super.onStop();\n'
                  '    }\n\n'
                  '    public void onBackPressed() {', 1)

    if 'wdPageTools' in s:
        raise Exception('wdPageTools still present')
    if 'wdRecent' in s:
        raise Exception('wdRecent still present')
    if 'wdCookies' in s:
        raise Exception('wdCookies still present')
    if 'evalJs' in s:
        raise Exception('evalJs still present')
    if 'restoreTabs' not in s:
        raise Exception('restoreTabs missing')
    if 'wdForum' not in s:
        raise Exception('wdForum missing')
    if s == orig:
        raise Exception('nothing changed')

    open(P, 'w', encoding='utf-8').write(s)
    open('patch_result.txt', 'w', encoding='utf-8').write('OK size=%d' % len(s))
    print('OK', len(s))
except Exception:
    open('patch_error.txt', 'w', encoding='utf-8').write(traceback.format_exc())
    print(traceback.format_exc())
    sys.exit(1)
