import sys
import traceback

P = 'src/com/mycompany/yytt/MainActivity.java'
JS = "(function(){var as=document.getElementsByTagName('a');for(var i=0;i<as.length;i++){var h=as[i].href||'';if(h.indexOf('account/logout')>=0)return 'yes';}var t=document.getElementById('account-topbutton');if(t&&(t.textContent||'').indexOf('|')>=0)return 'yes';return 'no';})()"

try:
    s = open(P, encoding='utf-8').read()
    orig = s

    s = s.replace('import android.app.DownloadManager;',
                  'import android.app.DownloadManager;\nimport android.app.Dialog;', 1)
    s = s.replace('import android.graphics.PorterDuff;',
                  'import android.graphics.PorterDuff;\nimport android.graphics.drawable.ColorDrawable;', 1)
    s = s.replace('import android.os.Environment;',
                  'import android.os.Environment;\nimport android.os.Handler;', 1)
    s = s.replace('import android.view.Gravity;',
                  'import android.view.Gravity;\nimport android.view.Window;\nimport android.view.WindowManager;', 1)

    s = s.replace('    private AlertDialog managerDialog = null;',
                  '    private AlertDialog managerDialog = null;\n'
                  '    private Handler loginHandler = null;\n'
                  '    private boolean loginDone = false;', 1)

    s = s.replace('    private static final int REQ_PICK_FILE = 1001;',
                  '    private static final String JS_LOGIN_CHECK = "' + JS + '";\n\n'
                  '    private static final int REQ_PICK_FILE = 1001;', 1)

    start = s.index('    private void wdLogin() {')
    end = s.index('    private void wdForum() {')

    newm = '''    private void wdLogin() {
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
        tip.setText("登录成功后会自动检测并关掉这个窗口");
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
        pollLogin(dlg, login);
    }

    private void pollLogin(final Dialog dlg, final WebView login) {
        stopLoginPoll();
        final Handler h = new Handler();
        loginHandler = h;
        final Runnable[] task = new Runnable[1];
        task[0] = new Runnable() {
            public void run() {
                if (loginDone || dlg == null || !dlg.isShowing()) {
                    return;
                }
                if (hasWikidotSession()) {
                    loginSuccess(dlg);
                    return;
                }
                if (Build.VERSION.SDK_INT < 19) {
                    h.postDelayed(task[0], 1500);
                    return;
                }
                login.evaluateJavascript(JS_LOGIN_CHECK, new ValueCallback<String>() {
                    public void onReceiveValue(String value) {
                        if (loginDone || !dlg.isShowing()) {
                            return;
                        }
                        String txt = jsonToText(value);
                        if (txt.indexOf("yes") >= 0) {
                            loginSuccess(dlg);
                            return;
                        }
                        h.postDelayed(task[0], 1500);
                    }
                });
            }
        };
        h.postDelayed(task[0], 1500);
    }

    private boolean hasWikidotSession() {
        try {
            String c = CookieManager.getInstance().getCookie("https://www.wikidot.com");
            if (c == null || c.length() == 0) {
                return false;
            }
            String lower = c.toLowerCase();
            return lower.indexOf("wikidot_token7") >= 0 || lower.indexOf("wikidot_session") >= 0;
        } catch (Exception e) {
            return false;
        }
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

    if 'pollLogin' not in s:
        raise Exception('pollLogin missing')
    if 'hasWikidotSession' not in s:
        raise Exception('hasWikidotSession missing')
    if 'indexOf("/account/login") < 0' in s:
        raise Exception('old url-based check still present')
    if s == orig:
        raise Exception('nothing changed')

    open(P, 'w', encoding='utf-8').write(s)
    open('patch_result.txt', 'w', encoding='utf-8').write('OK size=%d' % len(s))
    print('OK', len(s))
except Exception:
    open('patch_error.txt', 'w', encoding='utf-8').write(traceback.format_exc())
    print(traceback.format_exc())
    sys.exit(1)
