[
 {:id "simple" :description "Simplest core.logic goal"
  :title "Simple" :goal (== 3 q)}
 {:id "appendo" :title "Appendo"
  :goal (appendo [1 2] q [1 2 3 4 5]) :usage ["appendo"]}
 {:id "conso" :title "Conso" :goal (conso 'cat q '(cat dog bird))}
 {:id "membero" :title "Membero" :goal (membero 'zebra '(cat dog bird))}
 {:id "membero1" :title "RecMembero" :goal (membero 'cat q)}

 {:id "conde" :title "Conde" :goal (conde [(== q 'tea)] [(== q 'coffee)])}
 {:id "featurec1" :title "Featurec1"
  :description "coming from http://michaelrbernste.in/2013/05/12/featurec-and-maps.html"
  :goal "(featurec q {:foo 1})
         (== q {:foo 1 :bar 2})"}
 {:id "featurec2" :title "Featurec2"
  :description "coming from http://michaelrbernste.in/2013/05/12/featurec-and-maps.html"
  :goal (fresh [x]
               (featurec x {:foo q})
               (== x {:foo 1}))}         
 {:id "featurec3" :title "Featurec3"
  :goal (fresh [x y]
   (== x {:foo 1 :baz 1})
   (featurec x {:foo y :baz y}))}
 {:id "in" :title "fd.in" :goal (in q (interval 1 10))}
 {:id "equal" :title "fd.equal"
  :description "Resolve a linear equation code written by @swannodette https://github.com/swannodette/zrch-logic/blob/master/src/zrch_logic/core.clj"
  :lvar "x y"
  :goal "(in x y (interval 0 9))
    (eq
      (= (+ x y) 9)
      (= (+ (* x 2) (* y 4)) 24))"}
  {:id "alpha" :title "Alpha-equiv"
   :goal  (nom/fresh [a b]
                   (l/== (lam a (lam b a))
                       (lam b (lam a b))))}
 {:id "mix" :title "Mix"
  :goal (fresh [x y]
      (in y (interval 1 10))
      (== x {:foo {:bar y}})
      (featurec x {:foo {:bar q}})
      (in q (interval 1 3)))}
  {:id "mix2" :title "Mix2"
   :goal (fresh [x y z]
(in y (interval 1 3))
(featurec x {:foo {:bar z}}
          ) (in q (interval 1 3))) }
; {:id    s d }
 ]
