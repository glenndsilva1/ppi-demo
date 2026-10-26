package com.ppidemo.web;

import org.takes.rs.RsHtml;
import org.takes.rs.RsWrap;

/**
 * Full HTML page with the common layout. Every page is rendered on the
 * server and delivered whole; there is no client-side framework.
 */
public final class RsPage extends RsWrap {

    /**
     * Stylesheet, shared by all pages.
     */
    private static final String CSS = String.join(
        "\n",
        ":root{--bg:#f6f7fb;--card:#fff;--ink:#0f172a;--muted:#64748b;--line:#e5e7ef;",
        "--brand:#4338ca;--brand-soft:#eef2ff;--up:#047857;--down:#b91c1c;",
        "--bad-soft:#fef2f2;--ok-soft:#ecfdf5;--warn-soft:#fffbeb}",
        "*{box-sizing:border-box}",
        "body{margin:0;background:var(--bg);color:var(--ink);font:15px/1.5 system-ui,-apple-system,'Segoe UI',sans-serif}",
        "a{color:var(--brand)}",
        "header{background:var(--card);border-bottom:1px solid var(--line)}",
        ".bar{max-width:1080px;margin:0 auto;padding:12px 16px;display:flex;gap:24px;align-items:center;flex-wrap:wrap}",
        ".brand{font-weight:700;text-decoration:none;color:var(--ink);margin-right:auto}",
        ".brand b{color:var(--brand)}",
        "nav a{text-decoration:none;color:var(--muted);font-weight:500;margin-left:18px}",
        "nav a:hover{color:var(--ink)}",
        "main{max-width:1080px;margin:0 auto;padding:20px 16px 48px}",
        "h1{font-size:26px;margin:8px 0 4px}",
        "h2{font-size:18px;margin:32px 0 12px}",
        "h3{font-size:16px;margin:6px 0 2px}",
        ".muted{color:var(--muted)}.small{font-size:13px}",
        ".lead{color:var(--muted);font-size:16px;margin:0 0 16px;max-width:720px}",
        ".card{background:var(--card);border:1px solid var(--line);border-radius:14px;padding:16px}",
        ".grid{display:grid;gap:14px;grid-template-columns:repeat(auto-fill,minmax(230px,1fr))}",
        ".hero{display:flex;gap:24px;justify-content:space-between;flex-wrap:wrap;align-items:flex-start}",
        ".balance{font-size:44px;font-weight:700;letter-spacing:-1px;font-variant-numeric:tabular-nums}",
        ".big{font-size:28px;font-weight:700;font-variant-numeric:tabular-nums}",
        ".meter{height:8px;background:var(--line);border-radius:8px;overflow:hidden;margin:10px 0 4px;max-width:420px}",
        ".meter span{display:block;height:100%;background:var(--brand)}",
        ".tag{display:inline-block;font-size:12px;font-weight:600;padding:2px 8px;border-radius:99px;background:var(--brand-soft);color:var(--brand);margin-right:6px}",
        ".tag.ok{background:var(--ok-soft);color:var(--up)}.tag.bad{background:var(--bad-soft);color:var(--down)}",
        ".tag.soon{background:var(--line);color:var(--muted)}",
        ".chip{display:inline-block;font-size:11px;font-weight:700;letter-spacing:.3px;text-transform:uppercase;padding:2px 8px;border-radius:6px;white-space:nowrap}",
        ".chip.bc-wallet{background:#fef3c7;color:#92400e}.chip.ppi-card{background:#e0e7ff;color:#3730a3}",
        ".chip.ncmc-transit{background:#ccfbf1;color:#115e59}.chip.back-office{background:#f1f5f9;color:#475569}",
        ".chip.wallet-app{background:#fce7f3;color:#9d174d}.chip.upi-qr{background:#f1f5f9;color:#475569}.chip.netc-fastag{background:#f1f5f9;color:#475569}",
        "label{display:block;font-size:13px;color:var(--muted);margin:10px 0 0}",
        "input,select{display:block;width:100%;font:inherit;color:var(--ink);background:var(--card);padding:8px 10px;border:1px solid var(--line);border-radius:8px;margin-top:4px}",
        "button{font:inherit;font-weight:600;cursor:pointer;border:0;border-radius:8px;padding:9px 14px;background:var(--brand);color:#fff;margin-top:12px}",
        "button.ghost{background:transparent;color:var(--brand);border:1px solid var(--line)}",
        "button.danger{background:transparent;color:var(--down);border:1px solid var(--line)}",
        "button.link{background:none;color:var(--brand);padding:0;margin:0;font-weight:500}",
        ".fares{display:flex;gap:8px}.fares button{flex:1}",
        "table{width:100%;border-collapse:collapse;background:var(--card);border:1px solid var(--line);border-radius:14px;overflow:hidden}",
        "th,td{text-align:left;padding:10px 12px;border-bottom:1px solid var(--line);vertical-align:top}",
        "th{font-size:12px;text-transform:uppercase;letter-spacing:.4px;color:var(--muted);font-weight:600;background:var(--bg)}",
        "tr:last-child td{border-bottom:0}",
        ".num{text-align:right;font-variant-numeric:tabular-nums;white-space:nowrap}",
        ".up{color:var(--up);font-weight:600}.down{color:var(--down);font-weight:600}",
        ".struck{text-decoration:line-through;color:var(--muted)}",
        ".notice{border-radius:12px;padding:14px 16px;margin:12px 0 4px;border:1px solid var(--line)}",
        ".notice.ok{background:var(--ok-soft);border-color:#a7f3d0}",
        ".notice.bad{background:var(--bad-soft);border-color:#fecaca;font-size:17px}",
        ".notice.info{background:var(--brand-soft);border-color:#c7d2fe}",
        "ol.steps{margin:0;padding-left:20px}ol.steps li{margin:6px 0}",
        ".tile{display:flex;flex-direction:column;gap:4px}",
        ".tile.off{opacity:.6}",
        ".split{display:grid;gap:20px;grid-template-columns:minmax(0,3fr) minmax(0,2fr)}",
        "@media (max-width:760px){.split{grid-template-columns:1fr}.balance{font-size:36px}nav a{margin:0 14px 0 0}.wide{overflow-x:auto}}",
        "@media (prefers-color-scheme:dark){:root{--bg:#0b1020;--card:#141a2e;--ink:#e5e7eb;--muted:#94a3b8;--line:#263049;",
        "--brand:#a5b4fc;--brand-soft:#1e2550;--up:#34d399;--down:#f87171;--bad-soft:#2a1416;--ok-soft:#0f2a22;--warn-soft:#2a2410}",
        "button{color:#0b1020}.notice.ok{border-color:#14532d}.notice.bad{border-color:#7f1d1d}.notice.info{border-color:#3730a3}}"
    );

    /**
     * Ctor.
     * @param title Page title, escaped here
     * @param body HTML body, already safe
     */
    public RsPage(final String title, final CharSequence body) {
        super(
            new RsHtml(
                String.join(
                    "\n",
                    "<!DOCTYPE html>",
                    "<html lang='en'><head>",
                    "<meta charset='utf-8'>",
                    "<meta name='viewport' content='width=device-width, initial-scale=1'>",
                    String.format("<title>%s · AllTekkies PPI</title>", new Escaped(title)),
                    String.format("<style>%s</style>", RsPage.CSS),
                    "</head><body>",
                    "<header><div class='bar'>",
                    "<a class='brand' href='/'><b>AllTekkies</b> PPI Wallet <span class='tag'>Demo</span></a>",
                    "<nav><a href='/'>Wallets</a><a href='/agents'>BC agents</a><a href='/books'>The books</a></nav>",
                    "</div></header>",
                    "<main>",
                    body.toString(),
                    "</main></body></html>"
                )
            )
        );
    }
}
