public class Node {

    enum CStatus{IDLE, FIRST, SECOND, RESULT, ROOT};
    boolean locked;
    CStatus cStatus;
    int firstValue, secondValue;
    int result;
    Node parent;

    public Node() {
        cStatus = CStatus.ROOT;
        locked = false; 
    }

    public Node(Node myParent) {
        parent = myParent;
        cStatus = CStatus.IDLE;
        locked = false;
    }

    synchronized boolean precombine() throws Exception {
        while (locked) wait();
        switch (cStatus) {
            case IDLE:
                cStatus = CStatus.FIRST;
                return true;
            case FIRST:
                locked = true;
                cStatus = CStatus.SECOND;
                return false;
            case ROOT:
                return false;
            default:
                //throw new PanicException("unexpected Node state" + cStatus);
                System.out.println("ffffff");
                //return false;
                throw new Exception("123");
        }
    }

    synchronized int combine(int combined) throws Exception {
        while (locked) wait();
        locked = true;
        firstValue = combined;
        switch (cStatus) {
            case FIRST:
                return firstValue;
            case SECOND:
                return firstValue + secondValue;
            default:
                //throw new PanicException("unexpected Node state " + cStatus);
                System.out.println("ddddd");
                //return -1;
                throw new Exception("abc");
        }
    }

    synchronized int op(int combined) throws Exception {
        switch (cStatus) {
            case ROOT:
                int prior = result;
                result += combined;
                return prior;
            case SECOND:
                secondValue = combined;
                locked = false;
                notifyAll(); // wake up waiting threads 
                while (cStatus != CStatus.RESULT) wait();
                locked = false;
                notifyAll();
                cStatus = CStatus.IDLE;
                return result;
            default:
                //throw new PanicException("unexpected Node state");
                throw new Exception("op ---");

        }
    }

    synchronized void distribute(int prior) throws Exception {
        switch (cStatus) {
            case FIRST:
                cStatus = CStatus.IDLE;
                locked = false;
                break;
            case SECOND:
                result = prior + firstValue;
                cStatus = CStatus.RESULT;
                break;
            default:
                throw new Exception("distri ===="); 
        }
        notifyAll();
    }
}


