let a = mod { 
  let b = mod {
    let c = 1;
  }; 
};
let x = mod {
  use a;
  dbg b.c;
  let b = a.b;
  dbg b.c;
  dbg a.b.c;
};
let e = a.b;
