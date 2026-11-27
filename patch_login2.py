import re
import sys
import traceback

P = 'src/com/mycompany/yytt/MainActivity.java'

try:
    s = open(P, encoding='utf-8').read()
    orig = s

    s = s.replace('    private boolean loginDone = false;',
                  '    private boolean loginDone = false;\n'
                  '    private String loginBaseline = null;', 1)

    s = re.sub(r'    private static final String JS_LOGIN_CHECK = "[^"]*";\n\n', '', s, count=1)

    start = s.index('    private void wdLogin() {')
    end = s.index('    private void wdForum() {')

    newm = '''    private void wdLogin() {
        loginBaseline = CookieManager.getInstance().getCookie("https://www.wikidot.com");

        final Dialog dlg = new Dialog(this);
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackgroundDrawable(bg(C_BG, 16, 1, C_LINE));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(16), 0, dp(6), 0);

        TextView title = new TextView(this);
        title.setText("登录 Wikidot");
        title.setTextSize(14);
        title.setTextColor(C_TXT);
        title.setSingleLine(true);
        header.addView(title, new LinearLayout.LayoutParams(0, dp(44), 1));

        TextView closeBtn = textBtn("×", 20, C_TXT2);
        header.addView(closeBtn, new LinearLayout.LayoutParams(dp(44), dp(44)));
        box.addView(header, new LinearLayout.LayoutParams(MATCH, dp(44)));

        View line = new View(this);
        line.setBackgroundColor(C_LINE);
        box.addView(line, new LinearLayout.LayoutParams(MATCH, dp(1)));

        final WebView login = new WebView(this);
        WebSettings s1 = login.getSettings();
        s1.setJavaScriptEnabled(true);
        s1.setDomStorageEnabled(true);
        s1.setUserAgentString(desktopUa ? DESKTOP_UA : defaultUa);
        if (Build.VERSION.SDK_INT >= 21) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(login, true);
        }
        login.setBackgroundColor(C_BG);
        box.addView(login, new LinearLayout.LayoutParams(MATCH, 0, 1));

        TextView tip = new TextView(this);
        tip.setText("登录状态一旦落到 cookie 上，这里会自动关掉");
        tip.setTextSize(11);
        tip.setTextColor(C_TXT2);
        tip.setGravity(Gravity.CENTER);
        tip.setPadding(0, dp(8), 0, dp(8));
        box.addView(tip, new LinearLayout.LayoutParams(MATCH, WRAP));

        dlg.setContentView(box);

        Window w = dlg.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(0));
            WindowManager.LayoutParams lp = w.getAttributes();
            lp.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.94f);
            lp.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.72f);
            w.setAttributes(lp);
        }

        closeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dlg.dismiss();
            }
        });

        dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface d) {
                stopLoginPoll();
                try {
                    login.loadUrl("about:blank");
                    login.destroy();
                } catch (Exception e) {
                }
            }
        });

        login.loadUrl("https://www.wikidot.com/account/login");
        loginDone = false;
        dlg.show();
        pollLogin(dlg);
    }

    private void pollLogin(final Dialog dlg) {
        stopLoginPoll();
        final Handler h = new Handler();
        loginHandler = h;
        final Runnable[] task = new Runnable[1];
        task[0] = new Runnable() {
            public void run() {
                if (loginDone || dlg == null || !dlg.isShowing()) {
                    return;
                }
                String now = null;
                try {
                    now = CookieManager.getInstance().getCookie("https://www.wikidot.com");
                } catch (Exception e) {
                    now = null;
                }
                if (loginCookieChanged(loginBaseline, now)) {
                    loginSuccess(dlg);
                    return;
                }
                h.postDelayed(task[0], 400);
            }
        };
        h.postDelayed(task[0], 400);
    }

    private String cookieVal(String cookie, String key) {
        if (cookie == null || cookie.length() == 0) {
            return null;
        }
        String[] parts = cookie.split(";");
        for (int i = 0; i < parts.length; i++) {
            String p = parts[i].trim();
            int eq = p.indexOf('=');
            if (eq <= 0) {
                continue;
            }
            String name = p.substring(0, eq).trim();
            if (name.equalsIgnoreCase(key)) {
                return p.substring(eq + 1).trim();
            }
        }
        return null;
    }

    private boolean loginCookieChanged(String before, String now) {
        String b = cookieVal(before, "wikidot_token7");
        String n = cookieVal(now, "wikidot_token7");
        if (n != null && n.length() > 0 && !n.equals(b)) {
            return true;
        }
        String b2 = cookieVal(before, "wikidot_session");
        String n2 = cookieVal(now, "wikidot_session");
        if (n2 != null && n2.length() > 0 && !n2.equals(b2)) {
            return true;
        }
        return false;
    }

    private void loginSuccess(Dialog dlg) {
        loginDone = true;
        stopLoginPoll();
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

    private void stopLoginPoll() {
        if (loginHandler != null) {
            loginHandler.removeCallbacksAndMessages(null);
            loginHandler = null;
        }
    }

'''
    s = s[:start] + newm + s[end:]

    if 'hasWikidotSession' in s:
        raise Exception('old cookie-free check still present')
    if 'JS_LOGIN_CHECK' in s:
        raise Exception('JS check still present')
    if 'loginCookieChanged' not in s:
        raise Exception('loginCookieChanged missing')
    if 'loginBaseline' not in s:
        raise Exception('loginBaseline missing')
    if s == orig:
        raise Exception('nothing changed')

    open(P, 'w', encoding='utf-8').write(s)
    open('patch_result.txt', 'w', encoding='utf-8').write('OK size=%d' % len(s))
    print('OK', len(s))
except Exception:
    open('patch_error.txt', 'w', encoding='utf-8').write(traceback.format_exc())
    print(traceback.format_exc())
    sys.exit(1)
