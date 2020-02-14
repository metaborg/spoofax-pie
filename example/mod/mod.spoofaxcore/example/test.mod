// Use local declaration over import
use a;
let b = 2;
dbg b;

// Nested lookup
let y = a.b;
dbg y.c;

// Import in module scope
let z = mod {
  use x; 
  let c = b;
};
