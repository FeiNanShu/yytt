package com.feinanshu;

import javafx.application.Application;
import java.io.InputStream;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.Desktop;
import java.io.File;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class DuiyuApp extends Application {

    static final String C_BG = "#FCFDFF";
    static final String C_BAR = "#E8F5FC";
    static final String C_LINE = "#A4C1D7";
    static final String C_TXT = "#122337";
    static final String C_TXT2 = "#3A526C";
    static final String C_ACCENT = "#4691DC";
    static final String C_ACCENT_D = "#1B64BF";

    static final String HOME_LABEL = "新标签页";

    static final String LOGO_B64 = "iVBORw0KGgoAAAANSUhEUgAAAGAAAABgBAMAAAAQtmoLAAAABGdBTUEAALGPC/xhBQAAACBjSFJNAAB6JgAAgIQAAPoAAACA6AAAdTAAAOpgAAA6mAAAF3CculE8AAAAG1BMVEUAAAAAESIAM2YRVbsRRIgRVZkAIkQRM3cAETNA2M+NAAAAAWJLR0QAiAUdSAAAAAd0SU1FB+oLHAUZOKPdmB8AAAJPSURBVFjD7ZVNb9pAEIZ3LWN6XAN2rkahqFcYu1yxV6AcU5w0VxsRpUekNve0qvq7O/uBv7Jg55BL5PeEvfPsOzPrHQjp1atXr17vIsreGGWF0/b4EQ9KAGCX15ev4vqz8wxQAyDaVvMapVHVlB5SaAIA4ZcCcXA9yor1/UYGNAGAROf1icsNNOH/0suvAYgSYUKv1dOKFdmcAwDuMfXH08OOYW/KtWNZ06Z8Oy3jAb5X93qoduXPaaOYTKCiG+LpX5WWKJMnmepXbKio5QlgK3/EyrDedFlG7n/GhVtHhk0xk2AkIwNbFJP7QQMYQOL6G1iP1bYCIH8FsbAhnLqz9PYVANHcPRw9WaoCZDYLa+vu0dYAANzlbCybqQGCWS6oL3toBDBqDCEjBUA5OqiVbw3AVq+xhhUpAXItagCTg1cASzI/AYcLwKDikMbic83IJGIlsG4ARB3c2oMl5fhlTwQUFQ7RvHlweAdmhYPazobCIflNDKL/NqoGDkcnc7IhaIe7H8wUT+zE3WNbVxQdhiE/4hELwJq7s7UZEJnqlESXhioluudwDtBt1YCtUhqaWlQCXulg4aHromMjMNAOWMOxkpJ92UHUwIsaSDuAKVFeK9o2fBdKFi+K1imF2uE+MAJ41qkxpeSFnJMfeGVbdUpOTi7J4RKgP0MmTzp8IS1yHrCtO+ZkeD8j9py1xRPiYg2wlRMgYm57PErcjJuDmHadwvFmiiuTqnnTTY+nUdxVVExNWLLOgJhHaj51lsMrf3Gd5L8xvlevXr0+sP4D8Miuq6RN728AAAAldEVYdGRhdGU6Y3JlYXRlADIwMjYtMTEtMjhUMDU6MjU6NTUrMDA6MDDSdWixAAAAJXRFWHRkYXRlOm1vZGlmeQAyMDI2LTExLTI4VDA1OjI1OjU1KzAwOjAwoyjQDQAAAABJRU5ErkJggg==";

    static final String WELCOME =
        "<!doctype html><html><head><meta charset='utf-8'><style>"
        + "html,body{margin:0;height:100%;background:#FCFDFF;}"
        + "body{display:flex;flex-direction:column;align-items:center;justify-content:center;"
        + "font-family:'Segoe UI',sans-serif;color:#122337;}"
        + "img{width:96px;height:96px;margin:0 0 18px;}"
        + "h1{font-size:22px;letter-spacing:5px;margin:0 0 34px;color:#1B64BF;font-weight:600;}"
        + ".circles{display:flex;gap:16px;}"
        + "a.c{width:76px;height:76px;border-radius:50%;background:#E8F5FC;border:1px solid #A4C1D7;"
        + "display:flex;align-items:center;justify-content:center;text-decoration:none;color:#1B64BF;font-size:13px;}"
        + "a.plus{font-size:28px;}"
        + "</style></head><body>"
        + "<img src='data:image/png;base64," + LOGO_B64 + "'>"
        + "<h1>堆域浏览器</h1>"
        + "<div class='circles'>"
        + "<a class='c' href='https://scp-wiki-cn.wikidot.com'>SCP</a>"
        + "<a class='c' href='https://backrooms-wiki-cn.wikidot.com'>后室</a>"
        + "<a class='c plus' href='yytt://addr'>+</a>"
        + "</div></body></html>";

    static String UI_FONT = "System";

    static {
        try (InputStream in = DuiyuApp.class.getResourceAsStream("/fonts/NotoSansCJKsc-Regular.otf")) {
            if (in != null) {
                Font f = Font.loadFont(in, 13);
                if (f != null) {
                    UI_FONT = f.getFamily();
                }
            }
        } catch (Throwable ignored) {
        }
    }

    private final List<Tab> tabs = new ArrayList<>();
    private Tab current;
    private StackPane holder;
    private Label addrLabel;
    private TextField addrEdit;
    private Button tabBtn;
    private boolean editing;
    private Stage stage;

    private static class Tab {
        final WebView view = new WebView();
        String title = HOME_LABEL;
        String url = "";
    }

    static File dir() {
        File d = new File(System.getProperty("user.home"), ".duiyu-browser");
        if (!d.exists()) {
            d.mkdirs();
        }
        return d;
    }

    static File file(String name) {
        return new File(dir(), name);
    }

    @Override
    public void init() {
        try {
            CookieHandler.setDefault(new CookieManager(new FileCookieStore(file("cookies.json")), CookiePolicy.ACCEPT_ALL));
        } catch (Exception ignored) {
        }
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + C_BG + "; -fx-font-family: '" + UI_FONT + "';");
        root.setTop(topBar());
        holder = new StackPane();
        holder.setStyle("-fx-background-color: " + C_BG + ";");
        root.setCenter(holder);
        root.setBottom(bottomBar());

        Scene scene = new Scene(root, 1080, 720);
        stage.setScene(scene);
        stage.setTitle("堆域浏览器");
        stage.setMinWidth(520);
        stage.setMinHeight(420);
        stage.setOnCloseRequest(e -> saveTabs());

        restoreTabs();
        if (tabs.isEmpty()) {
            newTab("");
        }
        stage.show();
    }

    @Override
    public void stop() {
        saveTabs();
    }

    /* ---------------- UI ---------------- */

    private Region topBar() {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER);
        row.setStyle("-fx-background-color: " + C_BAR + ";");
        row.setPadding(new Insets(0, 2, 0, 2));

        Button gear = flat("≡", 48);
        gear.setOnAction(e -> showMenu(gear));

        StackPane cap = new StackPane();
        cap.setPrefHeight(36);
        cap.setMinHeight(36);
        cap.setStyle("-fx-background-color: " + C_BG + "; -fx-background-radius: 18;"
            + " -fx-border-color: " + C_LINE + "; -fx-border-radius: 18;");
        HBox.setHgrow(cap, Priority.ALWAYS);
        HBox.setMargin(cap, new Insets(0, 4, 0, 4));

        addrLabel = new Label(HOME_LABEL);
        addrLabel.setStyle("-fx-text-fill: " + C_TXT + "; -fx-font-size: 13px;");
        addrLabel.setMaxWidth(Double.MAX_VALUE);
        addrLabel.setPadding(new Insets(0, 14, 0, 14));

        addrEdit = new TextField();
        addrEdit.setPromptText("输入站点链接");
        addrEdit.setStyle("-fx-background-color: transparent; -fx-text-fill: " + C_TXT
            + "; -fx-font-size: 13px; -fx-border-color: transparent; -fx-padding: 0 12 0 12;");
        addrEdit.setVisible(false);
        addrEdit.setOnAction(e -> submitAddr());
        addrEdit.focusedProperty().addListener((o, a, b) -> {
            if (!b) {
                hideEdit();
            }
        });

        cap.getChildren().addAll(addrLabel, addrEdit);
        cap.setOnMouseClicked(e -> beginEdit());

        Button re = flat("↻", 44);
        re.setOnAction(e -> reload());
        Button go = flat("→", 48);
        go.setOnAction(e -> beginEdit());

        row.getChildren().addAll(gear, cap, re, go);

        VBox box = new VBox();
        Region line = new Region();
        line.setPrefHeight(2);
        line.setStyle("-fx-background-color: " + C_ACCENT + ";");
        box.getChildren().addAll(row, line);
        return box;
    }

    private Region bottomBar() {
        HBox row = new HBox();
        row.setPrefHeight(48);
        row.setAlignment(Pos.CENTER);
        row.setStyle("-fx-background-color: " + C_BAR + ";");

        Button tools = barBtn("▤  工具箱");
        tools.setOnAction(e -> toolsPanel());
        tabBtn = barBtn("⧉  标签页");
        tabBtn.setOnAction(e -> tabsPanel());

        Button back = flat("←", 44);
        back.setOnAction(e2 -> {
            javafx.scene.web.WebHistory h = current == null ? null : current.view.getEngine().getHistory();
            if (h != null && h.getCurrentIndex() > 0) {
                h.go(-1);
            }
        });
        Button fwd = flat("→", 44);
        fwd.setOnAction(e2 -> {
            javafx.scene.web.WebHistory h = current == null ? null : current.view.getEngine().getHistory();
            if (h != null && h.getCurrentIndex() < h.getEntries().size() - 1) {
                h.go(1);
            }
        });
        HBox.setHgrow(tools, Priority.ALWAYS);
        HBox.setHgrow(tabBtn, Priority.ALWAYS);
        row.getChildren().addAll(back, fwd, tools, tabBtn);
        return row;
    }

    private Button flat(String text, double w) {
        Button b = new Button(text);
        b.setPrefSize(w, 48);
        b.setMinWidth(w);
        b.setFocusTraversable(false);
        styleFlat(b, false);
        b.setOnMouseEntered(e -> styleFlat(b, true));
        b.setOnMouseExited(e -> styleFlat(b, false));
        return b;
    }

    private void styleFlat(Button b, boolean hover) {
        b.setStyle("-fx-background-color: " + (hover ? "rgba(70,145,220,0.16)" : "transparent")
            + "; -fx-text-fill: " + C_TXT + "; -fx-font-size: 19px; -fx-padding: 0;");
    }

    private Button barBtn(String text) {
        Button b = new Button(text);
        b.setPrefHeight(48);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setFocusTraversable(false);
        b.setStyle("-fx-background-color: transparent; -fx-text-fill: " + C_TXT2 + "; -fx-font-size: 13px;");
        b.setOnMouseEntered(e -> b.setStyle("-fx-background-color: rgba(70,145,220,0.12); -fx-text-fill: " + C_TXT2 + "; -fx-font-size: 13px;"));
        b.setOnMouseExited(e -> b.setStyle("-fx-background-color: transparent; -fx-text-fill: " + C_TXT2 + "; -fx-font-size: 13px;"));
        return b;
    }

    /* ---------------- tabs ---------------- */

    private Tab newTab(String url) {
        Tab t = new Tab();
        WebEngine e = t.view.getEngine();
        e.locationProperty().addListener((o, a2, b2) -> {
            if (b2 != null && b2.startsWith("yytt://")) {
                javafx.application.Platform.runLater(() -> {
                    welcome(t);
                    beginEdit();
                });
            }
        });
        e.titleProperty().addListener((o, a, b) -> {
            if (b != null && !b.trim().isEmpty()) {
                t.title = b.trim();
            }
            if (t == current) {
                refreshAddr();
            }
        });
        e.getLoadWorker().stateProperty().addListener((o, a, b) -> {
            if (b == Worker.State.RUNNING) {
                t.url = e.getLocation() == null ? "" : e.getLocation();
                if (t == current) {
                    refreshAddr();
                }
            }
            if (b == Worker.State.SUCCEEDED) {
                t.url = e.getLocation() == null ? "" : e.getLocation();
                if (t.title == null || t.title.trim().isEmpty() || t.title.equals(HOME_LABEL)) {
                    t.title = pretty(t.url);
                }
                fillLogin(e, t.url);
                injectScripts(e, t.url);
                if (t == current) {
                    refreshAddr();
                }
            }
        });

        tabs.add(t);
        String u = url == null ? "" : url.trim();
        if (u.isEmpty() || u.equals("about:blank")) {
            welcome(t);
        } else {
            t.url = u;
            e.load(u);
        }
        switchTab(t);
        return t;
    }

    private void welcome(Tab t) {
        t.url = "";
        t.title = HOME_LABEL;
        t.view.getEngine().loadContent(WELCOME);
    }

    private void switchTab(Tab t) {
        if (t == null) {
            return;
        }
        current = t;
        holder.getChildren().setAll(t.view);
        refreshAddr();
    }

    private void closeTab(int i) {
        if (i < 0 || i >= tabs.size()) {
            return;
        }
        tabs.remove(i);
        if (tabs.isEmpty()) {
            newTab("");
        } else {
            switchTab(tabs.get(Math.min(i, tabs.size() - 1)));
        }
        saveTabs();
    }

    private void closeCurrent() {
        if (current == null) {
            return;
        }
        closeTab(tabs.indexOf(current));
    }

    private void refreshAddr() {
        if (current == null) {
            return;
        }
        String txt = current.title;
        if (txt == null || txt.trim().isEmpty()) {
            txt = HOME_LABEL;
        }
        addrLabel.setText(txt);
        if (!editing) {
            addrEdit.setText(current.url == null ? "" : current.url);
        }
        if (tabBtn != null) {
            tabBtn.setText("⧉  标签页 " + tabs.size());
        }
        stage.setTitle(txt + " - 堆域浏览器");
    }

    /* ---------------- address ---------------- */

    private void beginEdit() {
        editing = true;
        addrEdit.setText(current == null || current.url == null ? "" : current.url);
        addrLabel.setVisible(false);
        addrLabel.setManaged(false);
        addrEdit.setVisible(true);
        addrEdit.setManaged(true);
        addrEdit.requestFocus();
        addrEdit.selectAll();
    }

    private void hideEdit() {
        editing = false;
        addrEdit.setVisible(false);
        addrEdit.setManaged(false);
        addrLabel.setVisible(true);
        addrLabel.setManaged(true);
    }

    private void submitAddr() {
        String s = addrEdit.getText() == null ? "" : addrEdit.getText().trim();
        hideEdit();
        if (s.isEmpty()) {
            return;
        }
        load(normalize(s));
    }

    private String normalize(String s) {
        if (s.startsWith("yytt://")) {
            return "";
        }
        if (s.startsWith("http://") || s.startsWith("https://") || s.startsWith("file:") || s.startsWith("view-source:")) {
            return s;
        }
        if (s.contains(" ") || !s.contains(".")) {
            try {
                return "https://www.bing.com/search?q=" + java.net.URLEncoder.encode(s, "UTF-8");
            } catch (Exception e) {
                return "https://www.bing.com/search?q=" + s;
            }
        }
        return "https://" + s;
    }

    private void load(String url) {
        if (current == null) {
            newTab(url);
            return;
        }
        if (url == null || url.isEmpty()) {
            welcome(current);
            refreshAddr();
            return;
        }
        current.view.getEngine().load(url);
    }

    private void reload() {
        if (current != null) {
            if (current.url == null || current.url.isEmpty()) {
                welcome(current);
            } else {
                current.view.getEngine().reload();
            }
        }
    }

    /* ---------------- panels ---------------- */

    private void showMenu(Button anchor) {
        ContextMenu m = new ContextMenu();
        MenuItem acc = new MenuItem("Wikidot 账号登录");
        acc.setOnAction(e -> accountDialog());
        MenuItem scr = new MenuItem("脚本管理");
        scr.setOnAction(e -> scriptDialog());
        MenuItem clr = new MenuItem("清除 Cookie 与缓存");
        clr.setOnAction(e -> clearAll());
        MenuItem ab = new MenuItem("关于");
        ab.setOnAction(e -> about());
        m.getItems().addAll(acc, scr, new SeparatorMenuItem(), clr, ab);
        m.show(anchor, Side.BOTTOM, 0, 0);
    }

    private void toolsPanel() {
        ContextMenu m = new ContextMenu();
        MenuItem src = new MenuItem("查看页面源码");
        src.setOnAction(e -> sourceJump());
        MenuItem re = new MenuItem("重新载入当前页");
        re.setOnAction(e -> reload());
        MenuItem nc = new MenuItem("关闭当前标签页");
        nc.setOnAction(e -> closeCurrent());
        m.getItems().addAll(src, re, nc);
        m.show(stage);
    }

    private void tabsPanel() {
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle("标签页");
        ListView<String> list = new ListView<>();
        for (Tab t : tabs) {
            list.getItems().add(t.title + "      " + (t.url == null || t.url.isEmpty() ? "（欢迎页）" : t.url));
        }
        list.getSelectionModel().select(tabs.indexOf(current));
        VBox box = new VBox(10, list);
        box.setPadding(new Insets(12));
        VBox.setVgrow(list, Priority.ALWAYS);
        dlg.getDialogPane().setContent(box);
        ButtonType btNew = new ButtonType("新标签页", ButtonBar.ButtonData.OTHER);
        ButtonType btClose = new ButtonType("关闭所选", ButtonBar.ButtonData.OTHER);
        dlg.getDialogPane().getButtonTypes().addAll(btNew, btClose, ButtonType.CLOSE);
        Optional<ButtonType> r = dlg.showAndWait();
        if (!r.isPresent()) {
            return;
        }
        if (r.get() == btNew) {
            newTab("");
            return;
        }
        int i = list.getSelectionModel().getSelectedIndex();
        if (r.get() == btClose) {
            closeTab(i);
            return;
        }
        if (i >= 0 && i < tabs.size()) {
            switchTab(tabs.get(i));
        }
    }

    private void accountDialog() {
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle("Wikidot 账号登录");
        GridPane g = new GridPane();
        g.setHgap(8);
        g.setVgap(8);
        g.setPadding(new Insets(16));
        String[] cur = loadAccount();
        TextField u = new TextField(cur == null ? "" : cur[0]);
        PasswordField p = new PasswordField();
        u.setPrefWidth(220);
        p.setPrefWidth(220);
        g.add(new Label("请输入正确的 Wikidot 账号和密码"), 0, 0, 2, 1);
        g.add(new Label("用户名"), 0, 1);
        g.add(u, 1, 1);
        g.add(new Label("密码"), 0, 2);
        g.add(p, 1, 2);
        dlg.getDialogPane().setContent(g);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Optional<ButtonType> r = dlg.showAndWait();
        if (r.isPresent() && r.get() == ButtonType.OK) {
            saveAccount(u.getText().trim(), p.getText());
            if (!u.getText().trim().isEmpty()) {
                load("https://www.wikidot.com/account/login");
            }
        }
    }

    private void scriptDialog() {
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle("脚本管理");
        dlg.setHeaderText("页面载入后自动执行，匹配看网址是否包含那段文字");
        ListView<String> list = new ListView<>();
        Runnable fill = () -> {
            list.getItems().clear();
            JSONArray arr = scripts();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                list.getItems().add((o.optBoolean("on", true) ? "[开] " : "[关] ")
                    + o.optString("name", "未命名") + "    →    " + o.optString("match", "*"));
            }
        };
        fill.run();
        Button add = new Button("新建");
        Button edit = new Button("编辑");
        Button del = new Button("删除");
        HBox bar = new HBox(8, add, edit, del);
        VBox box = new VBox(10, list, bar);
        box.setPadding(new Insets(12));
        VBox.setVgrow(list, Priority.ALWAYS);
        dlg.getDialogPane().setContent(box);
        dlg.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        add.setOnAction(e -> {
            editScript(-1);
            fill.run();
        });
        edit.setOnAction(e -> {
            int i = list.getSelectionModel().getSelectedIndex();
            if (i >= 0) {
                editScript(i);
                fill.run();
            }
        });
        del.setOnAction(e -> {
            int i = list.getSelectionModel().getSelectedIndex();
            if (i >= 0) {
                removeScript(i);
                fill.run();
            }
        });
        dlg.showAndWait();
    }

    private void editScript(int index) {
        JSONArray arr = scripts();
        JSONObject o = index >= 0 && index < arr.length() ? arr.getJSONObject(index) : new JSONObject();
        TextField name = new TextField(o.optString("name", "新脚本"));
        TextField match = new TextField(o.optString("match", "*.wikidot.com"));
        TextArea code = new TextArea(o.optString("code", ""));
        code.setPrefRowCount(12);
        code.setPrefColumnCount(40);
        code.setWrapText(false);
        CheckBox on = new CheckBox("启用");
        on.setSelected(o.optBoolean("on", true));
        GridPane g = new GridPane();
        g.setHgap(8);
        g.setVgap(8);
        g.setPadding(new Insets(12));
        g.add(new Label("名称"), 0, 0);
        g.add(name, 1, 0);
        g.add(new Label("匹配"), 0, 1);
        g.add(match, 1, 1);
        g.add(on, 1, 2);
        g.add(new Label("代码"), 0, 3);
        g.add(code, 1, 3);
        Dialog<ButtonType> d = new Dialog<>();
        d.setTitle("脚本");
        d.getDialogPane().setContent(g);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Optional<ButtonType> r = d.showAndWait();
        if (r.isPresent() && r.get() == ButtonType.OK) {
            JSONObject n = new JSONObject();
            n.put("name", name.getText().trim());
            n.put("match", match.getText().trim());
            n.put("code", code.getText());
            n.put("on", on.isSelected());
            if (index >= 0 && index < arr.length()) {
                arr.put(index, n);
            } else {
                arr.put(n);
            }
            saveScripts(arr);
        }
    }

    private void about() {
        alert("堆域浏览器 桌面版\n\n只给 Wikidot 站点用（后室 / SCP）。\n点顶部胶囊输链接，≡ 里管账号和脚本，底部左边工具箱、右边标签页。\n配色取自 duiyu-Warehouse。");
    }

    private void alert(String s) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, s, ButtonType.OK);
        a.setTitle("堆域浏览器");
        a.setHeaderText(null);
        a.showAndWait();
    }

    /* ---------------- features ---------------- */

    private void sourceJump() {
        if (current == null || current.url == null || current.url.isEmpty()) {
            alert("现在没有页面。");
            return;
        }
        try {
            Desktop.getDesktop().browse(new URI("view-source:" + current.url));
        } catch (Exception e) {
            alert("没能调起系统浏览器：\n" + e.getMessage());
        }
    }

    private void clearAll() {
        try {
            CookieHandler h = CookieHandler.getDefault();
            if (h instanceof CookieManager) {
                CookieStore st = ((CookieManager) h).getCookieStore();
                for (HttpCookie c : new ArrayList<>(st.getCookies())) {
                    st.remove(null, c);
                }
            }
        } catch (Exception ignored) {
        }
        try {
            Files.deleteIfExists(file("cookies.json").toPath());
        } catch (Exception ignored) {
        }
        alert("Cookie 已清，页面会重新载。");
        reload();
    }

    private void fillLogin(WebEngine e, String url) {
        if (url == null) {
            return;
        }
        String low = url.toLowerCase();
        if (!low.contains("wikidot.com") || !(low.contains("login") || low.contains("signin") || low.contains("account"))) {
            return;
        }
        String[] c = loadAccount();
        if (c == null) {
            return;
        }
        String js = "(function(){try{var u=" + JSONObject.quote(c[0]) + ",p=" + JSONObject.quote(c[1]) + ";"
            + "var us=document.querySelectorAll('input[type=text],input[type=email],input[name=username],input[name=user],input[name=nickname]');"
            + "for(var i=0;i<us.length;i++){if(!us[i].value){us[i].value=u;}}"
            + "var ps=document.querySelectorAll('input[type=password]');"
            + "for(var i=0;i<ps.length;i++){if(!ps[i].value){ps[i].value=p;}}"
            + "return 1;}catch(err){return 0;}})()";
        try {
            e.executeScript(js);
        } catch (Exception ignored) {
        }
    }

    private void injectScripts(WebEngine e, String url) {
        if (url == null) {
            return;
        }
        String low = url.toLowerCase();
        try {
            JSONArray arr = scripts();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                if (!o.optBoolean("on", true)) {
                    continue;
                }
                String match = o.optString("match", "*").trim().toLowerCase();
                if (!match.isEmpty() && !match.equals("*") && !low.contains(match)) {
                    continue;
                }
                String code = o.optString("code", "");
                if (code.isEmpty()) {
                    continue;
                }
                try {
                    e.executeScript(code);
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }
    }

    private String pretty(String url) {
        if (url == null) {
            return HOME_LABEL;
        }
        if (url.equals("about:blank") || url.trim().isEmpty()) {
            return HOME_LABEL;
        }
        String u = url;
        if (u.startsWith("https://")) {
            u = u.substring(8);
        } else if (u.startsWith("http://")) {
            u = u.substring(7);
        }
        if (u.endsWith("/")) {
            u = u.substring(0, u.length() - 1);
        }
        return u;
    }

    /* ---------------- storage ---------------- */

    private String[] loadAccount() {
        try {
            File f = file("account.json");
            if (!f.exists()) {
                return null;
            }
            JSONObject o = new JSONObject(new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8));
            String u = o.optString("u", "");
            String p = o.optString("p", "");
            if (u.isEmpty() || p.isEmpty()) {
                return null;
            }
            return new String[] {u, p};
        } catch (Exception e) {
            return null;
        }
    }

    private void saveAccount(String u, String p) {
        try {
            JSONObject o = new JSONObject();
            o.put("u", u);
            o.put("p", p);
            Files.write(file("account.json").toPath(), o.toString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception ignored) {
        }
    }

    private JSONArray scripts() {
        try {
            File f = file("scripts.json");
            if (!f.exists()) {
                return new JSONArray();
            }
            return new JSONArray(new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8));
        } catch (Exception e) {
            return new JSONArray();
        }
    }

    private void saveScripts(JSONArray arr) {
        try {
            Files.write(file("scripts.json").toPath(), arr.toString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception ignored) {
        }
    }

    private void removeScript(int i) {
        JSONArray a = scripts();
        JSONArray n = new JSONArray();
        for (int k = 0; k < a.length(); k++) {
            if (k != i) {
                n.put(a.get(k));
            }
        }
        saveScripts(n);
    }

    private void saveTabs() {
        try {
            JSONArray arr = new JSONArray();
            for (Tab t : tabs) {
                JSONObject o = new JSONObject();
                o.put("url", t.url == null ? "" : t.url);
                o.put("title", t.title == null ? HOME_LABEL : t.title);
                arr.put(o);
            }
            JSONArray keep = new JSONArray();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                String u = o.optString("url", "");
                if (u.startsWith("about:")) {
                    u = "";
                }
                JSONObject k = new JSONObject();
                k.put("url", u);
                k.put("title", o.optString("title", HOME_LABEL));
                keep.put(k);
            }
            Files.write(file("tabs.json").toPath(), keep.toString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception ignored) {
        }
    }

    private void restoreTabs() {
        try {
            File f = file("tabs.json");
            if (!f.exists()) {
                return;
            }
            JSONArray arr = new JSONArray(new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8));
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                newTab(o.optString("url", ""));
            }
        } catch (Exception ignored) {
        }
    }

    /* ---------------- cookie store ---------------- */

    static class FileCookieStore implements CookieStore {
        private final File f;
        private final List<HttpCookie> list = new ArrayList<>();

        FileCookieStore(File f) {
            this.f = f;
            load();
        }

        private void load() {
            try {
                if (!f.exists()) {
                    return;
                }
                JSONArray arr = new JSONArray(new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8));
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.getJSONObject(i);
                    String n = o.optString("n", "");
                    if (n.isEmpty()) {
                        continue;
                    }
                    HttpCookie c = new HttpCookie(n, o.optString("v", ""));
                    String d = o.optString("d", "");
                    if (!d.isEmpty()) {
                        c.setDomain(d);
                    }
                    String p = o.optString("p", "");
                    if (!p.isEmpty()) {
                        c.setPath(p);
                    }
                    list.add(c);
                }
            } catch (Exception ignored) {
            }
        }

        private void save() {
            try {
                JSONArray arr = new JSONArray();
                for (HttpCookie c : list) {
                    JSONObject o = new JSONObject();
                    o.put("n", c.getName());
                    o.put("v", c.getValue() == null ? "" : c.getValue());
                    o.put("d", c.getDomain() == null ? "" : c.getDomain());
                    o.put("p", c.getPath() == null ? "" : c.getPath());
                    arr.put(o);
                }
                Files.write(f.toPath(), arr.toString().getBytes(StandardCharsets.UTF_8));
            } catch (Exception ignored) {
            }
        }

        @Override
        public void add(URI uri, HttpCookie cookie) {
            if (cookie == null) {
                return;
            }
            list.removeIf(x -> x.getName().equals(cookie.getName())
                && Objects.equals(x.getDomain(), cookie.getDomain())
                && Objects.equals(x.getPath(), cookie.getPath()));
            list.add(cookie);
            save();
        }

        @Override
        public List<HttpCookie> get(URI uri) {
            List<HttpCookie> out = new ArrayList<>();
            if (uri == null || uri.getHost() == null) {
                return out;
            }
            String host = uri.getHost().toLowerCase();
            for (HttpCookie c : list) {
                String d = c.getDomain() == null ? "" : c.getDomain().toLowerCase();
                if (d.startsWith(".")) {
                    d = d.substring(1);
                }
                if (d.isEmpty()) {
                    continue;
                }
                if (host.equals(d) || host.endsWith("." + d)) {
                    out.add(c);
                }
            }
            return out;
        }

        @Override
        public List<HttpCookie> getCookies() {
            return new ArrayList<>(list);
        }

        @Override
        public List<URI> getURIs() {
            return new ArrayList<>();
        }

        @Override
        public boolean remove(URI uri, HttpCookie cookie) {
            boolean r = list.remove(cookie);
            if (r) {
                save();
            }
            return r;
        }

        @Override
        public boolean removeAll() {
            list.clear();
            save();
            return true;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
