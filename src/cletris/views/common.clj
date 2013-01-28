(ns cletris.views.common
  (:use [noir.core :only [defpartial]]
        [hiccup.page :only [include-css include-js html5]]))

(defpartial layout [& content]
            (html5
              [:head
               [:title "cletris"]
               (include-js "https://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js")
               (include-js "/js/rxjs/rx.js")
               (include-js "/js/rxjs/rx.time.js")
               (include-js "/js/rxjs/rxjs-jquery/lib/rx.jquery.js")
               (include-css "/css/reset.css")
               (include-css "/css/board.css")]
              [:body
               [:div#wrapper
                content]

               (include-js "/js/cljs.js")]))
