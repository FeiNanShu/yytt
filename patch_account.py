import sys
import traceback

P = 'src/com/mycompany/yytt/MainActivity.java'

try:
    s = open(P, encoding='utf-8').read()

    if 'KEY_ASK_ACCOUNT' in s:
        open('patch_result.txt', 'w', encoding='utf-8').write('ALREADY size=%d' % len(s))
        print('already applied', len(s))
        sys.exit(0)

    orig = s

    def swap(a_marker, b_marker, new):
        global s
        a = s.index(a_marker)
        b = s.index(b_marker, a)
        s = s[:a] + new + s[b:]

    swap('    private void openGearMenu() {', '    private void toggleDesktopUa() {', '''    private void openGearMenu() {
        String[] items = {"脚本管理", "Wikidot 账号登录", "设置默认首页", "清除 Cookie 与缓存", "关于"};
        new AlertDialog.Builder(this)
                .setTitle("浏览器设置")
                .setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int which) {
                        if (which == 0) {
                            openManager();
                        } else if (which == 1) {
                            credPanel();
                        } else if (which == 2) {
                            setHomePage();
                        } else if (which == 3) {
                            clearAll();
                        } else {
                            about();
                        }
                    }
                })
                .show();
    }

''')

    swap('    private void credPanel() {', '    private void about() {', '''    private void credPanel() {
        final String host = "wikidot.com";
        String[] old = loadCred(host);

        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(24), dp(24), dp(24), dp(24));

        TextView who = new TextView(this);
        who.setText("只支持 Wikidot 账号。登录后所有 wikidot 站点自动通行，登录页也会自动填好。");
        who.setTextSize(13);
        who.setTextColor(C_TXT2);
        body.addView(who);

        final EditText userBox = new EditText(this);
        userBox.setHint("Wikidot 用户名");
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
        note.setText("账号密码只存在本机，不上传。");
        note.setTextSize(11);
        note.setTextColor(C_TXT2);
        note.setPadding(0, dp(8), 0, 0);
        body.addView(note);

        new AlertDialog.Builder(this)
                .setTitle("Wikidot 账号登录")
                .setView(body)
                .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int w) {
                        saveCred(host, userBox.getText().toString().trim(),
                                passBox.getText().toString());
                        toast("已保存 Wikidot 账号");
                    }
                })
                .setNeutralButton("清除", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int w) {
                        saveCred(host, "", "");
                        toast("已清除 Wikidot 账号");
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

''')

    a = '''        String host = hostOf(url);
        String[] cr = loadCred(host);
        if (cr == null) {
            return;
        }
'''
    b = '''        String host = hostOf(url);
        if (!host.endsWith("wikidot.com")) {
            return;
        }
        String[] cr = loadCred(host);
        if (cr == null) {
            cr = loadCred("wikidot.com");
        }
        if (cr == null) {
            return;
        }
'''
    if a not in s:
        raise Exception('fill guard anchor missing')
    s = s.replace(a, b, 1)

    c = '    private static final String KEY_CRED = "creds";\n'
    if c not in s:
        raise Exception('KEY_CRED missing')
    s = s.replace(c, c + '    private static final String KEY_ASK_ACCOUNT = "ask_account";\n', 1)

    d = 'import android.widget.CompoundButton;\n'
    if d not in s:
        raise Exception('import anchor missing')
    s = s.replace(d, 'import android.widget.CheckBox;\n' + d, 1)

    e = '''        if (!restoreTabs()) {
            addNewTab(homeUrl);
        }
    }
'''
    if e not in s:
        raise Exception('buildUi tail anchor missing')
    s = s.replace(e, '''        if (!restoreTabs()) {
            addNewTab(homeUrl);
        }

        root.post(new Runnable() {
            public void run() {
                maybeAskAccount();
            }
        });
    }

    private void maybeAskAccount() {
        if (!sp.getBoolean(KEY_ASK_ACCOUNT, true)) {
            return;
        }
        String[] old = loadCred("wikidot.com");
        if (old != null && old[1].length() > 0) {
            return;
        }

        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(24), dp(24), dp(24), dp(24));

        TextView who = new TextView(this);
        who.setText("只能用 Wikidot 账号，QQ、Google 等其他账号不行。");
        who.setTextSize(13);
        who.setTextColor(C_TXT2);
        body.addView(who);

        final EditText userBox = new EditText(this);
        userBox.setHint("Wikidot 用户名");
        userBox.setSingleLine(true);
        body.addView(userBox);

        final EditText passBox = new EditText(this);
        passBox.setHint("密码");
        passBox.setSingleLine(true);
        passBox.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        body.addView(passBox);

        final CheckBox noAsk = new CheckBox(this);
        noAsk.setText("不再弹出");
        body.addView(noAsk);

        TextView note = new TextView(this);
        note.setText("登录后所有 wikidot 站点通用。账号密码只存在本机。");
        note.setTextSize(11);
        note.setTextColor(C_TXT2);
        note.setPadding(0, dp(8), 0, 0);
        body.addView(note);

        new AlertDialog.Builder(this)
                .setTitle("登录 Wikidot 账号")
                .setView(body)
                .setCancelable(false)
                .setPositiveButton("登录", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int w) {
                        String u = userBox.getText().toString().trim();
                        String p = passBox.getText().toString();
                        if (u.length() == 0 || p.length() == 0) {
                            toast("用户名和密码都要填");
                        } else {
                            saveCred("wikidot.com", u, p);
                            toast("已绑定 " + u + "，所有 wikidot 站点通用");
                        }
                        if (noAsk.isChecked()) {
                            sp.edit().putBoolean(KEY_ASK_ACCOUNT, false).apply();
                        }
                    }
                })
                .setNegativeButton("以后再说", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int w) {
                        if (noAsk.isChecked()) {
                            sp.edit().putBoolean(KEY_ASK_ACCOUNT, false).apply();
                        }
                    }
                })
                .show();
    }
''', 1)

    if 'Wikidot 账号登录' not in s:
        raise Exception('new menu missing')
    if 'maybeAskAccount' not in s:
        raise Exception('ask dialog missing')
    if 'endsWith("wikidot.com")' not in s:
        raise Exception('fill guard missing')
    if '「' in s:
        raise Exception('bad quote left')
    if s == orig:
        raise Exception('nothing changed')

    open(P, 'w', encoding='utf-8').write(s)
    open('patch_result.txt', 'w', encoding='utf-8').write('OK size=%d' % len(s))
    print('OK', len(s))
except Exception:
    open('patch_error.txt', 'w', encoding='utf-8').write(traceback.format_exc())
    print(traceback.format_exc())
    sys.exit(1)
