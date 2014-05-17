(ns splay-tree)

; Note: key must implement Comparable

(defn- smaller-than
	"Returns true iff a < b; according to compare.
	a and b must implement java.lang.Comparable."
	[a b]
	(< (compare a b) 0))

(defn to-string
	"Convert a tree to a 'human-readable' string."
	[node]
	(if (nil? node)
		"_"
		(str
			"(" (:key node) " " (to-string (:left node)) " "
			(to-string (:right node)) ")")))

(defn all-elements
	"Returns all keys in the tree, ordered."
	[node]
	(if (nil? node)
		[]
		(concat (all-elements (:left node)) [(:key node)] (all-elements (:right node)))))

(defn- left-or-right-child?
	"Returns :left if c is left child of p, :right if right child, or throws
	exception otherwise."
	[c p]
	(when (nil? p)
		(throw (Exception. "p is nil")))
	(if (= (:left p) c)
		:left
		(if (= (:right p) c)
			:right
			(throw (Exception.
				(str "Neither left nor right child: c = " c "; p = " p))))))

(defn- zig
	"Zig subtree underneath p. Returns replacement for p."
	[c p]
	(case (left-or-right-child? c p)
		:left
			(let [new-p (assoc p :left (:right c))
						new-c (assoc c :right new-p)]
				new-c)
		:right
			(let [new-p (assoc p :right (:left c))
						new-c (assoc c :left new-p)]
				new-c)))

(defn- zigzig
	"Zig-zig subtree underneath gp. Returns replacement for gp."
	[c p gp]
	; For zig-zig: (left-or-right-child? c p) == (left-or-right-child? p gp)
	(case (left-or-right-child? c p)
		:left
			(let [new-gp (assoc gp :left (:right p))
						new-p  (assoc p :left (:right c) :right new-gp)
						new-c  (assoc c :right new-p)]
				new-c)
		:right
			(let [new-gp (assoc gp :right (:left p))
						new-p  (assoc p :left new-gp :right (:left c))
						new-c  (assoc c :left new-p)]
				new-c)))

(defn- zigzag
	"Zig-zag subtree underneath gp. Returns replacement for gp."
	[c p gp]
	; For zig-zag: (left-or-right-child? c p) == opposite of (left-or-right-child? p gp)
	(case (left-or-right-child? c p)
		:left
			(let [new-gp (assoc gp :right (:left c))
						new-p  (assoc p :left (:right c))
						new-c  (assoc c :left new-gp :right new-p)]
				new-c)
		:right
			(let [new-gp (assoc gp :left (:right c))
						new-p  (assoc p :right (:left c))
						new-c  (assoc c :left new-p :right new-gp)]
				new-c)))

(defn- splay-step
	"Splay step on child, parent and grandparent.
	Returns replacement for grandparent, or if grandparent was nil, replacement
	for parent."
	[c p gp]
	(when (nil? p)
		(throw (Exception. "p should not be nil")))
	(if (nil? gp)
		(zig c p)
		(if (= (left-or-right-child? c p) (left-or-right-child? p gp))
			(zigzig c p gp)
			(zigzag c p gp))))

(defn- update-path
	"Given a path leading to a certain node (parent of child), will replace all
	elements in path to point to new-child instead.
	path is list of {:node ... :dir ...} from child up to root, new-child is a
	node.
	Returns list of nodes, from parent of new-child up to root."
	[path new-child]
	(if (empty? path)
		(list)
		(let [first-node     (:node (first path))
					first-dir      (:dir  (first path)) ; is new-child :left or :right of first-node?
					new-first-node (assoc first-node first-dir new-child) ; associate either :left or :right of first-node with new-child
					new-first      (assoc (first path) :node new-first-node)]
			(cons new-first (update-path (rest path) new-first-node)))))

(defn splay
	"Given a path through a tree leading up to a node, and that node (current),
	splay the node.
	path is list of {:node ... :dir ...} from parent of current up to root,
	current is a node."
	[path current]
	(if (empty? path)
		current
		(let [c       current                   ; current
					p       (:node (nth path 0 nil))  ; parent
					gp-to-p (:dir  (nth path 0 nil))  ; is p left or right of gp?
					gp      (:node (nth path 1 nil))  ; grandparent
					new-gp  (splay-step c p gp)       ; new subtree that replaces gp
					ggp     (:node (nth path 2 nil))] ; great-grandparent
			(if (nil? ggp)
				new-gp
				(splay (update-path (drop 2 path) new-gp) new-gp)))))
				; Drop c, p and gp from path, and update the nodes along it.
				; Recurse with new-gp as new 'current' node.

(defn find-without-splay
	"Find key in tree, returns corresponding node, or nil if not found.
	Does not splay."
	[tree key]
	(if (nil? tree)
		nil
		(let [c (compare key (:key tree))]
			(cond
				(< c 0) (find-without-splay (:left tree) key)
				(> c 0) (find-without-splay (:right tree) key)
				:else   tree))))

(defn find-with-splay
	"Find key in tree, returns corresponding node, or nil if not found.
	tree is a node, path is list of {:node ... :dir ...} from current node up to
	root."
	[tree key path]
	(if (nil? tree)
		; In theory, even when you don't find the node, you should still splay the
		; last non-null node. We don't do that.
		nil
		(let [c (compare key (:key tree))]
			(cond
				(< c 0) (find-with-splay (:left tree) key (conj path {:node tree :dir :left}))
				(> c 0) (find-with-splay (:right tree) key (conj path {:node tree :dir :right}))
				:else   (splay path tree)))))

(defn find
	"Find key in true, returns corresponding node (which is a new, reordered
	tree), or nil if not found."
	[tree key]
	(find-with-splay tree key (list)))

(defn insert-without-splay
	"Insert an element in the tree, without splaying."
	[tree key value]
	(if (nil? tree)
		{:key key :value value :left nil :right nil}
		(if (smaller-than key (:key tree))
			(assoc tree :left (insert-without-splay (:left tree) key value))
			(assoc tree :right (insert-without-splay (:right tree) key value)))))

(defn insert
	"Insert an element in the tree, returns the new tree."
	[tree key value]
	(find (insert-without-splay tree key value) key))

(defn remove-leaf
	"Remove a (randomly chosen) leaf node in the tree. Returns the updated tree."
	([tree] (remove-leaf tree (list)))
	([tree path]
		(if (nil? tree)
			nil
			(cond
				(and (nil? (:left tree)) (nil? (:right tree)))
					; this is a leaf, remove this one
					(:node (last (update-path path nil)))
				(nil? (:left tree))
					; can only continue to the right
					(remove-leaf (:right tree) (conj path {:node tree :dir :right}))
				(nil? (:right tree))
					; can only continue to the left
					(remove-leaf (:left tree) (conj path {:node tree :dir :left}))
				:else
					; randomly choose left or right
					(let [dir (rand-nth [:left :right])]
						(remove-leaf (dir tree) (conj path {:node tree :dir dir})))))))

(defn create
	"Create a node (or tree) without children."
	[key value]
	{:key key :value value :left nil :right nil})

(defn create-empty
	"Create an empty splay-tree"
	[]
	nil)

(defn insert-empty
	"Insert an element in the tree, which might be empty (nil)"
	[tree key value]

	(if (not tree)
		(create key value)
		(insert tree key value)))
