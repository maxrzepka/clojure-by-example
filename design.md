
* Re factor with example / section as Records

example :
 TODO how to handle multiple input/output
 :input map (for enlive : source (html chunk or url), selector, transformer)
 :output  default output clj but need to define fcts for other format HTML text
 :description  plain text with markdown support ( for links ()[] )
 :tags  list of keywords (can have different scope) to allow classification
 :code kind of mnemonic (use for display)
 :id unique id (6 letters as git commit number)

section : a container of examples or sections
 :name
 :title
 :description
 :sections
 :examples

protocol IExample  (EnliveExample , CascalogExample) :
   * fullify : append full namespace at clj code (attr= -> net.cgrand.enlive-html/attr=)
   * run : takes inputs and produces outputs
   * display : specific display for inputs/outputs ( HTML,... )
   * code : generates corresponding code

A Module is a set :
   * sections
   * 1 implementation IExample
   * list of namespaces and their associate fcts

Misc Utilities :
     - extract all fcts called in a example : usage of a fct
     - get code/ doc of any given fcts : clojure.repl/doc , source
     - link to github source code

* Simple file storage for sections and examples :
  - given data build the full web sites

* Search across all functions / sections

* use clojail to prevent any unfrinedly call :
  * test it, need to save setting in session

* Bootstrap CSS : vertical Sub menus

* Save Examples in DB for any authentificated user :
  - sync DB to files and purge DB

* How to generate 6 letters unique ID

* Quizz derived from examples :
  hide all clj codes and show only result

* Step-by-step tutorial based on examples :
   step enhanced example or group of examples ?
   - tutorial is an ordered sequence of steps
   - text mixed with code
   - console to test code (code paste to console with one clik)

* Build your own tutorial : create steps and link them together

* UX to edit code :
   - clickable code to paste
   - add,delete,move  expressions
   - shortcut for code snippet
   - english sentence to code

* Add more UX with clojurescript :
  - console with history ,

* add custom helper function :
   - can be used in examples or console

* Full edit mode when local : add/update sections, examples

* How to make lein project modular : only load selected dependencies...?
