import java.util.*;
//import static java.lang.Math.toIntExact;

public class CombiningTree {
    private Node[] leaf;

    public CombiningTree(int width) {
        Node[] nodes = new Node[width - 1];
        nodes[0] = new Node();
        for (int i = 1; i < nodes.length; i++) {
            nodes[i] = new Node(nodes[(i-1)/2]);
        }
        leaf = new Node[(width + 1)/2];
        for (int i = 0; i < leaf.length; i++) {
            leaf[i] = nodes[nodes.length - i - 1];
        }
    }

    public int getAndIncrement(int vv) throws Exception {
        Stack<Node> stack = new Stack<Node>();
        //int pid = toIntExact(Thread.currentThread().getId());
        int pid = (int)Thread.currentThread().getId();
        //System.out.println("Thread id:" + pid);
        Node myLeaf = leaf[(pid-10)/2];
        Node node = myLeaf;
        // precombining phase
        while (node.precombine()) {
            node = node.parent;
        }

        Node stop = node;
        // combining phase
        node = myLeaf;
        int combined = vv;
        while (node != stop) {
            combined = node.combine(combined);
            stack.push(node);
            node = node.parent;
        }
        // operation phase
        int prior = stop.op(combined);
        // distribution phase
        while (!stack.empty()) {
            node = stack.pop();
            node.distribute(prior);
        }
        return prior;
    }
}

