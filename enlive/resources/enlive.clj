[{:id "simple" :title "Simple" :selector [:a]
  :source "<span><a>llll</a></span>"}
 {:id "inclusion" :title "Inclusion"
  :selector  [:span :a]
  :source "<div><a>lll</a><span><a>inner link</a></span></div>"}
 {:id "conjunction" :title "Conjunction"
  :description "inner [] means conjunction"
  :selector  "[:li [:a (attr= :class \"special\")]]"
  :source "<div><ul><li><a class=\"special\">ll1</a></li><li><a href=\"/\">index</a></li></ul></div>"}
 {:id "but-node" :title "but-node"
  :description "All nodes in div except <a> or <br>"
  :selector "[:div (but-node #{:br :a})]"
  :source "<h2>title</h2><div><span>blah</span><br/><a>Link</a></div>"}
 {:id "content" :title "Content"
  :description "Simple transformation : replace content of a tag"
  :selector [:a] :transform "(content \"hello\")"
  :source "<span><a>llll</a></span>"}
 {:id "fragment" :title "Fragment" :selector "{[:h1] [:p]}"
  :source "<div><h1>title</h1><h2>Sub title</h2><p>some text</p></div><h1>Another Title</h1>"}
 {:id "4pb" :title "4pb scraping"
  :selector "[:div#prob-title]" :transform "texts"
  :source "http://4clojure.com/problem/111"}
 {:id "takenotwhile" :title "take not while"
  :description "Any elements in div before ul"
  :selector "[:div (pred #(not (= :ul (:tag %))))]"
  :source "<div><span>some text</span><a>link</a><br><ul><li>item1</li></ul></div>"}
 ]
