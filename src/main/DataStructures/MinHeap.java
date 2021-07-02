public class MinHeap{
    private int[] heap;
    MinHeap(int[] heap){
        this.heap = heap;


    }

    private int getParent(int i){
        return (i-1) /2;
    }
}