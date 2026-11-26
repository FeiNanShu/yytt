# yytt · 脚本浏览器

一个给 Android 用的极简 WebView 浏览器，支持「按网址自动跑 JS 脚本」。

用 Android 原生 Java 写的，**不依赖 Gson、不依赖 androidx、不用 lambda**，
所以老设备、老版 AIDE 也能直接编译。

---

## 功能

- 地址栏输入网址，一键打开（支持 `view-source:` 看源码）
- 刷新、返回（返回键走网页历史）
- **脚本管理**：新建 / 编辑 / 删除 / 单条开关 / 总开关
- 网页加载完成后，自动匹配网址并注入对应脚本
- 脚本存在手机本地（SharedPreferences + JSON），装好 App 之后直接在 App 内增删，不用重新编译

## 脚本怎么用

每个脚本有三个字段：

| 字段 | 说明 |
|---|---|
| 名称 | 随便写，只是显示用 |
| 匹配 | 网址里**包含**这段文字就触发，例如 `baidu.com`、`rooms-wiki-cn.wikidot.com` |
| 代码 | 一段 JS，页面加载完后执行 |

举个例子：匹配填 `rooms-wiki-cn.wikidot.com`，代码填

```js
document.body.style.background = "#111";
```

打开包含这个域名的页面，脚本就会自动跑。

## 目录结构

```
AndroidManifest.xml                 清单（已带 INTERNET 权限）
project.properties                  target SDK，按你 AIDE 里有的版本改
.project                            Eclipse/AIDE 项目描述
src/com/mycompany/yytt/
    MainActivity.java               全部代码（界面 + 脚本引擎 + 存储）
res/values/strings.xml              App 名字
```

## 在 AIDE 里编译

1. AIDE → 菜单 → **Git / Clone repository**
2. 地址填 `https://github.com/FeiNanShu/yytt.git`
3. 克隆完成后直接 Run
4. 如果报找不到 `android-21`，打开 `project.properties`，把 `target=android-21`
   改成 AIDE 里已安装的版本号（例如 `android-19`、`android-28`）

## 改名字和图标

- 名字：`res/values/strings.xml` 里的 `app_name`
- 图标：把图标放成 `res/drawable/ic_launcher.png`，然后在
  `AndroidManifest.xml` 的 `<application>` 里加一行 `android:icon="@drawable/ic_launcher"`

## 说明

- 脚本只能在本 App 自己的 WebView 里跑，不注入别的浏览器
- 页面内 `alert()` 会以系统弹窗显示
- 代码是老写法（匿名内部类），改代码时请不要引入 lambda 和 androidx，否则 AIDE 会炸
