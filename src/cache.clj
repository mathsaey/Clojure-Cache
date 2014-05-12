(ns cache
	(:require [splay-tree :as st]
						[store]))

(defn create
	"Create a new cache for the given store, storing at most size entries."
	[store size]
	; TODO
	nil)

(defn retrieve
	"Retrieve value with key from cache."
	[cache key]
	; TODO
	nil)

(defn elements
	"Returns the elements currently in the cache, ordered. Used for testing."
	[cache]
	(st/all-elements cache))
