import sys
import traceback

P = 'src/com/mycompany/yytt/MainActivity.java'

try:
    s = open(P, encoding='utf-8').read()

    if 'KEY_CRED' in s:
        open('patch_result.txt', 'w', encoding='utf-8').write('ALREADY size=%d' % len(s))
        print('already applied', len(s))
        sys.exit(0)

    orig = s

    a = '    private static final String KEY_TAB_INDEX = "tab_index";\n'
    if a not in s:
        raise Exception('const anchor missing')
    s = s.replace(a, a + '    private static final String KEY_CRED = "creds";\n', 1)

    b = '''        String[] items = {"脚本管理", "Wikidot 登录", "Wikidot 论坛",
                "设置默认首页", "清除 Cookie 与缓存", "关于"};
'''
    if b not in s:
        raise Exception('menu items anchor missing')
    s = s.replace(b, '''        String[] items = {"脚本管理", "Wikidot 登录", "Wikidot 论坛", "账号与密码",
                "设置默认首页", "清除 Cookie 与缓存", "关于"};
''', 1)

    c = '''                        } else if (which == 3) {
                            setHomePage();
                        } else if (which == 4) {
                            clearAll();
'''
    if c not in s:
        raise Exception('menu branch anchor missing')
    s = s.replace(c, '''                        } else if (which == 3) {
                            credPanel();
                        } else if (which == 4) {
                            setHomePage();
                        } else if (which == 5) {
                            clearAll();
''', 1)

    d = '                runScripts(v, url);\n'
    if s.count(d) != 1:
        raise Exception('runScripts anchor count %d' % s.count(d))
    s = s.replace(d, '                fillLoginForm(v, url);\n' + d, 1)

    anchor = '    private void about() {'
    if anchor not in s:
        raise Exception('about anchor missing')

    cred = '''    private String hostOf(String url) {
        try {
            String h = Uri.parse(url).getHost();
            if (h == null) {
                return "";
            }
            h = h.toLowerCase();
            if (h.startsWith("www.")) {
                h = h.substring(4);
            }
            return h;
        } catch (Exception e) {
            return "";
        }
    }

    private String[] loadCred(String host) {
        if (host == null || host.length() == 0) {
            return null;
        }
        try {
            JSONObject all = new JSONObject(sp.getString(KEY_CRED, "{}"));
            JSONObject o = all.optJSONObject(host);
            if (o == null) {
                return null;
            }
            String u = o.optString("u", "");
            String pw = o.optString("p", "");
            if (pw.length() == 0) {
                return null;
            }
            return new String[]{u, pw};
        } catch (Exception e) {
            return null;
        }
    }

    private void saveCred(String host, String user, String pass) {
        if (host == null || host.length() == 0) {
            return;
        }
        try {
            JSONObject all = new JSONObject(sp.getString(KEY_CRED, "{}"));
            if (pass == null || pass.length() == 0) {
                all.remove(host);
            } else {
                JSONObject o = new JSONObject();
                o.put("u", user == null ? "" : user);
                o.put("p", pass);
                all.put(host, o);
            }
            sp.edit().putString(KEY_CRED, all.toString()).apply();
            CookieManager.getInstance().flush();
        } catch (Exception e) {
        }
    }

    private void fillLoginForm(WebView wv, String url) {
        if (wv == null || url == null) {
            return;
        }
        String lower = url.toLowerCase();
        if (lower.indexOf("login") < 0 && lower.indexOf("signin") < 0
                && lower.indexOf("account") < 0) {
            return;
        }
        String host = hostOf(url);
        String[] cr = loadCred(host);
        if (cr == null) {
            return;
        }
        String js = "(function(){var u=" + jsString(cr[0]) + ",p=" + jsString(cr[1])
                + ";function q(a){return document.querySelector(a);}"
                + "var pw=q('input[type=\\"password\\"]');if(!pw){return;}"
                + "var un=q('input[name=\\"login\\"]')||q('input[name=\\"username\\"]')"
                + "||q('input[name=\\"user\\"]')||q('input[type=\\"text\\"]')"
                + "||q('input[type=\\"email\\"]');"
                + "if(un&&un.value.length==0){un.value=u;un.dispatchEvent(new Event('input',{bubbles:true}));}"
                + "if(pw.value.length==0){pw.value=p;pw.dispatchEvent(new Event('input',{bubbles:true}));}"
                + "})()";
        wv.evaluateJavascript(js, null);
    }

    private void credPanel() {
        final String host = hostOf(curUrl());
        if (host.length() == 0) {
            toast("先打开要保存密码的站点");
            return;
        }
        String[] old = loadCred(host);

        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(24), dp(24), dp(24), dp(24));

        TextView who = new TextView(this);
        who.setText("站点：" + host);
        who.setTextSize(13);
        who.setTextColor(C_TXT2);
        body.addView(who);

        final EditText userBox = new EditText(this);
        userBox.setHint("用户名");
        userBox.setSingleLine(true);
        if (old != null) {
            userBox.setText(old[0]);
        }
        body.addView(userBox);

        final EditText passBox = new EditText(this);
        passBox.setHint("密码");
        passBox.setSingleLine(true);
        passBox.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        if (old != null) {
            passBox.setText(old[1]);
        }
        body.addView(passBox);

        TextView note = new TextView(this);
        note.setText("保存后这个站点的登录页会自动填好，密码只存在本机，不上传。");
        note.setTextSize(11);
        note.setTextColor(C_TXT2);
        note.setPadding(0, dp(8), 0, 0);
        body.addView(note);

        new AlertDialog.Builder(this)
                .setTitle("账号与密码")
                .setView(body)
                .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int w) {
                        saveCred(host, userBox.getText().toString().trim(),
                                passBox.getText().toString());
                        toast("已保存，下次这家站点的登录页会自动填");
                    }
                })
                .setNeutralButton("清除", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int w) {
                        saveCred(host, "", "");
                        toast("已清除这家站点的密码");
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

'''
    s = s.replace(anchor, cred + anchor, 1)

    if s.count('fillLoginForm') != 2:
        raise Exception('fillLoginForm count %d' % s.count('fillLoginForm'))
    if s.count('credPanel') != 3:
        raise Exception('credPanel count %d' % s.count('credPanel'))
    if 'KEY_CRED' not in s:
        raise Exception('KEY_CRED missing')
    if s == orig:
        raise Exception('nothing changed')

    open(P, 'w', encoding='utf-8').write(s)
    open('patch_result.txt', 'w', encoding='utf-8').write('OK size=%d' % len(s))
    print('OK', len(s))
except Exception:
    open('patch_error.txt', 'w', encoding='utf-8').write(traceback.format_exc())
    print(traceback.format_exc())
    sys.exit(1)
