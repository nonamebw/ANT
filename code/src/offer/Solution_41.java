package offer;
/*二叉搜索树与双向链表
        输入一棵二叉搜索树，将该二叉搜索树转换成一个排序的双向链表。
        要求不能创建任何新的结点，只能调整树中结点指针的指向。*/
import java.util.Stack;
public class Solution_41 {
    public TreeNode Convert(TreeNode pRootOfTree) {
        if(pRootOfTree==null)
            return null;
        Stack<TreeNode> stack = new Stack<TreeNode>();
        TreeNode p = pRootOfTree;
        TreeNode pre = null;// 用于保存中序遍历序列的上一节点
        boolean isFirst = true;
        while(p!=null||!stack.isEmpty()){
            while(p!=null){
                stack.push(p);
                p = p.left;
            }
            p = stack.pop();
            if(isFirst){
                pRootOfTree = p;// 将中序遍历序列中的第一个节点记为root
                pre = pRootOfTree;
                isFirst = false;
            }else{
                pre.right = p;
                p.left = pre;
                pre = p;
            }
            p = p.right;
        }
        return pRootOfTree;

    }
}
