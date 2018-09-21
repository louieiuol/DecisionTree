package cmsc420.sortedmap;

import java.util.Arrays;
import java.util.Collections;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class AvlNode<K, V> {
    /* package */public final K mKey;
    /* package */public V mValue;
    /* package */public AvlNode<K, V> left, right;
    /* package */public int mHeight = 1;

    public AvlNode(final K key, final V value) {
        mKey = key;
        mValue = value;
    }

    public V put(final AvlGTree<K, V> tree, final K key, final V value) {
        final int comparison = tree.keyCompare(mKey, key);
        V ret = null;

        if (comparison > 0) {
            if (left == null) {
                left = new AvlNode<K, V>(key, value);
            } else {
                ret = left.put(tree, key, value);
                left = left.rebalance(tree.g);
            }
        } else if (comparison < 0) {
            if (right == null) {
                right = new AvlNode<K, V>(key, value);
            } else {
                ret = right.put(tree, key, value);
                right = right.rebalance(tree.g);
            }
        } else {
            final V oldVal = mValue;
            mValue = value;
            ret = oldVal;
        }
        balanceFactor();
        return ret;
    }

    public V get(final AvlGTree<K, V> tree, final K key) {
        final int comparison = tree.keyCompare(mKey, key);
        final V ret;

        if (comparison > 0 && left != null) {
            ret = left.get(tree, key);
        } else if (comparison < 0 && right != null) {
            ret = right.get(tree, key);
        } else if (comparison == 0) {
            ret = mValue;
        } else ret = null;
        return ret;
    }

    public boolean contains(final AvlGTree<K, V> tree, final K key) {
        final int comparison = tree.keyCompare(mKey, key);
        final boolean ret;

        if (comparison > 0 && left != null) {
            ret = left.contains(tree, key);
        } else if (comparison < 0 && right != null) {
            ret = right.contains(tree, key);
        } else if (comparison == 0) {
            ret = true;
        } else ret = false;
        return ret;
    }

    private V find(final AvlGTree<K, V> tree, final K key) {
        final int comparison = tree.keyCompare(key,mKey);
        V ret = null;

        if ( left != null && comparison < 0) {
        	if(tree.keyCompare(key, left.mKey) != 0){
        		
        		ret = left.find(tree, key);
        		left = left.rebalance(tree.g);
        		left.balanceFactor();
        		
        		
        	}else{
        		V val = left.mValue;
        		ret = val;
        		left = moveDown(tree, left);
        	}

        } else if (right != null && comparison > 0) {
        	if(tree.keyCompare(key, right.mKey) != 0){
        		ret = right.find(tree, key);
        		right = right.rebalance(tree.g);
        		right.balanceFactor();
        	      		
        	}else{
  
        		V val = right.mValue;
        		ret = val;
        		right = moveDown(tree, right);
        	}
        } else return null;
        
        this.balanceFactor();
        return ret;
    }
    
    public AvlNode<K, V> moveDown(final AvlGTree<K, V> tree,  AvlNode<K, V> toRemove) {

    	AvlNode<K,V> toRet = null;
    	AvlNode<K,V> right = toRemove.right;
		AvlNode<K,V> left = toRemove.left;
		
    	if(left != null && right != null){
            toRet = rotate(toRemove,right);
            AvlNode<K,V> temp = toRet.left;
            toRet.left = rotate(temp,temp.left);
            toRet.left.right = toRet.left.right.moveDown(tree, toRemove);
            toRet.left = toRet.left.rebalance(tree.g);
            toRet.left.balanceFactor();
    	}else if(right != null && left == null){
    			
    		toRet = rotate(toRemove,right);
    		toRet.left = toRet.left.moveDown(tree, toRemove);

    	}else if(left != null && right == null){
    		toRet = rotate(toRemove,left);
    		toRet.right = toRet.right.moveDown(tree, toRemove);
    		
    	}else{
    		return null;
    	}
    	toRet = toRet.rebalance(tree.g);
    	toRet.balanceFactor();
    	return toRet;
	}

	public V remove(final AvlGTree<K, V> tree, final K key) {
        
		if(tree.mRoot == null){
			return null;
		}
		
		if(tree.mRoot.mKey.equals(key) 
				&& tree.mRoot.left == null && tree.mRoot.right == null){
			V toRet = tree.mRoot.mValue;
			tree.mRoot = null;
			return toRet;
		}
    	
    	return find(tree,key);
    	
    }
    

    /* package */AvlNode<K, V> rebalance(final int g) {
        final int balance = balanceFactor();

        if (balance < -g) {
            // We check the right subtree.
            if (right.balanceFactor() > 0) {
                right = rotate(right, right.left);
                balanceFactor();
            }
            return rotate(this, right);
        } else if (balance > g) {
            // We check the left subtree.
            if (left.balanceFactor() < 0) {
                left = rotate(left, left.right);
                balanceFactor();
            }
            return rotate(this, left);
        } else return this;
    }

    /* package */int balanceFactor() {
        final int balance;
        if (left == null && right == null) {
            balance = 0;
            mHeight = 1;
        } else if (left == null) {
            mHeight = right.mHeight + 1;
            balance = -right.mHeight;
        } else if (right == null) {
            mHeight = left.mHeight + 1;
            balance = left.mHeight;
        } else {
            mHeight = Collections.max(Arrays.asList(left.mHeight, right.mHeight)) + 1;
            balance = left.mHeight - right.mHeight;
        }
        return balance;
    }

    public static <K, V> AvlNode<K, V> rotate(final AvlNode<K, V> parent, final AvlNode<K, V> child) {
        if (child == null) return parent;
        if (parent.left == child) {
            parent.left = child.right;
            child.right = parent;
        } else if (parent.right == child) {
            parent.right = child.left;
            child.left = parent;
        } else throw new IllegalStateException();

        parent.balanceFactor(); // Calling this method just to recalculate
                                // mHeight.
        child.balanceFactor();
        return child; // Child is the new parent
    }

    /* package */<A> A inOrderTraverse(final TraversalTask<K, V, A> task, final A seed) {
        A leftResult = left == null ? seed : left.inOrderTraverse(task, seed);
        A thisResult = task.visitItem(mKey, mValue, leftResult);
        return right == null ? thisResult : right.inOrderTraverse(task, thisResult);
    }

    /* package */static interface TraversalTask<K, V, A> {
        A visitItem(final K key, final V value, final A accumulator);
    }

    public Node buildXmlNode(final Node parent) {
        final Element e = parent.getOwnerDocument().createElement("node");
        e.setAttribute("key", mKey.toString());
        e.setAttribute("value", mValue.toString());
        if (left != null) {
            e.appendChild(left.buildXmlNode(e));
        } else e.appendChild(e.getOwnerDocument().createElement("emptyChild"));
        if (right != null) {
            e.appendChild(right.buildXmlNode(e));
        } else e.appendChild(e.getOwnerDocument().createElement("emptyChild"));
        return e;
    }

    public String toString() {
        return mKey + "=" + mValue + " [" + left + "] [" + right + "]";
    }
}
