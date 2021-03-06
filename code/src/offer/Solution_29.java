package offer;
/*两个链表中的第一个公共结点
        输入两个链表，找出它们的第一个公共结点。*/
import java.util.HashMap;
public class Solution_29 {
    public ListNode FindFirstCommonNode(ListNode pHead1, ListNode pHead2) {
        ListNode current1 = pHead1;
        ListNode current2 = pHead2;

        HashMap<ListNode,Integer> map = new HashMap<ListNode,Integer>();

        while (current1 != null){
            map.put(current1,1);
            current1 = current1.next;
        }
        while (current2 != null){
            if(map.containsKey(current2))
                return current2;
            current2 = current2.next;
        }
        return null;
    }
}
