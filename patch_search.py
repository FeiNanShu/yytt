# -*- coding: utf-8 -*-
import io
import sys
import traceback

P = "src/com/mycompany/yytt/MainActivity.java"


def run():
    s = io.open(P, encoding="utf-8").read()
    if "looksLikeUrl" in s:
        io.open("patch_result.txt", "w", encoding="utf-8").write("ALREADY size=%d\n" % len(s))
        return

    old_submit = (
        '        String url = fixUrl(raw);\n'
        '        editing = false;\n'
        '        endEdit();\n'
        '        loadInCurrent(url);\n'
    )
    assert s.count(old_submit) == 1, "submit %d" % s.count(old_submit)
    new_submit = (
        '        editing = false;\n'
        '        endEdit();\n'
        '        if (looksLikeUrl(raw)) {\n'
        '            loadInCurrent(fixUrl(raw));\n'
        '        } else {\n'
        '            showSearch(raw);\n'
        '        }\n'
    )
    s = s.replace(old_submit, new_submit, 1)

    anchor = '    private String fixUrl(String raw) {'
    assert s.count(anchor) == 1, "anchor %d" % s.count(anchor)

    code = '''
    private boolean looksLikeUrl(String raw) {
        String low = raw.toLowerCase();
        if (low.startsWith("http://") || low.startsWith("https://")
                || low.startsWith("view-source:") || low.startsWith("file:")
                || low.startsWith("about:") || low.startsWith("data:")) {
            return true;
        }
        if (raw.indexOf(' ') >= 0 || raw.indexOf('\\n') >= 0) {
            return false;
        }
        String host = raw;
        int k = host.indexOf('/');
        if (k > 0) {
            host = host.substring(0, k);
        }
        return host.indexOf('.') > 0 && !host.startsWith(".") && !host.endsWith(".");
    }

    private void showSearch(String q) {
        String e;
        try {
            e = java.net.URLEncoder.encode(q, "UTF-8");
        } catch (Exception ex) {
            e = q;
        }
        String h = "<!doctype html><html><head><title>" + esc(q) + " - 搜索</title>"
                + "<meta name='viewport' content='width=device-width,initial-scale=1'>"
                + "<style>body{margin:0;background:#FCFDFF;color:#122337;font-family:sans-serif}"
                + ".wrap{padding:22px 20px 40px}"
                + "h1{font-size:14px;color:#1B64BF;margin:0 0 4px;font-weight:600;letter-spacing:2px}"
                + ".q{font-size:22px;color:#122337;margin:0 0 6px;word-break:break-all}"
                + ".hint{font-size:12px;color:#3A526C;margin:0 0 22px;line-height:1.8}"
                + ".lab{font-size:12px;color:#3A526C;margin:26px 0 10px}"
                + "a{display:block;text-decoration:none;background:#E8F5FC;border:1px solid #A4C1D7;"
                + "border-radius:12px;padding:12px 16px;margin:0 0 10px}"
                + "a b{display:block;font-size:14px;color:#1B64BF;font-weight:600;margin-bottom:3px}"
                + "a span{font-size:11px;color:#3A526C;word-break:break-all}"
                + "</style></head><body><div class='wrap'>"
                + "<h1>搜索</h1><div class='q'>" + esc(q) + "</div>"
                + "<div class='hint'>先在各家 Wikidot 维基里找，找不到再上全网。</div>"
                + "<div class='lab'>Wikidot 维基</div>"
                + wl("SCP 中文维基", "https://scp-wiki-cn.wikidot.com/search:site/q/" + e)
                + wl("后室中文维基", "https://backrooms-wiki-cn.wikidot.com/search:site/q/" + e)
                + wl("SCP 英文维基", "https://scp-wiki.wikidot.com/search:site/q/" + e)
                + wl("后室英文维基", "https://backrooms-wiki.wikidot.com/search:site/q/" + e)
                + wl("所有 Wikidot 站点", "https://cn.bing.com/search?q=" + e + "%20site%3Awikidot.com")
                + "<div class='lab'>全网</div>"
                + wl("必应", "https://cn.bing.com/search?q=" + e)
                + wl("谷歌", "https://www.google.com/search?q=" + e)
                + "</div></body></html>";
        if (current != null && current.view != null) {
            current.view.loadDataWithBaseURL(null, h, "text/html", "utf-8", null);
        }
    }

    private String wl(String title, String href) {
        return "<a href='" + esc(href) + "'><b>" + esc(title) + "</b><span>" + esc(href) + "</span></a>";
    }

    private String esc(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

'''
    s = s.replace(anchor, code + anchor, 1)

    old_txt = '<p>为 Backrooms 与 SCP 的 Wikidot 站点准备<br>点下面的按钮，输入一个站点地址</p>'
    assert s.count(old_txt) == 1, "welcome %d" % s.count(old_txt)
    new_txt = '<p>为 Backrooms 与 SCP 的 Wikidot 站点准备<br>点下面的按钮，输入站点地址或直接输入关键词</p>'
    s = s.replace(old_txt, new_txt, 1)

    io.open(P, "w", encoding="utf-8").write(s)
    io.open("patch_result.txt", "w", encoding="utf-8").write("OK size=%d\n" % len(s))


try:
    run()
except Exception:
    io.open("patch_error.txt", "w", encoding="utf-8").write(traceback.format_exc())
    sys.exit(1)
