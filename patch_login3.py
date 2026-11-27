import sys
import traceback

P = 'src/com/mycompany/yytt/MainActivity.java'

try:
    s = open(P, encoding='utf-8').read()

    if 'keepInside' in s:
        open('patch_result.txt', 'w', encoding='utf-8').write('ALREADY size=%d' % len(s))
        print('already applied', len(s))
        sys.exit(0)

    orig = s

    a = '''    private void wdLogin() {
        loginBaseline = CookieManager.getInstance().getCookie("https://www.wikidot.com");
'''
    b = '''    private void wdLogin() {
        if (hasLoginCookie()) {
            toast("当前已经是登录状态，不用再登一次");
            return;
        }
        loginBaseline = CookieManager.getInstance().getCookie("https://www.wikidot.com");
'''
    if a not in s:
        raise Exception('wdLogin anchor missing')
    s = s.replace(a, b, 1)

    c = '''        final WebView login = new WebView(this);
        WebSettings s1 = login.getSettings();
'''
    d = '''        final WebView login = new WebView(this);
        WebSettings s1 = login.getSettings();
        s1.setSupportMultipleWindows(false);
        s1.setJavaScriptCanOpenWindowsAutomatically(false);
'''
    if c not in s:
        raise Exception('login webview anchor missing')
    s = s.replace(c, d, 1)

    e = '''        login.setBackgroundColor(C_BG);
'''
    f = '''        login.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView v, String url) {
                return keepInside(v, url);
            }

            public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                return keepInside(v, r.getUrl().toString());
            }
        });
        login.setBackgroundColor(C_BG);
'''
    if e not in s:
        raise Exception('login bg anchor missing')
    s = s.replace(e, f, 1)

    g = '''                runScripts(v, url);
'''
    h = '''                if (url != null && url.toLowerCase().indexOf("/account/login") >= 0
                        && hasLoginCookie()) {
                    toast("已经登录着，这个登录页自动跳过");
                    if (v.canGoBack()) {
                        v.goBack();
                        return;
                    }
                }
                runScripts(v, url);
'''
    if s.count(g) != 1:
        raise Exception('runScripts anchor count %d' % s.count(g))
    s = s.replace(g, h, 1)

    anchor = '    private void pollLogin(final Dialog dlg) {'
    if anchor not in s:
        raise Exception('pollLogin anchor missing')

    s = s.replace(anchor, '''    private boolean keepInside(WebView wv, String url) {
        if (url == null || url.length() == 0) {
            return true;
        }
        String lower = url.toLowerCase();
        if (lower.startsWith("http://") || lower.startsWith("https://")
                || lower.startsWith("about:") || lower.startsWith("data:")) {
            return false;
        }
        if (lower.startsWith("javascript:")) {
            return true;
        }
        if (lower.startsWith("intent:")) {
            try {
                Intent it = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                String fallback = it.getStringExtra("browser_fallback_url");
                if (fallback != null && fallback.length() > 0) {
                    wv.loadUrl(fallback);
                    return true;
                }
                String data = it.getDataString();
                if (data != null) {
                    String dl = data.toLowerCase();
                    if (dl.startsWith("http://") || dl.startsWith("https://")) {
                        wv.loadUrl(data);
                        return true;
                    }
                }
            } catch (Exception e) {
            }
            return true;
        }
        return true;
    }

    private boolean hasLoginCookie() {
        String c = null;
        try {
            c = CookieManager.getInstance().getCookie("https://www.wikidot.com");
        } catch (Exception e) {
            return false;
        }
        if (c == null || c.length() == 0) {
            return false;
        }
        String t = cookieVal(c, "wikidot_token7");
        String u = cookieVal(c, "wikidot_session");
        if (t != null && t.length() > 0) {
            return true;
        }
        if (u != null && u.length() > 0) {
            return true;
        }
        return false;
    }

''' + anchor, 1)

    if s.count('keepInside') != 3:
        raise Exception('keepInside count %d' % s.count('keepInside'))
    if s.count('hasLoginCookie') != 3:
        raise Exception('hasLoginCookie count %d' % s.count('hasLoginCookie'))
    if s == orig:
        raise Exception('nothing changed')

    open(P, 'w', encoding='utf-8').write(s)
    open('patch_result.txt', 'w', encoding='utf-8').write('OK size=%d' % len(s))
    print('OK', len(s))
except Exception:
    open('patch_error.txt', 'w', encoding='utf-8').write(traceback.format_exc())
    print(traceback.format_exc())
    sys.exit(1)
