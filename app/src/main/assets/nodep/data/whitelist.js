self.NODEP_WHITELIST = [];
self.NODEP_WL_SUFFIXES = [".gov", ".edu", ".mil", ".gov.uk", ".ac.uk"];
(function(){ let set=null; self.nodepIsWhitelisted=function(hostname){ if(!set)set=new Set(self.NODEP_WHITELIST); const h=String(hostname||'').toLowerCase().replace(/\.$/,''); if(h==='localhost') return true; for(const s of self.NODEP_WL_SUFFIXES) if(h.endsWith(s)) return true; const labels=h.split('.'); for(let i=0;i<labels.length-1;i++) if(set.has(labels.slice(i).join('.'))) return true; return false; }; })();
