[
 {:id "simple" :description "Simplest core.logic goal"
  :title "Simple" :goal "(== 3 q)"}
 {:id "appendo" :title "Appendo"
  :goal "(appendo [1 2] q [1 2 3 4 5])" :usage ["appendo"]}
 {:id "conso" :title "Conso" :goal "(conso 'cat q '(cat dog bird))"}
 {:id "membero" :title "Membero" :goal "(membero 'zebra '(cat dog bird))"}
 {:id "membero1" :title "RecMembero" :goal "(membero 'cat q)"}

 {:id "conde" :title "Conde" :goal "(conde [(== q 'tea)] [(== q 'coffee)])"}
 {:id "featurec" :title "Featurec"
  :goal "(fresh [x y]
   (== x {:foo 1 :baz 1})
   (featurec x {:foo y :baz y}))"}
 {:id "in" :title "fd.in" :goal "(in q (interval 1 10))"}
 {:id "mix" :title "Mix"
  :goal "(fresh [x y]
      (in y (interval 1 10))
      (== x {:foo {:bar y}})
      (featurec x {:foo {:bar q}})
      (in q (interval 1 3)))"}
 {:id "complex" :title "Complex" :goal "(fresh [x y z]
(in y (interval 1 3))
(featurec x {:foo {:bar z}}
          ) (in q (interval 1 3))) " }
 ]
