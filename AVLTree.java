package backend;

class AVLNode {
    Book book;
    AVLNode left, right;
    int height;
    public AVLNode(Book book) { this.book = book; this.height = 1; }
}

public class AVLTree {
    private AVLNode root;

    private int height(AVLNode n) { return n == null ? 0 : n.height; }
    private int getBalance(AVLNode n) { return n == null ? 0 : height(n.left) - height(n.right); }

    private AVLNode rightRotate(AVLNode y) {
        AVLNode x = y.left;
        AVLNode T2 = x.right;
        x.right = y; y.left = T2;
        y.height = Math.max(height(y.left), height(y.right)) + 1;
        x.height = Math.max(height(x.left), height(x.right)) + 1;
        return x;
    }

    private AVLNode leftRotate(AVLNode x) {
        AVLNode y = x.right;
        AVLNode T2 = y.left;
        y.left = x; x.right = T2;
        x.height = Math.max(height(x.left), height(x.right)) + 1;
        y.height = Math.max(height(y.left), height(y.right)) + 1;
        return y;
    }

    public void insert(Book book) { root = insertRec(root, book); }

    private AVLNode insertRec(AVLNode node, Book book) {
        if (node == null) return new AVLNode(book);
        if (book.title.compareToIgnoreCase(node.book.title) < 0) node.left = insertRec(node.left, book);
        else if (book.title.compareToIgnoreCase(node.book.title) > 0) node.right = insertRec(node.right, book);
        else return node;
        node.height = 1 + Math.max(height(node.left), height(node.right));
        int balance = getBalance(node);
        if (balance > 1 && book.title.compareToIgnoreCase(node.left.book.title) < 0) return rightRotate(node);
        if (balance > 1 && book.title.compareToIgnoreCase(node.left.book.title) > 0) {
            node.left = leftRotate(node.left); return rightRotate(node);
        }
        if (balance < -1 && book.title.compareToIgnoreCase(node.right.book.title) > 0) return leftRotate(node);
        if (balance < -1 && book.title.compareToIgnoreCase(node.right.book.title) < 0) {
            node.right = rightRotate(node.right); return leftRotate(node);
        }
        return node;
    }

    public Book search(String title) {
        AVLNode curr = root;
        while (curr != null) {
            int cmp = title.compareToIgnoreCase(curr.book.title);
            if (cmp == 0) return curr.book;
            curr = cmp < 0 ? curr.left : curr.right;
        }
        return null;
    }
}
