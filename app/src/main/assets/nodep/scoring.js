(function(){
  const K=self.NODEP_KEYWORDS||{maxWords:5,text:{},struct:{},domain:[],tld:[]}; let textMap=null,structMap=null;
  function maps(){if(!textMap){textMap=new Map(Object.entries(K.text||{}));structMap=new Map(Object.entries(K.struct||{}));}}
  function normalize(s){return String(s||'').toLowerCase().replace(/ё/g,'е').replace(/[^\p{L}\p{N}]+/gu,' ').trim();}
  function scoreTexts(pieces,map,seen){let score=0; const maxN=K.maxWords||5; for(const piece of pieces){const words=normalize(piece).split(' '); if(words.length===1&&words[0]==='')continue; let i=0; while(i<words.length){let hit=false; for(let n=Math.min(maxN,words.length-i);n>=1;n--){const phrase=n===1?words[i]:words.slice(i,i+n).join(' '); const v=map.get(phrase); if(v!==undefined){if(!seen.has(phrase)){seen.add(phrase);score+=v;} i+=n;hit=true;break;}} if(!hit)i++;}} return score;}
  function scoreTitleAndHeadings(p){maps();return scoreTexts(p,textMap,new Set());}
  function scoreBody(t){maps();return Math.max(-3,Math.min(6,scoreTexts([String(t||'').slice(0,4000)],textMap,new Set())));}
  function scoreStructure(p){maps();return Math.min(8,Math.max(0,scoreTexts(p,structMap,new Set())));}
  function scoreDomain(host){const h=String(host||'').toLowerCase().replace(/^www\./,''); const flat=h.replace(/[.\-_]/g,''); let s=0; for(const item of (K.domain||[])){const frag=item[0],w=item[1],except=item[2]; if(!flat.includes(frag))continue; if(except&&except.some(e=>flat.includes(e)))continue; s+=w;} for(const it of (K.tld||[]))if(h.endsWith(it[0]))s+=it[1]; return Math.min(s,14);}
  const T={ASK:3,BLOCK:9,STRUCT_MIN:3,HARD:16,CERTAIN:12};
  function decide(domainS,textS,bodyS,structS,article){let total=domainS+textS+bodyS+structS; if(domainS>=T.CERTAIN)return'block'; if(article&&structS<T.STRUCT_MIN){total-=10;return total>=10?'ask':'none';} if(total>=T.HARD)return'block'; if(total>=T.BLOCK&&structS>=T.STRUCT_MIN)return'block'; if(total>=T.ASK)return'ask'; return'none';}
  self.NodepScoring={normalize,scoreTitleAndHeadings,scoreBody,scoreStructure,scoreDomain,decide,...T};
})();
