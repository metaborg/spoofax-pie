let a = mod {
  let b = 1;
};

// Use local declaration over import
let b = 2;
use a;
dbg b;

// Nested lookup
let c = a.b;
dbg c;

// Import in module scope
let x = mod {
  use a; 
  let c = b;
};
