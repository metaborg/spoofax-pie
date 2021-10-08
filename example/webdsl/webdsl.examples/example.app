application example

// a root page is always required in a WebDSL app
page root { }

// function `f` with an integer return type
function f() : Int { return 1; }

// function `g` with void return type
function g() {
  var x := 1;

// uncomment the following line to spawn the exception
//  var y := f();
}

entity Ent {

  // derived property x with constant value 1
  x : Int := 1

// uncomment the following line to spawn the exception
//  y : Int := a()
}
