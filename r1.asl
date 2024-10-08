// mars robot 1

/* Initial beliefs */

at(P) :- pos(P,X,Y) & pos(r1,X,Y).

/* Initial goal */
/* !pause. */

!check(slots).
!stopsimulation.

/* Plans */

/* +!pause <- .wait(5000). */

+!check(slots) : not garbage(r1)
   <- next(slot);
      !stopsimulation;
      !!check(slots).

+!check(slots) : garbage(r1)
   <- .print("Doing 2nd check");
      !find_near(R);
      !!carry_to(R).

-!check(slot).

+!stopsimulation : true
   <- ?pos(r1, X, Y);
      ?grid_size(MaxX,MaxY);
      X == MaxX;
      Y == MaxY;
      .print("Reached the last square at position (", X, ",", Y, ") of (", MaxX, ",", MaxY, ") ");
      .stopMAS.

-!stopsimulation.

+garbage(r1) : not .desire(carry_to(R))
   <- !find_near(R);
      !carry_to(R).


+!find_near(R) <- 
   ?pos(r1, X1, Y1);
   ?pos(r2, X2, Y2);
   ?pos(r3, X3, Y3);
   .print(X1, ",", Y1, ",", X2, ",", Y2, ",", X3, ",", Y3);
   D2 = (X2 - X1)*(X2 - X1) + (Y2 - Y1)*(Y2 - Y1);
   D3 = (X3 - X1)*(X3 - X1) + (Y3 - Y1)*(Y3 - Y1);
   .print(D2, ",", D3);
   if(D2 < D3){
      R=r2
   } else {
      R=r3
   }.


+!carry_to(R)
   <- .drop_desire(check(slots)); // stop checking
      
      // remember where to go back
      ?pos(r1,X,Y);
      -+pos(last,X,Y);

      // carry garbage to r2
      !take(garb,R);

      // goes back and continue to check
      !at(last);
      !!check(slots).


+!take(S,L)
   <- !ensure_pick(S);
      !at(L);
      drop(S);
      -garbage(r1).

+!ensure_pick(S) : garbage(r1)
   <- pick(garb).


// +!ensure_pick(S) : garbage(r1)
//    <- pick(garb).
// +!ensure_pick(_).

+!at(L) : at(L).
+!at(L) <- ?pos(L,X,Y);
           move_towards(X,Y);
           !at(L).
