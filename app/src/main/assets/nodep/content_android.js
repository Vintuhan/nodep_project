(function () {
  if (!/^https?:$/.test(location.protocol)) return;
  const host = location.hostname.toLowerCase().replace(/^www\./, '');
  if (window.nodepIsWhitelisted && window.nodepIsWhitelisted(host)) return;
  const S = window.NodepScoring;
  if (!S) return;

  let blocked = false, asked = false;

  function headingTexts() {
    const out = [document.title || ''];
    const og = document.querySelector('meta[property="og:site_name"]');
    if (og && og.content) out.push(og.content);
    document.querySelectorAll('h1,h2').forEach((el, i) => { if (i < 30) out.push((el.textContent || '').slice(0, 200)); });
    return out;
  }
  function bodyText(){ return document.body ? (document.body.innerText || '').slice(0, 4000) : ''; }
  function looksLikeArticle(){
    const og = document.querySelector('meta[property="og:type"]');
    if (og && /article/i.test(og.content || '')) return true;
    if (document.querySelector('meta[property="article:published_time"]')) return true;
    const art = document.querySelector('article');
    return !!(art && art.querySelectorAll('p').length >= 5);
  }
  function structureTexts(){
    const out = [];
    document.querySelectorAll('a,button,[role="button"]').forEach((el,i)=>{ if(i<300){ const t=(el.textContent||'').trim(); if(t&&t.length<=50)out.push(t); }});
    document.querySelectorAll('input[placeholder]').forEach((el,i)=>{if(i<40&&el.placeholder)out.push(el.placeholder);});
    return out;
  }
  function doBlock(){
    if(blocked)return;
    blocked=true;
    window.NodepAndroid && window.NodepAndroid.blockCurrent(host);
  }
  function trust(){ window.NodepAndroid && window.NodepAndroid.trustForDay(host); }
  function evaluate(){
    if(blocked)return;
    const domainS=S.scoreDomain(host);
    if(domainS>=S.CERTAIN)return doBlock();
    const textS=S.scoreTitleAndHeadings(headingTexts());
    if(domainS+textS<=0)return;
    const verdict=S.decide(domainS,textS,S.scoreBody(bodyText()),S.scoreStructure(structureTexts()),looksLikeArticle());
    if(verdict==='block')return doBlock();
    if(verdict==='ask'&&!asked){ asked=true; window.NodepAndroid && window.NodepAndroid.showAskCard(); }
  }
  window.NodepAndroidContent = { evaluate, trust };
  evaluate(); setTimeout(evaluate,2500); setTimeout(evaluate,6000);
})();
