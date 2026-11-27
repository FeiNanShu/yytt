package com.mycompany.yytt;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.JsResult;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private static final int C_BG = 0xFFFCFDFF;
    private static final int C_BAR = 0xFFE8F5FC;
    private static final int C_LINE = 0xFFA4C1D7;
    private static final int C_TXT2 = 0xFF3A526C;
    private static final int C_TXT = 0xFF122337;
    private static final int C_ACCENT = 0xFF4691DC;
    private static final int C_ACCENT_D = 0xFF1B64BF;

    private static final int MATCH = ViewGroup.LayoutParams.MATCH_PARENT;
    private static final int WRAP = ViewGroup.LayoutParams.WRAP_CONTENT;

    private static final String PREF_NAME = "script_db";
    private static final String KEY_SCRIPTS = "scripts";
    private static final String KEY_GLOBAL = "global_on";
    private static final String KEY_UA = "ua_desktop";
    private static final String KEY_HOME = "home_url";
    private static final String KEY_TABS = "tabs";
    private static final String KEY_TAB_INDEX = "tab_index";

    private static final String DESKTOP_UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    private static final String JS_LOGIN_CHECK = "(function(){var as=document.getElementsByTagName('a');for(var i=0;i<as.length;i++){var h=as[i].href||'';if(h.indexOf('account/logout')>=0)return 'yes';}var t=document.getElementById('account-topbutton');if(t&&(t.textContent||'').indexOf('|')>=0)return 'yes';return 'no';})()";

    private static final int REQ_PICK_FILE = 1001;
    private static final int REQ_STORAGE = 2002;

    private SharedPreferences sp;
    private List<ScriptItem> scripts = new ArrayList<ScriptItem>();
    private boolean globalOn = true;
    private boolean desktopUa = false;
    private String homeUrl = "about:blank";
    private String defaultUa = null;

    private LinearLayout root;
    private FrameLayout webHolder;
    private TextView addrLabel;
    private EditText addrEdit;
    private ProgressBar progress;
    private TextView gearBtn;
    private TextView goBtn;
    private TextView toolsBtn;
    private TextView tabsBtn;

    private List<Tab> tabs = new ArrayList<Tab>();
    private Tab current = null;
    private boolean editing = false;
    private AlertDialog dialogRef = null;
    private AlertDialog managerDialog = null;
    private Handler loginHandler = null;
    private boolean loginDone = false;

    private ValueCallback<Uri[]> fileCallback = null;
    private String dlUrl = null;
    private String dlUa = null;
    private String dlCd = null;
    private String dlMime = null;

    private static class Tab {
        WebView view;
        String title = "新标签页";
        String url = "";
    }

    private interface JsCallback {
        void onResult(String text);
    }

    @Override
    protected void onCreate(Bundle saved) {
        super.onCreate(saved);

        sp = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        globalOn = sp.getBoolean(KEY_GLOBAL, true);
        desktopUa = sp.getBoolean(KEY_UA, false);
        homeUrl = sp.getString(KEY_HOME, "about:blank");
        scripts = new ArrayList<ScriptItem>();
        loadData();

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(C_ACCENT_D);
            getWindow().setNavigationBarColor(C_BAR);
        }

        buildUi();

        CookieManager.getInstance().setAcceptCookie(true);
        WebView v = curView();
        if (v != null && Build.VERSION.SDK_INT >= 21) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(v, true);
        }
    }

    private int dp(int v) {
        return (int) (getResources().getDisplayMetrics().density * v + 0.5f);
    }

    private GradientDrawable bg(int color, int radiusDp, int strokeDp, int strokeColor) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        if (radiusDp > 0) {
            g.setCornerRadius(dp(radiusDp));
        }
        if (strokeDp > 0) {
            g.setStroke(dp(strokeDp), strokeColor);
        }
        return g;
    }

    private TextView textBtn(String text, int size, int color) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER);
        t.setSingleLine(true);
        return t;
    }

    private void buildUi() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(C_BG);

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setBackgroundColor(C_BAR);
        top.setPadding(dp(2), 0, dp(2), 0);

        gearBtn = textBtn("≡", 22, C_TXT);
        top.addView(gearBtn, new LinearLayout.LayoutParams(dp(48), dp(48)));

        FrameLayout cap = new FrameLayout(this);
        cap.setBackgroundDrawable(bg(C_BG, 18, 1, C_LINE));
        LinearLayout.LayoutParams capLp = new LinearLayout.LayoutParams(0, dp(36), 1);
        capLp.leftMargin = dp(4);
        capLp.rightMargin = dp(4);
        top.addView(cap, capLp);

        addrLabel = new TextView(this);
        addrLabel.setTextSize(13);
        addrLabel.setTextColor(C_TXT);
        addrLabel.setSingleLine(true);
        addrLabel.setEllipsize(TextUtils.TruncateAt.END);
        addrLabel.setGravity(Gravity.CENTER_VERTICAL);
        addrLabel.setPadding(dp(14), 0, dp(14), 0);
        cap.addView(addrLabel, new FrameLayout.LayoutParams(MATCH, MATCH));

        addrEdit = new EditText(this);
        addrEdit.setTextSize(13);
        addrEdit.setTextColor(C_TXT);
        addrEdit.setSingleLine(true);
        addrEdit.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
        addrEdit.setBackgroundDrawable(null);
        addrEdit.setPadding(dp(14), 0, dp(12), 0);
        addrEdit.setVisibility(View.GONE);
        cap.addView(addrEdit, new FrameLayout.LayoutParams(MATCH, MATCH));

        goBtn = textBtn("→", 20, C_ACCENT_D);
        top.addView(goBtn, new LinearLayout.LayoutParams(dp(48), dp(48)));

        root.addView(top, new LinearLayout.LayoutParams(MATCH, dp(48)));

        View divider = new View(this);
        divider.setBackgroundDrawable(new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{C_ACCENT, C_ACCENT_D}));
        root.addView(divider, new LinearLayout.LayoutParams(MATCH, dp(2)));

        progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100);
        progress.setVisibility(View.GONE);
        if (progress.getProgressDrawable() != null) {
            progress.getProgressDrawable().setColorFilter(C_ACCENT, PorterDuff.Mode.SRC_IN);
        }
        root.addView(progress, new LinearLayout.LayoutParams(MATCH, dp(2)));

        webHolder = new FrameLayout(this);
        webHolder.setBackgroundColor(C_BG);
        root.addView(webHolder, new LinearLayout.LayoutParams(MATCH, 0, 1));

        LinearLayout bottom = new LinearLayout(this);
        bottom.setOrientation(LinearLayout.HORIZONTAL);
        bottom.setBackgroundColor(C_BAR);
        bottom.setPadding(dp(2), 0, dp(2), 0);

        toolsBtn = textBtn("▤ 工具箱", 12, C_TXT2);
        bottom.addView(toolsBtn, new LinearLayout.LayoutParams(0, dp(48), 1));

        tabsBtn = textBtn("⧉ 标签页", 12, C_TXT2);
        bottom.addView(tabsBtn, new LinearLayout.LayoutParams(0, dp(48), 1));

        root.addView(bottom, new LinearLayout.LayoutParams(MATCH, dp(48)));

        setContentView(root);

        gearBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openGearMenu();
            }
        });

        toolsBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openToolbox();
            }
        });

        tabsBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openTabsPanel();
            }
        });

        goBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (editing) {
                    submitAddress();
                } else {
                    beginEdit();
                }
            }
        });

        cap.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!editing) {
                    beginEdit();
                }
            }
        });

        addrEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, android.view.KeyEvent event) {
                submitAddress();
                return true;
            }
        });

        addrEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && editing) {
                    endEdit();
                }
            }
        });

        if (!restoreTabs()) {
            addNewTab(homeUrl);
        }
    }

    private void beginEdit() {
        if (current == null) {
            return;
        }
        editing = true;
        addrLabel.setVisibility(View.GONE);
        addrEdit.setVisibility(View.VISIBLE);
        addrEdit.setText(curUrl());
        addrEdit.setSelection(addrEdit.getText().length());
        addrEdit.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(addrEdit, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void endEdit() {
        editing = false;
        addrEdit.setVisibility(View.GONE);
        addrLabel.setVisibility(View.VISIBLE);
        addrEdit.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(addrEdit.getWindowToken(), 0);
        }
        updateAddress();
    }

    private void submitAddress() {
        String raw = addrEdit.getText().toString().trim();
        if (raw.length() == 0) {
            toast("先输入网址");
            return;
        }
        String url = fixUrl(raw);
        editing = false;
        endEdit();
        loadInCurrent(url);
    }

    private void updateAddress() {
        if (editing || current == null) {
            return;
        }
        String title = current.title;
        if (title == null || title.trim().length() == 0) {
            title = prettyUrl(current.url);
        }
        addrLabel.setText(title);
    }

    private String prettyUrl(String url) {
        if (url == null || url.length() == 0) {
            return "新标签页";
        }
        String u = url;
        if (u.startsWith("https://")) {
            u = u.substring(8);
        } else if (u.startsWith("http://")) {
            u = u.substring(7);
        }
        while (u.endsWith("/") && u.length() > 1) {
            u = u.substring(0, u.length() - 1);
        }
        return u;
    }

    private WebView curView() {
        return current == null ? null : current.view;
    }

    private String curUrl() {
        WebView v = curView();
        if (v == null) {
            return "";
        }
        String u = v.getUrl();
        return u == null ? "" : u;
    }

    private Tab findTab(WebView wv) {
        for (int i = 0; i < tabs.size(); i++) {
            if (tabs.get(i).view == wv) {
                return tabs.get(i);
            }
        }
        return null;
    }

    private Tab createTab(String url) {
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

    private void switchTab(Tab t) {
        if (t == null) {
            return;
        }
        webHolder.removeAllViews();
        webHolder.addView(t.view, new FrameLayout.LayoutParams(MATCH, MATCH));
        current = t;
        progress.setVisibility(View.GONE);
        if (!editing) {
            updateAddress();
        }
        updateTabCount();
    }

    private void closeTab(Tab t) {
        if (t == null) {
            return;
        }
        boolean wasCurrent = (t == current);
        tabs.remove(t);
        webHolder.removeView(t.view);
        try {
            t.view.loadUrl("about:blank");
            t.view.destroy();
        } catch (Exception e) {
        }
        if (tabs.size() == 0) {
            addNewTab(homeUrl);
            return;
        }
        if (wasCurrent) {
            switchTab(tabs.get(tabs.size() - 1));
        }
        updateTabCount();
    }

    private void updateTabCount() {
        if (tabsBtn != null) {
            tabsBtn.setText("⧉ 标签页 " + tabs.size());
        }
    }

    private void setupWebView(final WebView wv) {
        WebSettings s = wv.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setJavaScriptCanOpenWindowsAutomatically(true);
        s.setSupportMultipleWindows(true);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);
        s.setAllowFileAccess(true);
        s.setDatabaseEnabled(true);
        if (defaultUa == null) {
            defaultUa = s.getUserAgentString();
        }
        s.setUserAgentString(desktopUa ? DESKTOP_UA : defaultUa);

        wv.setBackgroundColor(C_BG);
        wv.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        if (Build.VERSION.SDK_INT >= 21) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(wv, true);
        }

        wv.setWebViewClient(new WebViewClient() {

            public boolean shouldOverrideUrlLoading(WebView v, String url) {
                return handleUrl(url);
            }

            public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                return handleUrl(r.getUrl().toString());
            }

            public void onPageStarted(WebView v, String url, Bitmap fav) {
                Tab t = findTab(v);
                if (t != null) {
                    t.url = url;
                }
                if (v == curView()) {
                    progress.setProgress(0);
                    progress.setVisibility(View.VISIBLE);
                    if (!editing) {
                        updateAddress();
                    }
                }
            }

            public void onReceivedTitle(WebView v, String title) {
                Tab t = findTab(v);
                if (t != null && title != null && title.trim().length() > 0) {
                    t.title = title.trim();
                }
                if (v == curView() && !editing) {
                    updateAddress();
                }
            }

            public void onPageFinished(WebView v, String url) {
                Tab t = findTab(v);
                if (t != null) {
                    t.url = url;
                    if (t.title == null || t.title.trim().length() == 0) {
                        t.title = prettyUrl(url);
                    }
                }
                if (v == curView()) {
                    progress.setVisibility(View.GONE);
                    if (!editing) {
                        updateAddress();
                    }
                }
                runScripts(v, url);
            }

            public void onReceivedError(WebView v, int code, String desc, String failingUrl) {
                if (v == curView()) {
                    toast("打不开：" + desc);
                }
            }
        });

        wv.setWebChromeClient(new WebChromeClient() {

            public void onProgressChanged(WebView v, int p) {
                if (v == curView()) {
                    progress.setProgress(p);
                    if (p >= 100) {
                        progress.setVisibility(View.GONE);
                    }
                }
            }

            public boolean onShowFileChooser(WebView v, ValueCallback<Uri[]> callback,
                                             FileChooserParams params) {
                if (fileCallback != null) {
                    fileCallback.onReceiveValue(null);
                }
                fileCallback = callback;

                Intent pick = new Intent(Intent.ACTION_GET_CONTENT);
                pick.addCategory(Intent.CATEGORY_OPENABLE);
                pick.setType("*/*");
                if (params != null && params.getMode() == FileChooserParams.MODE_OPEN_MULTIPLE) {
                    pick.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                }
                try {
                    startActivityForResult(Intent.createChooser(pick, "选择文件"), REQ_PICK_FILE);
                } catch (Exception e) {
                    fileCallback = null;
                    toast("这台机器没有可用的文件选择器");
                    return false;
                }
                return true;
            }

            public boolean onCreateWindow(WebView view, boolean isDialog,
                                          boolean isUserGesture, Message resultMsg) {
                final WebView tmp = new WebView(MainActivity.this);
                tmp.setWebViewClient(new WebViewClient() {
                    public boolean shouldOverrideUrlLoading(WebView v, String u) {
                        if (u != null && u.length() > 0) {
                            addNewTab(u);
                        }
                        tmp.destroy();
                        return true;
                    }

                    public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                        return shouldOverrideUrlLoading(v, r.getUrl().toString());
                    }
                });
                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(tmp);
                resultMsg.sendToTarget();
                return true;
            }

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

        wv.setDownloadListener(new DownloadListener() {
            public void onDownloadStart(String url, String userAgent, String contentDisposition,
                                        String mimetype, long contentLength) {
                startDownload(url, userAgent, contentDisposition, mimetype);
            }
        });
    }

    private void loadInCurrent(String url) {
        if (current == null || current.view == null) {
            addNewTab(url);
            return;
        }
        current.view.loadUrl(url);
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

    private void startDownload(String url, String userAgent, String contentDisposition, String mimetype) {
        if (url == null || url.length() == 0) {
            return;
        }
        if (url.startsWith("blob:") || url.startsWith("data:")) {
            toast("这种链接抓不到文件，换页面里的直链试试");
            return;
        }
        if (!(url.startsWith("http://") || url.startsWith("https://"))) {
            handleUrl(url);
            return;
        }

        dlUrl = url;
        dlUa = userAgent;
        dlCd = contentDisposition;
        dlMime = mimetype;

        if (Build.VERSION.SDK_INT >= 29 || hasStoragePermission()) {
            enqueueDownload();
            return;
        }
        requestStoragePermission();
    }

    private boolean hasStoragePermission() {
        try {
            Method check = Activity.class.getMethod("checkSelfPermission", String.class);
            Object result = check.invoke(this, "android.permission.WRITE_EXTERNAL_STORAGE");
            return ((Integer) result).intValue() == 0;
        } catch (Exception e) {
            return true;
        }
    }

    private void requestStoragePermission() {
        try {
            Method req = Activity.class.getMethod("requestPermissions", String[].class, int.class);
            req.invoke(this, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"},
                    Integer.valueOf(REQ_STORAGE));
        } catch (Exception e) {
            toast("没拿到存储权限，下载可能失败");
            enqueueDownload();
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQ_STORAGE) {
            if (grantResults != null && grantResults.length > 0 && grantResults[0] == 0) {
                enqueueDownload();
            } else {
                toast("没给存储权限，安卓 8/9 上没法存到下载目录");
            }
            return;
        }
    }

    private void enqueueDownload() {
        try {
            DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            if (dm == null) {
                toast("系统没有下载服务");
                return;
            }
            String name = URLUtil.guessFileName(dlUrl, dlCd, dlMime);

            DownloadManager.Request req = new DownloadManager.Request(Uri.parse(dlUrl));
            if (dlMime != null && dlMime.length() > 0) {
                req.setMimeType(dlMime);
            }
            if (dlUa != null && dlUa.length() > 0) {
                req.addRequestHeader("User-Agent", dlUa);
            }
            req.setTitle(name);
            req.setDescription("yytt 下载");
            req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name);

            dm.enqueue(req);
            toast("开始下载：" + name);
        } catch (Exception e) {
            toast("下载失败：" + e.getMessage());
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_PICK_FILE) {
            if (fileCallback == null) {
                return;
            }
            Uri[] results = null;
            if (resultCode == RESULT_OK && data != null) {
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    results = new Uri[count];
                    for (int i = 0; i < count; i++) {
                        results[i] = data.getClipData().getItemAt(i).getUri();
                    }
                } else if (data.getData() != null) {
                    results = new Uri[]{data.getData()};
                }
            }
            fileCallback.onReceiveValue(results);
            fileCallback = null;
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void runScripts(WebView wv, String url) {
        if (!globalOn || url == null) {
            return;
        }
        for (int i = 0; i < scripts.size(); i++) {
            ScriptItem item = scripts.get(i);
            if (!item.on) {
                continue;
            }
            if (!matchUrl(item.match, url)) {
                continue;
            }
            inject(wv, item.code);
        }
    }

    private boolean matchUrl(String match, String url) {
        if (match == null || match.trim().length() == 0) {
            return true;
        }
        String m = match.trim().toLowerCase();
        if (m.equals("*")) {
            return true;
        }
        String target = url == null ? "" : url.toLowerCase();
        if (m.indexOf('*') >= 0) {
            String[] parts = m.split("\\*");
            int idx = 0;
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].length() == 0) {
                    continue;
                }
                int p = target.indexOf(parts[i], idx);
                if (p < 0) {
                    return false;
                }
                idx = p + parts[i].length();
            }
            return true;
        }
        return target.indexOf(m) >= 0;
    }

    private void injectCurrent() {
        WebView v = curView();
        String url = curUrl();
        if (v != null) {
            runScripts(v, url);
        }
    }

    private void inject(final WebView wv, final String code) {
        if (wv == null || code == null || code.length() == 0) {
            return;
        }
        if (Build.VERSION.SDK_INT < 19) {
            wv.loadUrl("javascript:" + code.replace("\n", " ").replace("\r", " "));
            return;
        }
        String checker = "(function(){try{new Function(" + jsString(code) + ");"
                + "return 'OK';}catch(e){return 'ERR ' + e.message;}})()";
        wv.evaluateJavascript(checker, new ValueCallback<String>() {
            public void onReceiveValue(String value) {
                String msg = jsonToText(value);
                if (msg.startsWith("ERR ")) {
                    toast("脚本语法错误：" + msg.substring(4));
                    return;
                }
                wv.evaluateJavascript(code, null);
            }
        });
    }

    private String jsString(String code) {
        String s = code.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
        return "\"" + s + "\"";
    }

    private String jsonToText(String value) {
        if (value == null || value.equals("null")) {
            return "";
        }
        String v = value;
        if (v.length() >= 2 && v.startsWith("\"") && v.endsWith("\"")) {
            v = v.substring(1, v.length() - 1);
        }
        return v.replace("\\\"", "\"").replace("\\n", "\n").replace("\\r", "");
    }

    private void showSource() {
        WebView wv = curView();
        if (wv == null) {
            return;
        }
        if (Build.VERSION.SDK_INT < 19) {
            toast("系统版本太低，取不到源码");
            return;
        }
        wv.evaluateJavascript("document.documentElement.outerHTML", new ValueCallback<String>() {
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

    private void openGearMenu() {
        String uaText = desktopUa ? "桌面版 UA：开" : "桌面版 UA：关";
        String[] items = {"脚本管理", "刷新当前页", "查看页面源码", uaText,
                "设置默认首页", "清除 Cookie 与缓存", "关于"};
        new AlertDialog.Builder(this)
                .setTitle("设置")
                .setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int which) {
                        if (which == 0) {
                            openManager();
                        } else if (which == 1) {
                            WebView v = curView();
                            if (v != null) {
                                v.reload();
                            }
                        } else if (which == 2) {
                            showSource();
                        } else if (which == 3) {
                            toggleDesktopUa();
                        } else if (which == 4) {
                            setHomePage();
                        } else if (which == 5) {
                            clearAll();
                        } else {
                            about();
                        }
                    }
                })
                .show();
    }

    private void toggleDesktopUa() {
        desktopUa = !desktopUa;
        sp.edit().putBoolean(KEY_UA, desktopUa).apply();
        for (int i = 0; i < tabs.size(); i++) {
            WebSettings s = tabs.get(i).view.getSettings();
            s.setUserAgentString(desktopUa ? DESKTOP_UA : defaultUa);
            tabs.get(i).view.reload();
        }
        toast(desktopUa ? "已切换为桌面版" : "已切回手机版");
    }

    private void setHomePage() {
        final EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
        input.setText(homeUrl);
        new AlertDialog.Builder(this)
                .setTitle("默认首页")
                .setMessage("新标签页打开时加载这个地址")
                .setView(input)
                .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int w) {
                        String v = input.getText().toString().trim();
                        if (v.length() == 0) {
                            return;
                        }
                        homeUrl = fixUrl(v);
                        sp.edit().putString(KEY_HOME, homeUrl).apply();
                        toast("已保存");
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void clearAll() {
        CookieManager cm = CookieManager.getInstance();
        cm.removeAllCookies(null);
        cm.flush();
        WebStorage.getInstance().deleteAllData();
        for (int i = 0; i < tabs.size(); i++) {
            tabs.get(i).view.clearCache(true);
            tabs.get(i).view.clearHistory();
            tabs.get(i).view.clearFormData();
        }
        toast("已清除 Cookie 与缓存");
    }

    private void about() {
        new AlertDialog.Builder(this)
                .setTitle("关于")
                .setMessage("yytt 脚本浏览器\n\n顶部一条细栏：左边设置，中间地址胶囊（点一下才进入编辑），右边前往。\n底部一条细栏：工具箱 / 标签页。\n\n配色取自「堆域」站点主题 Liminal CN。")
                .setPositiveButton("好", null)
                .show();
    }

    private void openToolbox() {
        String[] items = {"Wikidot 登录", "Wikidot 论坛"};
        new AlertDialog.Builder(this)
                .setTitle("工具箱")
                .setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int which) {
                        if (which == 0) {
                            wdLogin();
                        } else {
                            wdForum();
                        }
                    }
                })
                .show();
    }

    private boolean isWikidotSite() {
        String u = curUrl();
        return u != null && u.toLowerCase().indexOf("wikidot.com") >= 0;
    }

    private String originOf(String url) {
        try {
            Uri u = Uri.parse(url);
            String s = u.getScheme();
            String h = u.getHost();
            if (s == null || h == null) {
                return "";
            }
            return s + "://" + h;
        } catch (Exception e) {
            return "";
        }
    }

    private void wdLogin() {
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

    private void openTabsPanel() {
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setBackgroundColor(C_BG);
        body.setPadding(dp(8), dp(8), dp(8), dp(8));

        for (int i = 0; i < tabs.size(); i++) {
            final Tab t = tabs.get(i);
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);

            TextView name = new TextView(this);
            String label = (t == current ? "● " : "　") + (t.title == null || t.title.length() == 0 ? "新标签页" : t.title);
            name.setText(label);
            name.setTextSize(14);
            name.setTextColor(t == current ? C_ACCENT_D : C_TXT);
            name.setSingleLine(true);
            name.setEllipsize(TextUtils.TruncateAt.END);
            name.setPadding(dp(8), dp(14), dp(8), dp(14));
            row.addView(name, new LinearLayout.LayoutParams(0, WRAP, 1));

            TextView close = textBtn("×", 18, C_TXT2);
            row.addView(close, new LinearLayout.LayoutParams(dp(44), dp(44)));

            View.OnClickListener pick = new View.OnClickListener() {
                public void onClick(View v) {
                    switchTab(t);
                    if (dialogRef != null) {
                        dialogRef.dismiss();
                    }
                }
            };
            row.setOnClickListener(pick);
            name.setOnClickListener(pick);
            close.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    closeTab(t);
                    if (dialogRef != null) {
                        dialogRef.dismiss();
                    }
                }
            });

            body.addView(row, new LinearLayout.LayoutParams(MATCH, WRAP));
        }

        TextView add = textBtn("＋ 新标签页", 14, C_ACCENT_D);
        add.setPadding(0, dp(14), 0, dp(14));
        add.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addNewTab(homeUrl);
                if (dialogRef != null) {
                    dialogRef.dismiss();
                }
            }
        });
        body.addView(add, new LinearLayout.LayoutParams(MATCH, WRAP));

        ScrollView sc = new ScrollView(this);
        sc.addView(body);

        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("标签页");
        b.setView(sc);
        b.setNegativeButton("关闭", null);
        dialogRef = b.show();
    }

    private void openManager() {
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(24), dp(24), dp(24), dp(24));

        Switch allSwitch = new Switch(this);
        allSwitch.setText("总开关（关掉后全部脚本停止）");
        allSwitch.setChecked(globalOn);
        allSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton button, boolean checked) {
                globalOn = checked;
                sp.edit().putBoolean(KEY_GLOBAL, checked).apply();
                if (checked) {
                    injectCurrent();
                }
            }
        });
        body.addView(allSwitch);

        if (scripts.size() == 0) {
            TextView empty = new TextView(this);
            empty.setText("还没有脚本，点下面新建一个。");
            empty.setTextColor(C_TXT2);
            empty.setPadding(0, dp(20), 0, dp(20));
            body.addView(empty);
        }

        for (int i = 0; i < scripts.size(); i++) {
            body.addView(scriptRow(scripts.get(i)));
        }

        TextView addBtn = textBtn("＋ 新建脚本", 14, C_ACCENT_D);
        addBtn.setPadding(0, dp(14), 0, dp(14));
        addBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                editScript(null);
            }
        });
        body.addView(addBtn, new LinearLayout.LayoutParams(MATCH, WRAP));

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
        row.setPadding(0, dp(6), 0, dp(6));

        TextView name = new TextView(this);
        name.setText(item.name.length() == 0 ? "(未命名)" : item.name);
        name.setTextSize(15);
        name.setTextColor(C_TXT);
        row.addView(name, new LinearLayout.LayoutParams(0, WRAP, 1));

        Switch sw = new Switch(this);
        sw.setChecked(item.on);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton button, boolean checked) {
                item.on = checked;
                saveData();
                if (checked) {
                    injectCurrent();
                }
            }
        });
        row.addView(sw);

        TextView editBtn = textBtn("改", 13, C_ACCENT_D);
        editBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                editScript(item);
            }
        });
        row.addView(editBtn, new LinearLayout.LayoutParams(dp(44), dp(44)));

        TextView delBtn = textBtn("删", 13, 0xFFE61744);
        delBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                confirmDelete(item);
            }
        });
        row.addView(delBtn, new LinearLayout.LayoutParams(dp(44), dp(44)));

        return row;
    }

    private void editScript(final ScriptItem item) {
        final boolean isNew = (item == null);

        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(24), dp(24), dp(24), dp(24));

        final EditText nameBox = new EditText(this);
        nameBox.setHint("脚本名称");
        nameBox.setSingleLine(true);
        body.addView(nameBox);

        final EditText matchBox = new EditText(this);
        matchBox.setHint("匹配网址，可留空 = 所有网页通用，支持 * 通配");
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
                injectCurrent();
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

    protected void onPause() {
        saveTabs();
        super.onPause();
    }

    protected void onStop() {
        saveTabs();
        super.onStop();
    }

    public void onBackPressed() {
        WebView v = curView();
        if (v != null && v.canGoBack()) {
            v.goBack();
            return;
        }
        super.onBackPressed();
    }

    protected void onDestroy() {
        try {
            CookieManager.getInstance().flush();
        } catch (Exception e) {
        }
        if (webHolder != null) {
            webHolder.removeAllViews();
        }
        for (int i = 0; i < tabs.size(); i++) {
            try {
                tabs.get(i).view.loadUrl("about:blank");
                tabs.get(i).view.destroy();
            } catch (Exception e) {
            }
        }
        tabs.clear();
        super.onDestroy();
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
