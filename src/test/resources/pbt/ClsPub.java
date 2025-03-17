package src.test.resources.pbt;

public class ClsPub {
    public ClsPub() {}
    protected ClsPub(int a) {}
    ClsPub(int a, int b) {}

    public int field_pub;
    protected int field_prot;
    int field_pack;

    public void meth_pub() {}
    protected void meth_prot() {}
    void meth_pack() {}
}