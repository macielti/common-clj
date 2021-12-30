(ns fixtures.config
  (:require [fixtures.update]))

(def token "123456:ABC-DEF1234ghIkl-zyx57W2v1u123ew11")

(def config {:telegram {:token                token
                        :chat-id              fixtures.update/chat-id
                        :message-template-dir "templates"}})
