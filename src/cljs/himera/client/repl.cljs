(ns himera.client.repl)

(defn- map->js [m]
  (let [out (js-obj)]
    (doseq [[k v] m]
      (aset out (name k) v))
    out))

(defn go-compile [code]
  (let [data (atom nil)
        params (map->js {:url "/compile"
                         :data (str "{:expr " code "}")
                         :contentType "application/clojure"
                         :async false
                         :type "POST"
                         :dataType "json"
                         :success #(reset! data %)})]
    (.ajax js/jQuery params)
    @data))

(defn- on-validate [input]
  (not (empty? input)))

(defn- build-msg 
  [title msg klass]
  (array
   (map->js {:msg (str title msg)
             :className klass})))

(defn- on-handle [line, report]
  (let [input (.trim js/jQuery line)
        compiled (go-compile input)]
    (if-let [err (and compiled (.-error compiled))]
      (build-msg "Compilation error: " err "jquery-console-message-error")
      (try
        (build-msg "" (js/eval (.-js compiled)) "jquery-console-message-value")
        (catch js/Error e
          (build-msg "Compilation error: " e "jquery-console-message-error"))))))

(defn ^:export go []
  (.ready (js/jQuery js/document)
          (fn []
            (set! js/controller
                  (doto (js/jQuery "#console")
                    (.console (map->js {:welcomeMessage "Never stop typing."
                                        :promptLabel "himera> "
                                        :commandValidate on-validate
                                        :commandHandle on-handle
                                        :autofocus true
                                        :animateScroll true
                                        :promptHistory true})))))))