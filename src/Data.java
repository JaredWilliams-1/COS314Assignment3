public class Data {
    public int result; // class label (can't use the keyword "class" as a field name)
    public int age;
    public int menopause;
    public int nodes;
    public int tumorSize;
    public int nodeCaps;
    public int irradiat;
    public int breast;
    public int quad;

    public Data(
            int result,
            int age,
            int menopause,
            int nodes,
            int tumorSize,
            int nodeCaps,
            int irradiat,
            int breast,
            int quad) {
        this.result = result;
        this.age = age;
        this.menopause = menopause;
        this.nodes = nodes;
        this.tumorSize = tumorSize;
        this.nodeCaps = nodeCaps;
        this.irradiat = irradiat;
        this.breast = breast;
        this.quad = quad;
    }
}
