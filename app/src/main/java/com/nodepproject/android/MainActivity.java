package com.nodepproject.android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import android.graphics.Typeface;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {
    private WebView web;
    private EditText address;
    private NodepBlocker blocker;
    private boolean loadingBlockedPage=false;

    private int dp(float v){ return (int)(v*getResources().getDisplayMetrics().density+0.5f); }
    private TextView label(String t,int size){ TextView v=new TextView(this);v.setText(t);v.setTextColor(Color.WHITE);v.setTextSize(size);v.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.NORMAL));return v; }

    @SuppressLint({"SetJavaScriptEnabled","AddJavascriptInterface"})
    @Override public void onCreate(Bundle b){
        super.onCreate(b); getWindow().setStatusBarColor(Color.BLACK); getWindow().setNavigationBarColor(Color.BLACK);
        blocker=new NodepBlocker(this);

        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(Color.BLACK); root.setPadding(dp(10),dp(8),dp(10),0);
        LinearLayout top=new LinearLayout(this); top.setGravity(View.TEXT_ALIGNMENT_CENTER); top.setPadding(0,0,0,dp(8));
        address=new EditText(this); address.setSingleLine(true); address.setTextColor(Color.WHITE); address.setHintTextColor(0x77999999); address.setHint("введите адрес сайта"); address.setTextSize(14); address.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_URI); address.setBackgroundResource(com.nodepproject.android.R.drawable.edittext_bg);
        top.addView(address,new LinearLayout.LayoutParams(0,dp(46),1));
        Button go=btn("GO",true); top.addView(go,new LinearLayout.LayoutParams(dp(58),dp(46)));
        Button menu=btn("⋮",false); LinearLayout.LayoutParams mp=new LinearLayout.LayoutParams(dp(46),dp(46));mp.setMargins(dp(6),0,0,0);top.addView(menu,mp);
        root.addView(top);
        web=new WebView(this); WebSettings s=web.getSettings(); s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setAllowFileAccess(false);s.setAllowContentAccess(false);s.setBuiltInZoomControls(false);s.setDisplayZoomControls(false);s.setSupportZoom(false);s.setUserAgentString(s.getUserAgentString()+" nodep-android/0.3.1");
        web.addJavascriptInterface(new AndroidBridge(),"NodepAndroid");
        web.setBackgroundColor(Color.BLACK);
        web.setWebViewClient(new Client());
        root.addView(web,new LinearLayout.LayoutParams(-1,0,1));
        TextView status=label("nodep project · фильтр включён",11);status.setTextColor(0x88999999);status.setPadding(0,dp(6),0,dp(8));root.addView(status);
        setContentView(root);
        go.setOnClickListener(v->navigate(address.getText().toString()));
        address.setOnEditorActionListener((v,a,e)->{navigate(address.getText().toString());return true;});
        menu.setOnClickListener(v->{ if(web.getUrl()!=null) blockCurrent(extractHost(web.getUrl())); });
        loadHome();
    }

    private Button btn(String t,boolean solid){ Button b=new Button(this);b.setText(t);b.setTextSize(12);b.setAllCaps(false);b.setTextColor(solid?Color.BLACK:Color.WHITE);b.setBackgroundResource(solid?R.drawable.button_bg:R.drawable.button_ghost);return b; }
    private void loadHome(){ String html="<html><body style='background:#000;color:#fff;font-family:Arial;text-align:center;padding:70px 20px'><h1 style='font-weight:400;font-size:48px'>nodep project</h1><p style='color:#999;font-size:15px'>защитный браузер с локальным фильтром</p><p style='color:#777;font-size:13px'>Открой сайт через адресную строку выше.</p></body></html>"; web.loadDataWithBaseURL("https://nodep.local/",html,"text/html","UTF-8",null); }
    private void navigate(String raw){ if(raw==null)return;String u=raw.trim();if(u.isEmpty())return;if(!u.matches("(?i)^[a-z][a-z0-9+.-]*://.*"))u="https://"+u;try{Uri uri=Uri.parse(u);if(!"http".equalsIgnoreCase(uri.getScheme())&&!"https".equalsIgnoreCase(uri.getScheme()))return; String host=uri.getHost(); if(host!=null&&blocker.isBlocked(host)){loadBlocked(host);return;}web.loadUrl(u);}catch(Exception ignored){} }
    private String extractHost(String u){try{Uri x=Uri.parse(u);return x.getHost()==null?"":x.getHost();}catch(Exception e){return"";}}
    private void blockCurrent(String host){ if(host==null||host.isEmpty())return; blocker.addCustom(host);loadBlocked(host); }
    private void loadBlocked(String host){ loadingBlockedPage=true; address.setText(host); web.loadUrl("file:///android_asset/nodep/blocked.html?host="+Uri.encode(host)); }
    private void trustCurrent(String host){ blocker.trustForDay(host); }

    private String readAsset(String path)throws Exception{try(InputStream in=getAssets().open(path);ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[]b=new byte[8192];int n;while((n=in.read(b))>0)out.write(b,0,n);return out.toString(StandardCharsets.UTF_8.name());}}
    private String buildInjection()throws Exception{
        String kw=readAsset("nodep/data/keywords.js"); String wl=readAsset("nodep/data/whitelist.js"); String scoring=readAsset("nodep/scoring.js"); String content=readAsset("nodep/content_android.js");
        return kw+"\n"+wl+"\n"+scoring+"\n"+content;
    }
    private void injectEngine(){ if(loadingBlockedPage)return; try{String js=buildInjection();String call="javascript:"+js;web.evaluateJavascript("(function(){"+js+"\n"+
                "try{if(window.nodepIsWhitelisted&&window.nodepIsWhitelisted(location.hostname))return;}catch(e){}\n"+
                "})();",null);}catch(Exception ignored){} }

    private class Client extends WebViewClient{
        @Override public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r){ if(r==null||r.getUrl()==null)return false;Uri u=r.getUrl();String scheme=u.getScheme();if(!"http".equalsIgnoreCase(scheme)&&!"https".equalsIgnoreCase(scheme))return true;String h=u.getHost();if(h!=null&&blocker.isBlocked(h)){loadBlocked(h);return true;}return false; }
        @Override public void onPageStarted(WebView v,String url,android.graphics.Bitmap favicon){ loadingBlockedPage=url!=null&&url.startsWith("file:///android_asset/nodep/blocked.html"); if(url!=null&&!loadingBlockedPage)address.setText(url);super.onPageStarted(v,url,favicon); }
        @Override public void onPageFinished(WebView v,String url){ super.onPageFinished(v,url); if(!loadingBlockedPage){injectEngine();} else {loadingBlockedPage=false;} }
    }

    private class AndroidBridge{
        @JavascriptInterface public void blockCurrent(String host){runOnUiThread(()->blockCurrent(host));}
        @JavascriptInterface public void trustForDay(String host){runOnUiThread(()->trustCurrent(host));}
        @JavascriptInterface public boolean isTrustedForDay(String host){return blocker.isTrustedForDay(host);}
        @JavascriptInterface public void showAskCard(){runOnUiThread(()->showAskCard());}
    }

    private void showAskCard(){
        if(web==null||web.getUrl()==null)return; if(blocker.isTrustedForDay(extractHost(web.getUrl())))return;
        String js=""+
        "(function(){if(document.getElementById('nodep-native-card'))return;"+
        "const r=document.createElement('div');r.id='nodep-native-card';r.innerHTML='<style>#nodep-native-card{position:fixed;z-index:2147483647;right:14px;top:14px;width:300px;max-width:calc(100vw - 28px);background:#000;color:#fff;border:1px solid rgba(255,255,255,.85);padding:20px;box-shadow:0 12px 40px rgba(0,0,0,.5);font-family:Arial,sans-serif;text-align:center}#nodep-native-card button{width:100%;padding:12px;margin-top:8px;border:1px solid #fff;background:#fff;color:#000}#nodep-native-card button.alt{background:#000;color:#fff}#nodep-native-card small{display:block;color:#888;margin-top:10px} </style><div>возможно, эта страница связана с азартными играми?<button id=ndyes>да, блокировать</button><button class=alt id=ndno disabled>нет, всё ок (5)</button><small>Это защитное предупреждение.</small></div>';document.documentElement.appendChild(r);let n=5,b=r.querySelector('#ndno'),t=setInterval(()=>{n--;b.textContent='нет, всё ок ('+n+')';if(n<=0){clearInterval(t);b.textContent='нет, всё ок';b.disabled=false;}},1000);r.querySelector('#ndyes').onclick=()=>{clearInterval(t);window.NodepAndroid.blockCurrent(location.hostname);};b.onclick=()=>{clearInterval(t);window.NodepAndroid.trustForDay(location.hostname);r.remove();};})()";
        web.evaluateJavascript(js,null);
    }

    @Override public void onBackPressed(){ if(web!=null&&web.canGoBack())web.goBack();else super.onBackPressed(); }
}
