package com.mycompany.yytt;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private static final String PREF_NAME = "script_db";
    private static final String KEY_SCRIPTS = "scripts";
    private static final String KEY_GLOBAL = "global_on";

    private WebView webView;
    private EditText urlBox;
    private SharedPreferences sp;
    private List<ScriptItem> scripts;
    private boolean globalOn = true;
    private AlertDialog managerDialog;

    @Override
    protected void onCreate(Bundle saved) {
        super.onCreate(saved);

        sp = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        globalOn = sp.getBoolean(KEY_GLOBAL, true);
        scripts = new ArrayList<ScriptItem>();
        loadData();

        buildUi();
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);

        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setPadding(8, 8, 8, 0);

        urlBox = new EditText(this);
        urlBox.setHint("输入网址，或 view-source:网址");
        urlBox.setSingleLine(true);
        urlBox.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
        LinearLayout.LayoutParams boxParams =
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        topBar.addView(urlBox, boxParams);

        Button goBtn = new Button(this);
        goBtn.setText("打开");
        topBar.addView(goBtn);
        root.addView(topBar);

        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setPadding(8, 0, 8, 4);

        Button scriptBtn = new Button(this);
        scriptBtn.setText("脚本管理");
        bar.addView(scriptBtn, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        Button refreshBtn = new Button(this);
        refreshBtn.setText("刷新");
        bar.addView(refreshBtn, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        Button sourceBtn = new Button(this);
        sourceBtn.setText("看源码");
        bar.addView(sourceBtn, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        root.addView(bar);

        webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setSupportMultipleWindows(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                runScripts(url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleUrl(url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return handleUrl(request.getUrl().toString());
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog,
                                          boolean isUserGesture, Message resultMsg) {
                final WebView tmp = new WebView(MainActivity.this);
                tmp.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView v, String u) {
                        if (u != null && !handleUrl(u)) {
                            webView.loadUrl(u);
                        }
                        tmp.destroy();
                        return true;
                    }

                    @Override
                    public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                        return shouldOverrideUrlLoading(v, r.getUrl().toString());
                    }
                });
                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(tmp);
                resultMsg.sendToTarget();
                return true;
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("网页提示")
                        .setMessage(message)
                        .setCancelable(false)
                        .setPositiveButton("知道了", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                result.confirm();
                            }
                        })
                        .show();
                return true;
            }
        });

        LinearLayout.LayoutParams webParams =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1);
        root.addView(webView, webParams);

        setContentView(root);

        goBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openPage();
            }
        });

        scriptBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openManager();
            }
        });

        refreshBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (webView.getUrl() != null) {
                    webView.reload();
                }
            }
        });

        sourceBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showSource();
            }
        });
    }

    private void openPage() {
        String raw = urlBox.getText().toString().trim();
        if (raw.length() == 0) {
            toast("先输入网址");
            return;
        }
        webView.loadUrl(fixUrl(raw));
    }

    private String fixUrl(String raw) {
        if (raw.startsWith("http://") || raw.startsWith("https://")
                || raw.startsWith("view-source:")
                || raw.startsWith("about:") || raw.startsWith("data:")
                || raw.startsWith("file:")) {
            return raw;
        }
        return "https://" + raw;
    }

    private boolean handleUrl(String url) {
        if (url == null || url.length() == 0) {
            return false;
        }
        String lower = url.toLowerCase();
        if (lower.startsWith("javascript:")) {
            return false;
        }
        if (lower.startsWith("http://") || lower.startsWith("https://")
                || lower.startsWith("about:") || lower.startsWith("data:")
                || lower.startsWith("file:")) {
            return false;
        }
        try {
            Intent intent;
            if (lower.startsWith("intent:")) {
                intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
            } else {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            }
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            toast("打不开这个链接：" + url);
        }
        return true;
    }

    private void runScripts(String url) {
        if (!globalOn || url == null) {
            return;
        }
        for (int i = 0; i < scripts.size(); i++) {
            ScriptItem item = scripts.get(i);
            if (!item.on) {
                continue;
            }
            if (item.match.length() == 0) {
                continue;
            }
            if (url.indexOf(item.match) < 0) {
                continue;
            }
            inject(item.code);
        }
    }

    private void inject(String code) {
        if (code == null || code.length() == 0) {
            return;
        }
        if (Build.VERSION.SDK_INT >= 19) {
            webView.evaluateJavascript(code, null);
        } else {
            // 老系统没有 evaluateJavascript，退回到 javascript: 方式
            String oneLine = code.replace("\n", " ").replace("\r", " ");
            webView.loadUrl("javascript:" + oneLine);
        }
    }

    private void showSource() {
        if (Build.VERSION.SDK_INT < 19) {
            toast("系统版本太低，取不到源码");
            return;
        }
        webView.evaluateJavascript("document.documentElement.outerHTML", new ValueCallback<String>() {
            public void onReceiveValue(String value) {
                String html = value;
                if (html == null) {
                    html = "(空)";
                } else {
                    html = html.replace("\\n", "\n")
                               .replace("\\r", "")
                               .replace("\\\"", "\"")
                               .replace("\\/", "/");
                    if (html.length() > 1 && html.startsWith("\"") && html.endsWith("\"")) {
                        html = html.substring(1, html.length() - 1);
                    }
                }

                EditText viewer = new EditText(MainActivity.this);
                viewer.setText(html);
                viewer.setMinLines(8);
                viewer.setGravity(Gravity.TOP | Gravity.LEFT);

                ScrollView scroll = new ScrollView(MainActivity.this);
                scroll.addView(viewer);

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("页面源码")
                        .setView(scroll)
                        .setPositiveButton("关闭", null)
                        .show();
            }
        });
    }

    private void openManager() {
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(24, 24, 24, 24);

        Switch allSwitch = new Switch(this);
        allSwitch.setText("总开关（关掉后全部脚本停止）");
        allSwitch.setChecked(globalOn);
        allSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton button, boolean checked) {
                globalOn = checked;
                sp.edit().putBoolean(KEY_GLOBAL, checked).apply();
            }
        });
        body.addView(allSwitch);

        if (scripts.size() == 0) {
            TextView empty = new TextView(this);
            empty.setText("还没有脚本，点下面新建一个。");
            empty.setPadding(0, 20, 0, 20);
            body.addView(empty);
        }

        for (int i = 0; i < scripts.size(); i++) {
            body.addView(scriptRow(scripts.get(i)));
        }

        Button addBtn = new Button(this);
        addBtn.setText("＋ 新建脚本");
        addBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                editScript(null);
            }
        });
        body.addView(addBtn);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(body);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("脚本管理");
        builder.setView(scroll);
        builder.setPositiveButton("关闭", null);
        managerDialog = builder.create();
        managerDialog.show();
    }

    private View scriptRow(final ScriptItem item) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, 6, 0, 6);

        TextView name = new TextView(this);
        name.setText(item.name.length() == 0 ? "(未命名)" : item.name);
        name.setTextSize(15);
        row.addView(name, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        Switch sw = new Switch(this);
        sw.setChecked(item.on);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton button, boolean checked) {
                item.on = checked;
                saveData();
            }
        });
        row.addView(sw);

        Button editBtn = new Button(this);
        editBtn.setText("改");
        editBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                editScript(item);
            }
        });
        row.addView(editBtn);

        Button delBtn = new Button(this);
        delBtn.setText("删");
        delBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                confirmDelete(item);
            }
        });
        row.addView(delBtn);

        return row;
    }

    private void editScript(final ScriptItem item) {
        final boolean isNew = (item == null);

        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(24, 24, 24, 24);

        final EditText nameBox = new EditText(this);
        nameBox.setHint("脚本名称");
        nameBox.setSingleLine(true);
        body.addView(nameBox);

        final EditText matchBox = new EditText(this);
        matchBox.setHint("网址要包含的文字，例如 baidu.com");
        matchBox.setSingleLine(true);
        body.addView(matchBox);

        final EditText codeBox = new EditText(this);
        codeBox.setHint("在这里写 JS 代码");
        codeBox.setMinLines(8);
        codeBox.setGravity(Gravity.TOP | Gravity.LEFT);
        codeBox.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        body.addView(codeBox);

        if (!isNew) {
            nameBox.setText(item.name);
            matchBox.setText(item.match);
            codeBox.setText(item.code);
        }

        ScrollView scroll = new ScrollView(this);
        scroll.addView(body);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(isNew ? "新建脚本" : "编辑脚本");
        builder.setView(scroll);
        builder.setPositiveButton("保存", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String name = nameBox.getText().toString().trim();
                String match = matchBox.getText().toString().trim();
                String code = codeBox.getText().toString();

                if (name.length() == 0) {
                    name = "未命名脚本";
                }

                if (isNew) {
                    ScriptItem newItem = new ScriptItem();
                    newItem.name = name;
                    newItem.match = match;
                    newItem.code = code;
                    newItem.on = true;
                    scripts.add(newItem);
                } else {
                    item.name = name;
                    item.match = match;
                    item.code = code;
                }

                saveData();
                reopenManager();
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void confirmDelete(final ScriptItem item) {
        new AlertDialog.Builder(this)
                .setTitle("删除脚本")
                .setMessage("确定删除「" + item.name + "」吗？")
                .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        scripts.remove(item);
                        saveData();
                        reopenManager();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void reopenManager() {
        if (managerDialog != null) {
            managerDialog.dismiss();
            managerDialog = null;
        }
        openManager();
    }

    private void loadData() {
        String raw = sp.getString(KEY_SCRIPTS, "");
        if (raw.length() == 0) {
            return;
        }
        try {
            JSONArray array = new JSONArray(raw);
            scripts.clear();
            for (int i = 0; i < array.length(); i++) {
                scripts.add(ScriptItem.fromJson(array.getJSONObject(i)));
            }
        } catch (Exception e) {
            // 数据坏了就先当空列表，不影响启动
            scripts.clear();
        }
    }

    private void saveData() {
        try {
            JSONArray array = new JSONArray();
            for (int i = 0; i < scripts.size(); i++) {
                array.put(scripts.get(i).toJson());
            }
            sp.edit().putString(KEY_SCRIPTS, array.toString()).apply();
        } catch (Exception e) {
            toast("保存失败：" + e.getMessage());
        }
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
            return;
        }
        super.onBackPressed();
    }
}

class ScriptItem {

    public String name = "";
    public String match = "";
    public String code = "";
    public boolean on = true;

    public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("name", name);
            obj.put("match", match);
            obj.put("code", code);
            obj.put("on", on);
        } catch (Exception e) {
            // 忽略
        }
        return obj;
    }

    public static ScriptItem fromJson(JSONObject obj) {
        ScriptItem item = new ScriptItem();
        item.name = obj.optString("name", "");
        item.match = obj.optString("match", "");
        item.code = obj.optString("code", "");
        item.on = obj.optBoolean("on", true);
        return item;
    }
}
